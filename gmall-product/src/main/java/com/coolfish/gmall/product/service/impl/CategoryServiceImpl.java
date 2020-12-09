package com.coolfish.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.Query;
import com.coolfish.gmall.product.dao.CategoryDao;
import com.coolfish.gmall.product.entity.CategoryEntity;
import com.coolfish.gmall.product.service.CategoryBrandRelationService;
import com.coolfish.gmall.product.service.CategoryService;
import com.coolfish.gmall.product.vo.Catelog2Vo;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author 28251
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类，
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2、組裝樹狀結構
        //2.1、找到所有一级分类
        List<CategoryEntity> treeMenu = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0).map(menu -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted(Comparator.comparingInt(menu2 -> ((menu2.getSort()) == null ? 0 : menu2.getSort()))).collect(Collectors.toList());
        return treeMenu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //todo: 檢查是否有別的地方引用

        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新 ,事务回滚需要完善
     *
     * @param category
     * @Transactional开启事务
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascader(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        //例如此处菜单有更新以后可以删除缓存中的数据， stringRedisTemplate.delete("cateLogJsonLock");
        //stringRedisTemplate.delete("cateLogJsonLock");
    }

    //每一个需要缓存的数据都要来指定放到哪个名字的缓存中（亦即缓存分区-->推荐按照业务类型分）
    @Cacheable({"category"}) //表示当前方法的执行结果需要被缓存，如果缓存中有，方法不用调用，如果缓存中没有，则执行方法，最后将方法的执行结果放入缓存
    @Override
    public List<CategoryEntity> getLevel1Category() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        //todo:产生堆外内存溢出
        //springboot 2.0以后默认使用的是lettuce作为操作redis的客户端。它使用netty进行网络通信。lettuce的bug导致堆外内存溢出。
        //netty如果没有指定堆外内存，就会默认使用在jvm设置中的-Xmx128m，可以通过-Dio.netty.maxDirectMemory进行设置
        //解决方案，不能使用-Dio.netty.maxDirectMemory来调大堆外内存
        //1、升级lettuce客户端。2、切换使用jedis客户端
        /**
         * 1、空结果缓存：解决缓存穿透
         * 2、设置过期时间（加随机值）：解决缓存雪崩
         * 3、加锁：解决缓存击穿,
         */
        String cateJson = stringRedisTemplate.opsForValue().get("cateJson");
        if (StringUtils.isEmpty(cateJson)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
            return catalogJsonFromDb;
        }
        Map<String, List<Catelog2Vo>> map = JSON.parseObject(cateJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return map;
    }

    /**
     * {
     * "1":[{},{},...],
     * "2":[{},{},...],
     * ...
     * }
     * 使用分布式锁。加锁和解锁的时候都需要保证原子性操作。
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        //1、占分布式锁setIfAbsent就是redis中的setNX命令
        String uuid = UUID.randomUUID().toString();
        //这样是原子操作，redis底层是这样做的：setnxex
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        Map<String, List<Catelog2Vo>> stringListMap = null;
        if (lock) {
            //设置过期时间，这样也是有缺陷的，这是非原子性的。stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
//            stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
            //加锁成功,执行业务
            try {
                stringListMap = getStringListMap();
            } finally {
                String lua_scripts = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                        "    return redis.call(\"del\",KEYS[1])\n" +
                        "else\n" +
                        "    return 0\n" +
                        "end";
                Long lock2 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(lua_scripts, Long.class), Arrays.asList("lock"), uuid);
            }
            //执行成功以后需要释放锁。
            //删除分布式锁的时候需要判断是不是自己的锁，是自己的锁才可以删除。但这样依然是有问题的，这样是非原子性操作
            //获取lock的值和对比成功删除也需要原子操作可以使用lua脚本
//            String lock1 = stringRedisTemplate.opsForValue().get("lock");
            /*if (uuid.equals(lock1)){
                stringRedisTemplate.delete("lock");
            }*/
            return stringListMap;
        } else {//加锁失败，重试,相当于自旋的方式进行重试。
            //可以休眠一段时间再重试
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    /**
     * 缓存里面的数据如何和数据库保持一直
     * 缓存数据一致性
     * 1、双写模式
     * 2、失效模式
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        //1、占分布式锁setIfAbsent就是redis中的setNX命令
        //这样是原子操作，redis底层是这样做的：setnxex
        Map<String, List<Catelog2Vo>> stringListMap = null;
        //加锁成功,执行业务
        RLock lock = redissonClient.getLock("cateLogJsonLock");
        lock.lock();
        try {
            stringListMap = getStringListMap();
        } finally {
            lock.unlock();
        }
        return stringListMap;
    }

    private Map<String, List<Catelog2Vo>> getStringListMap() {
        String cateJson = stringRedisTemplate.opsForValue().get("cateJson");
        if (StringUtils.isEmpty(cateJson)) {
            Map<String, List<Catelog2Vo>> map = JSON.parseObject(cateJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });

            return map;
        }
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
        //1、查所有1级分类
        List<CategoryEntity> level1Category = getParent_cid(selectList, 0L);
        Map<String, List<Catelog2Vo>> parent_cid = level1Category.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //遍历每一个一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            List<Catelog2Vo> collect = null;
            if (categoryEntities != null) {
                collect = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    List<CategoryEntity> categoryEntities1 = getParent_cid(selectList, l2.getCatId());
                    if (categoryEntities1 != null) {
                        List<Catelog2Vo.Category3Vo> collect1 = categoryEntities1.stream().map(l3 -> {
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect1);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return collect;
        }));

        stringRedisTemplate.opsForValue().set("cateJson", JSON.toJSONString(parent_cid), 1, TimeUnit.DAYS);
        return parent_cid;
    }

    /**
     * 使用本地锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        synchronized (this) {
            return getStringListMap();
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        return collect;
    }

    private List<Long> findParentPath(Long catId, List<Long> paths) {
        paths.add(catId);
        CategoryEntity byId = this.getById(catId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归查找所有子菜单
     *
     * @param menu
     * @param entities
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity menu, List<CategoryEntity> entities) {
        List<CategoryEntity> collect = entities.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(menu.getCatId())).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, entities));
            return categoryEntity;
        }).sorted(Comparator.comparingInt(menu2 -> ((menu2.getSort()) == null ? 0 : menu2.getSort()))).collect(Collectors.toList());
        return collect;
    }
}
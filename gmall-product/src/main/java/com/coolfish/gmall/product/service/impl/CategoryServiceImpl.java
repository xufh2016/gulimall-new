package com.coolfish.gmall.product.service.impl;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author 28251
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {
    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

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
     *  @Transactional开启事务
     * @param category
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCascader(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
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
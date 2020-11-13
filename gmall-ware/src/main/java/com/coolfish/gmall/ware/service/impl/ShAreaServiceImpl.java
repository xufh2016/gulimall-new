package com.coolfish.gmall.ware.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.Query;

import com.coolfish.gmall.ware.dao.ShAreaDao;
import com.coolfish.gmall.ware.entity.ShAreaEntity;
import com.coolfish.gmall.ware.service.ShAreaService;


@Service("shAreaService")
public class ShAreaServiceImpl extends ServiceImpl<ShAreaDao, ShAreaEntity> implements ShAreaService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ShAreaEntity> page = this.page(
                new Query<ShAreaEntity>().getPage(params),
                new QueryWrapper<ShAreaEntity>()
        );

        return new PageUtils(page);
    }

}
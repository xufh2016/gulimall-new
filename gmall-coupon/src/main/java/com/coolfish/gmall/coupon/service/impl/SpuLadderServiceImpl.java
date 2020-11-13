package com.coolfish.gmall.coupon.service.impl;

import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.Query;

import com.coolfish.gmall.coupon.dao.SpuLadderDao;
import com.coolfish.gmall.coupon.entity.SpuLadderEntity;
import com.coolfish.gmall.coupon.service.SpuLadderService;


@Service("spuLadderService")
public class SpuLadderServiceImpl extends ServiceImpl<SpuLadderDao, SpuLadderEntity> implements SpuLadderService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuLadderEntity> page = this.page(
                new Query<SpuLadderEntity>().getPage(params),
                new QueryWrapper<SpuLadderEntity>()
        );

        return new PageUtils(page);
    }

}
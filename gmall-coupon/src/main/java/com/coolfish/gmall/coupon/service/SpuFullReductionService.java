package com.coolfish.gmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.coupon.entity.SpuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:12:01
 */
public interface SpuFullReductionService extends IService<SpuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


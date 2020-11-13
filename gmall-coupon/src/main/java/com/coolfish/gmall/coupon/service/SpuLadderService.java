package com.coolfish.gmall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.coupon.entity.SpuLadderEntity;

import java.util.Map;

/**
 * 商品阶梯价格
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:12:01
 */
public interface SpuLadderService extends IService<SpuLadderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


package com.coolfish.gmall.coupon.dao;

import com.coolfish.gmall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:12:09
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}

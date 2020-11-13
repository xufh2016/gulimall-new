package com.coolfish.gmall.coupon.dao;

import com.coolfish.gmall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动场次
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 09:12:01
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {
	
}

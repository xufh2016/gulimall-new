package com.coolfish.gmall.order.dao;

import com.coolfish.gmall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 08:53:37
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}

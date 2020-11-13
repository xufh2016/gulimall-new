package com.coolfish.gmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 08:53:37
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


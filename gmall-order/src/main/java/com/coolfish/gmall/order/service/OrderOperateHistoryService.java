package com.coolfish.gmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.gmall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author coolfish
 * @email sunlightcs@gmail.com
 * @date 2020-11-03 08:53:37
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}


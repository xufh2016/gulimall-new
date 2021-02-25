package com.coolfish.gmall.order.service.impl;

import com.coolfish.gmall.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolfish.common.utils.PageUtils;
import com.coolfish.common.utils.Query;

import com.coolfish.gmall.order.dao.OrderItemDao;
import com.coolfish.gmall.order.entity.OrderItemEntity;
import com.coolfish.gmall.order.service.OrderItemService;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }


    /**
     *
     * @param message 是这个类型 org.springframework.amqp.core.Message 原生消息详细信息。头+体
     *        T<发送的消息的类型>  OrderReturnReasonEntity content
     *        Channel channel 当前传输数据的通道
     *
     * queues:声明需要监听的所有队列，可以很多人都来监听。只要收到消息，队列删除消息，而且只能有一个收到此消息
     *
     * 监听rabbitMq的方法中参数可以是以下类型：
     *
     *                @RabbitListener注解用來接受消息，方法+類
     * @RabbitHandler注解也用來接受消息，但只能標注在方法上
     *
     */
    @RabbitListener(queues = {"hello-java-queue"})
    public void receiveMessage(Object message, OrderReturnReasonEntity content, Channel channel) {
        System.out.println("接受消息。。。: " + message+"---Type----"+message.getClass());
        System.out.println("消息内容为："+content);
    }

}
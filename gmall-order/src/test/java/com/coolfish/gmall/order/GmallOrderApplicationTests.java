package com.coolfish.gmall.order;

import com.coolfish.gmall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * rabbitmq物理服务器有多个vhost（虚拟机），虚拟机里面可以有多个交换机（exchange），每个交换机里面可以有多个queue，每个queue需要和交换机进行绑定binding
 * RabbitTemplate.ConfirmCallback和RabbitTemplate.ReturnCallback分别是到交换机的回调和到队列的回调（这是消息生产者的确认机制）
 * 消费者的确认机制是通过接收消息时的channel.basicAck进行签收，通过channel.basicNack或channel.basicReject来进行拒签
 */
@Slf4j
@SpringBootTest
class GmallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessage(){
        //当发送消息为对象时，我们会使用序列化机制，因此需要实体类实现Serializable接口

        OrderReturnReasonEntity orderReturnReasonEntity = new OrderReturnReasonEntity();
        orderReturnReasonEntity.setId(1L);
        orderReturnReasonEntity.setName("test rabbitmq----");
        orderReturnReasonEntity.setStatus(1);
        rabbitTemplate.convertAndSend("hello-java-exchange","hello.java",orderReturnReasonEntity);
        log.info("消息发送完成{}","hello rabbitmq");

    }

    @Test
    void createExchange() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false, null);
        amqpAdmin.declareExchange(directExchange);
//        new DefaultSingletonBeanRegistry()


        log.info("Exchange[{}]创建成功", "hello-java-exchange");
    }
    @Test
    void createQueues(){
        Queue queue = new Queue("hello-java-queue",true,false,false,null);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", "hello-java-queue");
    }

    @Test
    public void createBinding(){
        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE,"hello-java-exchange","hello.java",null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", "hello-java-binding");
    }

//
//    @Autowired
//    RabbitTemplate rabbitTemplate;
    @Test
    public void test1(){
        Queue queue = new Queue("hello-java-queue-test",true,false,false,null);
        amqpAdmin.declareQueue(queue);

//        rabbitTemplate.
    }

}

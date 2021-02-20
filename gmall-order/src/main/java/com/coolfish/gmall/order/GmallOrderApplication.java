package com.coolfish.gmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 使用rabbitmq步骤：
 * 1.引入amqp的场景启动器，RabbitAutoConfiguration就会自动生效
 * 2.给容器中自动配置了RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 3.再配置文件中配置相关信息
 * 4.使用注解@EnableRabbit 开启Rabbitmq使用
 */
@SpringBootApplication

public class GmallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallOrderApplication.class, args);
    }

}

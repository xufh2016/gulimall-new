package com.coolfish.gmall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.HashSet;

/**
 * 核心原理
 * 1、@EnableRedisHttpSession 导入了 RedisHttpSessionConfiguration配置
 * @author 28251
 * 引入openfeign的时候注意版本冲突
 */


//@EnableScheduling
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GmallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallAuthServerApplication.class, args);
    }

}

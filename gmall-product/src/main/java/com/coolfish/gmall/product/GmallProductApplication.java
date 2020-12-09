package com.coolfish.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 1、整合mybatis-plus
 * 导入依赖
 * 配置
 * ：  导入驱动
 * 配置数据源
 * 邏輯刪除
 */
@EnableCaching
@EnableFeignClients(basePackages = "com.coolfish.gmall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.coolfish.gmall.product.dao")
@SpringBootApplication
public class GmallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallProductApplication.class, args);
    }

}

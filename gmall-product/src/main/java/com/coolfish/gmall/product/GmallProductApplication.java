package com.coolfish.gmall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1、整合mybatis-plus
 * 导入依赖
 * 配置
 * ：  导入驱动
 * 配置数据源
 * 邏輯刪除
 */
@EnableDiscoveryClient
@MapperScan("com.coolfish.gmall.product.dao")
@SpringBootApplication
public class GmallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallProductApplication.class, args);
    }

}

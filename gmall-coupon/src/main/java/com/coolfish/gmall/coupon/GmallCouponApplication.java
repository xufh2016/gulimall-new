package com.coolfish.gmall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class GmallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallCouponApplication.class, args);
    }

}

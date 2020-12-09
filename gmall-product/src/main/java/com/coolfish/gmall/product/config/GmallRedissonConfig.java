package com.coolfish.gmall.product.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GmallRedissonConfig {
    /**
     * 所有对redisson的使用都是通过RedissonClient对象
     * 这种方式是创建一个单节点的redissonclient
     * @return
     * @throws IOException
     */
    // java.lang.IllegalArgumentException: Redis url should start with redis:// or rediss:// (for SSL connection)
    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.0.84:6379");
        return Redisson.create(config);
    }

}

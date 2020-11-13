# gulimall-new


这是一个商城的微服务项目

共同：
+ 需要引入web 、openfeign
+ 每个服务，包名：com.coolfish.gmall


端口编排：
1. 微服务：
   + coupon：7000
   + memeber：8000
   + order：9000
   + product：10000
   + ware：11000
    
##使用nacos注册中心
+ 在主类上标记@EnableDiscoveryClient注解
+ 在yml文件中配置：spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848（nacos的地址）
+ 再配置spring.application.name=gmall-product(微服务名)

##使用openFeign进行远程调用
1. 在pom文件中引入open-feign
2. 在调用方编写一个接口，告诉springcloud这个接口需要调用远程服务,声明接口中的每一个方法都是调用哪个远程服务的那个请求，实例如下
    ```java
    @FeignClient("gmall-coupon")
    public interface CouponFeignService {
        @RequestMapping("coupon/coupon/member-coupon")
        public R memberCoupons();
    }
    ```
3. 开启远程调用功能,在主类上上添加注解 @EnableFeignClients并设置feign的基础包
    ```java
    @EnableDiscoveryClient
    @SpringBootApplication
    @EnableFeignClients(basePackages = "com.coolfish.gmall.member.feign")
    public class GmallMemberApplication {
        public static void main(String[] args) {
            SpringApplication.run(GmallMemberApplication.class, args);
        }
    }
    ```
##nacos作为配置中心使用
1. 引入spring-cloud-starter-alibaba-nacos-config依赖
2. 创建一个bootstrap.properties文件，名字是固定的，并在其中配置如下
    ```properties
    spring.cloud.nacos.config.server-addr=127.0.0.1:8848
    spring.application.name=gmall-coupon
    ```
    bootstrap.properties文件会早于application.yml文件被加载进工程中。
3. 通过 Spring Cloud 原生注解 @RefreshScope 实现配置自动更新:
    ```java
    @RestController
    @RequestMapping("/config")
    @RefreshScope
    public class ConfigController {
    
        @Value("${useLocalCache:false}")
        private boolean useLocalCache;
    
        @RequestMapping("/get")
        public boolean get() {
            return useLocalCache;
        }
    }
    ```
 4. 配置中心的配置文件默认命名规则为：服务名.properties.
 
 ###nacos配置中的详细解释
 * 细节
 1. 命名空间：配置隔离，
    默认：public（保留空间）；默认新增的所有配置都在public空间
    + 开发、测试、生产：利用命名空间来做环境隔离。注意：在bootstrap.properties配置上，需要使用哪个命名空间下的配置。
      ```properties
      spring.cloud.nacos.config.namespace=命名空间的uuid
      ```
    + 每个微服务之间互相隔离配置，每一个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置
 2. 配置集：所有的配置的集合
 3. 配置集Id：类似于配置文件名，就是Data ID
 4. 配置分组
    + 默认所有的配置集都属于：DEFAULT_GROUP
      ```properties
      spring.cloud.nacos.config,group=分组名（groupname）
      ```
    + 对本项目：每个微服务创建自己的命名空间，使用配置分组区分环境：dev  test   prod
    
 5. 同时加载多个配置集
    + 微服务任何配置信息，任何配置文件都可以放在配置中心中
    + 只需要在bootstrap.properties说明加载配置中心中的哪些配置文件即可，配置中心中有的优先使用配置中心的
  在bootstrap.properties文件中用这个配置项即可
      ```properties
       spring.cloud.nacos.config.ext-config[0].data-id=自定义的配置文件名
      ```  
      spring.cloud.nacos.config.ext-config是个集合List   
 6. 各个微服务中添加了配置中心的依赖以后，需要在资源文件中添加bootstrap.properties的配置文件，并配置相关的配置中心地址等相关信息
```yaml
 routes:
#id：表示唯一的路由，每个路由都需要一个唯一的ID
    - id: product_route
# uri:表示目标地址
      uri: lb://gmall-product #路由，此处必须和application.name的配置一直
#          判断依据，也即是断言，如果是以api/product开头的请求就转发到gmall-product服务
      predicates:
        - Path=/api/product/** #表示匹配/api/product/路径下的任意路径
      filters:
        - RewritePath=/api/(?<segment>),/$\{segment}
```

##网关Gateway

* 术语：
 1. 路由  
 2. 断言  
 3. 过滤器  
 * 需要注意的地方 熟悉gateway的路由和断言规则
 * 需要配置通用的跨域类或使用nginx做代理
 ```java
package com.coolfish.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;


@Configuration
public class CorsConfiguration {
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource co = new UrlBasedCorsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.setAllowCredentials(true);
        co.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(co);
    }
}
```
 
 
 ##Mybatis Plus
 
 1. 设置mybatis debug输出
 ```yaml
logging:
  level:
    com.baomidou.mybatisplus.samples: debug
```
 

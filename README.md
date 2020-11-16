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
 


##aliyun OSS 阿里对象云存储
1. 引入依赖
    ```xml
     <dependency>
         <groupId>com.alibaba.cloud</groupId>
         <artifactId>spring-cloud-starter-alicloud-oss</artifactId>
     </dependency>
    ```
2. 配置key、endpoint相关操作即可
3. 使用OSSClient进行相关操作
4. 在配置中心中添加如下
    ```yaml
       spring:
         cloud:
           alicloud:
             access-key: LTAI4G4a6BaJHCz73MtEFRkV
             secret-key: Za7Yo6T1W6YER4qUtXyFujA7SGdsh7
             oss:
               endpoint: oss-cn-qingdao.aliyuncs.com
    ```
5. 后端校验policy
    ```java
    public R policy() {
        // 请填写您的 endpoint。
        String endpoint = "oss-cn-qingdao.aliyuncs.com";
        // 请填写您的 bucketname 。
        String bucket = "xufh-test-oss-1";
        String accessId = "LTAI4G4a6BaJHCz73MtEFRkV";
        // host的格式为 bucketname.endpoint
        String host = "https://" + bucket + "." + endpoint;
        // callbackUrl为 上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        String dir = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/";
        Map<String, String> respMap = null;
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);
            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);
            respMap = new LinkedHashMap<>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return R.ok().put("data", respMap);
    }
    ```  
 ##JSR 303
 1. 给bean添加校验规则注解并定义自己的message提示,来源包如下
    ```java
     import javax.validation.constraints;
    ```
 2. 在请求接口函数的入参中添加 @Valid 注解，使得SpringMvc启用校验注解,如果要获取校验的结果，在被注解的参数后面紧跟**BindingResult参数**，
     例如：
     ```java
    @RequestMapping("/save")
    public R save(@Valid @RequestBody BrandEntity brand, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            result.getFieldErrors().forEach(fieldError -> {
                String defaultMessage = fieldError.getDefaultMessage();
                String field = fieldError.getField();
                map.put(field,defaultMessage);
            });
            return R.error(400,"提交的数据不合法").put("data",map);
        }else{
            brandService.save(brand);
            return R.ok();
        }
    }
    ```
 3. 统一异常处理
    + 使用注解@ControllerAdvice+@ResponseBody或@RestControllerAdvice注解，例如下面代码
      ```java
      package com.coolfish.gmall.product.exception;
      
      import com.coolfish.common.utils.R;
      import lombok.extern.slf4j.Slf4j;
      import org.springframework.validation.BindingResult;
      import org.springframework.web.bind.MethodArgumentNotValidException;
      import org.springframework.web.bind.annotation.ExceptionHandler;
      import org.springframework.web.bind.annotation.RestControllerAdvice;
      
      import java.util.HashMap;
      import java.util.Map;
      
      /**
       * 集中处理所有异常
       */
      @Slf4j
      //@ControllerAdvice(basePackages = "com.coolfish.gmall.product.controller")
      //
      @RestControllerAdvice(basePackages = "com.coolfish.gmall.product.controller")
      public class GMallExceptionControllerAdvice {
      
          @ExceptionHandler(value = MethodArgumentNotValidException.class)
          public R handleVaildException(MethodArgumentNotValidException e) {
              BindingResult bindingResult = e.getBindingResult();
              Map<String, String> map = new HashMap<>();
              bindingResult.getFieldErrors().forEach(item -> {
                  String field = item.getField();
                  String defaultMessage = item.getDefaultMessage();
                  map.put(field, defaultMessage);
              });
      
              log.error("数据校验出现问题{}，异常类型：{}", e.getMessage(), e.getClass());
              return R.error(400).put("data", map);
          }
      
          // 大范围的异常处理，在此例中是指上面的异常处理方法不能精确处理时，使用此处的异常处理方法
          @ExceptionHandler(value = Throwable.class)
          public R handleException(Throwable e) {
              return R.error();
          }
      }

      ```
       此处的code也可以使用枚举去做，因为整个项目都会用到枚举，因此放到common包中，例如枚举定义如下：
      ```java
        package com.xunqi.common.exception;
        
        /**
         * @Description: 错误状态码枚举
         * @Created: with IntelliJ IDEA.
         * @author: 夏沫止水
         * @createTime: 2020-05-27 17:29
         *
         * 错误码和错误信息定义类
         * 1. 错误码定义规则为5为数字
         * 2. 前两位表示业务场景，最后三位表示错误码。例如：100001。10:通用 001:系统未知异常
         * 3. 维护错误码后需要维护错误描述，将他们定义为枚举形式
         * 错误码列表：
         *  10: 通用
         *      001：参数格式校验
         *      002：短信验证码频率太高
         *  11: 商品
         *  12: 订单
         *  13: 购物车
         *  14: 物流
         *  15：用户
         *
         *
         *
         **/
        
        public enum BizCodeEnum {
        
            UNKNOW_EXCEPTION(10000,"系统未知异常"),
            VAILD_EXCEPTION(10001,"参数格式校验失败"),
            TO_MANY_REQUEST(10002,"请求流量过大，请稍后再试"),
            SMS_CODE_EXCEPTION(10002,"验证码获取频率太高，请稍后再试"),
            PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
            USER_EXIST_EXCEPTION(15001,"存在相同的用户"),
            PHONE_EXIST_EXCEPTION(15002,"存在相同的手机号"),
            NO_STOCK_EXCEPTION(21000,"商品库存不足"),
            LOGINACCT_PASSWORD_EXCEPTION(15003,"账号或密码错误"),
            ;
        
            private Integer code;
        
            private String message;
        
            BizCodeEnum(Integer code, String message) {
                this.code = code;
                this.message = message;
            }
        
            public Integer getCode() {
                return code;
            }
        
            public String getMessage() {
                return message;
            }
        }
    
      ```  
 4.  分组校验，也是JSR303注解提供的功能
       + 在校验注解中添加group属性及值，**@NotEmpty(message = "不能为空",groups = {UpdateGroup.class})**，这样可以标注校验注解在什么情况下
         需要进行校验例如：
       ```java
        @NotEmpty(message = "不能为空",groups = {UpdateGroup.class})
        @URL
        private String logo;
       ```
       用了分组校验就需要将原来接口方法入参的@Valid注解更换为@Validated注解，如下：
       ```java
        public R save(@Validated({AddGroup.class/*分组接口类*/}) @RequestBody BrandEntity brand/*, BindingResult result*/) {
       ``` 
   
     **注意： 默认没有指定分组的校验注解@NotBlank,在分组校验情况下不生效，只会在@Validated生效**
 5. 自定义校验过程
    + 编写一个自定义的校验注解
    + 编写一个自定义的校验器
    + 关联自定义的校验器和自定义的校验注解
    + 代码示例：
    ```java
     package com.coolfish.common.valid;
     
     import javax.validation.Constraint;
     import javax.validation.Payload;
     import java.lang.annotation.*;
     
     /**
      * @author 28251
      */
     @Documented
     @Constraint(
             validatedBy = {ListValueConstraintValidator.class}
     )
     @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
     @Retention(RetentionPolicy.RUNTIME)
     public @interface ListValue {
         String message() default "{com.coolfish.common.valid.ListValue.message}";
     
         Class<?>[] groups() default {};
     
         Class<? extends Payload>[] payload() default {};
     
         int[] vals() default {};
     }

    ```
    
    需要指定Constraint，代码如下：
       ```java
        package com.coolfish.common.valid;
        
        import javax.validation.ConstraintValidator;
        import javax.validation.ConstraintValidatorContext;
        import java.util.HashSet;
        import java.util.Set;
        
        public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
            private Set<Integer> set = new HashSet<>();
        
            @Override
            public void initialize(ListValue constraintAnnotation) {
                int[] vals = constraintAnnotation.vals();
                for (int val : vals) {
                    set.add(val);
                }
            }
        
            /**
             * @param integer 需要校验的值
             * @param constraintValidatorContext 上下文环境信息
             * @return
             */
            @Override
            public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
                return set.contains(integer);
            }
        }
    
       ```
    
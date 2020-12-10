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
2. springboot用于启动环境的配置：
   ```yaml
   spring:
     profiles:
       active: dev
   ```
    
##使用nacos注册中心
+ 在主类上标记@EnableDiscoveryClient注解
+ 在yml文件中配置：spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848（nacos的地址）
+ 再配置spring.application.name=gmall-product(微服务名)

##使用openFeign进行远程调用
1. 在pom文件中引入open-feign
2. 在调用方编写一个接口，告诉springcloud这个接口需要调用远程服务,声明接口中的每一个方法都是调用哪个远程服务的那个请求，实例如下
    ```java
    @FeignClient("gmall-coupon")//gmall-coupon是服务名
    public interface CouponFeignService { //完整拷贝被调用方的controller中的方法签名及路由请求
        @RequestMapping("coupon/coupon/member-coupon")
        public R memberCoupons();
    }
    ```
3. 开启远程调用功能,在主类上添加注解 @EnableFeignClients并设置feign的基础包basePackages属性
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
4. 使用openfeign做远程调用时，方法签名可以不一致，但请求方式、返回值、请求路径必须一致，方法参数需要json模型一致
##nacos作为配置中心使用
1. 引入spring-cloud-starter-alibaba-nacos-config依赖
2. 创建一个bootstrap.properties文件，文件的名字是固定的，并在其中配置如下
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
   配置文件： 
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
 
 ##Mybatis Plus
 
 1. 设置mybatis debug输出
    ```yaml
    logging:
      level:
        com.baomidou.mybatisplus.samples: debug
    ```
 2. Springboot中引入分页插件的方法
    ```java
    import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
    import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
    import org.mybatis.spring.annotation.MapperScan;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.transaction.annotation.EnableTransactionManagement;
    
    @Configuration
    @EnableTransactionManagement
    @MapperScan("com.coolfish.gmall.product.dao")
    public class MyBatisConfig {
        @Bean
        public PaginationInterceptor paginationInterceptor() {
            PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
            // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
             paginationInterceptor.setOverflow(true);
            // 设置最大单页限制数量，默认 500 条，-1 不受限制
             paginationInterceptor.setLimit(1000);
            // 开启 count 的 join 优化,只针对部分 left join
            paginationInterceptor.setCountSqlParser(new JsqlParserCountOptimize(true));
            return paginationInterceptor;
        }
    }
    ```
 3. 使用p6spy进行打印sql语句的步骤：
    + 引入依赖
      ```xml
      <dependency>
          <groupId>p6spy</groupId>
          <artifactId>p6spy</artifactId>
          <version>3.9.0</version>
      </dependency>
      ```
    + 在application.yml文件中添加如下：
      ```yaml
      spring:
        datasource:
          username: root
          password: root
          driver-class-name: com.p6spy.engine.spy.P6SpyDriver
          url: jdbc:p6spy:mysql://localhost:3306/guli_pms  
      ```
    + 在resources目录下添加spy.properties文件，值得注意的是modulelist需要注释掉
      ```properties
      #3.2.1以上使用
      # modulelist=com.baomidou.mybatisplus.extension.p6spy.MybatisPlusLogFactory,com.p6spy.engine.outage.P6OutageFactory
      #3.2.1以下使用或者不配置
      #modulelist=com.p6spy.engine.logging.P6LogFactory,com.p6spy.engine.outage.P6OutageFactory
      # 自定义日志打印
      logMessageFormat=com.baomidou.mybatisplus.extension.p6spy.P6SpyLogger
      #日志输出到控制台
      appender=com.baomidou.mybatisplus.extension.p6spy.StdoutLogger
      # 使用日志系统记录 sql
      #appender=com.p6spy.engine.spy.appender.Slf4JLogger
      # 设置 p6spy driver 代理
      deregisterdrivers=true
      # 取消JDBC URL前缀
      useprefix=true
      # 配置记录 Log 例外,可去掉的结果集有error,info,batch,debug,statement,commit,rollback,result,resultset.
      excludecategories=info,debug,result,commit,resultset
      # 日期格式
      dateformat=yyyy-MM-dd HH:mm:ss
      # 实际驱动可多个
      #driverlist=org.h2.Driver
      # 是否开启慢SQL记录
      outagedetection=true
      # 慢SQL记录标准 2 秒
      outagedetectioninterval=2
      ```

##Aliyun OSS 阿里对象云存储
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
         access-key: 
         secret-key: 
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
    + 使用注解@ControllerAdvice+@ResponseBody或@RestControllerAdvice注解，这样就可以在业务中直接抛出异常就行。例如下面代码
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
       * 1、@ControllerAdvice(basePackages = "com.coolfish.gmall.product.app") 用于指定给哪个包下的进行统一的异常处理
       * 2、@ExceptionHandler用于告诉SpringMvc此异常处理类用于处理哪些异常，这些异常由@ExceptionHandler中的value值进行指定
       *    例如：@ExceptionHandler(value = MethodArgumentNotValidException.class)
       * 3、@RestControllerAdvice = @ControllerAdvice + @ResponseBody
       * 4、通过异常对象获取BindingResult，异常内容都在BindingResult中。
       */
      @Slf4j
      @RestControllerAdvice(basePackages = "com.coolfish.gmall.product.app")
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
              return R.error().put("data", map);
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
    + 在controller中不接受异常，直接抛出异常就可以例如：
      ```java
      @RequestMapping("/save")
      public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult result*/) {      
          brandService.save(brand);
          return R.ok();
      }
       ```
      有@Validated或@Valid进行注解的会被直接抛出，而参数中的BindingResult result则会收集这些异常。
        
 
 
 4.  分组校验，也是JSR303注解提供的功能
       + 在校验注解中添加group属性及值，**@NotEmpty(message = "不能为空",groups = {UpdateGroup.class})**，这样可以标注校验注解在什么情况下
         需要进行校验例如：
       ```java
        @NotEmpty(message = "不能为空",groups = {UpdateGroup.class})
        @URL
        private String logo;
       ```
       用了分组校验就需要将原来接口方法入参的@Valid注解更换为@Validated注解，@Validated注解是hibernate包提供的注解，如下：
       ```java
        public R save(@Validated({AddGroup.class/*分组接口类*/}) @RequestBody BrandEntity brand/*, BindingResult result*/) {
       ``` 
   
     **注意： 默认没有指定分组的校验注解@NotBlank,在分组校验情况下不生效，只会在@Validated生效**
 5. 自定义校验过程
    + 编写一个自定义的校验注解
    + 编写一个自定义的校验器
    + 关联自定义的校验器和自定义的校验注解，关联是通过@Constraint中的属性validatedBy = {ListValueConstraintValidator.class}进行的
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
       **1、@JsonInclude(JsonInclude.Include.NON_EMPTY)注解表示某个字段不为空时，才返回到json对象中**
 6. @Transactional  注解只能应用到 public 可见度的方法上。默认情况下，Spring会对unchecked异常进行事务回滚；如果是checked异常则不回滚。
     通俗一点：你写代码出现的空指针等异常，会被回滚，文件读写，网络出问题，spring就没法回滚了。
    
##VO
1.  View Object，用于接收页面传递的数据，封装对象。将业务处理完成的对象，封装成页面要用的对象



##Elasticsearch
1. 是一个开源的分布式的搜索分析引擎。可以这样理解，ES中的Index对应Mysql中的database，Type对应Mysql中的Table，
   Document对应Mysql表中的一条一条记录
   + Index（索引）
     动词：相当于mysql中的insert；作为动词相当于Mysql中的database
   + Type（类型）
     在Index中，可以定义一个或多个类型。类似于Mysql中的Table；每一种类型的数据放在一起
   + Document
     是json格式的，相当于mysql表中的一条数据,ES中不存在列，只有属性和值，属性就是mysql中的列名。
2. 概念
   + 倒排索引
   
3. 安装，使用docker安装
   + docker pull elasticsearch:7.4.2           存储和检索数据
   + docker pull kibana:7.4.2                  可视化检索数据
   + docker update 容器Id --restart=always     设置容器自动启动
   + 基于docker的安装步骤：
      1. mkdir -p /mydata/elasticsearch/config
      2. mkdir -p /mydata/elasticsearch/data
      3. echo "http.host:0.0.0.0">>/mydata/elasticsearch/config/elasticsearch.yml
      4. docker run --name gmallES -p9200:9200 -p 9300:9300\
         -e "discovery.type=single-node"\
         -e ES_JAVA_OPTS="-Xms64m -Xmx128m"\
         -v /mydata/elasticsearch/config/elasticsearch.yml:/usr/share/elasticsearch/config/elasticsearch.yml\
         -v /mydata/elasticsearch/data:/usr/share/elasticsearch/data\
         -v /mydata/elasticsearch/plugins:/usr/share/elasticsearch/plugins\
         -d elasticsearch:7.4.2
      5. docker logs 查日志 例如docker logs elasticsearch，表示查看es的日志
         chmod -R 777 /mydata/elasticsearch/
      6. 启动容器 docker start 容器Id
      7. **出错**,上述创建过程会出现错误将elasticsearch.yml变为文件夹，因此需要执行下面操作。
         ```shell script
         drwxr-xr-x. 2 root root 6 Oct 26 14:07 elasticsearch.yml
         [root@localhost config]# rm -r elasticsearch.yml/
         rm: remove directory ‘elasticsearch.yml/’? y
         [root@localhost config]# touch elasticsearch.yml
         [root@localhost config]# ls -l
         total 0
         -rw-r--r--. 1 root root 0 Oct 26 14:24 elasticsearch.yml
         ```
   + 安装kibana
     1. docker run --name kibana -e ELASTICSEARCH_HOSTS=http://192.168.0.84:9200 -p 5601:5601 -d kibana:7.4.2

4. 本项目中，ES中的数据来源于Mysql，需要给ES存储一份Mysql中的数据

5. 初步检索
   1. _cat 用于查看es的相关信息
      + GET /_cat/nodes: 查看所有节点
      + GET /_cat/health: 查看es健康状况
      + GET /_cat/master: 查看主节点
      + GET /_cat/indices: 查看所有索引
   2. 索引一个文档（保存）
      + 保存一个数据，保存在哪个索引的哪个类型下，指定用哪个唯一标识
      + PUT customer/external/1
      + POST和PUT都是新增和修改二合一。PUT请求必须要带id；
      + POST请求可以不带，不带的情况下时新增，带的情况下如果有id则更新，没有则新增一次，id不变下次请求为更新。
   3. 更新文档
      + 带_update会对比原数据，如果没有变化则noop（no operation）
      + 不带_update，则会不断的进行更新并叠加版本等 
6. 进阶高级用法
   1. SearchApi
      + ES支持两种基本方式检索：
        + 一个是通过使用Rest request URI发送搜索参数（uri+检索参数）
        + 另一个是通过使用Rest request body来发送它们（uri+请求体） 
   2. QueryDsl
   3. term和match
      https://www.jianshu.com/p/d5583dff4157
   4. aggregations执行聚合  
      聚合提供了从数据中分组和提取数据的能力。最简单的聚合方法大致等于Sql中的group by和sql聚合函数。在ES中，有执行搜索返回hits，
      并且同事返回聚合结果，把一个响应中的所有hits分隔开的能力。
7. 分词（中文分词使用IK分词器）安装在elasticsearch目录下的plugins文件夹中（需要将ik分词器解压到ik文件夹下），  
   需要注意的点是ik分词器的版本要和elasticsearch的版本一一对应
   






##搭建域名访问环境
1. 安装nginx
   + 随便启动一个nginx实例，只是为了复制出配置
     + docker run -p 80:80 --name mynginx -d nginx:1.10
   + 将容器内的配置文件拷贝到当前目录：docker container cp nginx:/etc/nginx .
     + 千万别忘记后面的" ."
   + 修改文件名称：mv nginx conf 把这个conf移动到/mydata/nginx下
   + 终止原容器：docker stop nginx
   + 执行命令删除原容器：docker rm 容器Id
   + 创建新的nginx；执行以下命令
     docker run -p 80:80 --name mynginx\
     -v /mydata/nginx/html:/usr/share/nginx/html\
     -v /mydata/nginx/logs:/var/log/nginx\
     -v /mydata/nginx/conf:/etc/nginx\
     -d nginx:1.10
   + 在做数据卷挂载的时候会自动将html、logs文件夹创建好。     
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
    <properties>
            <comment>IK Analyzer 扩展配置</comment>
            <!--用户可以在这里配置自己的扩展字典 -->
            <entry key="ext_dict"></entry>
             <!--用户可以在这里配置自己的扩展停止词字典-->
             <entry key="ext_stopwords"></entry>
            <!--用户可以在这里配置远程扩展字典 -->
            <entry key="remote_ext_dict">http://192.168.0.84/es/fenci.txt</entry>
            <!--用户可以在这里配置远程扩展停止词字典-->
            <!-- <entry key="remote_ext_stopwords">words_location</entry> -->
    </properties>
    ```
    + 设置重启,docker 容器随系统自动重启
    ```shell script
    docker update mynginx --restart=always
    ```
2. 正向代理、反向代理
   + 让nginx进行反向代理，将所有来自gmall.com的请求，都转到商品服务。
     
   


##压力测试
1. hps： 每秒点击数（hits per second）,单位是次/秒
2. tps： 系统每秒处理交易数，单位是笔/秒（transaction per second）
3. qps： 系统每秒处理查询次数，单位是次/秒（query per second）
4. 无论tps、qps、hps，此项指标是衡量系统处理能力非常重要的指标，越大越好，根据经验，一般情况：
   + 金融行业： 1000tps~50000tps，不包括互联网化的活动，如秒杀等
   + 保险行业： 100tps~100000tps，不包括互联网化的活动
   + 制造行业： 10tps~5000tps
   + 互联网电子商务： 10000tps~1000000tps
   + 互联网中型网站： 1000tps~50000tps
   + 互联网小型网站： 500tps~10000tps
5. 从外部看，性能测试主要关注如下三个指标
   + 吞吐量： 每秒钟系统能够处理的请求数、任务数
   + 响应时间： 服务处理一个请求或一个任务的耗时
   + 错误率： 一批请求中结果出错的请求所占比例   
   
6. JVM调节
   + -Xmx1024m(最大可用内存)  -Xms1024(初始内存大小)  -Xmn512m(eden区大小，新生代)   
   
   
   
###JMeter报错
####JMeter Address Already in use
win本身提供的端口访问机制的问题。win提供给tcp/ip连接的端口为1024~5000，并且要四分钟来循环回收他们，这样就导致我们在短时间内跑大  
量的请求时将端口沾满了
1. cmd中，用regedit命令打开注册表
2. 在HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Tcpic\Parameters下，
   + 右击parameters，添加一个新的DWORD，名字为MaxUserPort
   + 然后双击MaxUserPort，输入数值数据为65534，基数选择十进制（如果是分布式运行的话，控制机器和负载机器都需要这样操作）
   + 修改配置完毕后记得重启机器才能生效
     TCPTimedWaitDelay: 30



##缓存与分布式缓存

1. 哪些数据适合放入缓存？
   + 即时性、数据一致性要求不高的
   + 访问量大且更新频率不高的数据（读多，写少）
2. 缓存的使用
   1. 可以使用本机缓存即Map，单机可以，对分布式系统是不可以的；分布式系统中，需要各个微服务去共享一个缓存中间件（例如redis）
3. 安装redis，使用docker进行安装
   ```shell script
   docker pull redis
   mkdir -p /mydata/redis/conf
   touch /mydata/redis/conf/redis.conf
   
   docker run -p 6379:6379 --name gmallredis -v /mydata/redis/data:/data\
   -v /mydata/redis/conf/redis.conf:/etc/redis/redis.conf\
   -d redis redis-server /etc/redis/redis.conf
   ```
4. springboot整合redis
   ```yaml
   spring:
     redis:
       host: 192.168.0.84
       port: 6379
   ```
   ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
   ```
   使用springboot自动配置好的RedisTemplate<Object,Object>或StringRedisTemplate来操作redis  
   无论使用的是lettuce还是jedis客户端作为redis底层的客户端，springboot都进行了重新封装即RedisTemplate<Object,Object>
    和StringRedisTemplate
5. redis报错redisexception io.netty.util.internal.OutOfDirectMemoryError
   ```java
    //springboot 2.0以后默认使用的是lettuce作为操作redis的客户端。它使用netty进行网络通信。lettuce的bug导致堆外内存溢出。
    //netty如果没有指定堆外内存，就会默认使用在jvm设置中的-Xmx128m，可以通过-Dio.netty.maxDirectMemory进行设置
    //解决方案，不能使用-Dio.netty.maxDirectMemory来调大堆外内存
    //1、升级lettuce客户端。2、切换使用jedis客户端
   ```
   可以这样修改，使用jedis客户端。
   ```xml
   <dependency>
     <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <exclusions>
            <exclusion>
                <groupId>io.lettcue</groupId>
                <artifactId>lettuce-core</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
    </dependency>
   ```
   使用redis缓存。要注意查数据库与存缓存要保持原子性。
6. 使用redis有可能产生的问题
   + 缓存穿透
     指查询一个一定不存在的数据，由于缓存是不命中的，将去查询数据库，但数据库也无此记录，我们没有将这次查询的null写入缓存，这将导致
     这个不存在的数据每次请求都要到存储层去查询，失去了缓存的意义。  
     风险：利用不存在的数据进行攻击，数据库瞬时压力增大，最终导致崩溃。  
     解决：null结果缓存，并加入短暂过期时间，比如几分钟后过期
   
   + 缓存雪崩
     缓存雪崩是指在我们设置缓存时key采用了相同的过期时间，导致缓存在某一时刻同时失效，请求全部转发到DB，DB瞬时压力过重雪崩。  
     解决：原有的失效时间基础上增加一个随机值，比如1~5分钟随机，这样每个缓存的过期时间的重复率就会降低，就较难引发集体失效的事件。
   
   + 缓存击穿
     - 对于一些设置了过期时间的key，如果这些key可能会在某些时间点被超高并发的访问，是一种非常热点的数据。
     - 如果这个key在大量请求同时进来前正好失效，那么所有对这个key的数据查询都落到了db，我们称之为缓存击穿  
     解决：加锁，大量并发只让一个去查，其他人等待，查到以后释放锁，其他人获取到锁，先查缓存，就会有数据，不用去db。
7. 分布式锁
   + 第一种方式
    ```java
        public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
            //1、占分布式锁setIfAbsent就是redis中的setNX命令
            String uuid = UUID.randomUUID().toString();
            //这样是原子操作，redis底层是这样做的：setnxex
            Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> stringListMap = null;
            if (lock) {
                //设置过期时间，这样也是有缺陷的，这是非原子性的。stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);
                //加锁成功,执行业务
                try {
                    stringListMap = getStringListMap();
                } finally {
                    String lua_scripts = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                            "    return redis.call(\"del\",KEYS[1])\n" +
                            "else\n" +
                            "    return 0\n" +
                            "end";
                    Long lock2 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(lua_scripts, Long.class), Arrays.asList("lock"), uuid);
                }
                //执行成功以后需要释放锁。
                //删除分布式锁的时候需要判断是不是自己的锁，是自己的锁才可以删除。但这样依然是有问题的，这样是非原子性操作
                //获取lock的值和对比成功删除也需要原子操作可以使用lua脚本
                //String lock1 = stringRedisTemplate.opsForValue().get("lock");
                /*if (uuid.equals(lock1)){
                    stringRedisTemplate.delete("lock");
                }*/
                return stringListMap;
            } else {//加锁失败，重试,相当于自旋的方式进行重试。
                //可以休眠一段时间再重试
                return getCatalogJsonFromDbWithRedisLock();
            }
        }
    ```
   
8. java中的各种锁
   + synchronized和juc包下的如reentrantlock、读锁、写锁、读写锁、countdownlatch（闭锁）、semaphore（信号量）等都是本地锁，不
     适应于分布式系统中
   + 锁的分类介绍  
     乐观锁和悲观锁:锁的一种宏观分类是乐观锁和悲观锁。乐观锁与悲观锁并不是特定的指哪个锁（java中没有那个具体锁的实现名就叫乐观锁
     或悲观锁），而是在并发情况下的两种不同的策略。
     1. 乐观锁（Optimistic Lock）就是很乐观，每次去拿数据的时候都认为别人不会修改。所以不会上锁。但是如果想要更新数据，则会**更
     新之前检查在读取至更新这段时间别人没有修改过这个数据**，如果修改过，则重新读取，再次尝试更新，循环上去步骤直到更新成功（当然
     也允许更新失败的线程放弃更新操作）。
     
     2. 悲观锁（Pessimistic Lock）就是很悲观，每次去拿数据的时候都认为别人会修改。所以每次都在拿数据的时候上锁。这样别人拿数据
     的时候就会被挡住，直到悲观锁释放，想获取数据的线程再去获取锁，然后再获取数据  
     **悲观锁阻塞事务，乐观锁回滚重试**，他们各有优缺点，没有好坏之分，只有适应的场景的不同区别。**乐观锁适合写比较少，冲突很少
     发生的场景；而写多的场景适合使用悲观锁**
   + 乐观锁的基础 --- CAS
     什么是CAS? Compare-and-Swap,即比较并替换，或者比较并设置
     1. 比较：读取到一个值A，在将其更新为B之前，检查原值是否为A（未被其他线程修改过，这里忽略ABA问题）。
     2. 替换：如果是，更新A为B，结束。如果不是，则不会更新。  
   + 自旋锁
     1. synchronized与Lock interface  
        Java中两种实现加锁的方式：一种是使用synchronized关键字，另一种是使用Lock接口的实现类。synchronized更像是自动挡，而lock
        及实现类则更像手动挡。


9. 整合redisson作为分布式锁等功能框架     
   + 引入依赖
   ```xml
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson</artifactId>
        <version>3.12.0</version>
    </dependency>
   ```
   + 配置redisson(此处配置的是单节点)，注意**setAddress("redis://192.168.0.84:6379")，此处必须加redis://**
   ```java
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
   ```
   Redisson解决了1、自动续期，如果业务超长，运行期间自动给锁续期上新的30s。不用担心业务时间长，锁自动过期被删除；  
   2、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除；3、有一个看门狗机制。4、自动解锁时间
   一定要大于业务的执行时间。5、在手动设置锁的过期时间的时候是不使用看门狗机制的  
   + 问题：lock.lock(10,TimeUnit.SECONDS);在锁时间到了以后，不会自动续期。
     1. 如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
     2. 如我们未指定锁的超时时间，就使用30*1000【LockWatchdogTimeOut看门狗的默认时间】；只要占锁成功，就会启动一个定时任务【
     重新给锁设置时间，新的过期时间就是看门狗的默认时间】，每隔10都会自动internaLockLeaseTime【看门狗时间】
   + 最佳实战：
     lock.lock(30,TimeUnit.SECONDS);//给的时间长点
   + 缓存数据一致性解决方案
     无论是双写模式还是失效模式，都会导致缓存的不一致问题。即多个实例同时更新会出事。
     + 如果是用户维度数据（订单数据、用户数据），这种并发几率非常小，不用考虑这个问题，缓存数据加上过期时间，每隔一段时间触发读的
       主动更新即可。
     + 如果是菜单，商品介绍等基础数据，也可以去使用canal订阅binlog的方式。
     + 缓存数据+过期时间也足够解决大部分业务对于缓存的要求。
     + 通过加锁保证并发读写，写写的时候按顺序排好队。读读无所谓。所以适合使用读写锁。（业务不关心脏数据，允许临时脏数据可忽略）
   + 总结
     + 我们能放入缓存的数据本就不应该是实时性，一致性要求很高的。所以缓存数据的时候加上过期时间，保证每天拿到当前最新数据即可
     + 不应该过度设计，增加系统的复杂性
     + 遇到实时性、一致性要求高的数据，就应该查数据库，即使慢点。
     
     
##Spring Cache
1. 简介
   + Spring从3.1开始定义了Cache和CacheManager接口来统一不同的缓存技术；并支持使用JCache（JSR-107）注解简化我们开发；
   + Cache接口为缓存的组件规范定义，包含缓存的各种操作集合；Cache接口下Spring提供了各种xxxCache的实现，如：RedisCache、
     EhCache、ConcurrentMapCache等。
   + 每次调用需要缓存功能的方法时，Spring会检查指定参数的指定目标方法是否已经被调用过；如果有就直接从缓存中读取方法调用后的结果，
     如果没有就调用方法并缓存结果后返回给用户。下次调用直接从缓存中读取
   + 使用Spring缓存抽象时我们需要关注以下两点
     1. 确定方法需要被缓存及他们的缓存策略
     2. 从缓存中读取之前缓存存储的数据
2. 整合Spring Cache
   1. 引入依赖
   ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
   ```
   2. 引入redis开发场景
   3. 写配置
      ```properties
       spring.cache.type=redis
       #spring.cache.cache-names=
      ```
   4. 使用缓存
      + 开启缓存功能，在启动类上配置
        ```java
        @EnableCaching
        ```
      + 使用注解
   5. 默认行为
      1. 如果缓存中有，方法不再调用
      2. key默认自动生成，缓存的名字：SimpleKey[](自助生成的key)
      3. 缓存的value值。默认使用jdk序列化机制。将序列化后的数据存到redis
      4. 默认ttl时间为-1（及永不过期，这样是不符合规范的），希望要有过期时间
   6. 自定义
      1. 指定生成的缓存使用的key
         ```java
         //每一个需要缓存的数据都要来指定放到哪个名字的缓存中（亦即缓存分区-->推荐按照业务类型分）
         @Cacheable(value = {"category"},key = "'level1Categroies'") //表示当前方法的执行结果需要被缓存，如果缓存中有，方法不用调用，如果缓存中没有，则执行方法，最后将方法的执行结果放入缓存
         @Override
         public List<CategoryEntity> getLevel1Category() {
             List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
             return categoryEntities;
         }
         ```
      2. 指定缓存的数据的存活时间ttl(在配置文件中修改ttl,是以毫秒为单位的)
         ```properties
         spring.cache.redis.time-to-live=3600000
         ```
      3. 将数据保存为json格式（需要自定义缓存管理器）
         + CacheAutoConfiguration->RedisCacheConfiguration->自动配置了RedisCacheManager->初始化所有的缓存->每个缓存决定使用
           什么配置->如果redisCacheConfiguration有就用已有的，没有就使用默认配置->想改缓存的配置，只需要给容器中放一个RedisCacheConfiguration
           即可->就会应用到当前RedisCacheManager管理的所有缓存分区中。
           ```java
           package com.coolfish.gmall.product.config;
           
           import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
           import org.springframework.beans.factory.annotation.Autowired;
           import org.springframework.boot.autoconfigure.cache.CacheProperties;
           import org.springframework.boot.context.properties.EnableConfigurationProperties;
           import org.springframework.cache.annotation.EnableCaching;
           import org.springframework.context.annotation.Bean;
           import org.springframework.context.annotation.Configuration;
           import org.springframework.data.redis.cache.RedisCacheConfiguration;
           import org.springframework.data.redis.serializer.RedisSerializationContext;
           import org.springframework.data.redis.serializer.StringRedisSerializer;
           
           @EnableConfigurationProperties(CacheProperties.class)
           @Configuration
           @EnableCaching
           public class GmallCacheConfig {
               @Autowired
               CacheProperties cacheProperties;
               @Bean
               RedisCacheConfiguration redisCacheConfiguration() {
                   RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig();
                   //redisCacheConfiguration = redisCacheConfiguration.entryTtl();
                   redisCacheConfiguration = redisCacheConfiguration.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
                   redisCacheConfiguration = redisCacheConfiguration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));
                   //将配置文件中的所有配置都生效
                   CacheProperties.Redis redisProperties = cacheProperties.getRedis();
                   if (redisProperties.getTimeToLive() != null) {
                       redisCacheConfiguration = redisCacheConfiguration.entryTtl(redisProperties.getTimeToLive());
                   }
                   if (redisProperties.getKeyPrefix() != null) {
                       redisCacheConfiguration = redisCacheConfiguration.prefixKeysWith(redisProperties.getKeyPrefix());
                   }
                   if (!redisProperties.isCacheNullValues()) {
                       redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
                   }
                   if (!redisProperties.isUseKeyPrefix()) {
                       redisCacheConfiguration = redisCacheConfiguration.disableKeyPrefix();
                   }
                   return redisCacheConfiguration;
               }
           }
           ```
           ```java
           /**
            * 级联更新 ,事务回滚需要完善
            * @CacheEvict:失效模式 key = "'level1Categroies'"是spel表达式，常量一定要加**单引号**
            * @param category
            * @Transactional开启事务
            */
           @CacheEvict(value = "category",key = "'level1Categroies'")
           @Transactional(rollbackFor = Exception.class)
           @Override
           public void updateCascader(CategoryEntity category) {
               this.updateById(category);
               categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
               //例如此处菜单有更新以后可以删除缓存中的数据， stringRedisTemplate.delete("cateLogJsonLock");
               //stringRedisTemplate.delete("cateLogJsonLock");
           }
           ```
         


#FastDFS

##linux
1. apt修改国内镜像源使用命令：
    ```shell script
    sudo nona /etc/apt/sources.list
    ```
   然后在该文件中替换如下内容
   ```shell script
   deb http://mirrors.aliyun.com/ubuntu/ bionic main restricted universe multiverse
   # deb-src https://mirrors.aliyun.com/ubuntu/ bionic main restricted universe multiverse
   deb http://mirrors.aliyun.com/ubuntu/ bionic-updates main restricted universe multiverse
   # deb-src http://mirrors.aliyun.com/ubuntu/ bionic-updates main restricted universe multiverse
   deb http://mirrors.aliyun.com/ubuntu/ bionic-backports main restricted universe multiverse
   # deb-src http://mirrors.aliyun.com/ubuntu/ bionic-backports main restricted universe multiverse
   deb http://mirrors.aliyun.com/ubuntu/ bionic-security main restricted universe multiverse
   # deb-src http://mirrors.aliyun.com/ubuntu/ bionic-security main restricted universe multiverse
   ```
   再ctrl+x保存退出，执行
   ```shell script
   sudo apt-get update
   ```
























#Idea报错信息
1. IDEA下报 SpringBoot Configuration Annotation Processor not found in classpath解决方案:
   ```xml
   <!-- 自定义的元数据依赖-->
   <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-configuration-processor</artifactId>
      <optional>true</optional>
   </dependency>
   ```
   
#细节
1. 统一的格式化时间，使用配置文件
   ```yaml
   spring:
     jackson:
       date-format: yyyy-MM-dd HH:mm:ss
   ```
   
2. 去除数据源依赖
   ```xml
   <dependency>
       <groupId>com.coolfish.gmall</groupId>
       <artifactId>gmall-common</artifactId>
       <version>0.0.1-SNAPSHOT</version>
       <exclusions>
           <exclusion>
               <groupId>mysql</groupId>
               <artifactId>mysql-connector-java</artifactId>
           </exclusion>
           <exclusion>
               <groupId>com.baomidou</groupId>
               <artifactId>mybatis-plus-boot-starter</artifactId>
           </exclusion>
       </exclusions>
   </dependency>
   ```
   或使用注解@SpringBootApplication中的属性exclude标识(exclude = DataSourceAutoConfiguration.class)
   ```java
   package com.coolfish.gmall.search;
   
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
   import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
   import javax.sql.DataSource;
   
   @SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
   @EnableDiscoveryClient
   public class GmallSearchApplication {
   
       public static void main(String[] args) {
           SpringApplication.run(GmallSearchApplication.class, args);
       }
   
   }
   ```
3. Java语法
    **new TypeReference<List<SkuHasStockVo>>()由于TypeReference的构造方法是protected的访问权限，因此需要在new TypeReference<List<SkuHasStockVo>>()后面加上“{}”**
    ```java
    TypeReference<List<SkuHasStockVo>> listTypeReference = new TypeReference<List<SkuHasStockVo>>(){};
    ```
4. docker 安装mysql
    ```shell script
    docker run -p 3306:3306 --name gmall_mysql\
    -v /mydata/mysql/log:/var/log/mysql\
    -v /mydata/mysql/data:/var/lib/mysql\
    -v /mydata/mysql/conf:/etc/mysql\
    -e MYSQL_ROOT_PASSWORD=root\
    -d mysql:5.7
   ```
   ```shell script
   docker exec -it gmall_mysql /bin/bash
   ```
   其中-e MYSQL_ROOT_PASSWORD=root是初始化root用户的密码
   ```shell script
   vi /mydata/mysql/conf/my.cnf
   ```
   做如下修改
   ```config
   [client]
   default-character-set=utf8
   
   [mysql]
   default-character-set=utf8
   
   [mysqld]
   init_connect='SET collation_connection=utf8_unicode_ci'
   init_connect='SET NAMES utf8'
   character-set-server=utf8
   collation-server=utf8_uncode_ci
   skip-character-set-client-handshake
   skip-name-resolve
   ```
5. ApplicationRunner接口和CommandLineRunner接口
   + 作用：实现系统启动完后做一些系统初始化的操作。
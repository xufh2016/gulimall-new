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
        String accessId = "";
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
    引入包依赖,第一个为基础的验证包，第二个为hibernate提供的附加验证包
    ```xml
    <dependency>
        <groupId>javax.validation</groupId>
        <artifactId>validation-api</artifactId>
        <version>2.0.1.Final</version>
    </dependency>
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>6.0.9.Final</version>
    </dependency>
    ```
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
    
    需要指定Constraint，写一个类实现ConstraintValidator接口，代码如下：
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
 7. 
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
         -e ES_JAVA_OPTS="-Xms128m -Xmx256m"\
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
量的请求时将端口占满了
1. cmd中，用regedit命令打开注册表
2. 在HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Tcpic\Parameters下，
   + 右击parameters，添加一个新的DWORD，名字为MaxUserPort
   + 然后双击MaxUserPort，输入数值数据为65534，基数选择十进制（如果是分布式运行的话，控制机器和负载机器都需要这样操作）
   + 修改配置完毕后记得重启机器才能生效
     TCPTimedWaitDelay: 30



##缓存与分布式缓存

1. 哪些数据适合放入缓存？(redis是单线程的)
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
     缓存雪崩是指在我们设置缓存时，key采用了相同的过期时间，导致缓存在某一时刻同时失效，请求全部转发到DB，DB瞬时压力过重雪崩。  
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
     什么是CaS? Compare-and-Swap,即比较并替换，或者比较并设置
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
   Redisson解决了  
   1、自动续期，如果业务超长，运行期间自动给锁续期上新的30s。不用担心业务时间长，锁自动过期被删除；  
   2、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除；  
   3、有一个看门狗机制。  
   4、自动解锁时间一定要大于业务的执行时间。  
   5、在手动设置锁的过期时间的时候是不使用看门狗机制的  
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
   + Spring Cache是Spring框架提供的对缓存使用的抽象类，支持多种缓存
   + Spring从3.1开始定义了Cache和CacheManager接口来统一不同的缓存技术；并支持使用JCache（JSR-107）注解简化我们开发；
   + Cache接口为缓存的组件规范定义，包含缓存的各种操作集合；Cache接口下Spring提供了各种xxxCache的实现，如：RedisCache、
     EhCache、ConcurrentMapCache等。
   + 每次调用需要缓存功能的方法时，Spring会检查指定参数的指定目标方法是否已经被调用过；如果有就直接从缓存中读取方法调用后的结果，
     如果没有就调用方法并缓存结果后返回给用户。下次调用直接从缓存中读取
   + 使用Spring缓存抽象时我们需要关注以下两点
     1. 确定方法需要被缓存及他们的缓存策略
     2. 从缓存中读取之前缓存存储的数据
   + @Cacheable：标记在一个方法上，也可以标记在一个类上。主要是缓存标注对象的返回结果，标注在方法上缓存该方法的返回值，标注在类上
     缓存该类所有的方法返回值。参数：value缓存名、key缓存键值、condition满足缓存条件、unless否决缓存条件
   + @CacheEvict：从缓存中移除响应数据
   + @CachePut：方法支持缓存功能。与@Cacheable不同的是使用@CachePut标注的方法在执行前不会去检查缓存中是否存在之前执行过的结果，
     而是每次都会执行该方法，并将执行结果以键值对的形式存入指定的缓存中。
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
      + 开启缓存功能，在启动类上配置，添加@EnableCaching注解后Spring Boot就会自动帮我们在后台配置一个RedisCacheManager，
        相关的配置是在org.springframework.boot.autoconfigure.cache.RedisCacheConfiguration类中完成的。因此我们在定制化使用redis
        时，可以不写RedisCacheManager
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
         //表示当前方法的执行结果需要被缓存，如果缓存中有，方法不用调用，如果缓存中没有，则执行方法，最后将方法的执行结果放入缓存
         @Cacheable(value = {"category"},key = "'level1Categroies'")
         @Override
         public List<CategoryEntity> getLevel1Category() {
             List<CategoryEntity> categoryEntities = 
                                  baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
             return categoryEntities;
         }
         ```
      2. 指定缓存的数据的存活时间ttl(在配置文件中修改ttl,是以毫秒为单位的)
         ```properties
         spring.cache.redis.time-to-live=3600000
         ```
      3. 将数据保存为json格式（需要自定义缓存管理器）
         + CacheAutoConfiguration->RedisCacheConfiguration->自动配置了RedisCacheManager->初始化所有的缓存->每个缓存决定使用
           什么配置->如果redisCacheConfiguration有就用已有的，没有就使用默认配置->想改缓存的配置，只需要给容器中放一个
           RedisCacheConfiguration即可->就会应用到当前RedisCacheManager管理的所有缓存分区中。
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
   7. Spring Cache的不足：
      + 读模式：
        + 缓存穿透：查询一个null数据（即查询一个永不存在的空数据）。解决：缓存一个空数据：cache-null-values=true
        + 缓存击穿：大量并发进来同时进来查询一个正好过期的数据。  
          解决：加锁?->默认是无加锁的，可以使用@Cacheable(value = {"category"}, key = "'level1Categroies'",sync = true)解决缓存击穿
        + 缓存雪崩：大量的key同时过期。解决：加随机时间
      + 写模式（缓存与数据库一致）：
        + 读写加锁。适用于读多写少的数据
        + 引入Canal，感知到Mysql的更新去更新数据库
        + 读多写多，直接数据库查询就行
      + 总结:
        + 常规数据（读多写少，即时性，一致性要求不高的数据）完全可以使用spring cache，写模式（只要缓存的数据有过期时间就足够了）
        + 特殊数据：特殊设计（如使用canal等）
3. Redis
   1. 常用的5种存储结构
      + key   string  一个key对应一个值
      + key   hash    一个key对应一个hashmap，一般用于存贮对象
      + key   list    一个key对应一个列表        可重复，有序
      + key   set     一个key对应一个集合        不可重复，无序
      + key   zset    一个key对应一个有序集合    不可重复，有序
   2. 另外三种数据结构
      + HyperLogLog   计算近似值的
      + GEO           地理位置
      + BIT：         一般存储的也是一个字符串，存储的是一个byte[] 字节数组  
      
      
      
      
      
##线程池
1. 自定义线程池
    ```java
    package net.agrorobot.car_daemon.config;
    
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.scheduling.annotation.EnableAsync;
    
    import java.util.concurrent.*;
    
    /**
     * @author 28251
     */
    @Configuration
    @EnableAsync
    public class WebullThreadPoolConfig {
   /**
    * 从源码中可以看出，线程池的构造函数有7个参数，分别是corePoolSize、maximumPoolSize、keepAliveTime、unit、workQueue、threadFactory、handler。下面会对这7个参数一一解释。
    *
    * 一、corePoolSize 线程池核心线程大小
    * 线程池中会维护一个最小的线程数量，即使这些线程处理空闲状态，他们也不会 被销毁，除非设置了allowCoreThreadTimeOut。这里的最小线程数量即是corePoolSize。
    *
    * 二、maximumPoolSize 线程池最大线程数量
    * 一个任务被提交到线程池以后，首先会找有没有空闲存活线程，如果有则直接执行，如果没有则会缓存到工作队列（后面会介绍）中，如果工作队列满了，才会创建一个新线程，然后从工作队列的头部取出一个任务交由新线程来处理，
    * 而将刚提交的任务放入工作队列尾部。线程池不会无限制的去创建新线程，它会有一个最大线程数量的限制，这个数量即由maximunPoolSize指定。
    *
    * 三、keepAliveTime 空闲线程存活时间
    * 一个线程如果处于空闲状态，并且当前的线程数量大于corePoolSize，那么在指定时间后，这个空闲线程会被销毁，这里的指定时间由keepAliveTime来设定
    *
    * 四、unit 空闲线程存活时间单位
    * keepAliveTime的计量单位
    *
    * 五、workQueue 工作队列
    * 新任务被提交后，会先进入到此工作队列中，任务调度时再从队列中取出任务。jdk中提供了四种工作队列：
    *
    * ①ArrayBlockingQueue
    * 基于数组的有界阻塞队列，按FIFO排序。新任务进来后，会放到该队列的队尾，有界的数组可以防止资源耗尽问题。当线程池中线程数量达到corePoolSize后，再有新任务进来，则会将任务放入该队列的队尾，等待被调度。
    * 如果队列已经是满的，则创建一个新线程，如果线程数量已经达到maxPoolSize，则会执行拒绝策略。
    *
    * ②LinkedBlockingQuene
    * 基于链表的无界阻塞队列（其实最大容量为Interger.MAX），按照FIFO排序。由于该队列的近似无界性，当线程池中线程数量达到corePoolSize后，再有新任务进来，会一直存入该队列，而不会去创建新线程直到maxPoolSize，
    * 因此使用该工作队列时，参数maxPoolSize其实是不起作用的。
    *
    * ③SynchronousQuene
    * 一个不缓存任务的阻塞队列，生产者放入一个任务必须等到消费者取出这个任务。也就是说新任务进来时，不会缓存，而是直接被调度执行该任务，如果没有可用线程，则创建新线程，如果线程数量达到maxPoolSize，则执行拒绝策略。
    *
    * ④PriorityBlockingQueue
    * 具有优先级的无界阻塞队列，优先级通过参数Comparator实现。
    *
    * 六、threadFactory 线程工厂
    * 创建一个新线程时使用的工厂，可以用来设定线程名、是否为daemon线程等等
    *
    * 七、handler 拒绝策略
    * 当工作队列中的任务已到达最大限制，并且线程池中的线程数量也达到最大限制，这时如果有新任务提交进来，该如何处理呢。这里的拒绝策略，就是解决这个问题的，jdk中提供了4中拒绝策略：
    *
    * ①CallerRunsPolicy
    * 该策略下，在调用者线程中直接执行被拒绝任务的run方法，除非线程池已经shutdown，则直接抛弃任务。
    * 
    * ②AbortPolicy
    * 该策略下，直接丢弃任务，并抛出RejectedExecutionException异常。
    * 
    * ③DiscardPolicy
    * 该策略下，直接丢弃任务，什么都不做。
    * 
    * ④DiscardOldestPolicy
    * 该策略下，抛弃进入队列最早的那个任务，然后尝试把这次拒绝的任务放入队列
    */

        @Bean
        public ExecutorService getAsyncExecutor() {
            /**
             * 自定义线程池
             * 1. 将所有的异步任务都交给线程池执行。当前系统中线程池只有一到两个，每个异步任务直接提交给线程池，让线程池去执行线程就好。
             * 2. 启动线程池可以使用submit()或execute()
             */
            int corePoolSize = Runtime.getRuntime().availableProcessors();
            ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
                    corePoolSize,
                    corePoolSize * 2,
                    3,
                    TimeUnit.SECONDS,
                    new LinkedBlockingDeque<>(100),//这个值可以根据压测得到的最大值
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.DiscardOldestPolicy());
            poolExecutor.allowCoreThreadTimeOut(true);
            return poolExecutor;
        }
    }
    ```
   
2. 为什么不用另外三个jdk提供的线程池api呢？  
   为了规避资源耗尽的风险。  
3. 当并发进来时，线程池是这样做的，先执行corepoolsize的任务，再将剩余的线程放到等待队列中，如果等待队列依然容纳不了剩余的线程，则会
   再开启maxpoolsize-corepoolsize大小的线程，将任务放入这些线程中，再有剩余则根据线程处理策略进行丢弃。   
4. CompletableFuture异步编排  
   1. 线程串行化(如果使用ComplatableFuture来开启异步编排，不要用链式调用，链式调用又会串行化，而不是异步)  
   
   这样使用：
   ```java
   public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {

       SkuItemVo skuItemVo = new SkuItemVo();

       CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
           //1、sku基本信息的获取  pms_sku_info
           SkuInfoEntity info = this.getById(skuId);
           skuItemVo.setInfo(info);
           return info;
       }, executor);

       CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
           //3、获取spu的销售属性组合
           List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrBySpuId(res.getSpuId());
           skuItemVo.setSaleAttr(saleAttrVos);
       }, executor);

       CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
           //4、获取spu的介绍    pms_spu_info_desc
           SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
           skuItemVo.setDesc(spuInfoDescEntity);
       }, executor);

       CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
           //5、获取spu的规格参数信息
           List<SpuItemAttrGroupVo> attrGroupVos = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
           skuItemVo.setGroupAttrs(attrGroupVos);
       }, executor);

       //2、sku的图片信息    pms_sku_images
       CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
           List<SkuImagesEntity> imagesEntities = skuImagesService.getImagesBySkuId(skuId);
           skuItemVo.setImages(imagesEntities);
       }, executor);

       CompletableFuture<Void> seckillFuture = CompletableFuture.runAsync(() -> {
           //3、远程调用查询当前sku是否参与秒杀优惠活动
           R skuSeckilInfo = seckillFeignService.getSkuSeckilInfo(skuId);
           if (skuSeckilInfo.getCode() == 0) {
               //查询成功
               SeckillSkuVo seckilInfoData = skuSeckilInfo.getData("data", new TypeReference<SeckillSkuVo>() {
               });
               skuItemVo.setSeckillSkuVo(seckilInfoData);

               if (seckilInfoData != null) {
                   long currentTime = System.currentTimeMillis();
                   if (currentTime > seckilInfoData.getEndTime()) {
                       skuItemVo.setSeckillSkuVo(null);
                   }
               }
           }
       }, executor);
       //等到所有任务都完成
       CompletableFuture.allOf(saleAttrFuture,descFuture,baseAttrFuture,imageFuture,seckillFuture).get();
       return skuItemVo;
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

##页面跳转
1. 可以用这种方式替换
   ```java
   package com.coolfish.gmall.auth.config;
   
   import org.springframework.context.annotation.Configuration;
   import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
   import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
   
   
   @Configuration
   public class GmallWebConfig implements WebMvcConfigurer {
       /**
        * 视图映射中添加好页面跳转
        * @param registry
        */
   
       @Override
       public void addViewControllers(ViewControllerRegistry registry) {
           registry.addViewController("/login.html").setViewName("login");
           registry.addViewController("/reg.html").setViewName("reg");
       }
   }
   ```
   原来的方式：
   ```java
   package com.coolfish.gmall.auth.controller;
   
   import org.springframework.stereotype.Controller;
   import org.springframework.web.bind.annotation.GetMapping;
   
   @Controller
   public class LoginController {
       /**
        * 发送一个请求直接跳转到一个页面。
        * springmvc的viewcontroller；将请求和页面映射过来
          路径转发默认用get请求
        * @return
        */
   
       @GetMapping("login.html")
       public String loginPage() {
   
   
           return "login";
       }
   
       @GetMapping("reg.html")
       public String regPage() {
   
   
           return "reg";
       }
   }
   ```
2. @RequestParam和@PathVariable   
   + 通过@PathVariable，例如/blogs/1  
     1、当URL指向的是某一具体业务资源（或资源列表），例如博客，用户时，使用@PathVariable
   + 通过@RequestParam，例如blogs?blogId=1  
     2、当URL需要对资源或者资源列表进行过滤，筛选时，用@RequestParam

3. BCryptPasswordEncoder类的使用
   + 用的时候直接new   

4.  OAuth2.0

5. session共享问题
   + 不同域名不能共享session
   + 分布式下session共享问题
   + session在集群环境下不同步 
   1. 解决方案，使用spring session进行session域放大
       + pom依赖
         ```xml
         <dependency>
             <groupId>org.springframework.session</groupId>
             <artifactId>spring-session-data-redis</artifactId>
         </dependency>
         ```
       + 在properties文件中做如下配置
         ```properties
         #配置springsesion保存类型
         spring.session.store-type=redis
         ```
       + 在主类或redis的配置类上添加注解@EnableRedisHttpSession，如：
         ```java
         package com.coolfish.gmall.auth;
            
         import org.springframework.boot.SpringApplication;
         import org.springframework.boot.autoconfigure.SpringBootApplication;
         import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
         import org.springframework.cloud.openfeign.EnableFeignClients;
         import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
            
         import java.util.HashSet;
            
         /**
         * 核心原理
         * 1、@EnableRedisHttpSession 导入了 RedisHttpSessionConfiguration配置
         * @author 28251
         * 引入openfeign的时候注意版本冲突
         */
         @EnableRedisHttpSession
         @EnableFeignClients
         @EnableDiscoveryClient
         @SpringBootApplication
         public class GmallAuthServerApplication {
             public static void main(String[] args) {
                 SpringApplication.run(GmallAuthServerApplication.class, args);
             }
         }
         ```
       + 放大作用域及反序列化
           ```java
           package com.coolfish.gmall.auth.config;
           
           import org.springframework.context.annotation.Bean;
           import org.springframework.context.annotation.Configuration;
           import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
           import org.springframework.data.redis.serializer.RedisSerializer;
           import org.springframework.session.web.http.CookieSerializer;
           import org.springframework.session.web.http.DefaultCookieSerializer;
           /**
            *
            * @author 28251
            */
           @Configuration
           public class GmallSessionConfig {
               /**
                * cookie中存放session，因此放大session的作用域相当于放大cookie的作用域
                * @return
                */
               //
               @Bean
               public CookieSerializer cookieSerializer() {
                   DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
                   //放大作用域 ，放大到父域名
                   cookieSerializer.setDomainName("gmall.com");
                   cookieSerializer.setCookieName("GULISESSION");
                   return cookieSerializer;
               }
               //json 序列化,自定义redis的序列化器
               @Bean
               public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
                   return new GenericJackson2JsonRedisSerializer();
               }
           }
           ```
       


##多系统单点登录
1. 






##购物车中的知识点
1. 拦截器的使用
   ```java
   package com.coolfish.gmall.cart.interceptor;
   
   import com.coolfish.common.constant.AuthServerConstant;
   import com.coolfish.common.constant.CartConstant;
   import com.coolfish.common.vo.MemberResponseVo;
   import com.coolfish.gmall.cart.to.UserInfoTo;
   import org.springframework.stereotype.Component;
   import org.springframework.util.StringUtils;
   import org.springframework.web.servlet.HandlerInterceptor;
   import org.springframework.web.servlet.ModelAndView;
   
   import javax.servlet.http.Cookie;
   import javax.servlet.http.HttpServletRequest;
   import javax.servlet.http.HttpServletResponse;
   import javax.servlet.http.HttpSession;
   import java.util.UUID;
   
   /**
    * 在执行目标方法之前，判断用户的登陆状态。并封装传递给目标请求
    *
    * @author 28251
    * springmvc拦截器必须实现HandlerInterceptor接口
    */
   @Component
   public class CartInterceptor implements HandlerInterceptor {
   
       public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
   
       /**
        * 拦截器用于拦截哪个请求是需要配置的
        * 这个方法用于处理目标方法执行之前判断用户登录状态，并封装传递给controller目标请求
        *
        * @param request
        * @param response
        * @param handler
        * @return false代表不放行目标方法，true代表放行目标方法
        * @throws Exception
        */
       @Override
       public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
           UserInfoTo info = new UserInfoTo();
   
           HttpSession session = request.getSession();
           MemberResponseVo attribute = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
           if (attribute != null) {
               //用户没登录
               info.setUserId(attribute.getId());
           }
           Cookie[] cookies = request.getCookies();
           if (cookies != null && cookies.length > 0) {
               for (Cookie cookie : cookies) {
                   if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                       info.setUserKey(cookie.getValue());
                       info.setTempUser(true);
                   }
               }
           }
           //如果没有临时用户一定分配一个临时用户
           if (StringUtils.isEmpty(info.getUserKey())) {
               String uuid = UUID.randomUUID().toString();
               info.setUserKey(uuid);
           }
           //目标方法执行之前
           threadLocal.set(info);
           return true;
       }
   
       /**
        * 业务执行之后需要操作的,分配临时用户，让浏览器保存
        *
        * @param request
        * @param response
        * @param handler
        * @param modelAndView
        * @throws Exception
        */
       @Override
       public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
           UserInfoTo userInfoTo = threadLocal.get();
           if (!userInfoTo.getTempUser()) {
               Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
               cookie.setDomain("gmall.com");
               cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
               response.addCookie(cookie);
           }
   
       }
   }
   ```
   要使自定义拦截器工作需要实现WebMvcConfigurer接口，并重写addInterceptors方法，在registry添加拦截请求.  
   在config包下实现如下配置：
   ```java
   package com.coolfish.gmall.cart.config;
   
   import com.coolfish.gmall.cart.interceptor.CartInterceptor;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
   import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
   
   /**
    * @author 28251
    * 要使自定义拦截器工作需要实现WebMvcConfigurer接口，并重新addInterceptors方法，在registry添加拦截请求
    */
   @Configuration
   public class GmallWebConfig implements WebMvcConfigurer {
       @Override
       public void addInterceptors(InterceptorRegistry registry) {
           //“/**”表示拦截所有请求
           registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
       }
   }
   ```
2. ThreadLocal的使用
   通常情况下，我们创建的变量是可以被任何一个线程访问并修改的。而使用ThreadLocal创建的变量只能被当前线程访问，其他线程则无法访问
   和修改。

##Java线程与硬件处理器
在Window系统和Linux系统上，Java线程的实现是基于一对一的线程模型，所谓的一对一模型，实际上就是通过语言级别层面程序去间接调用系统
内核的线程模型，即我们在使用Java线程时，Java虚拟机内部是转而调用当前操作系统的内核线程来完成当前任务。这里需要了解一个术语，内核
线程(Kernel-Level Thread，KLT)，它是由操作系统内核(Kernel)支持的线程，这种线程是由操作系统内核来完成线程切换，内核通过操作调度
器进而对线程执行调度，并将线程的任务映射到各个处理器上。每个内核线程可以视为内核的一个分身,这也就是操作系统可以同时处理多任务的
原因。由于我们编写的多线程程序属于语言层面的，程序一般不会直接去调用内核线程，取而代之的是一种轻量级的进程(Light Weight Process)
，也是通常意义上的线程，由于每个轻量级进程都会映射到一个内核线程，因此我们可以通过轻量级进程调用内核线程，进而由操作系统内核将任务
映射到各个处理器，这种轻量级进程与内核线程间1对1的关系就称为一对一的线程模型。
![avatar](static/img/7925105-79fa27d5bba342d5.webp)


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
   
6. 浏览器对空格“ ”的编译为20%  java对空格的编译为“+”    
 
7. springboot整合redis时，连接正常，但取不到数据的问题解决方式：  
   取不到数据的原因是：  
   如果key没做序列化存储，实际存进去的key前面会多几个字符，如果你用redis客户端查询你想要的key，  
   最好在程序里对key进行序列化，这样最终的key值才是你想要的key,redisTemplate下面有这两个属性
   ```xml
    <property name="keySerializer">  
        	<bean class="org.springframework.data.redis.serializer.StringRedisSerializer" />  
    </property>   
    <property name="hashKeySerializer">  
        <bean class="org.springframework.data.redis.serializer.StringRedisSerializer" />  
    </property> 
   ```
   解决方案是：在springboot中必须去自定义实现redis的序列化
   如下面方式：
  ```java
    @Configuration
    public class RedisConfig {
    
        @Bean
        JedisConnectionFactory jedisConnectionFactory() {
            return new JedisConnectionFactory();
        }
    
        @Bean
        public <T> RedisTemplate<String, T> redisTemplate(RedisConnectionFactory factory) {
            RedisTemplate<String, T> template = new RedisTemplate<String, T>();
            template.setConnectionFactory(factory);
    //1.序列化key
            template.setKeySerializer(new StringRedisSerializer());//spring自带
    //2.序列划value
            template.setValueSerializer(new RedisObjectSerializer());//自定义
            return template;
        }
    }
  ```
  spring-boot-starter-web默认引入了jackson的相关包。 
#Spring Boot

1. 遇到的注解 @QueryParam和@PathParam ，这两个注解是javax.websocket.server下的注解，也就是说是websocket的注解
   * @QueryParam 主要通过键值对这样取 如 a=1     QueryParam("a")
     如  localhost:8080/introduction?bookId=1?gg=2?version=3?platform=4?vps=5
     此时我们就用QueryParam   通过@QueryParam("bookId") Integer bookId 
     ```java
        @GET
        @Path("/introduction")
        Response introduction(
                @QueryParam("bookId") Integer bookId,
                @QueryParam("gg") Integer gg,
                @QueryParam("version") String version,
                @QueryParam("platform") String platform,
                @QueryParam("vps") String vps
        );
     ```
     
   * @PathParam   主要通过路径映射取(后面解
     localhost:8080/introduction/1/2/3/4/5  对应下面的  /{bookId}/{gg}/{version}/{plarform}/{vps}  
     ```java
        @GET
        Response introduction(
                @PathParam ("bookId") Integer bookId,
                @PathParam ("gg") Integer gg,
                @PathParam ("version") String version,
                @PathParam ("platform") String platform,
                @PathParam ("vps") String vps
        );
     ```







#并发发生的根本原因
## CPU核心数，线程数，时间片轮转机制解读
+ CPU个数：是指物理上，即硬件上的核心数；
+ 核心数：是逻辑上的，简单理解为逻辑上模拟出的核心数；
+ 线程数：是同一时刻设备能并行执行的程序个数，线程数 = cpu个数 * 核数；

##CPU核心数和Java多线程概念
+ 单个CPU线程在同一时刻只能执行单一Java程序，也就是一个线程。
+ 单个线程同时只能在单个CPU线程中执行
+ 线程是操作系统最小的调度单位，进程是资源分配的最小单位
+ Java中的所有线程在JVM进程中，CPU调度的是jvm进程中的线程
+ Java多线程并不是由于CPU线程数为多个才称为多线程，当Java线程数大于CPU线程数时，操作系统使用时间片轮转机制，采用线程调度算法，
  频繁的进行线程切换

##IO阻塞时，线程会释放CPU吗？
  当线程处于IO操作时，线程是阻塞的，线程由运行状态切换到等待状态。此时CPU会做上下文切换，以便处理其他程序；当IO操作完成后，CPU会
  收到一个来自硬盘的中断信号，CPU正在执行的线程因此会被打断，回到ready队列。而先前因IO而waiting的线程随着IO的完成也再次回到就绪
  队列，此时CPU可能会选择他执行

##Java中并发和并行的概念
+ 并行：指两个或多个事件在同一时刻点发生，CPU同时执行；
+ 并发：指两个或多个事件在同一时间段内发生，CPU交替执行；

##Java线程可以同时在多个核上运行吗？
  操作系统是基于线程调度的，在同一时刻，Java进程中不同的线程可能会在不同的核上并行运行。

##Java内存模型
Java 内存模型实际上就是规范了 JVM 如何提供按需禁用缓存和重排序优化的方法。其核心就包括volatile、synchronized 和 final 三个关键
字，以及几项Happens-Before 规则。有了JMM 作为java的开发人员只需要使用几个关键字(sychronized，volatile，final) ,并且理解几个
happens before规则，就能根据自己的需要来禁用缓存优化和指令重排序，从而避免并发问题。  
计算机在执行程序时，每条指令都是在CPU中执行的，而执行指令过程中，势必涉及到数据的读取和写入。由于程序运行过程中的临时数据是存放在
主存（物理内存）当中的，这时就存在一个问题，由于CPU执行速度很快，而从内存读取数据和向内存写入数据的过程跟CPU执行指令的速度比起来
要慢的多，因此如果任何时候对数据的操作都要通过和内存的交互来进行，会大大降低指令执行的速度。因此在CPU里面就有了高速缓存。  
也就是，当程序在运行过程中，会将运算需要的数据从主存复制一份到CPU的高速缓存当中，那么CPU进行计算时就可以直接从它的高速缓存读取数
据和向其中写入数据，当运算结束之后，再将高速缓存中的数据刷新到主存当中。  
　　为了解决缓存不一致性问题，通常来说有以下2种解决方法：  
　　1）通过在总线加LOCK#锁的方式  
　　2）通过缓存一致性协议  
　　这2种方式都是硬件层面上提供的方式。
在早期的CPU当中，是通过在总线上加LOCK#锁的形式来解决缓存不一致的问题。因为CPU和其他部件进行通信都是通过总线来进行的，如果对总线加
LOCK#锁的话，也就是说阻塞了其他CPU对其他部件访问（如内存），从而使得只能有一个CPU能使用这个变量的内存。比如上面例子中 如果一个线
程在执行 i = i +1，如果在执行这段代码的过程中，在总线上发出了LCOK#锁的信号，那么只有等待这段代码完全执行完毕之后，其他CPU才能从
变量i所在的内存读取变量，然后进行相应的操作。这样就解决了缓存不一致的问题。  
　　但是上面的方式会有一个问题，由于在锁住总线期间，其他CPU无法访问内存，导致效率低下。  
所以就出现了缓存一致性协议。最出名的就是Intel 的MESI协议，MESI协议保证了每个缓存中使用的共享变量的副本是一致的。它核心的思想
是：当CPU写数据时，如果发现操作的变量是共享变量，即在其他CPU中也存在该变量的副本，会发出信号通知其他CPU将该变量的缓存行置为无效状
态，因此当其他CPU需要读取这个变量时，发现自己缓存中缓存该变量的缓存行是无效的，那么它就会从内存重新读取。  
*指令重排序的原则是：不影响单线程的执行结果*  
在Java内存模型中，也会存在缓存一致性问题和指令重排序的问题  
Java内存模型规定所有的变量都是存在主存当中（类似于前面说的物理内存），每个线程都有自己的工作内存（类似于前面的高速缓存）。线程对
变量的所有操作都必须在工作内存中进行，而不能直接对主存进行操作。并且每个线程不能访问其他线程的工作内存。  
举个简单的例子：在java中，执行下面这个语句：  
```java
int i = 10;
```
执行线程必须先在自己的工作线程中对变量i所在的缓存行进行赋值操作，然后再写入主存当中。而不是直接将数值10写入主存当中。  
　　那么Java语言 本身对 原子性、可见性以及有序性提供了哪些保证呢？
   1. 原子性  
      + 在Java中，对基本数据类型的变量的读取和赋值操作是原子性操作，即这些操作是不可被中断的，要么执行，要么不执行。  
      + 只有简单的读取、赋值（而且必须是将数字赋值给某个变量，变量之间的相互赋值不是原子操作）才是原子操作。  
      + Java内存模型只保证了基本读取和赋值是原子性操作，如果要实现更大范围操作的原子性，可以通过synchronized和Lock来实现。由于
        synchronized和Lock能够保证任一时刻只有一个线程执行该代码块，那么自然就不存在原子性问题了，从而保证了原子性。
   2. 可见性
      + 对于可见性，Java提供了volatile关键字来保证可见性。
      + 当一个共享变量被volatile修饰时，它会保证修改的值会立即被更新到主存，当有其他线程需要读取时，它会去内存中读取新值。
      + 另外，通过synchronized和Lock也能够保证可见性，synchronized和Lock能保证同一时刻只有一个线程获取锁然后执行同步代码，并且
        在释放锁之前会将对变量的修改刷新到主存当中。因此可以保证可见性。
   3. 有序性  
      + 在Java内存模型中，允许编译器和处理器对指令进行重排序，但是重排序过程不会影响到单线程程序的执行，却会影响到多线程并发执行
        的正确性。  
      + 在Java里面，可以通过volatile关键字来保证一定的“有序性”（具体原理在下一节讲述）。另外可以通过synchronized和Lock来保证有序
        性，很显然，synchronized和Lock保证每个时刻是有一个线程执行同步代码，相当于是让线程顺序执行同步代码，自然就保证了有序性。
      + Java内存模型具备一些先天的“有序性”，即不需要通过任何手段就能够得到保证的有序性，这个通常也称为 happens-before 原则。如
        果两个操作的执行次序无法从happens-before原则推导出来，那么它们就不能保证它们的有序性，虚拟机可以随意地对它们进行重排序。
      + happens-before原则（先行发生原则）
        + 程序顺序规则：一个线程内（就是指单线程内），按照代码顺序，书写在前面的操作现行发生于书写在后面的操作（这条规则只能用来
          保证单线程的执行顺序，同时这条规则要阐述了为什么单线程情况下不影响指令重排序）
        + 锁定规则：一个unlock操作现行发生于后面对同一个锁的lock操作
        + volatile变量规则：对一个变量的写操作先行发生于后面对这个变量的读操作（如果一个线程先去写一个变量，然后一个线程去进行读
          取,那么写入操作肯定会先行发生于读操作）
        + 传递规则：如果操作A先行发生于操作B，而操作B又先行发生于操作C，则可以得出操作A先行发生于操作C
        + 线程启动规则：Thread对象的start方法先行发生于此线程的每一个动作
        + 线程中断规则：对线程interrrupt方法的调用先行发生于被中断线程的代码检测到中断事件的发生
        + 线程终结规则：线程中所有的操作都先行发生于线程的终止检测，我们可以通过Thread.join()方法结束、Thread.isAlive方法的返回
          值手段检测到线程已经终止执行。
        + 对象终结规则：一个对象的初始化完成先行发生于他的finalize()方法的开始    
   4. volatile关键字的两层语义，一旦一个共享变量（类的成员变量、类的静态成员变量）被volatile修饰之后，那么就具备了两层语义:      
   　 + 保证了不同线程对这个变量进行操作时的可见性，即一个线程修改了某个变量的值，这新值对其他线程来说是立即可见的。  
   　 + 禁止进行指令重排序。
      + volatile不能保证原子性操作。
      + 在java 1.5的java.util.concurrent.atomic包下提供了一些原子操作类，即对基本数据类型的 自增（加1操作），自减（减1操作）、
        以及加法操作（加一个数），减法操作（减一个数）进行了封装，保证这些操作是原子性操作。atomic是利用CAS来实现原子性操作的
        （Compare And Swap），CAS实际上是利用处理器提供的CMPXCHG指令实现的，而处理器执行CMPXCHG指令是一个原子性操作。
      + volatile能保证有序性吗？
        在前面提到volatile关键字能禁止指令重排序，所以volatile能在一定程度上保证有序性。  
        volatile关键字禁止指令重排序有两层意思：
        1. 当程序执行到volatile变量的读操作或写操作时，在其前面的操作的更改肯定全部已经进行，且结果已经对后面的操作可见；在其后面
           的操作肯定还没有进行
        2. 在进行指令优化时，不能讲在对volatile变量访问的语句放在其后面执行，也不能把volatile变量后面的语句放到其前面执行
   5. volatile关键字的原理和实现机制
      + 观察加入volatile关键字和没有加入volatile关键字时所生成的汇编代码发现，加入volatile关键字时，会多出一个lock前缀指令
      + lock前缀指令实际上相当于一个内存屏障（也成内存栅栏），内存屏障会提供3个功能：
        1. 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障
           这句指令时，在它前面的操作已经全部完成；
        2. 它会强制将对缓存的修改操作立即写入主存；
        3. 如果是写操作，它会导致其他CPU中对应的缓存行无效。
   6. 使用volatile关键字的场景  
      synchronized关键字是防止多个线程同时执行一段代码，那么就会很影响程序执行效率，而volatile关键字在某些情况下性能要优于
      synchronized，但是要注意volatile关键字是无法替代synchronized关键字的，因为volatile关键字无法保证操作的原子性。通常来说，
      使用volatile必须具备以下2个条件：
      1. 对变量的写操作不依赖于当前值
      2. 该变量没有包含在具有其他变量的不变式中
      实际上，这些条件表明，可以被写入volatile变量的这些有效值独立于任何程序的状态，包括变量的当前状态
##时间片轮转机制
  + 时间片轮转法（Round-Robin，RR）
    根据先进先出原则，排成队列（就绪队列），调度时，将CPU分配给队首进程，让其执行一个时间段（称为时间片），时间片通常为10-100ms，
    当执行的时间片用完时，会由计数器发出时钟中断请求，调度程序便据此来停止该进程的执行，并将它排到队列末尾，然后再把CPU重新分配给
    当前队列的队首进程，同理如此往复。  
    时间片大小取决于：
    1. 系统对响应时间的要求
    2. 就绪队列中进程的数目
    3. 系统的处理能力
  + Java调度机制
    所有的Java虚拟机都有一个线程调度器，用来确定哪个时刻运行哪个线程。主要包含两种：抢占式线程调度器和协作式线程调度器
    1. 抢占式线程调度：每个线程可能会有自己的优先级，但是优先级并不意味这高优先级的线程一定会被调度，而是由CPU随机的选择，所谓抢
       占式的线程调度，就是说一个线程在执行自己的任务时，虽然任务还没有执行完，但是CPU会迫使它暂停，让其他线程占用CPU的使用权。
    2. 协作式线程调度：每个线程可以有自己的优先级，但优先级并不意味着高优先级的线程一定会被最先调度，而是由CPU时机选择的，所谓协
       作式的线程调度，就是说一个线程在执行自己的任务时，不允许被中途中途打断，一定等当前线程将任务执行完毕后才会释放对cpu的占有，
       其他线程才可以抢占该cpu  
    总结:
    - Java在调度机制上采用的是抢占式的线程调度机制
    - Java线程在运行的过程中多个线程之间是协作式的

##并发理论基础：并发问题产生的三大根源

通俗来讲就是操作系统为了最大化的利用cpu，而有了多线程概念。操作系统的多线程对应到硬件上就是cpu的核心数，当操作系统的线程数大于cpu
核数时，cpu就会采用时间片轮转机制进行切换。

1. CPU切换线程执行导致原子性问题（应对这个出现了”AtomicXxxx“类）  
   原子性就指是把一个操作或者多个操作视为一个整体，在执行的过程不能被中断的特性叫原子性。  
   为了提高CPU的利用率，操作系统就有了进程和时间片的概念，同一个进程里的所有线程都共享一个内存空间，CPU每执行一个时间段就会切换到
   另一个进程处理指令，而这执行的时间长度是以时间片为单位的，通过这种方式让CPU切换不同的进程执行，让CPU更好的利用起来。后来操作系
   统又在CPU切换进程执行的基础上做了进一步的优化，以更细的维度“线程”来切换任务执行，更加提高了CPU的利用率。但正是这种cpu可以在不
   同线程中切换执行的方式会使得我们程序执行的过程中产生原子性问题
     + 高速缓存的产生
       在计算机系统中，CPU高速缓存是用于减少处理器访问内存所需的时间，其容量远小于内存，但其访问速度却是内存IO的几十上百倍。当处理
       器发出内存访问请求时，会先查看高速缓存内是否有请求数据。如果存在（命中），则不需要访问内存直接返回该数据；如果不存在（失效）
       ，则要先把内存中的相应数据载入缓存，再将其返回处理器。  
       *注意一点：不管是高速缓存还是cpu中的寄存器中的数据都是内存中数据的副本*

2. 缓存导致的可见性问题（volatile解决了线程间数据的可见性）  
   在有了高速缓存之后，CPU的执行操作数据的过程会是这样的，CPU首先会从内存把数据拷贝到CPU缓存区。
   然后CPU再对缓存里面的数据进行更新等操作，最后CPU把缓存区里面的数据更新到内存。
   缓存导致的可见性问题就是指我们在操作CPU缓存过程中，由于多个CPU缓存之间独立不可见的特性，导致共享变量的操作结果无法预期。
   在单核CPU时代，因为只有一个核心控制器，所以只会有一个CPU缓存区，这时各个线程访问的CPU缓存也都是同一个，在这种情况一个线程把共
   享变量更新到CPU缓存后另外一个线程是可以马上看见的，因为他们操作的是同一个缓存，所以他们操作后的结果不存在可见性问题。
   然而随着CPU的发展，CPU逐渐发展成了多核，CPU可以同时使用多个核心控制器执行线程任务，多核CPU每个核心控制器工作的时候都会有自己
   独立的cpu缓存，每个核心控制器在执行任务的时候都是操作自己的CPU缓存，多核cpu之间的缓存是相互独立不可见的。这种情况下多线程操作
   共享变量就因为缓存不可见而带来问题，多线程情况下线程并不一定是在同一个CPU上执行，他们如果同时操作一个共享变量，但因为在不同的
   CPU执行所以他们只能查看和更新自己CPU缓存里的变量，线程鸽子的执行结果对于别的线程来说是不可见的，所以在并发的情况下会因为这种
   缓存不可见的情况导致问题出现。

3. 指令优化导致的重排序问题（锁机制保证了程序执行的原子性）  
   进程和线程本质上是增加并行的任务数量来提升CPU的利用率，缓存是通过把IO时间减少来提升CPU的利用率，而指令顺序优化的初衷的初衷就
   是想通过调整CPU指令的执行顺序和异步化的操作来提升CPU执行指令任务的效率。
   指令顺序优化可能发生在编译、CPU指令执行、缓存优化几个阶，其优化原则就是只要能保证重排序后不影响单线程的运行结果，那么就允许指
   令重排序的发生。*其重排序的大体逻辑就是优先把CPU比较耗时的指令放到最先执行，然后在这些指令执行的空余时间来执行其他指令，就像我
   们做菜的时候会把熟的最慢的菜最先开始煮，然后在这个菜熟的时间段去做其它的菜，通过这种方式减少CPU的等待，更好的利用CPU的资源*。
   *指令重排，可能会发生在两个没有相互依赖关系之间的指令。*

##并发基础理论：缓存可见性问题、mesi协议、内存屏障
1. CPU缓存导致的可见性问题  
   通过如下方式保证各缓存间的数据一致性问题：  
   + 总线锁  
     如果想要每个CPU的缓存数据一致，那么最直接的办法就是同时只允许一个人修改内存的数据，当前面一个人操作结束之后，然后 通知其它缓
     存了该共享变量的缓存，通过这种串行化的方式加上通知机制来保证各个缓存之间的数据一致性，这也就是总线锁的思路。
   + 缓存锁  
     总线锁的机制很简单思路也很清晰，通过总线加锁和嗅探机制能保证缓存数据的一致性，但是总线锁有一个比较粗暴的点让人难以忍受，当
     某个CPU对总线进行加锁之后，其它CPU就无法与主存建立通讯，当一个CPU加锁之后所有后续对主存的操作都是阻塞的，所以这种机制就必定
     会降低性能。  
     所以就需要一种优化方案，这个就是缓存锁要解决的事情，缓存锁的实现机制是通过缓存一致性协议来解决的，缓存一致性协议也有多种,
     比如MESI、MOESI，这个是根据不同的操作系统和不同的硬件架构决定的，这里我们以常说的MESI协议来理解缓存锁是如何实现的。
   + 缓存一致性协议MESI  
   + 内存屏障  
     内存屏障可以简单的认为它就是用来禁用我们的CPU缓存优化的，使用了内存屏障后，写入数据时候会保证所有的指令都执行完毕，这样就能
     保证修改过的数据能即时的暴露给其他的CPU。在读取数据的时候保证所有的“无效队列”消息都已经被读取完毕，这样就保证了其他CPU修改
     的数据消息都能被当前CPU知道，然后根据Invalid消息判断自己的缓存是否处于无效状态，这样就读取数据的时候就能正确的读取到最新的
     数据。
   

  
##管程(Moniter) 并发编程的基本心法
1. 所谓管程：指的是管理共享变量以及对共享变量的操作过程，让它们支持并发。翻译为Java就是管理类的成员变量和成员方法，让这个类是线程
   安全的。  
   
##Java并发关键字final
成员变量分为类变量（被static修饰的变量）和实例变量
1. 类变量：必须要在静态初始化块中指定初始值或声明该类变量时指定初始值，而且只能在这两个地方之一进行指定
2. 实例变量：必须要在非静态初始化块，声明该实例变量或者在构造器中指定初始值，而且只能在这三个地方进行指定
3. 对于局部变量使用final，理解就更简单，局部变量的仅有一次赋值，final只保证这个引用类型变量所引用的地址不会发生改变
4. 当一个方法被final关键字修饰时，说明此方法不能被子类重写
5. 当一个类被final修饰时，表示该类是不能被子类继承的
6. final的重排序规则（被final关键字修饰的域被称为final域）
   + 在构造函数内对一个final域的写入，与随后把这个被构造对象的引用赋值给一个引用变量，这两个操作之间不能重排序。
   + 初次读一个包含final域的对象的引用，与随后初次读这个final域，这两个操作之间不能重排序。
   
   
   
   
   





#TCP/IP协议族
1. TCP：建立连接需要三次握手，而断开连接则需要四次握手，这是因为TCP的办关闭造成的。
2. TCP断开连接的流程：
   * 某个应用进程首先调用close，称该端执行“主动关闭”（active close）。该端的TCP于是发送一个FIN分节，表示数据发送完毕。
   * 接收到这个FIN的对端执行“被动关闭”，这个FIN由TCP确认
   * 一段时间后，接受到这个文件结束符的应用进程将调用close关闭他的套接字。这导致TCP也发送一个FIN。
   * 接收这个最终FIN的原发送端TCP确认这个FIN。既然每个方向都需要一个FIN和一个ACK，因此通常需要4个分节。
3. TCP、UDP
   * TCP协议是面向连接的、可靠的、基于字节流的传输层通信协议。
   * UDP面向事务的简单不可靠信息传送服务
   * TCP和UDP的区别
     1. TCP用于在传输层有必要实现可靠传输的情况。由于它是面向有链接并具备顺序控制、重发控制等机制的，所以他可以为应用提供可靠的传输
     2. UDP主要用于那些对高速传输和实时性有较高要求的通信或广播通信，多播或广播通信中也用的UDP
     
##Websocket
1. socket原理
   * Socket连接，至少需要一堆套接字，分别为clientSocket、serverSocket连接分为3个步骤：
     1. 服务器监听：服务器并不定位具体客户端的套接字，而是时刻处于监听状态；
     2. 客户端请求：客户端的套接字要描述它要连接的服务器的套接字，提供地址和端口号，然后向服务器套接字提出连接请求；
     3. 连接确认：当服务器套接字收到客户端套接字发来的请求后，就响应客户端套接字的请求，并建立一个新的线程，把服务器端的套接字的
        描述发给客户端。一旦客户端确认了此描述，就正式建立连接。而服务器套接字继续处于监听状态，继续接受其他客户端套接字的连接请求
   * Socket为长连接：通常情况下Socket连接就是TCP连接，因此Socket连接一旦建立，通讯双方开始护发数据内容，直到双方断开连接。在实际
     应用中，由于网络节点过多，在传输过程中，会被节点断开连接，因此要通过轮询高速网络，该节点处于活跃状态。  
     很多情况下，都需要服务器端想客户端主动推送数据，保持客户端与服务端的实现同步。  
     若双方都是Socket连接，可以由服务器直接向客户端发送数据。  
   * 网络模型（七层）中的部分协议
     1. HTTP 协议：超文本传输协议，对应于应用层，用于如何封装数据。
     2. TCP/UDP 协议：传输控制协议，对应于传输层，主要解决数据在网络中的传输。
     3. IP 协议：对应于网络层，同样解决数据在网络中的传输。
   
     
2. Websockt相关知识
   1. WebSocket是html5开始提供的一种在单个TCP连接上进行全双工通信的协议  
      WebSocket使得客户端和服务器之间的数据交换变得更简单，允许服务端主动向客户端推送数据。在 WebSocket API 中，浏览器和服务器
      只需要完成一次握手，两者之间就直接可以创建持久性的连接，并进行双向数据传输。
      
      
   * 一个Websocket客户端是一个Websocket终端，它初始化了一个到对方的连接。一个Websocket服务器也是一个Websocket终端，它被发布出  
     去并且等待来自对方的连接。在客户端和服务器端都有回调监听方法 -- onOpen、onMessage、onError、onClose。
   * 如何玩Websocket？
     基本上我们还是会使用JavaScript API编写Websocket客户端，在服务器端可以使用JSR356规范定义的通用模式和技术处理Websocket的通讯  
     前端实现websocket
     ```html
        <html>
            <head>
                <meta http-equiv="content-type" content="text/html; charset=ISO-8859-1">
            </head>
         
            <body>
                <meta charset="utf-8">
                <title>HelloWorld Web sockets</title>
                <script language="javascript" type="text/javascript">
                    var wsUri = getRootUri() + "/websocket-hello/hello";
         
                    function getRootUri() {
                        return "ws://" + (document.location.hostname == "" ? "localhost" : document.location.hostname) + ":" +
                                (document.location.port == "" ? "8080" : document.location.port);
                    }
         
                    function init() {
                        output = document.getElementById("output");
                    }
         
                    function send_message() {
         
                        websocket = new WebSocket(wsUri);
                        websocket.onopen = function(evt) {
                            onOpen(evt)
                        };
                        websocket.onmessage = function(evt) {
                            onMessage(evt)
                        };
                        websocket.onerror = function(evt) {
                            onError(evt)
                        };
         
                    }
         
                    function onOpen(evt) {
                        writeToScreen("Connected to Endpoint!");
                        doSend(textID.value);
         
                    }
         
                    function onMessage(evt) {
                        writeToScreen("Message Received: " + evt.data);
                    }
         
                    function onError(evt) {
                        writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
                    }
         
                    function doSend(message) {
                        writeToScreen("Message Sent: " + message);
                        websocket.send(message);
                    }
         
                    function writeToScreen(message) {
                        var pre = document.createElement("p");
                        pre.style.wordWrap = "break-word";
                        pre.innerHTML = message;
                          
                        output.appendChild(pre);
                    }
         
                    window.addEventListener("load", init, false);
         
                </script>
         
                <h1 style="text-align: center;">Hello World WebSocket Client</h2>
         
                <br>
         
                <div style="text-align: center;">
                    <form action="">
                        <input onclick="send_message()" value="Send" type="button">
                        <input id="textID" name="message" value="Hello WebSocket!" type="text"><br>
                    </form>
                </div>
                <div id="output"></div>
        </body>
        </html>
     ```
     如你所见，要想使用WebSocket协议与服务器通信, 需要一个WebSocket对象。它会自动连接服务器.
     ```js
     websocket = new WebSocket(wsUri);
     ```
     连接上会触发open事件：
     ```js
      websocket.onopen = function(evt) {
             onOpen(evt)
      };
     ```
     一旦连接成功，则向服务器发送一个简单的hello消息。
     ```js
     websocket.send(message);
     ```
   服务器端代码  
   有两种创建服务器端代码的方法：
   1. 注解方式Annotation-driven：通过POJO加上注解，开发者就可以处理Websocket声明周期时间
   2. 实现接口方式Interface-driven：开发者可以实现Endpoint接口和生命周期的各个方法  
      建议开发时采用注解方式， 这样可以使用POJO就可以实现WebSocket Endpoint. 而且不限定处理事件的方法名。 代码也更简单。  
      本例就采用注解的方式， 接收WebSocket请求的类是一个POJO, 通过@ServerEndpoint标注. 这个注解告诉容器此类应该被当作一个
      WebSocket的Endpoint。value值就是WebSocket endpoint的path.
      ```java
        package com.sample.websocket;
        import javax.websocket.*;
        import javax.websocket.server.ServerEndpoint;
      
        @ServerEndpoint("/hello")
        public class HelloWorldEndpoint {
            @OnMessage
            public String hello(String message) {
                System.out.println("Received : "+ message);
                return message;
            }
            @OnOpen
            public void myOnOpen(Session session) {
                System.out.println("WebSocket opened: " + session.getId());
            }
            @OnClose
            public void myOnClose(CloseReason reason) {
                System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());
            }
        }
      ```
      @OnOpen标注的方法在Websocket连接开始时被调用， Session作为参数。另外一个@OnClose标注的方法在连接关闭时被调用。
      
   
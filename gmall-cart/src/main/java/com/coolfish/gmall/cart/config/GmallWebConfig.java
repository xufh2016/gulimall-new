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

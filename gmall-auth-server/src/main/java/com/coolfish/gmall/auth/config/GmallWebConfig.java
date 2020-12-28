package com.coolfish.gmall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * @author 28251
 */
@Configuration
public class GmallWebConfig implements WebMvcConfigurer {
    /**
     * 视图映射中添加好页面跳转
     * @param registry
     */

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //addViewController添加请求路径，setViewName添加视图名
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}

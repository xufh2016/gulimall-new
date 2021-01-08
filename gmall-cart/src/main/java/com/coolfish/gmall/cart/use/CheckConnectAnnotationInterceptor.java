package com.coolfish.gmall.cart.use;

import com.coolfish.gmall.cart.annotation.CheckConnected;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 28251
 */
@Component
public class CheckConnectAnnotationInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        System.out.println("---------------------------------preHandle---------------------------");

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        CheckConnected methodAnnotation = handlerMethod.getMethodAnnotation(CheckConnected.class);

        return true;

    }
}



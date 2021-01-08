package com.coolfish.gmall.cart.use;

import com.coolfish.gmall.cart.annotation.CheckConnected;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author 28251
 */
@Component
public class AnnotationContext implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {


        System.out.println("-------------------------------------");
        Class<CheckConnected> checkConnectedClass = CheckConnected.class;

        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(checkConnectedClass);
        beansWithAnnotation.forEach((s, o) -> {
            System.out.println("-----------------s---------------" + s);
            System.out.println("-----------------o---------------" + o);
        });
        Set<Map.Entry<String, Object>> entries = beansWithAnnotation.entrySet();

        entries.stream().forEach(stringObjectEntry -> {
            System.out.println(stringObjectEntry.getKey() + "-------------------key------------------");
            Class<?> aClass = stringObjectEntry.getValue().getClass();
            System.out.println(aClass.getName());
            CheckConnected checkConnected = AnnotationUtils.findAnnotation(aClass, checkConnectedClass);

            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
//                CheckConnected checkConnectedMethod = AnnotationUtils.findAnnotation(method, checkConnectedClass);
                CheckConnected annotation = method.getAnnotation(checkConnectedClass);
                if (null != annotation && annotation.check()) {
                    System.out.println("---------------------------執行檢查重連邏輯----------------------------");
                }
            }
        });
//        contextRefreshedEvent.get
    }
}

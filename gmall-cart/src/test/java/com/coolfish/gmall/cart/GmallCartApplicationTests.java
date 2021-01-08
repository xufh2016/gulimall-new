package com.coolfish.gmall.cart;

import com.coolfish.gmall.cart.annotation.CheckConnected;
import com.coolfish.gmall.cart.temp.UseAnnotation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;

@SpringBootTest
class GmallCartApplicationTests {

    @Test
    void contextLoads() {
    }
//    @Autowired
//    UseAnnotation useAnnotation;

    @Test
    public void testAnno() throws NoSuchMethodException {
        /*Class<UseAnnotation> useAnnotationClass = UseAnnotation.class;

        Method[] methods = useAnnotationClass.getMethods();
        for (Method method : methods) {
            CheckConnected annotation = method.getAnnotation(CheckConnected.class);
            if (null != annotation && annotation.check()) {

                System.out.println("----------------------------------------");
//                method.invoke();
            }

        }*/
        /*Method method = useAnnotationClass.getMethod("com.coolfish.gmall.cart.temp.UseAnnotation.mqttConn",null);
        CheckConnected annotation = method.getAnnotation(CheckConnected.class);
        if (annotation.check()) {
            System.out.println("--------------------------执行mqtt是否需要重连并执行其他的逻辑------------------------");
        }*/
        UseAnnotation useAnnotation = new UseAnnotation();
        useAnnotation.mqttConn();
    }

}

package com.coolfish.gmall.cart.temp;

import com.coolfish.gmall.cart.annotation.CheckConnected;
import org.springframework.stereotype.Component;

/**
 * @author 28251
 */
//@Component
public class UseAnnotation {
    @CheckConnected(check = true)
    public void mqttConn(){
        System.out.println("    ---------------------------hahahahaha-----------------------------    ");
    }
}

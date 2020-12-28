package com.coolfish.gmall.cart.controller;


import com.coolfish.gmall.cart.GmallCartApplication;
import com.coolfish.gmall.cart.interceptor.CartInterceptor;
import com.coolfish.gmall.cart.service.CartService;
import com.coolfish.gmall.cart.to.UserInfoTo;
import com.coolfish.gmall.cart.vo.CartItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 28251
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 浏览器有一个cookie：user-key，标识用户身份，一个月后过期
     * 去购物车页面
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage() {
        //快速得到用户信息:id  user-key
        //ThreadLocal 同一个线程共享数据
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        return "cartList";
    }

    /**
     * 添加商品到购物车
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, Model model){
        CartItemVo itemVo = cartService.addToCart(skuId,num);
        model.addAttribute("item",itemVo);
        return "success";
    }
}

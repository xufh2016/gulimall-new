package com.coolfish.gmall.cart.controller;


import com.coolfish.gmall.cart.interceptor.CartInterceptor;
import com.coolfish.gmall.cart.service.CartService;
import com.coolfish.gmall.cart.to.UserInfoTo;
import com.coolfish.gmall.cart.vo.CartItemVo;
import com.coolfish.gmall.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author 28251
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 使用拦截器来处理目标方法执行之前的一些逻辑
     * 浏览器有一个cookie：user-key，标识用户身份，一个月后过期
     * 去购物车页面
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartListPage(Model model) {
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }

    /**
     * 添加商品到购物车
     *
     * @return
     */
    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num, RedirectAttributes model) {
        CartItemVo itemVo = cartService.addToCart(skuId, num);
        model.addAttribute("item", itemVo);
        model.addAttribute("skuId", skuId);
        return "redirect:http://cart.gmall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String addToCartSuccessPage(@RequestParam("skuId") String skuId, Model model) {
        CartItemVo item = cartService.getCartItem(skuId);
        model.addAttribute("item", item);
        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId,@RequestParam("check") Integer check){
        cartService.checkItem(skuId,check);

        return "redirect:http://cart.gmall.com/cart.html";
    }
    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num){
        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.gmall.com/cart.html";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId){
        cartService.deleteItem(skuId);

        return "redirect:http://cart.gmall.com/cart.html";
    }
}

package com.coolfish.gmall.cart.service;

import com.coolfish.gmall.cart.vo.CartItemVo;

/**
 * @author 28251
 */
public interface CartService {
    CartItemVo addToCart(Long skuId, Integer num);
}

package com.coolfish.gmall.cart.service;

import com.coolfish.gmall.cart.vo.CartItemVo;
import com.coolfish.gmall.cart.vo.CartVo;

/**
 * @author 28251
 */
public interface CartService {


    /**
     * 将商品添加到购物车
     * @param skuId
     * @param num
     * @return
     */
    CartItemVo addToCart(Long skuId, Integer num);

    /**
     * 获取购物车中某个购物项
     * @param skuId
     * @return
     */
    CartItemVo getCartItem(String skuId);

    /**
     * 获取购物车
     * @return
     */
    CartVo getCart();

    /**
     * 清空购物车
     * @param cartKey
     */
    void clearCart(String cartKey);

    /**
     * 勾选 购物项
     * @param skuId
     * @param check
     */
    void checkItem(Long skuId, Integer check);

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     */
    void changeItemCount(Long skuId, Integer num);

    /**
     * 删除购物车中的项
     * @param skuId
     */
    void deleteItem(Long skuId);
}

package com.coolfish.gmall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.coolfish.common.utils.R;
import com.coolfish.gmall.cart.feign.ProductFeignService;
import com.coolfish.gmall.cart.interceptor.CartInterceptor;
import com.coolfish.gmall.cart.service.CartService;
import com.coolfish.gmall.cart.to.UserInfoTo;
import com.coolfish.gmall.cart.vo.CartItemVo;
import com.coolfish.gmall.cart.vo.CartVo;
import com.coolfish.gmall.cart.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @author 28251
 */

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;


    @Autowired
    ExecutorService executorService;

    private final String CART_PREFIX = "gmall:cart:";

    @Override
    public CartItemVo addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s1 = (String) cartOps.get(skuId.toString());

        if (StringUtils.isEmpty(s1)) {
            CartItemVo cartItemVo = new CartItemVo();
            //购物车无此商品
            //2、添加新商品到购物车
            CompletableFuture<Void> getSkuInfoTask = CompletableFuture.runAsync(() -> {
                R info = productFeignService.info(skuId);
                SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setImage(skuInfo.getSkuDefaultImg());
                cartItemVo.setTitle(skuInfo.getSkuTitle());
                cartItemVo.setSkuId(skuId);
                cartItemVo.setPrice(skuInfo.getPrice());
            }, executorService);
            CompletableFuture<Void> getSkuSaleAttrValues = CompletableFuture.runAsync(() -> {
                List<String> values = productFeignService.getSkuSaleAttrValues(skuId);
                cartItemVo.setSkuAttrValues(values);
            }, executorService);
            try {
                CompletableFuture.allOf(getSkuInfoTask, getSkuSaleAttrValues).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            String s = JSON.toJSONString(cartItemVo.toString());
            cartOps.put(skuId.toString(), s);
            return cartItemVo;
        } else {
            //购物车有此商品，修改数量即可
            CartItemVo itemVo = JSON.parseObject(s1, CartItemVo.class);
            itemVo.setCount(itemVo.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(itemVo));
            return itemVo;
        }

    }

    @Override
    public CartItemVo getCartItem(String skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String o = (String) cartOps.get(skuId.toString());
        CartItemVo itemVo = JSON.parseObject(o, CartItemVo.class);

        return itemVo;
    }

    @Override
    public CartVo getCart() {
        CartVo cartVo = new CartVo();
        //区分是否登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        if (userInfoTo.getUserId() != null) {
            //1 登录状态
            String cartKey = CART_PREFIX + userInfoTo.getUserId();
            //2如果临时购物车的数据没有合并
            List<CartItemVo> tempCartItems = getCartItems(CART_PREFIX + userInfoTo.getUserKey());
            if (tempCartItems != null) {
                //临时购物车有数据，需要合并
                for (CartItemVo tempCartItem : tempCartItems) {
                    addToCart(tempCartItem.getSkuId(), tempCartItem.getCount());
                }
                //清除临时购物车的数据
                clearCart(CART_PREFIX + userInfoTo.getUserKey());
            }
            //3获取登录后的购物车的数据[包含合并过来的临时购物车的数据，和登录后的购物车的数据]
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        } else {
            //没登录
            String cartKey = CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        }
        return cartVo;
    }


    /**
     * 获取购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        //1、购物车是否是已登录
        if (userInfoTo.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoTo.getUserKey();
        }
        //2、添加商品到购物车
//        redisTemplate.opsForHash().get(cartKey,"")
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItemVo> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        if (values != null && values.size() > 0) {
            List<CartItemVo> collect = values.stream().map(obj -> {
                String str = (String) obj;
                CartItemVo itemVo = JSON.parseObject(str, CartItemVo.class);

                return itemVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        CartItemVo cartItem = getCartItem(skuId + "");
        cartItem.setCheck(check == 1);
        String s = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), s);
    }

    @Override
    public void changeItemCount(Long skuId, Integer num) {
        CartItemVo cartItem = getCartItem(skuId + "");
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId + "", JSON.toJSONString(cartItem));

    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId + "");
    }

}

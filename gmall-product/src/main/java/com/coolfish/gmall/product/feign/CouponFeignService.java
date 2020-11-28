package com.coolfish.gmall.product.feign;


import com.coolfish.common.to.SkuReductionTo;
import com.coolfish.common.to.SpuBoundTo;
import com.coolfish.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gmall-coupon")
public interface CouponFeignService {

    /**
     * 使用openfeign做远程调用时，方法签名可以不一致，但请求方式、返回值、请求路径必须一致，方法参数需要json模型一致
     * @param spuBoundTo
     * @return
     */
    @PostMapping("coupon/spubounds/save")
    R saveSpuBounds(SpuBoundTo spuBoundTo);

    @PostMapping("coupon/skufullreduction/saveinfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}

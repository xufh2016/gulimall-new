package com.coolfish.gmall.member.feign;

import com.coolfish.common.utils.R;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gmall-coupon")

public interface CouponFeignService {
    @RequestMapping("coupon/coupon/member-coupon")
    public R memberCoupons();
}

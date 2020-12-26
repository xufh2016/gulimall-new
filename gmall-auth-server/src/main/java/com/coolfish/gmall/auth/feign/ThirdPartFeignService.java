package com.coolfish.gmall.auth.feign;


import com.coolfish.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 28251
 */
@FeignClient("gmall-third-party")
public interface ThirdPartFeignService {
    /**
     * aaa
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sms/sendcode")
    R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}

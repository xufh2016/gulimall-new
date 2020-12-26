package com.coolfish.gmall.auth.controller;

import com.coolfish.common.utils.R;
import com.coolfish.gmall.auth.feign.ThirdPartFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.concurrent.TimeUnit;

/**
 * @author 28251
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    /**
     * 发送一个请求直接跳转到一个页面。
     * springmvc的viewcontroller；将请求和页面映射过来
     *
     * @return 1、接口防刷
     * 2、验证码的再次校验,redis
     */

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/sms/code")
    public R sendCode(@RequestParam("phone") String phone) {
        //todo:接口防刷

        String redisCode = redisTemplate.opsForValue().get(phone);
        if (!StringUtils.isEmpty(redisCode)) {
            String timemiles = redisCode.split("_")[1];
            if ((System.currentTimeMillis() - Long.parseLong(timemiles)) < 1000 * 60) {
                return R.error("发送频率过高");
            }
        }
        String code = "667788" + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(phone, code, 10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code);

        return R.ok();
    }
//    @GetMapping("login.html")
//    public String loginPage() {
//
//
//        return "login";
//    }
//
//    @GetMapping("reg.html")
//    public String regPage() {
//
//
//        return "reg";
//    }
}

package com.coolfish.gmall.auth.controller;

import com.coolfish.common.constant.AuthServerConstant;
import com.coolfish.common.exception.BizCodeEnum;
import com.coolfish.common.utils.R;
import com.coolfish.gmall.auth.feign.ThirdPartFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;
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
     * @RequestParam 获取的url路径中 ? 如：xxx/?id=aa&index=1
     * @return 1、接口防刷
     * 2、验证码的再次校验,redis
     */

    @Autowired
    StringRedisTemplate redisTemplate;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //todo 1、接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            String timemiles = redisCode.split("_")[1];
            if ((System.currentTimeMillis() - Long.parseLong(timemiles)) < 1000 * 60) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }
        //2.验证码再次校验,redis。存key-phone，value-code
        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        //保存验证码到redis中,保存时效10分钟。
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);

        thirdPartFeignService.sendCode(phone, code);

        return R.ok();
    }
}

package com.coolfish.gmall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.coolfish.common.constant.AuthServerConstant;
import com.coolfish.common.exception.BizCodeEnum;
import com.coolfish.common.utils.R;
import com.coolfish.gmall.auth.feign.MemberFeignService;
import com.coolfish.gmall.auth.feign.ThirdPartFeignService;
import com.coolfish.gmall.auth.vo.UserLoginVo;
import com.coolfish.gmall.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    MemberFeignService memberFeignService;

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
        String code = UUID.randomUUID().toString().substring(0, 5);
        String s1 = code + "_" + System.currentTimeMillis();
        //保存验证码到redis中,保存时效10分钟。
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, s1, 10, TimeUnit.MINUTES);

        thirdPartFeignService.sendCode(phone, code);

        return R.ok();
    }

    /**
     * todo 分布式下的session问题
     * forward: 转发，转发是指原封不动的过去，及请求方式等也不发生变化，而到reg页面
     * redirect: 重定向，可以防止表单重复提交
     * <p>
     * RedirectAttributes redirectAttributes 模拟重定向携带数据
     *
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            //如果有错误转发到注册页面，
            //用户注册会发给  /regist[post请求]==>需要转发到/reg.html页面，而该页面是在GmallWebConfig中做的路径映射，该方式下在默认情况使用get方式才能访问，
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
//            model.addAttribute("errors", errors);
            return "redirect:/reg.html";
        }
        //如果前置校验没有问题，就执行真正的注册功能，需要调用远程服务进行注册
        //1、校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)) {
            if (code.equals(s.split("_")[0])) {
                //验证码成功,调用远程服务注册，并删除验证码(令牌机制)
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                R r = memberFeignService.register(vo);
                if (r.getCode() == 0) {
                    //成功
                    return "redirect:/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData(new TypeReference<String>() {
                    }));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gmall.com/reg.html";
                }
            } else {
                HashMap<String, String> map = new HashMap<>();
                map.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", map);
                return "redirect:http://auth.gmall.com/reg.html";
            }
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", map);
            return "redirect:http://auth.gmall.com/reg.html";
        }
        //注册成功回到首页，可以回到登录页,在本服务中可以不写 全地址（http://auth.gmall.com/login.html）
    }

    /**
     * 提交的为kv数据是不需要再参数前加注解，提交的为json数据时则需要加@RequestBody注解
     *
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes) {
        //真正的登陆需要发给远程进行
        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            //成功
            return "redirect:http://gmall.com";
        } else {
            Map<String, String> errors = new HashMap();
            errors.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gmall.com/login.html";
        }
    }
}


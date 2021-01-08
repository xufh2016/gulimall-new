package com.coolfish.gmall.cart.interceptor;

import com.coolfish.common.constant.AuthServerConstant;
import com.coolfish.common.constant.CartConstant;
import com.coolfish.common.vo.MemberResponseVo;
import com.coolfish.gmall.cart.to.UserInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 在执行目标方法之前，判断用户的登陆状态。并封装传递给目标请求
 *
 * @author 28251
 * 标注组件@Component注解并且springmvc拦截器必须实现HandlerInterceptor接口
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 拦截器用于拦截哪个请求是需要配置的
     * 这个方法用于处理目标方法执行之前判断用户登录状态，并封装传递给controller目标请求
     *
     * @param request
     * @param response
     * @param handler
     * @return false代表不放行目标方法，true代表放行目标方法
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo info = new UserInfoTo();

        HttpSession session = request.getSession();
        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (member != null) {
            //用户没登录
            info.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    info.setUserKey(cookie.getValue());
                    info.setTempUser(true);
                }
            }
        }
        //如果没有临时用户一定分配一个临时用户
        if (StringUtils.isEmpty(info.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            info.setUserKey(uuid);
        }
        //目标方法执行之前
        threadLocal.set(info);
        return true;
    }

    /**
     * 业务执行之后需要操作的,分配临时用户，让浏览器保存
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        if (!userInfoTo.getTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gmall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}

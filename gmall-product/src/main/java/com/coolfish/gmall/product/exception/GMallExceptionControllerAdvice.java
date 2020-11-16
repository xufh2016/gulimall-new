package com.coolfish.gmall.product.exception;


import com.coolfish.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 集中处理所有异常
 */
@Slf4j
//@ControllerAdvice(basePackages = "com.coolfish.gmall.product.controller")
//
@RestControllerAdvice(basePackages = "com.coolfish.gmall.product.controller")
public class GMallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> map = new HashMap<>();
        bindingResult.getFieldErrors().forEach(item -> {
            String field = item.getField();
            String defaultMessage = item.getDefaultMessage();
            map.put(field, defaultMessage);
        });

        log.error("数据校验出现问题{}，异常类型：{}", e.getMessage(), e.getClass());
        return R.error().put("data", map);
    }

    // 大范围的异常处理，在此例中是指上面的异常处理方法不能精确处理时，使用此处的异常处理方法
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e) {
        return R.error();
    }
}
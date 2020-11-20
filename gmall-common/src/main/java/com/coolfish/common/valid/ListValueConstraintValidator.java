package com.coolfish.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 *  ConstraintValidator<ListValue, Integer>用到了两个泛型，第一个泛型是注解，第二个泛型是将要被注解的类型
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer> {
    private Set<Integer> set = new HashSet<>();

    /**
     * 初始化方法，会获取被ListValue注解的字段的所有给定的值
     * @param constraintAnnotation
     */
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val : vals) {
            set.add(val);
        }
    }

    /**
     * 该方法是校验是否成功
     * @param integer 需要校验的值
     * @param constraintValidatorContext 上下文环境信息
     * @return
     */
    @Override
    public boolean isValid(Integer integer, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(integer);
    }
}

package com.franklin.myspring.annotations;

import java.lang.annotation.*;

/**
 * @author 叶俊晖
 * @date 2019/9/5 0005 10:20
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyRequestMapping {
    String value() default "";
}

package com.franklin.myspring.annotations;

import java.lang.annotation.*;

/**
 * @author 叶俊晖
 * @date 2019/9/5 0005 10:18
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MyAutowired {
    String value() default "";
}

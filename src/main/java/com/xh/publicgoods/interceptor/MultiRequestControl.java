package com.xh.publicgoods.interceptor;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultiRequestControl {
    String second() default "0";//描述

    String message() default "请求过于频繁,请稍后重试";
}

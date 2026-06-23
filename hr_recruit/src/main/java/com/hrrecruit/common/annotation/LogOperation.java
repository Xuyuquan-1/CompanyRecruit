package com.hrrecruit.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 - 标记需要记录日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 操作模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String type() default "";

    /**
     * 操作描述
     */
    String desc() default "";

    /**
     * 是否保存请求参数
     */
    boolean saveParam() default true;

    /**
     * 是否保存响应结果
     */
    boolean saveResult() default false;
}

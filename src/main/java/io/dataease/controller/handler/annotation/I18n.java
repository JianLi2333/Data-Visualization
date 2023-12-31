package io.dataease.controller.handler.annotation;


import io.dataease.commons.constants.I18nConstants;

import java.lang.annotation.*;



@Documented //
@Inherited //表示这个注解可以被子类继承
@Target(ElementType.METHOD) //表示这个注解可以用在方法上
@Retention(RetentionPolicy.RUNTIME) //表示这个注解在运行时起作用
public @interface I18n {

    //这个注解的作用是，如果没有传值，就用默认值
    String value() default I18nConstants.LANG_COOKIE_NAME;
}

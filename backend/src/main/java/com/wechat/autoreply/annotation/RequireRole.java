package com.wechat.autoreply.annotation;

import java.lang.annotation.*;

/**
 * 需要特定角色权限的注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    
    /**
     * 需要的角色
     */
    String[] value();
}

package com.ddl.manager.infrastructure.annotation;

import java.lang.annotation.*;

/**
 * 权限控制注解
 * 用于方法级别的权限控制
 * @author developer
 * @since 2025-12-14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /** 需要的权限标识 */
    String value() default "";

    /** 需要的角色 */
    String[] roles() default {};

    /** 逻辑关系：AND-需要同时满足，OR-满足任意一个 */
    Logical logical() default Logical.AND;

    enum Logical {
        AND, OR
    }
}
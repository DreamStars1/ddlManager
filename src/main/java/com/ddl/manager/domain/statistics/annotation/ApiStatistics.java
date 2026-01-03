// main/java/com/ddl/manager/domain/statistics/annotation/ApiStatistics.java
package com.ddl.manager.domain.statistics.annotation;

import java.lang.annotation.*;

/**
 * 接口统计注解，用于标记需要进行调用统计的接口方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiStatistics {
    /**
     * 接口名称
     */
    String name();

    /**
     * 接口路径（默认空，可通过请求自动获取）
     */
    String path() default "";
}
// main/java/com/ddl/manager/domain/statistics/aspect/ApiStatisticsAspect.java
package com.ddl.manager.domain.statistics.aspect;

import com.ddl.manager.domain.statistics.annotation.ApiStatistics;
import com.ddl.manager.domain.statistics.service.StatisticsService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Aspect
@Component
public class ApiStatisticsAspect {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 环绕通知，统计接口调用次数
     */
    @Around("@annotation(com.ddl.manager.domain.statistics.annotation.ApiStatistics)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ApiStatistics apiStatistics = method.getAnnotation(ApiStatistics.class);

        // 获取接口名称
        String apiName = apiStatistics.name();

        // 获取接口路径（注解指定优先，否则从请求中获取）
        String apiPath = apiStatistics.path();
        if (apiPath.isEmpty()) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                apiPath = request.getRequestURI();
            }
        }

        try {
            // 执行原方法
            return joinPoint.proceed();
        } finally {
            // 无论方法是否执行成功，都统计调用次数
            statisticsService.incrementCallCount(apiName, apiPath);
        }
    }
}
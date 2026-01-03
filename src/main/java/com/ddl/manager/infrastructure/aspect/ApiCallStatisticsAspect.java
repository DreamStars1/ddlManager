//// src/main/java/com/ddl/manager/infrastructure/aspect/ApiCallStatisticsAspect.java
//package com.ddl.manager.infrastructure.aspect;
//
//import com.ddl.manager.domain.statistics.dto.ApiStatistics;
//import com.ddl.manager.domain.statistics.service.StatisticsService;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Pointcut;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//
//import java.lang.reflect.Method;
//
//@Aspect
//@Component
//@Slf4j
//public class ApiCallStatisticsAspect {
//
//    @Autowired
//    private StatisticsService statisticsService;
//
//    // 定义切入点：任务控制器和测试控制器中的接口
//
//    @Pointcut("(execution(* com.ddl.manager.domain.auth.controller.AuthController.*(..)) || " +
//            "execution(* com.ddl.manager.domain.task.controller.TaskController.*(..))) && " +
//            "( @annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
//            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
//            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
//            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
//            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) )")
//    public void apiPointcut() {}
//
//    @AfterReturning("apiPointcut()")
//    public void countApiCalls(JoinPoint joinPoint) {
//        try {
//            // 获取类上的@RequestMapping路径
//            Class<?> targetClass = joinPoint.getTarget().getClass();
//            RequestMapping classRequestMapping = targetClass.getAnnotation(RequestMapping.class);
//            String classPath = classRequestMapping != null && classRequestMapping.value().length > 0
//                    ? classRequestMapping.value()[0] : "";
//
//            // 获取方法上的@RequestMapping路径
//            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//            Method method = signature.getMethod();
//            RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
//            String methodPath = "";
//
//            if (methodRequestMapping != null && methodRequestMapping.value().length > 0) {
//                methodPath = methodRequestMapping.value()[0];
//            } else if (method.isAnnotationPresent(GetMapping.class)) {
//                GetMapping getMapping = method.getAnnotation(GetMapping.class);
//                methodPath = getMapping.value().length > 0 ? getMapping.value()[0] : "";
//            } else if (method.isAnnotationPresent(PostMapping.class)) {
//                PostMapping postMapping = method.getAnnotation(PostMapping.class);
//                methodPath = postMapping.value().length > 0 ? postMapping.value()[0] : "";
//            }
//
//            // 组合完整路径
//            String fullPath = classPath + methodPath;
//            String apiName = method.getName();
//
//            // 更新统计信息
//            statisticsService.incrementCallCount(apiName, fullPath);
//
//        } catch (Exception e) {
//            log.error("API调用统计失败", e);
//        }
//    }
//}
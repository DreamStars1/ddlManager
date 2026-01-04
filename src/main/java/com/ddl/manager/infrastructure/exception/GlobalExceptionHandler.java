package com.ddl.manager.infrastructure.exception;

import com.ddl.manager.shared.dto.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * @param e 业务异常
     * @param request HTTP请求
     * @param redirectAttributes 重定向属性
     * @return 响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException e,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());

        if (isApiRequest(request)) {
            return ResponseEntity.ok(AjaxResult.error(e.getCode(), e.getMessage()));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return redirectToReferer(request);
        }
    }

    /**
     * 处理系统异常
     * @param e 系统异常
     * @param request HTTP请求
     * @param redirectAttributes 重定向属性
     * @return 响应结果
     */
    @ExceptionHandler(SystemErrorException.class)
    public Object handleSystemErrorException(SystemErrorException e,
                                             HttpServletRequest request,
                                             RedirectAttributes redirectAttributes) {
        log.error("系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);

        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AjaxResult.error(e.getCode(), "系统繁忙，请稍后重试"));
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "系统繁忙，请稍后重试");
            return "error/500";
        }
    }

    /**
     * 处理未知异常
     * @param e 异常
     * @param request HTTP请求
     * @return 响应结果
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error("未知异常: ", e);

        if (isApiRequest(request)) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AjaxResult.error(80000, "系统繁忙，请稍后重试"));
        } else {
            return "error/500";
        }
    }

    /**
     * 处理权限拒绝异常
     * @return 403页面
     */
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied() {
        return "error/403";
    }

    /**
     * 判断是否为API请求
     */
    private boolean isApiRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                || request.getRequestURI().startsWith("/api/");
    }

    /**
     * 重定向到来源页
     */
    private String redirectToReferer(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}

package com.ddl.manager.domain.auth.controller;

import com.ddl.manager.shared.dto.AjaxResult;
import com.ddl.manager.shared.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Session测试控制器
 * @author developer
 * @since 2025-12-14
 */
@Slf4j
@RestController
@RequestMapping("/api/session/test")
public class SessionTestController {

    /**
     * 测试Session存储和读取
     */
    @GetMapping("/store")
    public AjaxResult testSessionStore(HttpSession session) {
        try {
            // 存储测试数据到Session
            String username = SecurityUtils.getCurrentUsername();
            session.setAttribute("testData", "Hello, " + username);
            session.setAttribute("loginTime", System.currentTimeMillis());

            // 从Session读取数据
            String testData = (String) session.getAttribute("testData");
            Long loginTime = (Long) session.getAttribute("loginTime");

            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", session.getId());
            result.put("testData", testData);
            result.put("loginTime", loginTime);
            result.put("username", username);

            return AjaxResult.ok("Session测试成功", result);
        } catch (Exception e) {
            log.error("Session测试失败", e);
            return AjaxResult.error("Session测试失败");
        }
    }

    /**
     * 验证Session持久化
     */
    @GetMapping("/verify")
    public AjaxResult verifySessionPersistence(HttpSession session) {
        try {
            String testData = (String) session.getAttribute("testData");
            Long loginTime = (Long) session.getAttribute("loginTime");

            Map<String, Object> result = new HashMap<>();
            result.put("sessionId", session.getId());
            result.put("testData", testData);
            result.put("loginTime", loginTime);
            result.put("exists", testData != null);

            return AjaxResult.ok("Session验证完成", result);
        } catch (Exception e) {
            log.error("Session验证失败", e);
            return AjaxResult.error("Session验证失败");
        }
    }
}
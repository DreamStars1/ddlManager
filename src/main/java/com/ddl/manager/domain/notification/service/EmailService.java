package com.ddl.manager.domain.notification.service;

import com.ddl.manager.domain.notification.dto.EmailMessage;

/**
 * 邮件服务接口
 * @author zhenghaipei
 * @since 2025-12-13
 */
public interface EmailService {

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件正文
     * @throws Exception 发送失败时抛出异常
     */
    void sendSimpleEmail(String to, String subject, String text) throws Exception;

    /**
     * 发送HTML格式邮件
     *
     * @param to      收件人邮箱
     * @param subject 邮件主题
     * @param html    邮件HTML内容
     * @throws Exception 发送失败时抛出异常
     */
    void sendHtmlEmail(String to, String subject, String html) throws Exception;

    /**
     * 发送邮件（使用EmailMessage对象）
     *
     * @param message 邮件消息对象
     * @throws Exception 发送失败时抛出异常
     */
    void sendEmail(EmailMessage message) throws Exception;

    /**
     * 批量发送邮件
     *
     * @param message 邮件消息对象（支持多个收件人）
     * @throws Exception 发送失败时抛出异常
     */
    void sendBatchEmail(EmailMessage message) throws Exception;
}


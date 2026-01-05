package com.ddl.manager.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 邮件消息DTO
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailMessage {

    /** 收件人邮箱地址（单个） */
    private String to;

    /** 收件人邮箱地址（多个） */
    private List<String> toList;

    /** 邮件主题 */
    private String subject;

    /** 邮件正文（纯文本） */
    private String text;

    /** 邮件正文（HTML格式） */
    private String html;

    /** 抄送人 */
    private List<String> cc;

    /** 密送人 */
    private List<String> bcc;

    /**
     * 创建简单文本邮件
     */
    public static EmailMessage simple(String to, String subject, String text) {
        return EmailMessage.builder()
                .to(to)
                .subject(subject)
                .text(text)
                .build();
    }

    /**
     * 创建HTML格式邮件
     */
    public static EmailMessage html(String to, String subject, String html) {
        return EmailMessage.builder()
                .to(to)
                .subject(subject)
                .html(html)
                .build();
    }
}


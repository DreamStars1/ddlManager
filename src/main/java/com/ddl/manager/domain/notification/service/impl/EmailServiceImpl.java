package com.ddl.manager.domain.notification.service.impl;

import com.ddl.manager.domain.notification.dto.EmailMessage;
import com.ddl.manager.domain.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * 邮件服务实现类
* @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendSimpleEmail(String to, String subject, String text) throws Exception {
        EmailMessage message = EmailMessage.simple(to, subject, text);
        sendEmail(message);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String html) throws Exception {
        EmailMessage message = EmailMessage.html(to, subject, html);
        sendEmail(message);
    }

    @Override
    public void sendEmail(EmailMessage message) throws Exception {
        if (message == null) {
            throw new IllegalArgumentException("邮件消息不能为空");
        }

        // 如果只有单个收件人，使用简单邮件
        if (StringUtils.hasText(message.getTo()) && CollectionUtils.isEmpty(message.getToList())) {
            if (StringUtils.hasText(message.getHtml())) {
                sendHtmlEmailInternal(message.getTo(), message.getSubject(), message.getHtml(),
                        message.getCc(), message.getBcc());
            } else {
                sendSimpleEmailInternal(message.getTo(), message.getSubject(), message.getText(),
                        message.getCc(), message.getBcc());
            }
        } else if (!CollectionUtils.isEmpty(message.getToList())) {
            // 多个收件人，批量发送
            sendBatchEmail(message);
        } else {
            throw new IllegalArgumentException("收件人地址不能为空");
        }
    }

    @Override
    public void sendBatchEmail(EmailMessage message) throws Exception {
        if (message == null || CollectionUtils.isEmpty(message.getToList())) {
            throw new IllegalArgumentException("收件人列表不能为空");
        }

        List<String> recipients = message.getToList();
        log.info("开始批量发送邮件，收件人数量: {}", recipients.size());

        for (String to : recipients) {
            try {
                if (StringUtils.hasText(message.getHtml())) {
                    sendHtmlEmailInternal(to, message.getSubject(), message.getHtml(),
                            message.getCc(), message.getBcc());
                } else {
                    sendSimpleEmailInternal(to, message.getSubject(), message.getText(),
                            message.getCc(), message.getBcc());
                }
                log.debug("邮件发送成功: {}", to);
            } catch (Exception e) {
                log.error("邮件发送失败，收件人: {}", to, e);
                // 继续发送其他邮件，不中断批量发送
            }
        }

        log.info("批量邮件发送完成");
    }

    /**
     * 发送简单文本邮件（内部方法）
     */
    private void sendSimpleEmailInternal(String to, String subject, String text,
                                        List<String> cc, List<String> bcc) throws Exception {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);

        if (!CollectionUtils.isEmpty(cc)) {
            mailMessage.setCc(cc.toArray(new String[0]));
        }
        if (!CollectionUtils.isEmpty(bcc)) {
            mailMessage.setBcc(bcc.toArray(new String[0]));
        }

        mailSender.send(mailMessage);
        log.info("简单邮件发送成功: {} -> {}", fromEmail, to);
    }

    /**
     * 发送HTML格式邮件（内部方法）
     */
    private void sendHtmlEmailInternal(String to, String subject, String html,
                                      List<String> cc, List<String> bcc) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true); // true 表示HTML格式

        if (!CollectionUtils.isEmpty(cc)) {
            helper.setCc(cc.toArray(new String[0]));
        }
        if (!CollectionUtils.isEmpty(bcc)) {
            helper.setBcc(bcc.toArray(new String[0]));
        }

        mailSender.send(mimeMessage);
        log.info("HTML邮件发送成功: {} -> {}", fromEmail, to);
    }
}






package com.ddl.manager.domain.notification.service;

import com.alibaba.fastjson2.JSON;
import com.ddl.manager.domain.notification.dto.DdlReminderMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * DDL 提醒消息生产者
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@Service
public class DdlReminderProducer {

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${ddl.reminder.topic:ddl-reminder}")
    private String topic;

    /**
     * 发送 DDL 提醒消息
     *
     * @param message DDL 提醒消息
     */
    public void sendReminder(DdlReminderMessage message) {
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate 未配置，跳过消息发送。请检查 kafka.enabled 配置。");
            return;
        }

        try {
            // 将消息对象序列化为 JSON 字符串
            String messageJson = JSON.toJSONString(message);
            
            // 使用任务ID作为消息的key，确保同一任务的消息有序
            String key = String.valueOf(message.getTaskId());
            
            // 发送消息
            ListenableFuture<SendResult<String, String>> future = 
                    kafkaTemplate.send(topic, key, messageJson);
            
            // 添加回调处理
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> result) {
                    log.info("DDL提醒消息发送成功 - Topic: {}, Key: {}, Offset: {}, 任务: {}",
                            topic, key, 
                            result.getRecordMetadata().offset(),
                            message.getTaskTitle());
                }

                @Override
                public void onFailure(Throwable ex) {
                    log.error("DDL提醒消息发送失败 - Topic: {}, Key: {}, 任务: {}",
                            topic, key, message.getTaskTitle(), ex);
                }
            });
            
        } catch (Exception e) {
            log.error("发送DDL提醒消息异常 - 任务: {}", message.getTaskTitle(), e);
        }
    }

    /**
     * 同步发送消息（等待发送结果）
     *
     * @param message DDL 提醒消息
     * @return 是否发送成功
     */
    public boolean sendReminderSync(DdlReminderMessage message) {
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate 未配置，跳过消息发送。");
            return false;
        }

        try {
            String messageJson = JSON.toJSONString(message);
            String key = String.valueOf(message.getTaskId());
            
            SendResult<String, String> result = kafkaTemplate.send(topic, key, messageJson).get();
            
            log.info("DDL提醒消息同步发送成功 - Topic: {}, Key: {}, Offset: {}, 任务: {}",
                    topic, key, 
                    result.getRecordMetadata().offset(),
                    message.getTaskTitle());
            return true;
            
        } catch (Exception e) {
            log.error("同步发送DDL提醒消息失败 - 任务: {}", message.getTaskTitle(), e);
            return false;
        }
    }
}





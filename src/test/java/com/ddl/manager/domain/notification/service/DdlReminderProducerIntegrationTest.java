package com.ddl.manager.domain.notification.service;

import com.alibaba.fastjson2.JSON;
import com.ddl.manager.domain.notification.dto.DdlReminderMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DDL 提醒消息生产者集成测试类
 * 使用真实的 Kafka 进行测试
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("docker")
@DisplayName("DDL提醒消息生产者集成测试")
class DdlReminderProducerIntegrationTest {

    @Autowired(required = false)
    private DdlReminderProducer ddlReminderProducer;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    private DdlReminderMessage testMessage;
    private String testTopic = "ddl-reminder-test";

    @BeforeEach
    void setUp() {
        if (ddlReminderProducer == null) {
            log.warn("DdlReminderProducer 未注入，跳过测试。请确保 kafka.enabled=true");
            return;
        }
        if (kafkaTemplate == null) {
            log.warn("KafkaTemplate 未注入，跳过测试。请确保 Kafka 服务正在运行");
            return;
        }

        // 创建测试消息
        testMessage = DdlReminderMessage.builder()
                .taskId(1L)
                .taskUuid("test-uuid-001")
                .userId(100L)
                .userEmail("test@example.com")
                .taskTitle("测试任务")
                .taskDescription("这是一个测试任务的描述")
                .deadline(LocalDateTime.now().plusHours(24))
                .priority("HIGH")
                .status("TODO")
                .reminderTime(LocalDateTime.now())
                .hoursUntilDeadline(24L)
                .build();
    }

    @Test
    @DisplayName("测试异步发送消息到 Kafka")
    void testSendReminder_RealKafka() throws Exception {
        // 跳过测试如果 Kafka 未配置
        if (ddlReminderProducer == null || kafkaTemplate == null) {
            log.warn("跳过测试：Kafka 未配置");
            return;
        }

        // 执行测试 - 异步发送
        assertDoesNotThrow(() -> {
            ddlReminderProducer.sendReminder(testMessage);
        });

        // 等待消息发送完成
        Thread.sleep(1000);

        log.info("✅ 异步消息发送测试完成");
    }

    @Test
    @DisplayName("测试同步发送消息到 Kafka")
    void testSendReminderSync_RealKafka() throws Exception {
        // 跳过测试如果 Kafka 未配置
        if (ddlReminderProducer == null || kafkaTemplate == null) {
            log.warn("跳过测试：Kafka 未配置");
            return;
        }

        // 执行测试 - 同步发送
        boolean result = ddlReminderProducer.sendReminderSync(testMessage);

        // 验证发送结果
        assertTrue(result, "消息应该发送成功");
        log.info("✅ 同步消息发送测试完成，结果: {}", result);
    }

    @Test
    @DisplayName("测试直接使用 KafkaTemplate 发送消息")
    void testDirectKafkaTemplateSend() throws Exception {
        // 跳过测试如果 Kafka 未配置
        if (kafkaTemplate == null) {
            log.warn("跳过测试：KafkaTemplate 未配置");
            return;
        }

        // 准备消息
        String messageJson = JSON.toJSONString(testMessage);
        String key = String.valueOf(testMessage.getTaskId());

        // 直接使用 KafkaTemplate 发送
        ListenableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(testTopic, key, messageJson);

        // 添加回调
        final boolean[] success = {false};
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
                success[0] = true;
                log.info("✅ 消息发送成功 - Topic: {}, Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
                log.error("❌ 消息发送失败", ex);
            }
        });

        // 等待发送完成（最多等待 5 秒）
        SendResult<String, String> result = future.get(5, TimeUnit.SECONDS);
        
        // 验证
        assertNotNull(result);
        assertNotNull(result.getRecordMetadata());
        assertEquals(testTopic, result.getRecordMetadata().topic());
        log.info("✅ 直接 KafkaTemplate 发送测试完成");
    }

    @Test
    @DisplayName("测试消息序列化和反序列化")
    void testMessageSerialization() {
        // 序列化
        String json = JSON.toJSONString(testMessage);
        assertNotNull(json);
        assertTrue(json.contains("\"taskId\":1"));
        assertTrue(json.contains("\"taskTitle\":\"测试任务\""));

        // 反序列化
        DdlReminderMessage deserialized = JSON.parseObject(json, DdlReminderMessage.class);
        assertNotNull(deserialized);
        assertEquals(testMessage.getTaskId(), deserialized.getTaskId());
        assertEquals(testMessage.getTaskTitle(), deserialized.getTaskTitle());
        assertEquals(testMessage.getUserEmail(), deserialized.getUserEmail());
        
        log.info("✅ 消息序列化/反序列化测试完成");
    }

    @Test
    @DisplayName("测试批量发送消息")
    void testBatchSend() throws Exception {
        // 跳过测试如果 Kafka 未配置
        if (ddlReminderProducer == null || kafkaTemplate == null) {
            log.warn("跳过测试：Kafka 未配置");
            return;
        }

        // 创建多个测试消息
        for (int i = 1; i <= 3; i++) {
            DdlReminderMessage message = DdlReminderMessage.builder()
                    .taskId((long) i)
                    .taskUuid("test-uuid-" + String.format("%03d", i))
                    .userId(100L)
                    .userEmail("test@example.com")
                    .taskTitle("批量测试任务 " + i)
                    .deadline(LocalDateTime.now().plusHours(24))
                    .priority("HIGH")
                    .status("TODO")
                    .reminderTime(LocalDateTime.now())
                    .hoursUntilDeadline(24L)
                    .build();

            boolean result = ddlReminderProducer.sendReminderSync(message);
            assertTrue(result, "消息 " + i + " 应该发送成功");
        }

        log.info("✅ 批量发送测试完成，共发送 3 条消息");
    }
}


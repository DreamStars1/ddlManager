package com.ddl.manager.domain.notification.service;

import com.alibaba.fastjson2.JSON;
import com.ddl.manager.domain.notification.dto.DdlReminderMessage;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DDL 提醒消息生产者测试类
 * @author zhenghaipei
 * @since 2025-12-13
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("DDL提醒消息生产者测试")
class DdlReminderProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private DdlReminderProducer ddlReminderProducer;

    private DdlReminderMessage testMessage;
    private String testTopic = "ddl-reminder-test";

    @BeforeEach
    void setUp() {
        // 设置测试主题
        ReflectionTestUtils.setField(ddlReminderProducer, "topic", testTopic);
        
        // 注入 Mock 的 KafkaTemplate
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", kafkaTemplate);

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
    @DisplayName("测试异步发送消息 - 成功场景")
    void testSendReminder_Success() throws Exception {
        // 确保 kafkaTemplate 被注入（setUp 中已设置，但这里显式设置以确保）
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", kafkaTemplate);
        
        // 准备 Mock 数据
        SendResult<String, String> sendResult = createMockSendResult();
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();
        future.set(sendResult);

        // Mock KafkaTemplate 行为
        when(kafkaTemplate.send(eq(testTopic), eq("1"), anyString()))
                .thenReturn(future);

        // 执行测试
        ddlReminderProducer.sendReminder(testMessage);

        // 验证
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate, times(1))
                .send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        // 验证发送参数
        assertEquals(testTopic, topicCaptor.getValue());
        assertEquals("1", keyCaptor.getValue()); // taskId 作为 key

        // 验证消息内容
        String sentMessage = valueCaptor.getValue();
        DdlReminderMessage sentMessageObj = JSON.parseObject(sentMessage, DdlReminderMessage.class);
        assertEquals(testMessage.getTaskId(), sentMessageObj.getTaskId());
        assertEquals(testMessage.getTaskTitle(), sentMessageObj.getTaskTitle());
        assertEquals(testMessage.getUserEmail(), sentMessageObj.getUserEmail());
    }

    @Test
    @DisplayName("测试异步发送消息 - KafkaTemplate 为 null")
    void testSendReminder_KafkaTemplateNull() {
        // 先确保 kafkaTemplate 被注入（setUp 中已设置）
        // 然后设置为 null 来测试 null 处理逻辑
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", null);

        // 执行测试（不应该抛出异常）
        assertDoesNotThrow(() -> ddlReminderProducer.sendReminder(testMessage));

        // 验证没有调用 KafkaTemplate（因为它是 null）
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("测试异步发送消息 - 发送失败场景")
    void testSendReminder_Failure() {
        // 确保 kafkaTemplate 被注入
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", kafkaTemplate);
        
        // 准备 Mock 数据 - 发送失败
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();
        RuntimeException exception = new RuntimeException("Kafka发送失败");
        future.setException(exception);

        // Mock KafkaTemplate 行为
        when(kafkaTemplate.send(eq(testTopic), eq("1"), anyString()))
                .thenReturn(future);

        // 执行测试（不应该抛出异常，因为内部有异常处理）
        assertDoesNotThrow(() -> ddlReminderProducer.sendReminder(testMessage));

        // 验证调用了 send 方法
        verify(kafkaTemplate, times(1))
                .send(eq(testTopic), eq("1"), anyString());
    }

    @Test
    @DisplayName("测试同步发送消息 - 成功场景")
    void testSendReminderSync_Success() throws Exception {
        // 确保 kafkaTemplate 被注入
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", kafkaTemplate);
        
        // 准备 Mock 数据
        SendResult<String, String> sendResult = createMockSendResult();
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();
        future.set(sendResult);

        // Mock KafkaTemplate 行为
        when(kafkaTemplate.send(eq(testTopic), eq("1"), anyString()))
                .thenReturn(future);

        // 执行测试
        boolean result = ddlReminderProducer.sendReminderSync(testMessage);

        // 验证
        assertTrue(result);
        verify(kafkaTemplate, times(1))
                .send(eq(testTopic), eq("1"), anyString());
    }

    @Test
    @DisplayName("测试同步发送消息 - KafkaTemplate 为 null")
    void testSendReminderSync_KafkaTemplateNull() {
        // 先确保 kafkaTemplate 被注入（setUp 中已设置）
        // 然后设置为 null 来测试 null 处理逻辑
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", null);

        // 执行测试
        boolean result = ddlReminderProducer.sendReminderSync(testMessage);

        // 验证
        assertFalse(result);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("测试同步发送消息 - 发送异常场景")
    void testSendReminderSync_Exception() throws Exception {
        // 确保 kafkaTemplate 被注入
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", kafkaTemplate);
        
        // 准备 Mock 数据 - 发送异常
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();
        RuntimeException exception = new RuntimeException("Kafka发送异常");
        future.setException(exception);

        // Mock KafkaTemplate 行为
        when(kafkaTemplate.send(eq(testTopic), eq("1"), anyString()))
                .thenReturn(future);

        // 执行测试
        boolean result = ddlReminderProducer.sendReminderSync(testMessage);

        // 验证
        assertFalse(result);
        verify(kafkaTemplate, times(1))
                .send(eq(testTopic), eq("1"), anyString());
    }

    @Test
    @DisplayName("测试消息序列化")
    void testMessageSerialization() throws Exception {
        // 确保 kafkaTemplate 被注入
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", kafkaTemplate);
        
        // 准备 Mock 数据
        SendResult<String, String> sendResult = createMockSendResult();
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();
        future.set(sendResult);

        // Mock KafkaTemplate 行为
        when(kafkaTemplate.send(eq(testTopic), anyString(), anyString()))
                .thenReturn(future);

        // 执行测试
        ddlReminderProducer.sendReminder(testMessage);

        // 验证消息序列化
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1))
                .send(anyString(), anyString(), valueCaptor.capture());

        String sentJson = valueCaptor.getValue();
        assertNotNull(sentJson);
        assertTrue(sentJson.contains("\"taskId\":1"));
        assertTrue(sentJson.contains("\"taskTitle\":\"测试任务\""));
        assertTrue(sentJson.contains("\"userEmail\":\"test@example.com\""));

        // 验证可以反序列化
        DdlReminderMessage deserialized = JSON.parseObject(sentJson, DdlReminderMessage.class);
        assertNotNull(deserialized);
        assertEquals(testMessage.getTaskId(), deserialized.getTaskId());
        assertEquals(testMessage.getTaskTitle(), deserialized.getTaskTitle());
    }

    @Test
    @DisplayName("测试使用任务ID作为消息Key")
    void testMessageKey() throws Exception {
        // 确保 kafkaTemplate 被注入
        ReflectionTestUtils.setField(ddlReminderProducer, "kafkaTemplate", kafkaTemplate);
        
        // 准备 Mock 数据
        SendResult<String, String> sendResult = createMockSendResult();
        SettableListenableFuture<SendResult<String, String>> future = new SettableListenableFuture<>();
        future.set(sendResult);

        // Mock KafkaTemplate 行为
        when(kafkaTemplate.send(eq(testTopic), anyString(), anyString()))
                .thenReturn(future);

        // 执行测试
        ddlReminderProducer.sendReminder(testMessage);

        // 验证消息 Key
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1))
                .send(anyString(), keyCaptor.capture(), anyString());

        assertEquals("1", keyCaptor.getValue()); // taskId 作为 key
    }

    /**
     * 创建 Mock 的 SendResult
     */
    private SendResult<String, String> createMockSendResult() {
        SendResult<String, String> sendResult = mock(SendResult.class);
        RecordMetadata recordMetadata =
                mock(RecordMetadata.class);

        // Mock RecordMetadata 的常用方法
        when(recordMetadata.topic()).thenReturn(testTopic);
        when(recordMetadata.partition()).thenReturn(0);
        when(recordMetadata.offset()).thenReturn(0L);
        when(recordMetadata.timestamp()).thenReturn(System.currentTimeMillis());
        
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        return sendResult;
    }
}


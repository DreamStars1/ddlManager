package com.ddl.manager.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.PostConstruct;

/**
 * Kafka 配置诊断工具
 * 用于检查 Kafka 配置是否正确加载
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@Configuration
public class KafkaConfigDiagnostic {

    @Value("${kafka.enabled:false}")
    private boolean kafkaEnabled;

    @Value("${kafka.producer.servers:not-configured}")
    private String producerServers;

    @Value("${kafka.consumer.servers:not-configured}")
    private String consumerServers;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostConstruct
    public void logKafkaConfig() {
        log.info("========================================");
        log.info("Kafka 配置诊断信息:");
        log.info("  kafka.enabled: {}", kafkaEnabled);
        log.info("  kafka.producer.servers: {}", producerServers);
        log.info("  kafka.consumer.servers: {}", consumerServers);
        log.info("  KafkaTemplate 是否存在: {}", kafkaTemplate != null);
        log.info("========================================");
        
        if (!kafkaEnabled) {
            log.warn("⚠️  Kafka 未启用！请检查配置文件中 kafka.enabled 是否为 true");
        } else if (kafkaTemplate == null) {
            log.error("❌ Kafka 已启用但 KafkaTemplate 未创建！请检查 KafkaProducerConfig 是否正确加载");
        } else {
            log.info("✅ Kafka 配置正常，KafkaTemplate 已创建");
        }
    }
}


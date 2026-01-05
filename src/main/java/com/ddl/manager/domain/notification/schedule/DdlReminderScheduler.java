package com.ddl.manager.domain.notification.schedule;

import com.ddl.manager.domain.notification.service.TaskReminderService;
import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.domain.task.repository.TaskRepository;
import com.ddl.manager.shared.enums.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * DDL 提醒定时任务
 * 定期扫描快到期的任务并发送提醒消息到 Kafka
 * 
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Slf4j
@Component
public class DdlReminderScheduler {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskReminderService taskReminderService;

    /**
     * 默认提醒小时数（从配置读取，默认24小时）
     */
    @Value("${ddl.reminder.default-hours:24}")
    private int defaultReminderHours;

    /**
     * 定时扫描并发送提醒
     * 执行频率：每30分钟执行一次
     * 
     * Cron 表达式说明：
     * - 秒 分 时 日 月 周
     * - 0 0/30 * * * ? 表示每30分钟执行一次
     * - 0 0 * * * ? 表示每小时执行一次
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    @Transactional
    public void scanAndSendReminders() {
        log.info("========== 开始扫描需要提醒的任务 ==========");
        
        LocalDateTime now = LocalDateTime.now();
        
        // 计算提醒时间阈值：当前时间 + 提醒小时数
        // 如果任务的截止时间在这个阈值之前，就需要发送提醒
        LocalDateTime reminderThreshold = now.plusHours(defaultReminderHours);
        
        log.info("当前时间: {}, 提醒阈值: {} ({}小时后)", 
                now, reminderThreshold, defaultReminderHours);
        
        // 查询需要发送提醒的任务
        // 条件：未发送提醒 && 截止时间 <= 提醒阈值 && 截止时间 > 当前时间（未过期） && 状态为待办或进行中
        List<TaskStatus> activeStatuses = Arrays.asList(
                TaskStatus.TODO, 
                TaskStatus.IN_PROGRESS
        );
        
        List<TaskEntity> tasksNeedingReminder = taskRepository.findTasksNeedingReminder(
                reminderThreshold, 
                now,
                activeStatuses
        );
        
        if (tasksNeedingReminder == null || tasksNeedingReminder.isEmpty()) {
            log.info("✅ 没有需要提醒的任务");
            return;
        }
        
        log.info("📋 找到 {} 个需要提醒的任务", tasksNeedingReminder.size());
        
        int successCount = 0;
        int failCount = 0;
        
        // 遍历任务并发送提醒
        for (TaskEntity task : tasksNeedingReminder) {
            try {
                // 检查任务是否真的需要提醒（双重检查）
                if (task.getReminderSent() != null && task.getReminderSent()) {
                    log.debug("任务已发送过提醒，跳过 - 任务ID: {}", task.getId());
                    continue;
                }
                
                // 检查任务状态
                if (task.getStatus() == TaskStatus.COMPLETED || 
                    task.getStatus() == TaskStatus.CANCELED) {
                    log.debug("任务已完成或已取消，跳过 - 任务ID: {}, 状态: {}", 
                            task.getId(), task.getStatus());
                    continue;
                }
                
                // 检查截止时间是否已过
                if (task.getDeadline().isBefore(now)) {
                    log.debug("任务已过期，跳过 - 任务ID: {}, 截止时间: {}", 
                            task.getId(), task.getDeadline());
                    continue;
                }
                
                // 发送提醒
                boolean sent = taskReminderService.sendTaskReminder(task);
                
                if (sent) {
                    // 标记为已发送提醒
                    task.setReminderSent(true);
                    taskRepository.save(task);
                    successCount++;
                    log.info("✅ 任务提醒已发送 - 任务ID: {}, 标题: {}, 截止时间: {}", 
                            task.getId(), task.getTitle(), task.getDeadline());
                } else {
                    failCount++;
                    log.warn("⚠️  任务提醒发送失败 - 任务ID: {}, 标题: {}", 
                            task.getId(), task.getTitle());
                }
                
            } catch (Exception e) {
                failCount++;
                log.error("❌ 发送任务提醒异常 - 任务ID: {}, 标题: {}", 
                        task.getId(), task.getTitle(), e);
            }
        }
        
        log.info("========== 扫描完成 ==========");
        log.info("📊 统计: 总数={}, 成功={}, 失败={}", 
                tasksNeedingReminder.size(), successCount, failCount);
    }

    /**
     * 手动触发扫描（用于测试或手动执行）
     */
    public void manualScan() {
        log.info("手动触发任务扫描");
        scanAndSendReminders();
    }
}


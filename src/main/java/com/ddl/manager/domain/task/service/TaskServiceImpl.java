package com.ddl.manager.domain.task.service;

import com.ddl.manager.domain.task.dto.TaskDTO;
import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.domain.task.repository.TaskRepositoryPort;
import com.ddl.manager.infrastructure.exception.BusinessException;
import com.ddl.manager.shared.enums.TaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 任务服务实现类
 * @author zhenghaipei
 * @since 2025-12-14
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepositoryPort taskRepositoryPort;

    /**
     * 创建任务
     */
    @Override
    @Transactional
    public TaskEntity createTask(TaskDTO taskDTO, Long userId) {
        // 1. 校验截止时间不能早于当前时间
        if (taskDTO.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("截止时间不能早于当前时间");
        }

        // 2. 构建任务实体
        TaskEntity task = TaskEntity.builder()
                .userId(userId)
                .title(taskDTO.getTitle())
                .description(taskDTO.getDescription())
                .category(taskDTO.getCategory())
                .deadline(taskDTO.getDeadline())
                .status(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatus.TODO)
                .priority(taskDTO.getPriority() != null ? taskDTO.getPriority() : com.ddl.manager.shared.enums.TaskPriority.MEDIUM)
                .progress(taskDTO.getProgress() != null ? taskDTO.getProgress() : 0)
                .reminderSent(false)
                .build();

        // 3. 保存任务
        TaskEntity savedTask = taskRepositoryPort.save(task);
        log.info("用户 {} 创建任务: {}", userId, savedTask.getTitle());

        return savedTask;
    }

    /**
     * 更新任务
     */
    @Override
    @Transactional
    public TaskEntity updateTask(String uuid, TaskDTO taskDTO, Long userId) {
        // 1. 获取任务并校验权限
        TaskEntity task = getTaskByUuid(uuid, userId);

        // 2. 校验截止时间
        if (taskDTO.getDeadline() != null && taskDTO.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("截止时间不能早于当前时间");
        }

        // 3. 更新任务信息
        if (taskDTO.getTitle() != null) {
            task.setTitle(taskDTO.getTitle());
        }
        if (taskDTO.getDescription() != null) {
            task.setDescription(taskDTO.getDescription());
        }
        if (taskDTO.getCategory() != null) {
            task.setCategory(taskDTO.getCategory());
        }
        if (taskDTO.getDeadline() != null) {
            task.setDeadline(taskDTO.getDeadline());
        }
        if (taskDTO.getStatus() != null) {
            // 如果状态变为已完成，自动标记完成
            if (taskDTO.getStatus() == TaskStatus.COMPLETED) {
                task.markCompleted();
            } else {
                task.setStatus(taskDTO.getStatus());
            }
        }
        if (taskDTO.getPriority() != null) {
            task.setPriority(taskDTO.getPriority());
        }
        if (taskDTO.getProgress() != null) {
            task.setProgress(taskDTO.getProgress());
        }
        if (taskDTO.getProgressLog() != null) {
            task.setProgressLog(taskDTO.getProgressLog());
        }

        // 4. 保存更新
        TaskEntity updatedTask = taskRepositoryPort.save(task);
        log.info("用户 {} 更新任务: {}", userId, updatedTask.getTitle());

        return updatedTask;
    }

    /**
     * 根据UUID获取任务（带权限校验）
     */
    @Override
    public TaskEntity getTaskByUuid(String uuid, Long userId) {
        TaskEntity task = taskRepositoryPort.findByUuid(uuid)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        // 权限校验：只能查看自己的任务
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该任务");
        }

        return task;
    }

    /**
     * 根据ID获取任务（带权限校验）
     */
    @Override
    public TaskEntity getTaskById(Long id, Long userId) {
        TaskEntity task = taskRepositoryPort.findById(id)
                .orElseThrow(() -> new BusinessException("任务不存在"));

        // 权限校验：只能查看自己的任务
        if (!task.getUserId().equals(userId)) {
            throw new BusinessException("无权访问该任务");
        }

        return task;
    }

    /**
     * 删除任务（带权限校验）
     */
    @Override
    @Transactional
    public void deleteTask(String uuid, Long userId) {
        // 1. 获取任务并校验权限
        TaskEntity task = getTaskByUuid(uuid, userId);

        // 2. 删除任务
        taskRepositoryPort.deleteById(task.getId());
        log.info("用户 {} 删除任务: {}", userId, task.getTitle());
    }

    /**
     * 获取用户的任务列表（分页）
     */
    @Override
    public Page<TaskEntity> getUserTasks(Long userId, TaskStatus status, Pageable pageable) {
        if (status != null) {
            return taskRepositoryPort.findByUserIdAndStatus(userId, status, pageable);
        } else {
            return taskRepositoryPort.findByUserId(userId, pageable);
        }
    }

    /**
     * 获取即将截止的任务
     */
    @Override
    public List<TaskEntity> getUpcomingTasks(Long userId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(days);
        List<TaskStatus> statuses = Arrays.asList(TaskStatus.TODO, TaskStatus.IN_PROGRESS);
        return taskRepositoryPort.findUpcomingTasks(userId, now, deadline, statuses);
    }

    /**
     * 更新任务进度
     */
    @Override
    @Transactional
    public TaskEntity updateProgress(String uuid, Integer progress, String progressLog, Long userId) {
        // 1. 获取任务并校验权限
        TaskEntity task = getTaskByUuid(uuid, userId);

        // 2. 校验进度范围
        if (progress < 0 || progress > 100) {
            throw new BusinessException("进度必须在0-100之间");
        }

        // 3. 更新进度
        task.setProgress(progress);
        if (progressLog != null && !progressLog.trim().isEmpty()) {
            // 追加进度备注（保留历史记录）
            String existingLog = task.getProgressLog();
            String newLog = String.format("[%s] %s\n", LocalDateTime.now(), progressLog);
            task.setProgressLog(existingLog != null ? existingLog + "\n" + newLog : newLog);
        }

        // 4. 如果进度达到100%，自动标记为已完成
        if (progress == 100 && task.getStatus() != TaskStatus.COMPLETED) {
            task.markCompleted();
        }

        // 5. 保存更新
        TaskEntity updatedTask = taskRepositoryPort.save(task);
        log.info("用户 {} 更新任务进度: {} -> {}%", userId, task.getTitle(), progress);

        return updatedTask;
    }

    /**
     * 更新任务状态
     */
    @Override
    @Transactional
    public TaskEntity updateStatus(String uuid, TaskStatus status, Long userId) {
        // 1. 获取任务并校验权限
        TaskEntity task = getTaskByUuid(uuid, userId);

        // 2. 校验状态变更
        if (task.getStatus().isTerminal() && status != task.getStatus()) {
            throw new BusinessException("已完成或已取消的任务不能修改状态");
        }

        // 3. 更新状态
        if (status == TaskStatus.COMPLETED) {
            task.markCompleted();
        } else {
            task.setStatus(status);
        }

        // 4. 保存更新
        TaskEntity updatedTask = taskRepositoryPort.save(task);
        log.info("用户 {} 更新任务状态: {} -> {}", userId, task.getTitle(), status.getLabel());

        return updatedTask;
    }

    /**
     * 获取待办任务数量
     */
    @Override
    public int getPendingTaskCount(Long userId) {
        try {
            // 获取TODO状态的任务数量
            Page<TaskEntity> todoTasks = taskRepositoryPort.findByUserIdAndStatus(
                    userId, TaskStatus.TODO, Pageable.unpaged());
            return (int) todoTasks.getTotalElements();
        } catch (Exception e) {
            log.error("获取待办任务数量失败: userId={}", userId, e);
            return 0;
        }
    }

    /**
     * 获取进行中任务数量
     */
    @Override
    public int getInProgressCount(Long userId) {
        try {
            // 获取IN_PROGRESS状态的任务数量
            Page<TaskEntity> inProgressTasks = taskRepositoryPort.findByUserIdAndStatus(
                    userId, TaskStatus.IN_PROGRESS, Pageable.unpaged());
            return (int) inProgressTasks.getTotalElements();
        } catch (Exception e) {
            log.error("获取进行中任务数量失败: userId={}", userId, e);
            return 0;
        }
    }

    /**
     * 获取即将截止的任务数量
     */
    @Override
    public int getDueSoonCount(Long userId, int days) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deadline = now.plusDays(days);
            List<TaskStatus> activeStatuses = Arrays.asList(TaskStatus.TODO, TaskStatus.IN_PROGRESS);

            // 获取即将截止的任务
            List<TaskEntity> dueSoonTasks = taskRepositoryPort.findUpcomingTasks(
                    userId, now, deadline, activeStatuses);
            return dueSoonTasks.size();
        } catch (Exception e) {
            log.error("获取即将截止任务数量失败: userId={}, days={}", userId, days, e);
            return 0;
        }
    }

    /**
     * 获取本月完成的任务数量
     */
    @Override
    public int getCompletedThisMonthCount(Long userId) {
        try {
            // 获取本月第一天
            LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
            LocalDateTime startOfMonth = firstDayOfMonth.atStartOfDay();

            // 获取本月完成的任务
            Page<TaskEntity> completedTasks = taskRepositoryPort.findByUserIdAndStatus(
                    userId, TaskStatus.COMPLETED, Pageable.unpaged());

            // 过滤本月完成的任务
            long count = completedTasks.getContent().stream()
                    .filter(task -> task.getCompletedTime() != null &&
                            task.getCompletedTime().isAfter(startOfMonth))
                    .count();

            return (int) count;
        } catch (Exception e) {
            log.error("获取本月完成任务数量失败: userId={}", userId, e);
            return 0;
        }
    }


}


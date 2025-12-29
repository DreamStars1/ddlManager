package com.ddl.manager.domain.task.service;

import com.ddl.manager.domain.task.dto.TaskDTO;
import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.shared.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 任务服务接口
 * @author zhenghaipei
 * @since 2025-12-14
 */
public interface TaskService {

    /**
     * 创建任务
     * @param taskDTO 任务信息
     * @param userId 用户ID
     * @return 创建的任务实体
     */
    TaskEntity createTask(TaskDTO taskDTO, Long userId);

    /**
     * 更新任务
     * @param uuid 任务UUID
     * @param taskDTO 任务信息
     * @param userId 用户ID
     * @return 更新后的任务实体
     */
    TaskEntity updateTask(String uuid, TaskDTO taskDTO, Long userId);

    /**
     * 根据UUID获取任务（带权限校验）
     * @param uuid 任务UUID
     * @param userId 用户ID
     * @return 任务实体
     */
    TaskEntity getTaskByUuid(String uuid, Long userId);

    /**
     * 根据ID获取任务（带权限校验）
     * @param id 任务ID
     * @param userId 用户ID
     * @return 任务实体
     */
    TaskEntity getTaskById(Long id, Long userId);

    /**
     * 删除任务（带权限校验）
     * @param uuid 任务UUID
     * @param userId 用户ID
     */
    void deleteTask(String uuid, Long userId);

    /**
     * 获取用户的任务列表（分页）
     * @param userId 用户ID
     * @param status 任务状态（可选）
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<TaskEntity> getUserTasks(Long userId, TaskStatus status, Pageable pageable);

    /**
     * 获取即将截止的任务
     * @param userId 用户ID
     * @param days 未来N天
     * @return 任务列表
     */
    List<TaskEntity> getUpcomingTasks(Long userId, int days);

    /**
     * 更新任务进度
     * @param uuid 任务UUID
     * @param progress 进度百分比
     * @param progressLog 进度备注
     * @param userId 用户ID
     * @return 更新后的任务实体
     */
    TaskEntity updateProgress(String uuid, Integer progress, String progressLog, Long userId);

    /**
     * 更新任务状态
     * @param uuid 任务UUID
     * @param status 任务状态
     * @param userId 用户ID
     * @return 更新后的任务实体
     */
    TaskEntity updateStatus(String uuid, TaskStatus status, Long userId);
}


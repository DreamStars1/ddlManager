package com.ddl.manager.domain.task.repository;

import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.shared.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务仓储端口接口（业务层使用）
 * Service层只依赖此接口，不关心底层是JPA还是MyBatis
 * @author developer
 * @since 2025-12-13
 */
public interface TaskRepositoryPort {

    /**
     * 根据ID查找任务
     * @param id 任务ID
     * @return 任务实体
     */
    Optional<TaskEntity> findById(Long id);

    /**
     * 根据UUID查找任务
     * @param uuid UUID
     * @return 任务实体
     */
    Optional<TaskEntity> findByUuid(String uuid);

    /**
     * 保存任务
     * @param task 任务实体
     * @return 保存后的任务实体
     */
    TaskEntity save(TaskEntity task);

    /**
     * 根据ID删除任务
     * @param id 任务ID
     */
    void deleteById(Long id);

    /**
     * 查找所有任务
     * @return 任务列表
     */
    List<TaskEntity> findAll();

    /**
     * 根据用户ID查找任务列表（分页）
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<TaskEntity> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据用户ID和状态查找任务（分页）
     * @param userId 用户ID
     * @param status 任务状态
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<TaskEntity> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);


    /**
     * 查找即将截止的任务
     * @param userId 用户ID
     * @param now 当前时间
     * @param deadline 截止时间上限
     * @param statuses 任务状态列表
     * @return 任务列表
     */
    List<TaskEntity> findUpcomingTasks(Long userId, LocalDateTime now, 
                                       LocalDateTime deadline, List<TaskStatus> statuses);

    /**
     * 查找需要发送提醒的任务
     * @param reminderTime 提醒时间阈值
     * @param statuses 任务状态列表
     * @return 任务列表
     */
    List<TaskEntity> findTasksNeedingReminder(LocalDateTime reminderTime, List<TaskStatus> statuses);

    /**
     * 判断任务是否存在
     * @param id 任务ID
     * @return true-存在
     */
    boolean existsById(Long id);
}

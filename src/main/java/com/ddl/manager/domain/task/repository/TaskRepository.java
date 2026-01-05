package com.ddl.manager.domain.task.repository;

import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.shared.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务数据访问接口
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    /**
     * 根据UUID查找任务
     * @param uuid UUID
     * @return 任务实体
     */
    Optional<TaskEntity> findByUuid(String uuid);

    /**
     * 根据用户ID查找任务列表
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<TaskEntity> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据用户ID和状态查找任务
     * @param userId 用户ID
     * @param status 任务状态
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<TaskEntity> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable);

    /**
     * 查找即将截止的任务（用于仪表盘）
     * @param userId 用户ID
     * @param now 当前时间
     * @param deadline 截止时间上限
     * @param statuses 任务状态列表
     * @return 任务列表
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.userId = :userId " +
            "AND t.deadline BETWEEN :now AND :deadline " +
            "AND t.status IN :statuses " +
            "ORDER BY t.deadline ASC")
    List<TaskEntity> findUpcomingTasks(@Param("userId") Long userId,
                                       @Param("now") LocalDateTime now,
                                       @Param("deadline") LocalDateTime deadline,
                                       @Param("statuses") List<TaskStatus> statuses);

    /**
     * 查找需要发送提醒的任务
     * @param reminderTime 提醒时间阈值（当前时间 + 提醒小时数）
     * @param statuses 任务状态列表
     * @return 任务列表
     */
    @Query("SELECT t FROM TaskEntity t WHERE t.reminderSent = false " +
            "AND t.deadline <= :reminderTime " +
            "AND t.deadline > :now " +
            "AND t.status IN :statuses " +
            "ORDER BY t.deadline ASC")
    List<TaskEntity> findTasksNeedingReminder(@Param("reminderTime") LocalDateTime reminderTime,
                                              @Param("now") LocalDateTime now,
                                              @Param("statuses") List<TaskStatus> statuses);
}

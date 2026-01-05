package com.ddl.manager.domain.task.Manager;

import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.domain.task.repository.TaskRepository;
import com.ddl.manager.domain.task.repository.TaskRepositoryPort;
import com.ddl.manager.shared.enums.TaskStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务仓储JPA适配器
 * 将JPA Repository适配为业务层使用的Manager层接口
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Component
@Primary
public class TaskManager implements TaskRepositoryPort {

    /** JPA Repository */
    private final TaskRepository taskRepository;

    public TaskManager(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Optional<TaskEntity> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public Optional<TaskEntity> findByUuid(String uuid) {
        return taskRepository.findByUuid(uuid);
    }

    @Override
    public TaskEntity save(TaskEntity task) {
        return taskRepository.save(task);
    }

    @Override
    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public List<TaskEntity> findAll() {
        return taskRepository.findAll();
    }


    @Override
    public Page<TaskEntity> findByUserId(Long userId, Pageable pageable) {
        return taskRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<TaskEntity> findByUserIdAndStatus(Long userId, TaskStatus status, Pageable pageable) {
        return taskRepository.findByUserIdAndStatus(userId, status, pageable);
    }

    @Override
    public List<TaskEntity> findUpcomingTasks(Long userId, LocalDateTime now, 
                                              LocalDateTime deadline, List<TaskStatus> statuses) {
        return taskRepository.findUpcomingTasks(userId, now, deadline, statuses);
    }

    @Override
    public List<TaskEntity> findTasksNeedingReminder(LocalDateTime reminderTime, LocalDateTime now, List<TaskStatus> statuses) {
        return taskRepository.findTasksNeedingReminder(reminderTime, now, statuses);
    }

    @Override
    public boolean existsById(Long id) {
        return taskRepository.existsById(id);
    }
}

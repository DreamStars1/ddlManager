package com.ddl.manager.domain.task.controller;

import com.ddl.manager.domain.task.dto.ProgressDTO;
import com.ddl.manager.domain.task.dto.TaskDTO;
import com.ddl.manager.domain.task.model.TaskEntity;
import com.ddl.manager.domain.task.service.TaskService;
import com.ddl.manager.shared.dto.AjaxResult;
import com.ddl.manager.shared.enums.TaskPriority;
import com.ddl.manager.shared.enums.TaskStatus;
import com.ddl.manager.shared.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ddl.manager.infrastructure.annotation.RequiresPermission;

import javax.validation.Valid;
import java.util.Arrays;

/**
 * 任务控制器
 * @author zhenghaipei
 * @since 2025-12-14
 */
@Slf4j
@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 任务列表页
     * @param model 模型
     * @param status 任务状态筛选（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 任务列表视图
     */

    @GetMapping
    public String listTasks(Model model,
                            @RequestParam(required = false) TaskStatus status,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {
        // 使用安全的方法获取用户ID
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        // 分页参数：按截止时间升序排序
        Pageable pageable = PageRequest.of(page, size, Sort.by("deadline").ascending());
        Page<TaskEntity> taskPage = taskService.getUserTasks(userId, status, pageable);

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("totalElements", taskPage.getTotalElements());
        model.addAttribute("status", status);
        model.addAttribute("statuses", Arrays.asList(TaskStatus.values()));
        model.addAttribute("priorities", Arrays.asList(TaskPriority.values()));

        return "task-list";
    }

    /**
     * 任务详情页
     * @param uuid 任务UUID
     * @param model 模型
     * @return 任务详情视图
     */

    @GetMapping("/{uuid}")
    public String taskDetail(@PathVariable String uuid, Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            TaskEntity task = taskService.getTaskByUuid(uuid, userId);
            model.addAttribute("task", task);
            model.addAttribute("statuses", Arrays.asList(TaskStatus.values()));
            model.addAttribute("priorities", Arrays.asList(TaskPriority.values()));
            return "task-detail";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/tasks";
        }
    }

    /**
     * 新建任务页
     * @param model 模型
     * @return 任务表单视图
     */
    @GetMapping("/new")
    public String newTask(Model model) {
        if (!model.containsAttribute("taskDTO")) {
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setStatus(TaskStatus.TODO);
            taskDTO.setPriority(TaskPriority.MEDIUM);
            taskDTO.setProgress(0);
            model.addAttribute("taskDTO", taskDTO);
        }
        model.addAttribute("statuses", Arrays.asList(TaskStatus.values()));
        model.addAttribute("priorities", Arrays.asList(TaskPriority.values()));
        model.addAttribute("categories", Arrays.asList("学习", "生活", "工作", "其他"));
        return "task-form";
    }

    /**
     * 创建任务
     * @param taskDTO 任务信息
     * @param bindingResult 校验结果
     * @param redirectAttributes 重定向属性
     * @return 重定向地址
     */

    @PostMapping
    public String createTask(@Valid @ModelAttribute TaskDTO taskDTO,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getFieldErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("taskDTO", taskDTO);
            return "redirect:/tasks/new";
        }

        try {
            TaskEntity task = taskService.createTask(taskDTO, userId);
            redirectAttributes.addFlashAttribute("successMessage", "任务创建成功");
            return "redirect:/tasks/" + task.getUuid();
        } catch (Exception e) {
            log.error("创建任务失败", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("taskDTO", taskDTO);
            return "redirect:/tasks/new";
        }
    }

    /**
     * 编辑任务页
     * @param uuid 任务UUID
     * @param model 模型
     * @return 任务表单视图
     */
    @GetMapping("/{uuid}/edit")
    public String editTask(@PathVariable String uuid, Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            TaskEntity task = taskService.getTaskByUuid(uuid, userId);
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setUuid(task.getUuid());
            taskDTO.setTitle(task.getTitle());
            taskDTO.setDescription(task.getDescription());
            taskDTO.setCategory(task.getCategory());
            taskDTO.setDeadline(task.getDeadline());
            taskDTO.setStatus(task.getStatus());
            taskDTO.setPriority(task.getPriority());
            taskDTO.setProgress(task.getProgress());
            taskDTO.setProgressLog(task.getProgressLog());

            model.addAttribute("taskDTO", taskDTO);
            model.addAttribute("statuses", Arrays.asList(TaskStatus.values()));
            model.addAttribute("priorities", Arrays.asList(TaskPriority.values()));
            model.addAttribute("categories", Arrays.asList("学习", "生活", "工作", "其他"));
            return "task-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/tasks";
        }
    }

    /**
     * 更新任务
     * @param uuid 任务UUID
     * @param taskDTO 任务信息
     * @param bindingResult 校验结果
     * @param redirectAttributes 重定向属性
     * @return 重定向地址
     */
    @PostMapping("/{uuid}")
    public String updateTask(@PathVariable String uuid,
                            @Valid @ModelAttribute TaskDTO taskDTO,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getFieldErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("taskDTO", taskDTO);
            return "redirect:/tasks/" + uuid + "/edit";
        }

        try {
            taskService.updateTask(uuid, taskDTO, userId);
            redirectAttributes.addFlashAttribute("successMessage", "任务更新成功");
            return "redirect:/tasks/" + uuid;
        } catch (Exception e) {
            log.error("更新任务失败", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("taskDTO", taskDTO);
            return "redirect:/tasks/" + uuid + "/edit";
        }
    }

    /**
     * 删除任务
     * @param uuid 任务UUID
     * @param redirectAttributes 重定向属性
     * @return 重定向地址
     */

    @PostMapping("/{uuid}/delete")
    public String deleteTask(@PathVariable String uuid,
                            RedirectAttributes redirectAttributes) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            taskService.deleteTask(uuid, userId);
            redirectAttributes.addFlashAttribute("successMessage", "任务删除成功");
        } catch (Exception e) {
            log.error("删除任务失败", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/tasks";
    }

    /**
     * 进度记录页面
     * @param uuid 任务UUID
     * @param model 模型
     * @return 进度记录视图
     */
    @GetMapping("/{uuid}/progress")
    public String progressPage(@PathVariable String uuid, Model model) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            TaskEntity task = taskService.getTaskByUuid(uuid, userId);
            model.addAttribute("task", task);

            if (!model.containsAttribute("progressDTO")) {
                ProgressDTO progressDTO = new ProgressDTO();
                progressDTO.setProgress(task.getProgress());
                model.addAttribute("progressDTO", progressDTO);
            }

            return "task-progress";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/tasks";
        }
    }

    /**
     * 更新任务进度
     * @param uuid 任务UUID
     * @param progressDTO 进度信息
     * @param bindingResult 校验结果
     * @param redirectAttributes 重定向属性
     * @return 重定向地址
     */
    @PostMapping("/{uuid}/progress")
    public String updateProgress(@PathVariable String uuid,
                                @Valid @ModelAttribute ProgressDTO progressDTO,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getFieldErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("progressDTO", progressDTO);
            return "redirect:/tasks/" + uuid + "/progress";
        }

        try {
            taskService.updateProgress(uuid, progressDTO.getProgress(), progressDTO.getProgressLog(), userId);
            redirectAttributes.addFlashAttribute("successMessage", "进度更新成功");
            return "redirect:/tasks/" + uuid;
        } catch (Exception e) {
            log.error("更新任务进度失败", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("progressDTO", progressDTO);
            return "redirect:/tasks/" + uuid + "/progress";
        }
    }

    /**
     * 更新任务状态（API接口）
     * @param uuid 任务UUID
     * @param status 任务状态
     * @return 响应结果
     */
    @PostMapping("/{uuid}/status")
    @ResponseBody
    public AjaxResult updateStatus(@PathVariable String uuid,
                                  @RequestParam TaskStatus status) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return AjaxResult.error(401, "未登录");
        }

        try {
            TaskEntity task = taskService.updateStatus(uuid, status, userId);
            return AjaxResult.ok("状态更新成功", task);
        } catch (Exception e) {
            log.error("更新任务状态失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }
}

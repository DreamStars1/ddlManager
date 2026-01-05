package com.ddl.manager.domain.notification.service;

import com.ddl.manager.domain.notification.dto.DdlReminderMessage;
import com.ddl.manager.shared.util.DateTimeUtils;
import org.springframework.stereotype.Component;

/**
 * DDL 邮件内容构建器
 * 用于生成 DDL 提醒邮件的 HTML 内容
 * @author zhenghaipei
 * @since 2025-12-13
 */
@Component
public class DdlEmailContentBuilder {

    /**
     * 构建 DDL 提醒邮件的 HTML 内容
     *
     * @param message DDL 提醒消息
     * @return HTML 格式的邮件内容
     */
    public String buildReminderHtml(DdlReminderMessage message) {
        if (message == null) {
            return buildErrorHtml("消息内容为空");
        }

        String taskTitle = message.getTaskTitle() != null ? message.getTaskTitle() : "未命名任务";
        String taskDescription = message.getTaskDescription() != null ? 
                message.getTaskDescription() : "无描述";
        String deadline = DateTimeUtils.format(message.getDeadline(), "未设置");
        String priority = getPriorityLabel(message.getPriority());
        String priorityColor = getPriorityColor(message.getPriority());
        String hoursUntilDeadline = message.getHoursUntilDeadline() != null ? 
                String.valueOf(message.getHoursUntilDeadline()) : "未知";
        String status = getStatusLabel(message.getStatus());

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>DDL 提醒</title>\n" +
                "</head>\n" +
                "<body style=\"margin: 0; padding: 0; font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f5f5;\">\n" +
                "    <table role=\"presentation\" style=\"width: 100%; border-collapse: collapse;\">\n" +
                "        <tr>\n" +
                "            <td style=\"padding: 20px 0;\">\n" +
                "                <table role=\"presentation\" style=\"width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);\">\n" +
                "                    <!-- 头部 -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 30px 40px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 8px 8px 0 0;\">\n" +
                "                            <h1 style=\"margin: 0; color: #ffffff; font-size: 24px; font-weight: bold;\">\n" +
                "                                ⚠️ DDL 提醒通知\n" +
                "                            </h1>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <!-- 内容区域 -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 30px 40px;\">\n" +
                "                            <p style=\"margin: 0 0 20px 0; color: #333333; font-size: 16px; line-height: 1.6;\">\n" +
                "                                您好，\n" +
                "                            </p>\n" +
                "                            <p style=\"margin: 0 0 20px 0; color: #333333; font-size: 16px; line-height: 1.6;\">\n" +
                "                                您有一个即将到期的任务，请及时处理：\n" +
                "                            </p>\n" +
                "                            <!-- 任务信息卡片 -->\n" +
                "                            <div style=\"background-color: #f8f9fa; border-left: 4px solid " + priorityColor + "; padding: 20px; margin: 20px 0; border-radius: 4px;\">\n" +
                "                                <table role=\"presentation\" style=\"width: 100%; border-collapse: collapse;\">\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 5px 0;\">\n" +
                "                                            <strong style=\"color: #333333; font-size: 14px;\">任务名称：</strong>\n" +
                "                                            <span style=\"color: #666666; font-size: 14px;\">" + escapeHtml(taskTitle) + "</span>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 5px 0;\">\n" +
                "                                            <strong style=\"color: #333333; font-size: 14px;\">任务描述：</strong>\n" +
                "                                            <span style=\"color: #666666; font-size: 14px;\">" + escapeHtml(taskDescription) + "</span>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 5px 0;\">\n" +
                "                                            <strong style=\"color: #333333; font-size: 14px;\">截止时间：</strong>\n" +
                "                                            <span style=\"color: #ff6b6b; font-size: 14px; font-weight: bold;\">" + deadline + "</span>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 5px 0;\">\n" +
                "                                            <strong style=\"color: #333333; font-size: 14px;\">任务优先级：</strong>\n" +
                "                                            <span style=\"color: " + priorityColor + "; font-size: 14px; font-weight: bold;\">" + priority + "</span>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 5px 0;\">\n" +
                "                                            <strong style=\"color: #333333; font-size: 14px;\">任务状态：</strong>\n" +
                "                                            <span style=\"color: #666666; font-size: 14px;\">" + status + "</span>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                    <tr>\n" +
                "                                        <td style=\"padding: 5px 0;\">\n" +
                "                                            <strong style=\"color: #333333; font-size: 14px;\">距离截止：</strong>\n" +
                "                                            <span style=\"color: #ff6b6b; font-size: 14px; font-weight: bold;\">" + hoursUntilDeadline + " 小时</span>\n" +
                "                                        </td>\n" +
                "                                    </tr>\n" +
                "                                </table>\n" +
                "                            </div>\n" +
                "                            <!-- 提醒信息 -->\n" +
                "                            <div style=\"background-color: #fff3cd; border: 1px solid #ffc107; border-radius: 4px; padding: 15px; margin: 20px 0;\">\n" +
                "                                <p style=\"margin: 0; color: #856404; font-size: 14px; line-height: 1.6;\">\n" +
                "                                    <strong>⚠️ 重要提醒：</strong>任务将在 " + hoursUntilDeadline + " 小时后到期，请及时处理，避免逾期。\n" +
                "                                </p>\n" +
                "                            </div>\n" +
                "                            <p style=\"margin: 20px 0 0 0; color: #333333; font-size: 16px; line-height: 1.6;\">\n" +
                "                                祝您工作顺利！\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                    <!-- 底部 -->\n" +
                "                    <tr>\n" +
                "                        <td style=\"padding: 20px 40px; background-color: #f8f9fa; border-radius: 0 0 8px 8px; border-top: 1px solid #e9ecef;\">\n" +
                "                            <p style=\"margin: 0; color: #666666; font-size: 12px; text-align: center; line-height: 1.6;\">\n" +
                "                                此邮件由 DDL Manager 系统自动发送，请勿回复。<br>\n" +
                "                                如有疑问，请联系系统管理员。\n" +
                "                            </p>\n" +
                "                        </td>\n" +
                "                    </tr>\n" +
                "                </table>\n" +
                "            </td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 构建简单文本格式的 DDL 提醒内容
     *
     * @param message DDL 提醒消息
     * @return 纯文本格式的邮件内容
     */
    public String buildReminderText(DdlReminderMessage message) {
        if (message == null) {
            return "消息内容为空";
        }

        StringBuilder text = new StringBuilder();
        text.append("========== DDL 提醒 ==========\n\n");
        text.append("您好，\n\n");
        text.append("您有一个即将到期的任务，请及时处理：\n\n");
        text.append("任务名称：").append(message.getTaskTitle() != null ? message.getTaskTitle() : "未命名任务").append("\n");
        text.append("任务描述：").append(message.getTaskDescription() != null ? message.getTaskDescription() : "无描述").append("\n");
        text.append("截止时间：").append(DateTimeUtils.format(message.getDeadline(), "未设置")).append("\n");
        text.append("任务优先级：").append(getPriorityLabel(message.getPriority())).append("\n");
        text.append("任务状态：").append(getStatusLabel(message.getStatus())).append("\n");
        text.append("距离截止：").append(message.getHoursUntilDeadline() != null ? 
                message.getHoursUntilDeadline() + " 小时" : "未知").append("\n\n");
        text.append("⚠️ 重要提醒：任务将在 ").append(message.getHoursUntilDeadline() != null ? 
                message.getHoursUntilDeadline() : "未知").append(" 小时后到期，请及时处理，避免逾期。\n\n");
        text.append("祝您工作顺利！\n\n");
        text.append("此邮件由 DDL Manager 系统自动发送，请勿回复。\n");
        text.append("================================\n");

        return text.toString();
    }

    /**
     * 构建邮件主题
     *
     * @param message DDL 提醒消息
     * @return 邮件主题
     */
    public String buildSubject(DdlReminderMessage message) {
        if (message == null || message.getTaskTitle() == null) {
            return "DDL 提醒通知";
        }
        return "DDL 提醒: " + message.getTaskTitle();
    }

    /**
     * 获取优先级标签
     */
    private String getPriorityLabel(String priority) {
        if (priority == null) {
            return "未设置";
        }
        switch (priority.toUpperCase()) {
            case "HIGH":
                return "高";
            case "MEDIUM":
                return "中";
            case "LOW":
                return "低";
            default:
                return priority;
        }
    }

    /**
     * 获取优先级颜色
     */
    private String getPriorityColor(String priority) {
        if (priority == null) {
            return "#666666";
        }
        switch (priority.toUpperCase()) {
            case "HIGH":
                return "#ff6b6b";
            case "MEDIUM":
                return "#ffa726";
            case "LOW":
                return "#66bb6a";
            default:
                return "#666666";
        }
    }

    /**
     * 获取状态标签
     */
    private String getStatusLabel(String status) {
        if (status == null) {
            return "未设置";
        }
        switch (status.toUpperCase()) {
            case "TODO":
                return "待办";
            case "IN_PROGRESS":
                return "进行中";
            case "COMPLETED":
                return "已完成";
            case "CANCELED":
                return "已取消";
            default:
                return status;
        }
    }

    /**
     * HTML 转义（防止 XSS）
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 构建错误提示的 HTML
     */
    private String buildErrorHtml(String errorMessage) {
        return "<html><body><p style='color: red;'>错误: " + escapeHtml(errorMessage) + "</p></body></html>";
    }
}





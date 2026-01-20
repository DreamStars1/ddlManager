package com.ddl.manager.domain.auth.dto;

import lombok.Data;

/**
 * 会话统计数据DTO
 * 用于封装会话统计接口的返回数据
 */
@Data
public class SessionStatsDTO {
    /** 总用户数 */
    private Long totalUsers;
    /** 今日登录次数 */
    private Long todayLogin;
    /** 当前在线用户数 */
    private Long onlineUsers;
    /** 总会话数 */
    private Long totalSessions;
 }
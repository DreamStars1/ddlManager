package com.ddl.manager.domain.auth.service;

import com.ddl.manager.domain.auth.dto.RegisterDTO;
import com.ddl.manager.domain.auth.dto.LoginDTO;
import com.ddl.manager.domain.auth.dto.SessionStatsDTO;
import com.ddl.manager.domain.auth.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户认证服务接口
 * 定义登录、注册、登出核心业务逻辑
 */
public interface UserService {

    /**
     * 用户登录
     * @param loginDTO 登录凭证（用户名+密码）
     * @return 包含token、用户名、用户ID的登录结果
     * @throws Exception 登录失败时抛出异常（如用户名密码错误、用户不存在等）
     */
    Map<String, Object> login(LoginDTO loginDTO) throws Exception;

    /**
     * 用户注册
     * @param registerDTO 注册信息（用户名+邮箱+密码）
     * @throws Exception 注册失败时抛出异常（如用户名已存在、邮箱已注册等）
     */
    void register(RegisterDTO registerDTO, MultipartFile avatar) throws Exception;

    /**
     * 用户登出
     * @param request 请求对象（用于获取当前登录用户的token/session信息）
     * @throws Exception 登出失败时抛出异常
     */
    void logout(HttpServletRequest request) throws Exception;

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    UserEntity findByUsername(String username);

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户实体
     */
    UserEntity findByEmail(String email);

    boolean toggleUserStatus(Long userId);

    Page<UserEntity> getAllUsers(Pageable pageable);

    /**
     * 获取会话统计数据
     * @return 会话统计数据DTO
     */
    SessionStatsDTO getSessionStatistics();
}


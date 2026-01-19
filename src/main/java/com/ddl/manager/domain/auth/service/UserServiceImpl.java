package com.ddl.manager.domain.auth.service;

import com.ddl.manager.domain.auth.dto.LoginDTO;
import com.ddl.manager.domain.auth.dto.RegisterDTO;
import com.ddl.manager.domain.auth.dto.SessionStatsDTO;
import com.ddl.manager.domain.auth.model.RoleEntity;
import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.auth.repository.RoleRepository;
import com.ddl.manager.domain.auth.repository.UserRepository;
import com.ddl.manager.infrastructure.config.JwtTokenUtil;
import com.ddl.manager.infrastructure.exception.BusinessException;
import com.ddl.manager.infrastructure.service.RedisSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * @author zhenghaipei
 * @since 2025-12-14
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 注入JWT工具类（核心改造点）
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // Redis存储
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisSessionService redisSessionService;

    /**
     * 用户登录逻辑
     * 1. 校验用户名密码
     * 2. 生成JWT Token（使用JwtTokenUtil）
     * 3. 返回登录结果
     */
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) throws Exception {
        // 1. 校验入参
        if (!StringUtils.hasText(loginDTO.getUsername()) || !StringUtils.hasText(loginDTO.getPassword())) {
            throw new Exception("用户名或密码不能为空");
        }

        // 2. 查询用户
        UserEntity user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在: " + loginDTO.getUsername()));

        // 3. 判断是否禁用
        if (!user.getEnabled()) {
            throw new Exception("用户被禁用");
        }

        // 4. 校验密码（数据库存储加密后的密码，需匹配）
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new Exception("密码错误");
        }

        // 5. 生成JWT Token（核心改造：使用JwtTokenUtil生成）
        // 构建UserDetails对象（适配JwtTokenUtil的入参要求）
        UserDetails userDetails = buildUserDetails(user);
        String token = jwtTokenUtil.generateToken(userDetails);

        // 6. 构造返回结果（匹配控制器示例数据格式）
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        Set<RoleEntity> roleEntities = user.getRoles();
        for (RoleEntity roleEntitie : roleEntities) {
            if (roleEntitie.getCode().equals("ROLE_ADMIN")) {
                result.put("role", "ADMIN");
                break;
            }
        }
        result.putIfAbsent("role", "USER");
        result.put("roles", user.getRoles());
        result.put("avatar", user.getAvatar());

        return result;
    }

    /**
     * 构建UserDetails对象（适配JwtTokenUtil）
     */
    private UserDetails buildUserDetails(UserEntity user) {
        // 提取用户角色（转换为Spring Security的权限格式）
        Set<String> authorities = new HashSet<>();
        for (RoleEntity role : user.getRoles()) {
            authorities.add(role.getCode());
        }
        // 构建UserDetails（Spring Security标准用户详情对象）
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities.toArray(new String[0]))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }

    /**
     * 用户注册
     * @param registerDTO 注册信息
     */
    @Override
    @Transactional
    public void register(RegisterDTO registerDTO, MultipartFile avatar) throws Exception {
        // 1. 检查用户名是否已存在
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        // 3. 获取默认角色（ROLE_USER）
        RoleEntity userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new BusinessException("系统角色配置错误，请联系管理员"));

        // 4. 创建用户实体
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(userRole);

        UserEntity user = UserEntity.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .email(registerDTO.getEmail())
                .avatar(uploadAvatarFile(avatar, registerDTO.getUsername()))
                .enabled(true)
                .reminderHours(24)  // 默认24小时前提醒
                .emailNotificationEnabled(true)  // 默认开启邮件提醒
                .roles(roles)
                .build();

        // 5. 保存用户
        UserEntity savedUser = userRepository.save(user);
        log.info("用户注册成功: username={}, email={}", savedUser.getUsername(), savedUser.getEmail());
    }

    /**
     * 用户登出逻辑
     */
    @Override
    public void logout(HttpServletRequest request) throws Exception {
        // 1. 从请求头获取token（前端需在请求头携带token，如：Authorization: Bearer {token}）
        String token = extractTokenFromRequest(request);
        if (!StringUtils.hasText(token)) {
            throw new Exception("未获取到登录凭证");
        }

        // 验证token是否有效（新增：使用JwtTokenUtil验证）
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            UserEntity user = findByUsername(username);
            UserDetails userDetails = buildUserDetails(user);
            if (!jwtTokenUtil.validateToken(token, userDetails)) {
                throw new Exception("Token已失效");
            }
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            throw new Exception("无效的登录凭证");
        }

        // 设置token黑名单，过期时间与JWT过期时间一致（从JwtTokenUtil获取过期时间）
        // 注意：如果需要精准匹配过期时间，建议在JwtTokenUtil中新增获取过期时间的方法
        long jwtExpiration = jwtTokenUtil.getExpiration();
        redisTemplate.opsForValue().set("TOKEN_BLACKLIST:" + token, "1", jwtExpiration, TimeUnit.MILLISECONDS);
        log.info("用户登出成功，token已加入黑名单：{}", token);
    }

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    @Override
    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在: " + username));
    }

    /**
     * 根据邮箱查找用户
     * @param email 邮箱
     * @return 用户实体
     */
    @Override
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("用户不存在: " + email));
    }

    /**
     * 切换用户状态
     * @param userId 用户ID
     * @return true-启用，false-禁用
     */
    @Override
    public boolean toggleUserStatus(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在: " + userId));
        user.setEnabled(!user.getEnabled());
        userRepository.save(user);
        return user.getEnabled();
    }

    /**
     * 获取所有用户
     * @param pageable 分页参数
     * @return 用户实体分页对象
     */
    @Override
    public Page<UserEntity> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * 获取会话统计数据
     * @return 会话统计数据DTO
     */
    @Override
    @Transactional(readOnly = true)
    public SessionStatsDTO getSessionStatistics() {
        SessionStatsDTO stats = new SessionStatsDTO();

        // 1. 总用户数：优先Redis，无则查DB并更新Redis
        Long totalUsers = redisSessionService.getTotalUserCount();
        if (totalUsers == 0) {
            totalUsers = userRepository.count(); // 从数据库查询总用户数
            redisSessionService.updateTotalUserCount(totalUsers); // 更新到Redis
        }
        stats.setTotalUsers(totalUsers);

        // 2. 今日登录数：从Redis的今日登录Set中统计
        long todayLogin = redisSessionService.getTodayLoginCount();
        stats.setTodayLogin(todayLogin);

        // 3. 在线用户数：复用现有getOnlineUserCount方法（基于Session Key统计）
        long onlineUsers = redisSessionService.getOnlineUserCount();
        stats.setOnlineUsers(onlineUsers);

        log.info("会话统计数据（基于RedisSessionService）：总用户数={}, 今日登录={}, 在线用户数={}",
                totalUsers, todayLogin, onlineUsers);
        return stats;
    }

    /**
     * 从请求头提取token
     * 前端请求头格式：Authorization: Bearer {token}
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // 截取"Bearer "后的token部分
        }
        return null;
    }

    /**
     * 处理头像文件上传
     */
    public String uploadAvatarFile(MultipartFile avatar, String name) {
        // 1. 校验文件是否为空
        if (avatar == null || avatar.isEmpty()) {
            return null; // 或返回默认头像路径
        }

        // 2. 定义头像保存目录
        String projectRootPath = System.getProperty("user.dir"); // 项目根目录（对应你截图中的C:\Users\alphabeta\Desktop\ddlManager）
        String basePath = projectRootPath + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "images" + File.separator + "image" + File.separator;

        // 3. 生成唯一文件名（避免重名）
        String originalFilename = avatar.getOriginalFilename();
        String suffix = null;
        if (originalFilename != null) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = name + suffix;

        // 4. 创建File对象，检查并创建父目录
        File targetFile = new File(basePath + fileName);
        File parentDir = targetFile.getParentFile(); // 获取父目录对象

        // 关键：如果父目录不存在，递归创建所有多级目录
        if (!parentDir.exists()) {
            parentDir.mkdirs(); // mkdirs()：创建多级目录；mkdir()：仅创建单级目录，此处必须用mkdirs()
        }

        // 5. 保存文件（此时父目录已存在，不会报错）
        try {
            avatar.transferTo(targetFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 6. 返回文件访问路径（根据你的业务需求调整）
        return "/images/image/" + fileName;
    }
}
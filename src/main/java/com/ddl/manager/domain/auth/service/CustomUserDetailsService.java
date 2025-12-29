package com.ddl.manager.domain.auth.service;

import com.ddl.manager.domain.auth.model.SecurityUser;
import com.ddl.manager.domain.auth.model.UserEntity;
import com.ddl.manager.domain.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义用户详情服务
 * @author developer
 * @since 2025-12-13
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 根据用户名加载用户
     * @param username 用户名
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        return new SecurityUser(user);
    }
}

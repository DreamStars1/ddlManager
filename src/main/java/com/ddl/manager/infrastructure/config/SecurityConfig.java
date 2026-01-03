package com.ddl.manager.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * Spring Security配置类
 * @author developer
 * @since 2025-12-13
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // 启用方法级权限控制
public class SecurityConfig {

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();

        // 会话管理配置
        http.sessionManagement()
                .maximumSessions(1) // 同一用户最多1个会话
                .maxSessionsPreventsLogin(false) // 不阻止新登录，踢掉旧会话
                .expiredUrl("/login?expired"); // 会话过期跳转

        http.exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler);

        http.authorizeRequests()
                .antMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
                .antMatchers("/api/test/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN") // 管理员权限
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("JSESSIONID") // 清除Cookie
                .invalidateHttpSession(true) // 使Session失效
                .permitAll();

        return http.build();
    }

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
package com.ddl.manager.infrastructure.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

/**
 * Spring Security配置类 - 增强Redis Session管理
 * @author developer
 * @since 2025-12-13
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    private RedisIndexedSessionRepository sessionRepository;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 构造器注入
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Session注册表，用于管理分布式Session
     */
    @Bean
    public SpringSessionBackedSessionRegistry<?> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(sessionRepository);
    }

    /**
     * 安全过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf().disable();
//
//        // 会话管理配置 - 增强Redis支持
//        http.sessionManagement()
//                .maximumSessions(1) // 同一用户最多1个会话
//                .sessionRegistry(sessionRegistry()) // 使用Redis Session注册表
//                .maxSessionsPreventsLogin(false) // 不阻止新登录，踢掉旧会话
//                .expiredUrl("/login?expired") // 会话过期跳转
//                .and()
//                .sessionFixation().migrateSession() // Session固定保护
//                .invalidSessionUrl("/login?invalid"); // 无效Session跳转
//
//        http.exceptionHandling()
//                .accessDeniedHandler(accessDeniedHandler);
//
//        http.authorizeRequests()
//                .antMatchers("/register", "/login", "/css/**", "/js/**", "/images/**").permitAll()
//                .antMatchers("/api/test/**", "/api/session/**").permitAll()
//                .antMatchers("/admin/**").hasRole("ADMIN")
//                .anyRequest().authenticated()
//                .and()
//                .formLogin()
//                .loginPage("/login")
//                .loginProcessingUrl("/login")
//                .defaultSuccessUrl("/dashboard", true)
//                .failureUrl("/login?error=true")
//                .permitAll()
//                .and()
//                .logout()
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/login?logout=true")
//                .deleteCookies("JSESSIONID", "DDL_SESSION") // 清除Cookie
//                .invalidateHttpSession(true) // 使Session失效
//                .clearAuthentication(true) // 清除认证信息
//                .permitAll();
//
//        return http.build();
        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/static/**", "/templates/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // 核心：添加JWT过滤器，在用户名密码过滤器之前执行
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
package com.ddl.manager.infrastructure.config;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 自定义JWT过滤器：每次请求解析Token，完成认证
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil; // 自己实现JWT工具类（生成/验证Token）
    private final UserDetailsService userDetailsService; // 用户信息服务

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头提取Token（格式：Authorization: Bearer <token>）
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            // 2. 解析Token获取用户名（需自己实现JwtTokenUtil）
            username = jwtTokenUtil.getUsernameFromToken(token);
        }

        // 3. 验证Token有效性，并设置认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 查询用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 验证Token是否有效
            if (jwtTokenUtil.validateToken(token, userDetails)) {
                // 生成认证对象，存入SecurityContext
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 4. 继续执行过滤链
        filterChain.doFilter(request, response);
    }
}
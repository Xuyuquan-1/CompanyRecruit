package com.hrrecruit.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 每个请求进来时，从 Header 中取出 Token 进行验证，验证通过则把用户信息放入 SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. 从请求头中取出 Token
        String token = resolveToken(request);

        // 调试日志：打印实际收到的 token
        if (StringUtils.hasText(token)) {
            log.debug("收到的JWT Token: [{}], 长度: {}", token, token.length());
        }

        // 2. 验证 Token 是否有效
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 3. 从 Token 解析出用户名
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // 4. 从数据库加载用户信息（含权限）
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. 创建认证对象并放入 SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 6. 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Bearer Token
     * 格式：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // 截取 "Bearer " 后面的部分
        }
        return null;
    }
}

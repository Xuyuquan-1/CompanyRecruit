package com.hrrecruit.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 前端开发服务器（如 localhost:5173）访问后端（localhost:8080）时需要跨域
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许前端开发地址（Vite 默认端口 5173）
        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);          // 允许携带 Cookie
        config.addAllowedMethod("*");              // 允许所有 HTTP 方法
        config.addAllowedHeader("*");              // 允许所有请求头
        config.addExposedHeader("Authorization");  // 暴露 Authorization 头给前端读取

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}

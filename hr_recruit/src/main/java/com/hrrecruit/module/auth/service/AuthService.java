package com.hrrecruit.module.auth.service;

import com.hrrecruit.module.auth.dto.LoginRequest;
import com.hrrecruit.module.auth.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String token);

    /**
     * 获取当前登录用户信息
     */
    LoginResponse.UserInfo getCurrentUser();
}
package com.hrrecruit.module.auth.controller;

import com.hrrecruit.common.Result;
import com.hrrecruit.module.auth.dto.LoginRequest;
import com.hrrecruit.module.auth.dto.LoginResponse;
import com.hrrecruit.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        return Result.success(loginResponse);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refreshToken(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        LoginResponse loginResponse = authService.refreshToken(token);
        return Result.success(loginResponse);
    }

    @GetMapping("/info")
    public Result<LoginResponse.UserInfo> getUserInfo() {
        LoginResponse.UserInfo userInfo = authService.getCurrentUser();
        return Result.success(userInfo);
    }
}
package com.hrrecruit.module.auth.service.impl;

import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.SysMenu;
import com.hrrecruit.entity.SysUser;
import com.hrrecruit.module.auth.dto.LoginRequest;
import com.hrrecruit.module.auth.dto.LoginResponse;
import com.hrrecruit.module.auth.service.AuthService;
import com.hrrecruit.security.JwtTokenProvider;
import com.hrrecruit.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("登录请求 - 用户名: [{}], 密码长度: {}", request.getUsername(),
                request.getPassword() != null ? request.getPassword().length() : 0);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.error("认证失败 - 用户名: [{}], 原因: {}", request.getUsername(), e.getMessage());
            throw e;
        }

        log.info("认证成功 - 用户名: [{}]", request.getUsername());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser user = loginUser.getSysUser();

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(604800L);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setRoles(loginUser.getRoleCodes());
        userInfo.setPermissions(loginUser.getPermissions());
        userInfo.setMenus(convertMenus(loginUser.getMenus()));

        response.setUserInfo(userInfo);

        return response;
    }

    private List<LoginResponse.MenuVO> convertMenus(List<SysMenu> menus) {
        return menus.stream().map(m -> {
            LoginResponse.MenuVO vo = new LoginResponse.MenuVO();
            vo.setId(m.getId());
            vo.setParentId(m.getParentId());
            vo.setName(m.getName());
            vo.setPermission(m.getPermission());
            vo.setType(m.getType());
            vo.setPath(m.getPath());
            vo.setIcon(m.getIcon());
            vo.setSortOrder(m.getSortOrder());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    @Override
    public LoginResponse refreshToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BusinessException("Token无效或已过期");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String newToken = jwtTokenProvider.generateToken(userId, username);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(newToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(604800L);

        return response;
    }

    @Override
    public LoginResponse.UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser user = loginUser.getSysUser();

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setRoles(loginUser.getRoleCodes());
        userInfo.setPermissions(loginUser.getPermissions());
        userInfo.setMenus(convertMenus(loginUser.getMenus()));

        return userInfo;
    }
}
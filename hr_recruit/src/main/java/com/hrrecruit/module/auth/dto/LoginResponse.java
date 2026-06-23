package com.hrrecruit.module.auth.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 登录响应数据
 */
@Data
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String avatar;
        private String phone;
        private String email;
        private Set<String> roles;
        private Set<String> permissions;
        private List<MenuVO> menus;
    }

    @Data
    public static class MenuVO {
        private Long id;
        private Long parentId;
        private String name;
        private String permission;
        private Integer type;
        private String path;
        private String icon;
        private Integer sortOrder;
    }
}
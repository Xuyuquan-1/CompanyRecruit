package com.hrrecruit.common.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限工具类
 */
public class PermissionUtils {

    /**
     * 检查用户是否拥有指定角色
     */
    public static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) return false;
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    /**
     * 检查用户是否是讲师
     */
    public static boolean isTeacher(Authentication authentication) {
        return hasRole(authentication, "teacher") || hasRole(authentication, "admin") 
                || hasRole(authentication, "super_admin");
    }

    /**
     * 检查用户是否是管理员
     */
    public static boolean isAdmin(Authentication authentication) {
        return hasRole(authentication, "admin") || hasRole(authentication, "super_admin");
    }

    /**
     * 检查用户是否是学员
     */
    public static boolean isStudent(Authentication authentication) {
        return hasRole(authentication, "student");
    }

    /**
     * 获取用户的所有角色
     */
    public static Set<String> getUserRoles(Authentication authentication) {
        if (authentication == null) return Set.of();
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5))
                .collect(Collectors.toSet());
    }
}

package com.hrrecruit.security;

import com.hrrecruit.entity.SysMenu;
import com.hrrecruit.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 的用户对象
 * 包装了 SysUser 实体，实现了 UserDetails 接口，用于认证和授权
 */
@Data
public class LoginUser implements UserDetails {

    /** 原始用户实体 */
    private SysUser sysUser;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 用户状态 */
    private Integer status;

    /** 角色编码列表，如 ["ROLE_ADMIN", "ROLE_RECRUITER"] */
    private Set<String> roleCodes;

    /** 权限标识列表，如 ["job:list", "job:add"] */
    private Set<String> permissions;

    /** 菜单列表（用于返回给前端渲染侧边栏） */
    private List<SysMenu> menus;

    public LoginUser(SysUser user, Set<String> roleCodes, Set<String> permissions, List<SysMenu> menus) {
        this.sysUser = user;
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.realName = user.getRealName();
        this.status = user.getStatus();
        this.roleCodes = roleCodes;
        this.permissions = permissions;
        this.menus = menus;
    }

    /**
     * 将角色编码和权限标识合并成 Spring Security 需要的权限列表
     * 例如：[ROLE_ADMIN, job:list, job:add, ...]
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 角色权限（带 ROLE_ 前缀）
        List<GrantedAuthority> authorities = roleCodes.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        // 菜单/按钮权限
        permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否启用（status=1 为启用）
     */
    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
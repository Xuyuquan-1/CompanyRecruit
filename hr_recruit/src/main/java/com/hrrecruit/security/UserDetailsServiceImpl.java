package com.hrrecruit.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrrecruit.entity.SysMenu;
import com.hrrecruit.entity.SysRole;
import com.hrrecruit.entity.SysUser;
import com.hrrecruit.mapper.SysMenuMapper;
import com.hrrecruit.mapper.SysRoleMapper;
import com.hrrecruit.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户认证服务
 * Spring Security 登录时调用此类从数据库加载用户信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("加载用户信息 - 用户名: [{}]", username);

        // 1. 根据用户名查询用户（只查启用且未删除的）
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getStatus, 1)
                        .eq(SysUser::getDeleted, 0)
        );
        if (user == null) {
            log.warn("用户未找到或已被禁用/删除 - 用户名: [{}]", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        log.info("用户已找到 - ID: {}, 用户名: {}, 状态: {}, 密码密文前20位: {}",
                user.getId(), user.getUsername(), user.getStatus(),
                user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) : "null");

        // 2. 查询用户的角色列表
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(user.getId());
        Set<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());

        // 3. 查询用户的权限标识列表
        List<String> permissionList = sysMenuMapper.selectPermissionsByUserId(user.getId());
        Set<String> permissions = permissionList.stream().collect(Collectors.toSet());

        // 4. 查询用户的菜单列表（用于返回给前端渲染侧边栏）
        List<SysMenu> menus = sysMenuMapper.selectMenusByUserId(user.getId());

        // 5. 组装成 LoginUser 对象
        return new LoginUser(user, roleCodes, permissions, menus);
    }
}
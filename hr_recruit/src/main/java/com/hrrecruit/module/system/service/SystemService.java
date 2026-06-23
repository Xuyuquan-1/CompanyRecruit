package com.hrrecruit.module.system.service;

import com.hrrecruit.entity.SysMenu;
import com.hrrecruit.entity.SysRole;
import com.hrrecruit.entity.SysUser;

import java.util.List;

/**
 * 系统管理服务接口
 */
public interface SystemService {

    // ========== 用户管理 ==========
    List<SysUser> getUserList();
    SysUser getUserById(Long id);
    void addUser(SysUser user);
    void updateUser(Long id, SysUser user);
    void deleteUser(Long id);

    // ========== 角色管理 ==========
    List<SysRole> getRoleList();
    SysRole getRoleById(Long id);
    void addRole(SysRole role);
    void updateRole(Long id, SysRole role);
    void deleteRole(Long id);
    void assignRoleMenus(Long roleId, List<Long> menuIds);

    // ========== 菜单管理 ==========
    List<SysMenu> getMenuList();
    SysMenu getMenuById(Long id);
    void addMenu(SysMenu menu);
    void updateMenu(Long id, SysMenu menu);
    void deleteMenu(Long id);
    List<Long> getRoleMenuIds(Long roleId);
}
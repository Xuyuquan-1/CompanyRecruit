package com.hrrecruit.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrrecruit.common.exception.BusinessException;
import com.hrrecruit.entity.SysMenu;
import com.hrrecruit.entity.SysRole;
import com.hrrecruit.entity.SysRoleMenu;
import com.hrrecruit.entity.SysUser;
import com.hrrecruit.mapper.SysMenuMapper;
import com.hrrecruit.mapper.SysRoleMapper;
import com.hrrecruit.mapper.SysRoleMenuMapper;
import com.hrrecruit.mapper.SysUserMapper;
import com.hrrecruit.module.system.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统管理服务实现
 */
@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<SysUser> getUserList() {
        return sysUserMapper.selectList(null);
    }

    @Override
    public SysUser getUserById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public void addUser(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sysUserMapper.insert(user);
    }

    @Override
    public void updateUser(Long id, SysUser user) {
        SysUser existUser = getUserById(id);
        existUser.setRealName(user.getRealName());
        existUser.setPhone(user.getPhone());
        existUser.setEmail(user.getEmail());
        existUser.setStatus(user.getStatus());
        sysUserMapper.updateById(existUser);
    }

    @Override
    public void deleteUser(Long id) {
        getUserById(id);
        sysUserMapper.deleteById(id);
    }

    @Override
    public List<SysRole> getRoleList() {
        return sysRoleMapper.selectList(null);
    }

    @Override
    public SysRole getRoleById(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    @Override
    public void addRole(SysRole role) {
        sysRoleMapper.insert(role);
    }

    @Override
    public void updateRole(Long id, SysRole role) {
        SysRole existRole = getRoleById(id);
        existRole.setRoleName(role.getRoleName());
        existRole.setDescription(role.getDescription());
        existRole.setStatus(role.getStatus());
        sysRoleMapper.updateById(existRole);
    }

    @Override
    public void deleteRole(Long id) {
        getRoleById(id);
        sysRoleMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void assignRoleMenus(Long roleId, List<Long> menuIds) {
        getRoleById(roleId);
        sysRoleMenuMapper.delete(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId)
        );
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            sysRoleMenuMapper.insert(roleMenu);
        }
    }

    @Override
    public List<SysMenu> getMenuList() {
        return sysMenuMapper.selectList(null);
    }

    @Override
    public SysMenu getMenuById(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    @Override
    public void addMenu(SysMenu menu) {
        sysMenuMapper.insert(menu);
    }

    @Override
    public void updateMenu(Long id, SysMenu menu) {
        SysMenu exist = getMenuById(id);
        exist.setName(menu.getName());
        exist.setParentId(menu.getParentId());
        exist.setPermission(menu.getPermission());
        exist.setType(menu.getType());
        exist.setPath(menu.getPath());
        exist.setIcon(menu.getIcon());
        exist.setSortOrder(menu.getSortOrder());
        exist.setStatus(menu.getStatus());
        sysMenuMapper.updateById(exist);
    }

    @Override
    public void deleteMenu(Long id) {
        getMenuById(id);
        sysMenuMapper.deleteById(id);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return sysRoleMenuMapper.selectObjs(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
                        .select(SysRoleMenu::getMenuId)
        );
    }
}
package com.hrrecruit.module.system.controller;

import com.hrrecruit.common.Result;
import com.hrrecruit.entity.SysMenu;
import com.hrrecruit.entity.SysRole;
import com.hrrecruit.entity.SysUser;
import com.hrrecruit.module.system.service.SystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统管理控制器
 */
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemService systemService;

    // ========== 用户管理 ==========

    @GetMapping("/user/list")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<List<SysUser>> getUserList() {
        return Result.success(systemService.getUserList());
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<SysUser> getUserById(@PathVariable Long id) {
        return Result.success(systemService.getUserById(id));
    }

    @PostMapping("/user")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<Void> addUser(@RequestBody SysUser user) {
        systemService.addUser(user);
        return Result.successMsg("用户创建成功");
    }

    @PutMapping("/user/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody SysUser user) {
        systemService.updateUser(id, user);
        return Result.successMsg("用户更新成功");
    }

    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        systemService.deleteUser(id);
        return Result.successMsg("用户删除成功");
    }

    // ========== 角色管理 ==========

    @GetMapping("/role/list")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<List<SysRole>> getRoleList() {
        return Result.success(systemService.getRoleList());
    }

    @GetMapping("/role/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<SysRole> getRoleById(@PathVariable Long id) {
        return Result.success(systemService.getRoleById(id));
    }

    @PostMapping("/role")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<Void> addRole(@RequestBody SysRole role) {
        systemService.addRole(role);
        return Result.successMsg("角色创建成功");
    }

    @PutMapping("/role/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<Void> updateRole(@PathVariable Long id, @RequestBody SysRole role) {
        systemService.updateRole(id, role);
        return Result.successMsg("角色更新成功");
    }

    @DeleteMapping("/role/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<Void> deleteRole(@PathVariable Long id) {
        systemService.deleteRole(id);
        return Result.successMsg("角色删除成功");
    }

    @PostMapping("/role/{id}/assign-menus")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<Void> assignRoleMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        systemService.assignRoleMenus(id, params.get("menuIds"));
        return Result.successMsg("权限分配成功");
    }

    // ========== 菜单管理 ==========

    @GetMapping("/menu/list")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<List<SysMenu>> getMenuList() {
        return Result.success(systemService.getMenuList());
    }

    @GetMapping("/menu/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<SysMenu> getMenuById(@PathVariable Long id) {
        return Result.success(systemService.getMenuById(id));
    }

    @PostMapping("/menu")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<Void> addMenu(@RequestBody SysMenu menu) {
        systemService.addMenu(menu);
        return Result.successMsg("菜单创建成功");
    }

    @PutMapping("/menu/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<Void> updateMenu(@PathVariable Long id, @RequestBody SysMenu menu) {
        systemService.updateMenu(id, menu);
        return Result.successMsg("菜单更新成功");
    }

    @DeleteMapping("/menu/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<Void> deleteMenu(@PathVariable Long id) {
        systemService.deleteMenu(id);
        return Result.successMsg("菜单删除成功");
    }

    // ========== 角色菜单关联 ==========

    @GetMapping("/role/{id}/menu-ids")
    @PreAuthorize("hasAuthority('system:role')")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        return Result.success(systemService.getRoleMenuIds(id));
    }
}
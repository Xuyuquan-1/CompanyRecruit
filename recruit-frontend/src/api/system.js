import request from '../utils/request'

// 用户管理
export function getUserList() { return request({ url: '/api/system/user/list', method: 'get' }) }
export function getUserById(id) { return request({ url: `/api/system/user/${id}`, method: 'get' }) }
export function addUser(data) { return request({ url: '/api/system/user', method: 'post', data }) }
export function updateUser(id, data) { return request({ url: `/api/system/user/${id}`, method: 'put', data }) }
export function deleteUser(id) { return request({ url: `/api/system/user/${id}`, method: 'delete' }) }

// 角色管理
export function getRoleList() { return request({ url: '/api/system/role/list', method: 'get' }) }
export function getRoleById(id) { return request({ url: `/api/system/role/${id}`, method: 'get' }) }
export function addRole(data) { return request({ url: '/api/system/role', method: 'post', data }) }
export function updateRole(id, data) { return request({ url: `/api/system/role/${id}`, method: 'put', data }) }
export function deleteRole(id) { return request({ url: `/api/system/role/${id}`, method: 'delete' }) }
export function assignRoleMenus(roleId, menuIds) { return request({ url: `/api/system/role/${roleId}/assign-menus`, method: 'post', data: { menuIds } }) }

// 菜单管理
export function getMenuList() { return request({ url: '/api/system/menu/list', method: 'get' }) }
export function addMenu(data) { return request({ url: '/api/system/menu', method: 'post', data }) }
export function updateMenu(id, data) { return request({ url: `/api/system/menu/${id}`, method: 'put', data }) }
export function deleteMenu(id) { return request({ url: `/api/system/menu/${id}`, method: 'delete' }) }
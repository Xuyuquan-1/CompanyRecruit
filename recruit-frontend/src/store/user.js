import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api/auth'
import { ref } from 'vue'

/**
 * 用户状态管理
 * 管理 Token、用户信息、权限、菜单等全局状态
 */
export const useUserStore = defineStore('user', () => {
  // ============ 状态 ============
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(null)
  const username = ref('')
  const realName = ref('')
  const roles = ref([])       // 角色编码数组
  const permissions = ref([])  // 权限标识数组
  const menus = ref([])        // 菜单列表

  // ============ 操作 ============

  /**
   * 登录
   */
  async function login(loginForm) {
    const res = await loginApi(loginForm)
    const data = res.data

    // 保存 Token（后端字段名为 accessToken）
    token.value = data.accessToken
    localStorage.setItem('token', data.accessToken)

    // 保存用户信息（后端将用户信息嵌套在 userInfo 对象中）
    const userInfo = data.userInfo
    userId.value = userInfo.id
    username.value = userInfo.username
    realName.value = userInfo.realName
    roles.value = userInfo.roles || []
    permissions.value = userInfo.permissions || []
    menus.value = userInfo.menus || []

    return data
  }

  /**
   * 获取用户信息（刷新页面后重新获取）
   */
  async function getInfo() {
    const res = await getUserInfoApi()
    const data = res.data

    userId.value = data.id
    username.value = data.username
    realName.value = data.realName
    roles.value = data.roles || []
    permissions.value = data.permissions || []
    menus.value = data.menus || []

    return data
  }

  /**
   * 退出登录
   */
  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    realName.value = ''
    roles.value = []
    permissions.value = []
    menus.value = []
    localStorage.removeItem('token')
  }

  /**
   * 检查是否拥有某个权限
   */
  function hasPermission(permission) {
    return permissions.value.includes(permission)
  }

  /**
   * 检查是否拥有某个角色
   */
  function hasRole(role) {
    return roles.value.includes(role)
  }

  /**
   * 是否为管理员
   */
  function isAdmin() {
    return roles.value.includes('ROLE_ADMIN')
  }

  /**
   * 是否为应聘者
   */
  function isCandidate() {
    return roles.value.includes('ROLE_CANDIDATE')
  }

  return {
    token, userId, username, realName, roles, permissions, menus,
    login, getInfo, logout, hasPermission, hasRole, isAdmin, isCandidate
  }
})
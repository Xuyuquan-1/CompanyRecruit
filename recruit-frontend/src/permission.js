import router from './router'
import { useUserStore } from './store/user'
import { ElMessage } from 'element-plus'

/**
 * 全局路由守卫
 * 控制页面访问权限：未登录跳转登录页，已登录获取用户信息
 */

// 不需要登录就能访问的白名单路径
const whiteList = ['/login']

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 招聘管理系统` : '招聘管理系统'

  if (userStore.token) {
    // 已登录
    if (to.path === '/login') {
      // 已登录状态下访问登录页，重定向到首页
      next('/')
    } else {
      // 如果没有用户信息，说明是刷新页面，需要重新获取
      if (!userStore.username) {
        try {
          await userStore.getInfo()
          next()
        } catch (error) {
          // 获取用户信息失败（可能Token过期），清除Token并跳转登录
          userStore.logout()
          next(`/login?redirect=${to.path}`)
        }
      } else {
        // 检查当前路由及父路由是否需要权限
        const needsPermission = to.matched.some(route => route.meta?.permission && !userStore.hasPermission(route.meta.permission))
        if (needsPermission) {
          next('/dashboard') // 没有权限，跳转到首页
        } else {
          // 检查角色限制
          const allowedRoles = to.matched.find(route => route.meta?.roles)?.meta?.roles
          if (allowedRoles && allowedRoles.length > 0) {
            const hasRole = allowedRoles.some(role => userStore.hasRole(role))
            if (!hasRole) {
              ElMessage.warning('您没有权限访问此页面')
              next('/dashboard') // 没有对应角色，跳转到首页
              return
            }
          }
          next()
        }
      }
    }
  } else {
    // 未登录
    if (whiteList.includes(to.path)) {
      next()
    } else {
      // 重定向到登录页，并携带原始路径作为参数
      next(`/login?redirect=${to.path}`)
    }
  }
})

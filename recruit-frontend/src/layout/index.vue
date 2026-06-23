<template>
  <el-container class="layout-container">
    <!-- 左侧菜单栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <h2 v-show="!isCollapse">招聘管理系统</h2>
        <h2 v-show="isCollapse">招</h2>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        :collapse="isCollapse"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        :unique-opened="true"
      >
        <template v-for="route in menuRoutes" :key="route.path">
          <!-- 只有一个子菜单时，直接显示子菜单 -->
          <el-menu-item
            v-if="route.children && route.children.length === 1"
            :index="route.path + '/' + route.children[0].path"
          >
            <el-icon><component :is="route.children[0].meta?.icon || route.meta?.icon" /></el-icon>
            <span>{{ route.children[0].meta?.title || route.meta?.title }}</span>
          </el-menu-item>

          <!-- 有多个子菜单时，显示为子菜单组 -->
          <el-sub-menu
            v-else-if="route.children && route.children.length > 1"
            :index="route.path"
          >
            <template #title>
              <el-icon><component :is="route.meta?.icon" /></el-icon>
              <span>{{ route.meta?.title }}</span>
            </template>
            <el-menu-item
              v-for="child in route.children"
              :key="child.path"
              :index="route.path + '/' + child.path"
            >
              <el-icon><component :is="child.meta?.icon" /></el-icon>
              <span>{{ child.meta?.title }}</span>
            </el-menu-item>
          </el-sub-menu>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
            >
              {{ item.meta?.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ userStore.realName || userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="dashboard">首页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'
import { UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

// 从路由配置中筛选用户有权限的菜单项
const menuRoutes = computed(() => {
  const allRoutes = router.options.routes.filter(r => r.path !== '/login' && r.children)
  return allRoutes.map(route => {
    // 检查路由级别的权限
    if (route.meta?.permission && !userStore.hasPermission(route.meta.permission)) {
      return null
    }
    // 检查角色限制
    if (route.meta?.roles && route.meta.roles.length > 0) {
      const hasRole = route.meta.roles.some(role => userStore.hasRole(role))
      if (!hasRole) {
        return null
      }
    }
    // 过滤子路由
    const visibleChildren = (route.children || []).filter(child => {
      if (!child.meta?.permission) return true
      return userStore.hasPermission(child.meta.permission)
    })
    if (visibleChildren.length === 0) return null
    return { ...route, children: visibleChildren }
  }).filter(Boolean)
})

// 面包屑导航
const breadcrumbs = computed(() => {
  return route.matched.filter(item => item.meta?.title)
})

// 下拉菜单操作
function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'dashboard') {
    router.push('/dashboard')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background-color: #263445;
}

.logo h2 {
  font-size: 16px;
  white-space: nowrap;
  margin: 0;
}

.el-menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
}

.username {
  font-size: 14px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
}
</style>

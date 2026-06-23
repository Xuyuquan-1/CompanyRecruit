<template>
  <div class="dashboard">
    <h2>欢迎使用人力资源招聘管理系统</h2>
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>岗位总数</template>
          <div class="stat-number">{{ stats.jobCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>简历总数</template>
          <div class="stat-number">{{ stats.resumeCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>面试安排</template>
          <div class="stat-number">{{ stats.interviewCount }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>已录用</template>
          <div class="stat-number">{{ stats.hiredCount }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>系统说明</template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="技术栈">Spring Boot 3 + MyBatis-Plus + Spring Security + JWT + Vue3 + Element Plus</el-descriptions-item>
        <el-descriptions-item label="功能模块">岗位管理、简历管理、应聘筛选、面试管理、录用管理、统计分析、AI助手</el-descriptions-item>
        <el-descriptions-item label="当前用户">{{ userStore.realName }}（{{ userStore.username }}）</el-descriptions-item>
        <el-descriptions-item label="用户角色">{{ userStore.roles.join(', ') }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { getProgressStats } from '../../api/report'

const userStore = useUserStore()
const loading = ref(false)
const stats = reactive({ jobCount: '-', resumeCount: '-', interviewCount: '-', hiredCount: '-' })

async function loadStats() {
  // 没有报表权限的用户不加载统计数据（如应聘者）
  if (!userStore.hasPermission('report:list')) {
    return
  }
  loading.value = true
  try {
    const res = await getProgressStats()
    const d = res.data || {}
    stats.jobCount = d.totalJobs ?? d.jobCount ?? '-'
    stats.resumeCount = d.totalResumes ?? d.resumeCount ?? '-'
    stats.interviewCount = d.totalInterviews ?? d.interviewCount ?? '-'
    stats.hiredCount = d.hiredCount ?? '-'
  } catch (err) {
    console.error('首页数据加载失败:', err)
    stats.jobCount = '-'
    stats.resumeCount = '-'
    stats.interviewCount = '-'
    stats.hiredCount = '-'
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)
</script>

<style scoped>
.dashboard h2 {
  color: #333;
}
.stat-number {
  font-size: 36px;
  font-weight: bold;
  text-align: center;
  color: #409EFF;
}
</style>
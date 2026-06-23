<template>
  <div class="dashboard">
    <!-- 管理员/HR首页 -->
    <template v-if="userStore.isAdmin() || userStore.hasRole('ROLE_HR')">
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
        <template #header>系统信息</template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="当前用户">{{ userStore.realName }}（{{ userStore.username }}）</el-descriptions-item>
          <el-descriptions-item label="用户角色">{{ userStore.roles.join(', ') }}</el-descriptions-item>
          <el-descriptions-item label="功能模块">岗位管理、简历管理、应聘筛选、面试管理、录用管理、统计分析、AI助手</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>

    <!-- 应聘者首页 -->
    <template v-else-if="userStore.isCandidate()">
      <h2>欢迎使用招聘系统</h2>
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="8">
          <el-card shadow="hover" class="quick-action-card">
            <template #header>
              <el-icon><Briefcase /></el-icon>
              <span style="margin-left: 8px">浏览岗位</span>
            </template>
            <p>查看最新招聘岗位</p>
            <el-button type="primary" @click="router.push('/candidate/jobs/index')" style="width: 100%">
              立即查看
            </el-button>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="quick-action-card">
            <template #header>
              <el-icon><Document /></el-icon>
              <span style="margin-left: 8px">我的应聘</span>
            </template>
            <p>查看应聘进度</p>
            <el-button type="success" @click="router.push('/candidate/applications/index')" style="width: 100%">
              查看详情
            </el-button>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="quick-action-card">
            <template #header>
              <el-icon><ChatDotSquare /></el-icon>
              <span style="margin-left: 8px">我的面试</span>
            </template>
            <p>查看面试安排</p>
            <el-button type="warning" @click="router.push('/candidate/interviews/index')" style="width: 100%">
              查看安排
            </el-button>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px">
        <template #header>个人信息</template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="姓名">{{ userStore.realName }}（{{ userStore.username }}）</el-descriptions-item>
          <el-descriptions-item label="身份">求职者</el-descriptions-item>
          <el-descriptions-item label="提示">请及时查看岗位信息，保持通讯畅通</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>

    <!-- 其他角色默认首页 -->
    <template v-else>
      <h2>欢迎使用招聘管理系统</h2>
      <el-card style="margin-top: 20px">
        <template #header>用户信息</template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="当前用户">{{ userStore.realName }}（{{ userStore.username }}）</el-descriptions-item>
          <el-descriptions-item label="用户角色">{{ userStore.roles.join(', ') }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { getRecruitmentProgress } from '../../api/report'
import { Briefcase, Document, ChatDotSquare } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const stats = reactive({ jobCount: '-', resumeCount: '-', interviewCount: '-', hiredCount: '-' })

async function loadStats() {
  // 只有管理员和HR才加载统计数据
  if (!userStore.isAdmin() && !userStore.hasRole('ROLE_HR')) {
    return
  }
  loading.value = true
  try {
    const res = await getRecruitmentProgress()
    const d = res.data?.summary || {}
    stats.jobCount = d.totalJobs ?? '-'
    stats.resumeCount = d.totalApplications ?? '-'
    stats.interviewCount = d.totalInterviews ?? '-'
    stats.hiredCount = d.totalOffers ?? '-'
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
.quick-action-card p {
  color: #666;
  margin: 15px 0;
  min-height: 40px;
}
.quick-action-card :deep(.el-card__header) {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}
</style>
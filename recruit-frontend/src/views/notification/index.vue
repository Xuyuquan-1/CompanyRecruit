<template>
  <div class="notification-page">
    <el-card shadow="never">
      <div class="table-header">
        <span class="title">消息通知</span>
        <el-button type="primary" text @click="markAllRead">全部标为已读</el-button>
      </div>
      <div v-if="list.length === 0" style="text-align:center;padding:60px;color:#909399">暂无通知</div>
      <div v-for="item in list" :key="item.id" class="notify-item" :class="{ unread: !item.isRead }">
        <div class="notify-title">
          <span class="notify-dot" v-if="!item.isRead"></span>
          <strong>{{ item.title }}</strong>
          <span class="notify-time">{{ formatDate(item.createTime) }}</span>
        </div>
        <div class="notify-content">{{ item.content }}</div>
        <div v-if="!item.isRead" style="margin-top:6px">
          <el-button type="primary" text size="small" @click="markRead(item)">标为已读</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotificationList, markAsRead, markAllAsRead } from '../../api/notification'

const list = ref([])

async function loadData() {
  try { const res = await getNotificationList(); list.value = res.data || [] } catch {}
}

async function markRead(item) {
  try { await markAsRead(item.id); item.isRead = true } catch {}
}

async function markAllRead() {
  try { await markAllAsRead(); list.value.forEach(i => i.isRead = true); ElMessage.success('已全部标为已读') } catch {}
}

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 19) : '' }
onMounted(loadData)
</script>

<style scoped>
.table-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:16px }
.title { font-size:16px; font-weight:bold }
.notify-item { padding:14px 16px; border-bottom:1px solid #ebeef5; cursor:pointer }
.notify-item.unread { background:#f0f9ff }
.notify-item:hover { background:#f5f7fa }
.notify-title { display:flex; align-items:center; gap:8px }
.notify-dot { width:8px; height:8px; background:#f56c6c; border-radius:50%; flex-shrink:0 }
.notify-time { margin-left:auto; color:#909399; font-size:12px }
.notify-content { margin-top:4px; color:#606266; font-size:14px }
</style>
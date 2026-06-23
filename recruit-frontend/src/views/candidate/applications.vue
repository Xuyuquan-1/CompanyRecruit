<template>
  <div class="my-applications">
    <el-card shadow="never" class="header-card">
      <h3 style="margin:0">我的应聘记录</h3>
      <p style="color:#999;margin:8px 0 0">查看您的应聘流程进度和状态</p>
    </el-card>

    <el-card shadow="never" style="margin-top:15px">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column prop="jobTitle" label="应聘岗位" min-width="150" show-overflow-tooltip />
        <el-table-column prop="resumeName" label="投递简历" min-width="150" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="投递时间" width="160">
          <template #default="{ row }">{{ formatDate(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流程进度" min-width="400">
          <template #default="{ row }">
            <el-steps :active="getProcessStep(row.status)" align-center size="small">
              <el-step title="已投递" description="" />
              <el-step title="筛选通过" description="" />
              <el-step title="面试中" description="" />
              <el-step title="录用" description="" />
            </el-steps>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link icon="View" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="margin-top:15px;text-align:right">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="应聘详情" width="700px">
      <el-descriptions :column="2" border v-if="currentApp">
        <el-descriptions-item label="应聘岗位" :span="2">{{ currentApp.jobTitle }}</el-descriptions-item>
        <el-descriptions-item label="投递简历">{{ currentApp.resumeName }}</el-descriptions-item>
        <el-descriptions-item label="投递时间">{{ formatDate(currentApp.applyTime) }}</el-descriptions-item>
        <el-descriptions-item label="当前状态" :span="2">
          <el-tag :type="statusType(currentApp.status)">
            {{ statusLabel(currentApp.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentApp.remark || '暂无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 如果有面试信息，显示面试详情 -->
      <div v-if="currentApp?.interviewInfo" style="margin-top:20px">
        <h4>面试安排</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="面试时间">
            {{ formatDateTime(currentApp.interviewInfo.interviewTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="面试地点">
            {{ currentApp.interviewInfo.location || '待定' }}
          </el-descriptions-item>
          <el-descriptions-item label="面试官">
            {{ currentApp.interviewInfo.interviewerName || '待定' }}
          </el-descriptions-item>
          <el-descriptions-item label="面试结果">
            <el-tag v-if="currentApp.interviewInfo.result === 1" type="success">通过</el-tag>
            <el-tag v-else-if="currentApp.interviewInfo.result === 2" type="danger">未通过</el-tag>
            <el-tag v-else type="info">待面试</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="面试评价" :span="2">
            {{ currentApp.interviewInfo.evaluation || '暂无评价' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyApplications } from '../../api/application'

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 详情
const detailVisible = ref(false)
const currentApp = ref(null)

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await getMyApplications(queryForm)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) {
    console.error('加载失败:', e)
  } finally {
    loading.value = false
  }
}

// 查看详情
function viewDetail(row) {
  currentApp.value = row
  detailVisible.value = true
}

// 格式化日期
function formatDate(d) {
  return d ? d.replace('T', ' ').substring(0, 16) : ''
}

// 格式化日期时间
function formatDateTime(d) {
  return d ? d.replace('T', ' ').substring(0, 19) : ''
}

// 状态类型
function statusType(s) {
  const map = { 0: 'info', 1: 'warning', 2: '', 3: 'success', 4: 'danger' }
  return map[s] || 'info'
}

// 状态标签
function statusLabel(s) {
  const map = { 
    0: '待筛选', 
    1: '通过筛选', 
    2: '面试中', 
    3: '已录用',
    4: '不录用'
  }
  return map[s] || '未知'
}

// 获取流程步骤
function getProcessStep(status) {
  if (status === 0) return 1 // 待筛选 -> 已投递
  if (status === 1) return 2 // 通过筛选 -> 筛选通过
  if (status === 2) return 3 // 面试中
  if (status === 3) return 4 // 录用
  if (status === 4) return 0 // 不录用
  return 0
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-applications {
  padding: 20px;
}

.header-card {
  margin-bottom: 15px;
}
</style>

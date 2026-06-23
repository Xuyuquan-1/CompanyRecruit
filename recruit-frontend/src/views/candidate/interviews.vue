<template>
  <div class="my-interviews">
    <el-card shadow="never" class="header-card">
      <h3 style="margin:0">我的面试安排</h3>
      <p style="color:#999;margin:8px 0 0">查看面试时间、地点和评价结果</p>
    </el-card>

    <el-card shadow="never" style="margin-top:15px">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="jobTitle" label="应聘岗位" min-width="150" show-overflow-tooltip />
        <el-table-column prop="interviewTime" label="面试时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.interviewTime) }}</template>
        </el-table-column>
        <el-table-column prop="location" label="面试地点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="interviewerName" label="面试官" width="100" />
        <el-table-column prop="status" label="面试状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="面试结果" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.result === 1" type="success">通过</el-tag>
            <el-tag v-else-if="row.result === 2" type="danger">未通过</el-tag>
            <span v-else style="color:#999">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 只要有评价就可以查看 -->
            <el-button v-if="row.evaluation || row.result" type="success" link icon="View" @click="viewEvaluation(row)">查看评价</el-button>
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

    <!-- 查看评价对话框 -->
    <el-dialog v-model="viewEvalVisible" title="面试评价详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应聘者">{{ evalView.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ evalView.jobTitle }}</el-descriptions-item>
        <el-descriptions-item label="面试官">{{ evalView.interviewerName }}</el-descriptions-item>
        <el-descriptions-item label="面试时间">{{ formatDateTime(evalView.interviewTime) }}</el-descriptions-item>
        <el-descriptions-item label="面试地点" :span="2">{{ evalView.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试结果" :span="2">
          <el-tag v-if="evalView.result===1" type="success" size="large">通过</el-tag>
          <el-tag v-else-if="evalView.result===2" type="danger" size="large">未通过</el-tag>
          <el-tag v-else type="info" size="large">待评价</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="面试评价" :span="2">
          <div style="white-space:pre-wrap;padding:10px;background:#f5f7fa;border-radius:4px">
            {{ evalView.evaluation || '暂无评价' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewEvalVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyInterviews } from '../../api/interview'

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

// 查看评价
const viewEvalVisible = ref(false)
const evalView = reactive({
  id: null,
  candidateName: '',
  jobTitle: '',
  interviewerName: '',
  interviewTime: '',
  location: '',
  result: null,
  evaluation: ''
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await getMyInterviews(queryForm)
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

// 查看评价
function viewEvaluation(row) {
  evalView.id = row.id
  evalView.candidateName = row.candidateName
  evalView.jobTitle = row.jobTitle
  evalView.interviewerName = row.interviewerName
  evalView.interviewTime = row.interviewTime
  evalView.location = row.location
  evalView.result = row.result
  evalView.evaluation = row.evaluation
  viewEvalVisible.value = true
}

// 格式化日期时间
function formatDateTime(d) {
  return d ? d.replace('T', ' ').substring(0, 19) : ''
}

// 状态类型
function statusType(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[s] || 'info'
}

// 状态标签
function statusLabel(s) {
  const map = { 0: '待面试', 1: '已完成', 2: '已取消' }
  return map[s] || '未知'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-interviews {
  padding: 20px;
}

.header-card {
  margin-bottom: 15px;
}
</style>
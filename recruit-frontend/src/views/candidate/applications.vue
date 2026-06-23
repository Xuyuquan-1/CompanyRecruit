<template>
  <div class="my-applications">
    <el-card shadow="never" class="header-card">
      <h3 style="margin:0">我的应聘记录</h3>
      <p style="color:#999;margin:8px 0 0">查看您的应聘流程进度和状态</p>
    </el-card>

    <el-card shadow="never" style="margin-top:15px">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
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
        <el-table-column label="流程进度" min-width="600">
          <template #default="{ row }">
            <div style="padding: 10px 0">
              <el-steps :active="getProcessStep(row.status)" align-center size="small" :space="70">
                <el-step :title="'已投递'" :status="getStepStatus(row, 0)" />
                <el-step :title="'简历通过'" :status="getStepStatus(row, 1)" />
                <el-step :title="'面试中'" :status="getStepStatus(row, 2)" />
                <el-step :title="'Offer待确认'" :status="getStepStatus(row, 3)" />
                <el-step :title="'待提交资料'" :status="getStepStatus(row, 4)" />
                <el-step :title="'已提交(待录取)'" :status="getStepStatus(row, 5)" />
                <el-step :title="'已录用'" :status="getStepStatus(row, 6)" />
                <el-step :title="'已入职'" :status="getStepStatus(row, 7)" />
              </el-steps>
              <div v-if="row.status === 4" style="margin-top: 8px; color: #f56c6c; font-size: 12px">
                <el-icon><WarningFilled /></el-icon>
                失败原因：{{ getRefuseReason(row.refuseType) }}
              </div>
              <div v-if="row.status === 7" style="margin-top: 8px; color: #909399; font-size: 12px">
                <el-icon><InfoFilled /></el-icon>
                状态：候选人主动撤回
              </div>
            </div>
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
import { WarningFilled, InfoFilled } from '@element-plus/icons-vue'
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
  const map = {
    0: 'info',      // 待筛选
    1: 'warning',   // 通过筛选
    2: '',          // 面试中
    3: 'warning',   // 待确认Offer
    4: 'danger',    // 不录用
    5: 'success',   // 已接受Offer(待入职)
    6: 'success',   // 已入职
    7: 'info'       // 候选人撤回
  }
  return map[s] || 'info'
}

// 状态标签
function statusLabel(s) {
  const map = {
    0: '待筛选',
    1: '通过筛选',
    2: '面试中',
    3: '待确认Offer',
    4: '不录用',
    5: '已接受Offer(待入职)',
    6: '已入职',
    7: '候选人撤回'
  }
  return map[s] || '未知'
}

// 获取流程步骤（返回当前进行到第几步，1-8）
function getProcessStep(status, offerStatus, docsSubmitted) {
  const stepMap = {
    0: 1,  // 待筛选 → 第1步（已投递）
    1: 2,  // 通过筛选 → 第2步（简历通过）
    2: 3,  // 面试中 → 第3步
    3: 4,  // 待确认Offer → 第4步
    4: 0,  // 不录用 → 不亮任何步骤（失败）
    5: checkDocsStep(offerStatus, docsSubmitted),  // 已接受Offer → 根据资料提交情况判断
    6: 8,  // 已入职 → 第8步（最终步）
    7: 0   // 候选人撤回 → 不亮任何步骤
  }
  return stepMap[status] || 0
}

// 检查资料提交情况，返回对应的步骤
function checkDocsStep(offerStatus, docsSubmitted) {
  // 如果没有Offer，返回第5步（待提交资料）
  if (!offerStatus && offerStatus !== 0) {
    return 5
  }
  
  // Offer已接受，检查资料
  if (offerStatus === 1) { // 1-已接受
    // 解析资料提交情况
    let docs = {}
    try {
      docs = docsSubmitted ? JSON.parse(docsSubmitted) : {}
    } catch (e) {
      docs = {}
    }
    
    // 检查是否所有资料都已提交
    const allSubmitted = docs.idCardFront && docs.idCardBack && docs.contract && docs.medicalReport
    
    if (allSubmitted) {
      return 6  // 已提交资料（待录取）
    } else {
      return 5  // 待提交资料
    }
  }
  
  return 5  // 默认返回第5步
}

// 获取单个步骤的状态（完成/当前/等待/错误）
function getStepStatus(row, stepIndex) {
  const status = row.status
  
  // 不录用或撤回：所有步骤显示失败
  if (status === 4 || status === 7) {
    return 'error'
  }
  
  // 传入 offer 信息
  const currentStep = getProcessStep(status, row.offerStatus, row.offerDocsSubmitted)
  
  // 已完成的步骤
  if (stepIndex < currentStep - 1) {
    return 'finish'
  }
  // 当前步骤
  if (stepIndex === currentStep - 1) {
    return 'process'
  }
  // 未到达的步骤
  return 'wait'
}

// 获取失败原因描述
function getRefuseReason(refuseType) {
  const reasonMap = {
    1: '简历筛选未通过',
    2: '面试未通过',
    3: '候选人拒绝Offer',
    4: '材料审核不合格',
    5: '录用审批驳回',
    6: '候选人主动撤回',
    7: '岗位已关闭'
  }
  return reasonMap[refuseType] || '未知原因'
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

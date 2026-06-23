<template>
  <div class="report-page">
    <!-- 筛选条件 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="统计时间">
          <el-date-picker
            v-model="filterForm.startDate"
            type="date"
            placeholder="开始日期"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
          <span style="margin: 0 8px">至</span>
          <el-date-picker
            v-model="filterForm.endDate"
            type="date"
            placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
          <el-button icon="Refresh" @click="resetFilter">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right">
          <el-button type="success" icon="Download" @click="handleExport" :loading="exporting">
            导出Excel报表
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #409EFF">{{ summary.totalApplications || 0 }}</div>
          <div class="stat-label">总简历投递量</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #67C23A">{{ summary.totalInterviews || 0 }}</div>
          <div class="stat-label">总面试人数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #E6A23C">{{ summary.totalOffers || 0 }}</div>
          <div class="stat-label">总录用人数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #F56C6C">{{ summary.totalOnboard || 0 }}</div>
          <div class="stat-label">成功入职人数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 招聘进度统计 -->
    <el-card shadow="never" style="margin-top: 16px" header="📊 招聘进度统计（按岗位）">
      <el-table :data="progressList" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="jobId" label="岗位ID" width="80" align="center" />
        <el-table-column prop="jobTitle" label="岗位名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="headcount" label="招聘人数" width="100" align="center" />
        <el-table-column prop="applicationCount" label="简历投递量" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="primary" effect="plain">{{ row.applicationCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="interviewCount" label="面试人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" effect="plain">{{ row.interviewCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="offerCount" label="录用人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="warning" effect="plain">{{ row.offerCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="onboardCount" label="入职人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="danger" effect="plain">{{ row.onboardCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="conversionRate" label="转化率" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: parseFloat(row.conversionRate) > 50 ? '#67C23A' : '#E6A23C', fontWeight: 'bold' }">
              {{ row.conversionRate }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 招聘效果分析 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never" header="📈 招聘效果分析">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="总应聘数">
              <el-tag type="primary">{{ effectData.totalApplications || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="通过筛选">
              {{ effectData.passedScreening || 0 }} 
              <el-tag size="small" type="success" style="margin-left: 8px">{{ effectData.screeningPassRate }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="面试中">
              <el-tag type="warning">{{ effectData.interviewing || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="总面试数">
              {{ effectData.totalInterviews || 0 }}
              <el-tag size="small" type="success" style="margin-left: 8px">{{ effectData.interviewPassRate }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="已录用">
              <el-tag type="success">{{ effectData.hired || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="已淘汰">
              <el-tag type="danger">{{ effectData.rejected || 0 }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="总Offer数">
              {{ effectData.totalOffers || 0 }}
              <el-tag size="small" type="success" style="margin-left: 8px">{{ effectData.offerAcceptRate }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="成功入职">
              <el-tag type="success" size="large">{{ effectData.onboarded || 0 }}</el-tag>
              <el-tag size="small" type="danger" style="margin-left: 8px">整体转化率: {{ effectData.overallConversionRate }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" header="🎯 招聘转化漏斗">
          <div class="funnel">
            <div class="funnel-item" style="width: 100%; background: #409EFF">
              简历投递: {{ effectData.totalApplications || 0 }}
            </div>
            <div class="funnel-item" style="width: 75%; background: #67C23A">
              通过筛选: {{ effectData.passedScreening || 0 }}
            </div>
            <div class="funnel-item" style="width: 55%; background: #E6A23C">
              参加面试: {{ effectData.totalInterviews || 0 }}
            </div>
            <div class="funnel-item" style="width: 35%; background: #F56C6C">
              发放Offer: {{ effectData.totalOffers || 0 }}
            </div>
            <div class="funnel-item" style="width: 20%; background: #909399">
              成功入职: {{ effectData.onboarded || 0 }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各岗位分析 -->
    <el-card shadow="never" style="margin-top: 16px" header="🏢 各岗位招聘效果分析">
      <el-table :data="jobAnalysisList" border stripe>
        <el-table-column prop="jobId" label="岗位ID" width="80" align="center" />
        <el-table-column prop="jobTitle" label="岗位名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="applicationCount" label="简历投递量" width="120" align="center" />
        <el-table-column prop="onboardCount" label="入职人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success">{{ row.onboardCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="conversionRate" label="转化率" width="100" align="center">
          <template #default="{ row }">
            <span :style="{ color: parseFloat(row.conversionRate) > 30 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
              {{ row.conversionRate }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecruitmentProgress, getRecruitmentEffect, exportReportExcel } from '../../api/report'

// 筛选表单
const filterForm = reactive({
  startDate: '',
  endDate: ''
})

const loading = ref(false)
const exporting = ref(false)

// 数据
const summary = ref({})
const progressList = ref([])
const effectData = ref({})
const jobAnalysisList = ref([])

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (filterForm.startDate) params.startDate = filterForm.startDate
    if (filterForm.endDate) params.endDate = filterForm.endDate

    // 并行加载进度统计和效果分析
    const [progressRes, effectRes] = await Promise.all([
      getRecruitmentProgress(params),
      getRecruitmentEffect(params)
    ])

    console.log('=== 招聘进度数据 ===')
    console.log('progressRes:', progressRes)
    console.log('summary:', progressRes.data?.summary)
    console.log('progressList:', progressRes.data?.progressList)
    
    console.log('=== 招聘效果数据 ===')
    console.log('effectRes:', effectRes)
    console.log('effectData:', effectRes.data)
    console.log('jobAnalysis:', effectRes.data?.jobAnalysis)

    if (progressRes.code === 200) {
      summary.value = progressRes.data.summary || {}
      progressList.value = progressRes.data.progressList || []
    }

    if (effectRes.code === 200) {
      effectData.value = effectRes.data || {}
      jobAnalysisList.value = effectRes.data.jobAnalysis || []
    }
  } catch (err) {
    console.error('报表数据加载失败:', err)
    ElMessage.error('报表数据加载失败')
  } finally {
    loading.value = false
  }
}

// 重置筛选
function resetFilter() {
  filterForm.startDate = ''
  filterForm.endDate = ''
  loadData()
}

// 导出Excel
async function handleExport() {
  exporting.value = true
  try {
    const params = {}
    if (filterForm.startDate) params.startDate = filterForm.startDate
    if (filterForm.endDate) params.endDate = filterForm.endDate

    const res = await exportReportExcel(params)
    
    // 创建下载链接
    const blob = new Blob([res.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const now = new Date()
    const dateStr = now.toISOString().slice(0, 10)
    link.download = `招聘统计报表_${dateStr}.xlsx`
    
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (err) {
    console.error('导出失败:', err)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.report-page {
  padding: 20px;
}

.filter-card {
  margin-bottom: 16px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.funnel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
}

.funnel-item {
  color: #fff;
  padding: 12px;
  text-align: center;
  border-radius: 4px;
  font-weight: bold;
  transition: all 0.3s;
  cursor: pointer;
}

.funnel-item:hover {
  opacity: 0.8;
  transform: scale(1.02);
}
</style>

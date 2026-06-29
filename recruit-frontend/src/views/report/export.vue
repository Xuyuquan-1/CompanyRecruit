<template>
  <div class="report-export">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="header-title">报表导出中心</span>
          <span class="header-desc">选择导出配置，生成并下载Excel报表文件</span>
        </div>
      </template>

      <el-form :model="exportForm" label-width="120px" style="max-width: 600px">
        <el-form-item label="导出类型">
          <el-radio-group v-model="exportForm.exportType" size="large">
            <el-radio-button label="all">
              <div class="radio-content">
                <div class="radio-title">全部报表</div>
                <div class="radio-desc">包含进度统计 + 效果分析</div>
              </div>
            </el-radio-button>
            <el-radio-button label="dashboard">
              <div class="radio-content">
                <div class="radio-title">招聘进度统计</div>
                <div class="radio-desc">各岗位进度数据</div>
              </div>
            </el-radio-button>
            <el-radio-button label="analysis">
              <div class="radio-content">
                <div class="radio-title">招聘效果分析</div>
                <div class="radio-desc">多维度分析数据</div>
              </div>
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-radio-group v-model="dateRangeType" @change="handleDateRangeChange">
            <el-radio label="month">本月</el-radio>
            <el-radio label="quarter">本季度</el-radio>
            <el-radio label="year">最近一年</el-radio>
            <el-radio label="all">全部时间</el-radio>
            <el-radio label="custom">自定义</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item v-if="dateRangeType === 'custom'" label="自定义日期">
          <el-date-picker
            v-model="exportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="文件名称">
          <el-input v-model="exportForm.fileName" placeholder="请输入文件名（不含后缀）">
            <template #append>.xlsx</template>
          </el-input>
        </el-form-item>

        <el-form-item label="包含内容">
          <el-checkbox-group v-model="exportForm.content">
            <el-checkbox label="kpi">核心指标数据</el-checkbox>
            <el-checkbox label="chart">图表数据</el-checkbox>
            <el-checkbox label="table">数据明细表</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" icon="Download" @click="handleExport" :loading="exporting"
                     style="width: 200px">
            {{ exporting ? '正在生成...' : '确认导出' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 导出历史 -->
    <el-card shadow="never" style="margin-top: 16px">
      <template #header>
        <span class="header-title">导出说明</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="文件格式">Excel (.xlsx)，支持多Sheet页</el-descriptions-item>
        <el-descriptions-item label="全部报表">包含招聘进度统计、招聘效果分析、各岗位分析3个Sheet</el-descriptions-item>
        <el-descriptions-item label="进度统计">包含各岗位简历投递量、面试人数、录用人数、转化率</el-descriptions-item>
        <el-descriptions-item label="效果分析">包含按时间/岗位维度的分析数据及转化率统计</el-descriptions-item>
        <el-descriptions-item label="数据时效">导出数据为当前筛选条件下的实时数据</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { exportReportExcel } from '../../api/report'

const exporting = ref(false)
const dateRangeType = ref('month')

const exportForm = reactive({
  exportType: 'all',
  dateRange: [],
  fileName: '',
  content: ['kpi', 'chart', 'table']
})

function formatDate(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function handleDateRangeChange(type) {
  const now = new Date()
  if (type === 'month') {
    exportForm.dateRange = [formatDate(new Date(now.getFullYear(), now.getMonth(), 1)), formatDate(now)]
  } else if (type === 'quarter') {
    const qMonth = Math.floor(now.getMonth() / 3) * 3
    exportForm.dateRange = [formatDate(new Date(now.getFullYear(), qMonth, 1)), formatDate(now)]
  } else if (type === 'year') {
    exportForm.dateRange = [formatDate(new Date(now.getFullYear() - 1, now.getMonth(), now.getDate())), formatDate(now)]
  } else if (type === 'all') {
    exportForm.dateRange = []
  }
  updateFileName()
}

function updateFileName() {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const typeMap = { all: '招聘统计报表', dashboard: '招聘进度报表', analysis: '招聘效果报表' }
  exportForm.fileName = `${typeMap[exportForm.exportType] || '招聘报表'}_${dateStr}`
}

async function handleExport() {
  exporting.value = true
  try {
    const params = { exportType: exportForm.exportType }
    if (exportForm.dateRange && exportForm.dateRange.length === 2) {
      params.startDate = exportForm.dateRange[0]
      params.endDate = exportForm.dateRange[1]
    }
    const res = await exportReportExcel(params)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${exportForm.fileName || '招聘报表'}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功，文件已下载')
  } catch (err) {
    console.error('导出失败:', err)
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  handleDateRangeChange('month')
})
</script>

<style scoped>
.report-export { padding: 20px; }
.card-header { display: flex; flex-direction: column; gap: 4px; }
.header-title { font-size: 16px; font-weight: 600; color: #303133; }
.header-desc { font-size: 13px; color: #909399; }
.radio-content { text-align: left; padding: 4px 0; }
.radio-title { font-size: 14px; font-weight: 500; }
.radio-desc { font-size: 12px; color: #909399; margin-top: 2px; }
</style>

<template>
  <div class="report-dashboard">
    <!-- 顶部：全局筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="时间范围">
          <el-radio-group v-model="dateRangeType" size="small" @change="handleDateRangeChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="quarter">本季度</el-radio-button>
            <el-radio-button label="half">近半年</el-radio-button>
            <el-radio-button label="custom">自定义</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="dateRangeType === 'custom'">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="loadData"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="filterForm.department" placeholder="全部部门" clearable @change="loadData" style="width: 160px">
            <el-option v-for="d in departmentOptions" :key="d" :label="d" :value="d" />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位">
          <el-select v-model="filterForm.jobId" placeholder="全部岗位" clearable @change="loadData" style="width: 180px">
            <el-option v-for="j in jobOptions" :key="j.id" :label="j.title" :value="j.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Refresh" @click="loadData" :loading="loading">刷新</el-button>
          <el-button type="success" icon="Download" @click="showExportDialog">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标卡 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="5" v-for="(item, idx) in kpiCards" :key="idx">
        <el-card shadow="hover" class="kpi-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="kpi-icon" :style="{ color: item.color }">
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-value" :style="{ color: item.color }">
              {{ item.suffix === '%' ? item.value + '%' : item.value }}
            </div>
            <div class="kpi-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区第一行 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">各岗位投递量排行</span></template>
          <div ref="chartJobRanking" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">招聘进度漏斗图</span></template>
          <div ref="chartFunnel" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">面试状态分布</span></template>
          <div ref="chartInterview" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区第二行 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">投递趋势</span></template>
          <div ref="chartTrend" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">各部门招聘进度</span></template>
          <div ref="chartDept" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="chart-card">
          <template #header><span class="chart-title">岗位录用率 TOP5</span></template>
          <div ref="chartTop5" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 导出配置弹窗 -->
    <el-dialog v-model="exportDialogVisible" title="导出配置" width="480px">
      <el-form :model="exportConfig" label-width="100px">
        <el-form-item label="导出范围">
          <el-radio-group v-model="exportConfig.scope">
            <el-radio label="current">当前视图</el-radio>
            <el-radio label="all">全部数据</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="文件名">
          <el-input v-model="exportConfig.fileName" placeholder="招聘报表_YYYYMMDD" />
        </el-form-item>
        <el-form-item label="导出类型">
          <el-select v-model="exportConfig.exportType" style="width: 100%">
            <el-option label="全部数据" value="all" />
            <el-option label="进度统计" value="dashboard" />
            <el-option label="效果分析" value="analysis" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExport" :loading="exporting">确认导出</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, User, ChatDotSquare, CircleCheck, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboard, exportReportExcel } from '../../api/report'

// refs
const chartJobRanking = ref(null)
const chartFunnel = ref(null)
const chartInterview = ref(null)
const chartTrend = ref(null)
const chartDept = ref(null)
const chartTop5 = ref(null)

let chartInstances = []

const loading = ref(false)
const exporting = ref(false)
const exportDialogVisible = ref(false)
const dateRangeType = ref('all')

const departmentOptions = ref([])
const jobOptions = ref([])

const filterForm = reactive({
  dateRange: [],
  department: '',
  jobId: null
})

const exportConfig = reactive({
  scope: 'current',
  fileName: '',
  exportType: 'all'
})

const dashboardData = ref({})

// KPI 卡片数据
const kpiCards = computed(() => {
  const kpi = dashboardData.value.kpi || {}
  return [
    { label: '总简历投递量', value: kpi.totalApplications || 0, color: '#409EFF', icon: 'Document', suffix: '' },
    { label: '待面试人数', value: kpi.pendingInterviews || 0, color: '#E6A23C', icon: 'ChatDotSquare', suffix: '' },
    { label: '面试通过率', value: kpi.interviewPassRate || '0', color: '#67C23A', icon: 'CircleCheck', suffix: '%' },
    { label: '已录用人数', value: kpi.totalHired || 0, color: '#F56C6C', icon: 'User', suffix: '' },
    { label: '招聘完成率', value: kpi.recruitmentCompletionRate || '0', color: '#909399', icon: 'TrendCharts', suffix: '%' }
  ]
})

// 格式化日期
function formatDate(d) {
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 日期范围切换
function handleDateRangeChange(type) {
  const now = new Date()
  if (type === 'all') {
    filterForm.dateRange = []
    loadData()
  } else if (type === 'month') {
    filterForm.dateRange = [formatDate(new Date(now.getFullYear(), now.getMonth(), 1)), formatDate(now)]
    loadData()
  } else if (type === 'quarter') {
    const qMonth = Math.floor(now.getMonth() / 3) * 3
    filterForm.dateRange = [formatDate(new Date(now.getFullYear(), qMonth, 1)), formatDate(now)]
    loadData()
  } else if (type === 'half') {
    const halfYearAgo = new Date(now.getFullYear(), now.getMonth() - 6, 1)
    filterForm.dateRange = [formatDate(halfYearAgo), formatDate(now)]
    loadData()
  }
  // custom: wait for date picker
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params = {}
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    if (filterForm.department) params.department = filterForm.department
    if (filterForm.jobId) params.jobId = filterForm.jobId

    const res = await getDashboard(params)
    if (res.code === 200) {
      dashboardData.value = res.data || {}
      // 提取部门和岗位选项
      const deptSet = new Set()
      const jobList = []
      const ranking = dashboardData.value.jobApplicationRanking || []
      ranking.forEach(item => {
        if (item.jobTitle) jobList.push({ id: item.jobId, title: item.jobTitle })
      })
      const deptProgress = dashboardData.value.departmentProgress || []
      deptProgress.forEach(item => { if (item.department) deptSet.add(item.department) })
      departmentOptions.value = [...deptSet]
      jobOptions.value = jobList

      await nextTick()
      renderCharts()
    }
  } catch (err) {
    console.error('仪表盘数据加载失败:', err)
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

// 渲染所有图表
function renderCharts() {
  destroyCharts()
  renderJobRanking()
  renderFunnel()
  renderInterviewDistribution()
  renderTrend()
  renderDeptProgress()
  renderTop5()
}

function destroyCharts() {
  chartInstances.forEach(c => c && c.dispose())
  chartInstances = []
}

function initChart(elRef) {
  if (!elRef) return null
  const instance = echarts.init(elRef)
  chartInstances.push(instance)
  return instance
}

// 图表1：各岗位投递量排行（横向柱状图）
function renderJobRanking() {
  const chart = initChart(chartJobRanking.value)
  if (!chart) return
  const data = dashboardData.value.jobApplicationRanking || []
  const names = data.map(d => d.jobTitle).reverse()
  const values = data.map(d => d.applicationCount).reverse()
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '30%', right: '10%', bottom: '10%', top: '10%' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: names, axisLabel: { width: 80, overflow: 'truncate' } },
    series: [{ type: 'bar', data: values, itemStyle: { color: '#409EFF', borderRadius: [0, 4, 4, 0] } }]
  })
}

// 图表2：招聘漏斗图
function renderFunnel() {
  const chart = initChart(chartFunnel.value)
  if (!chart) return
  const data = dashboardData.value.funnel || []
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    series: [{
      type: 'funnel',
      left: '10%', right: '10%', top: '5%', bottom: '5%',
      minSize: '20%', maxSize: '100%',
      gap: 4,
      label: { show: true, position: 'inside', formatter: '{b}\n{c}人' },
      itemStyle: { borderWidth: 0 },
      data: data.map((d, i) => ({
        name: d.stage, value: d.count,
        itemStyle: { color: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'][i] || '#409EFF' }
      }))
    }]
  })
}

// 图表3：面试状态分布（环形图）
function renderInterviewDistribution() {
  const chart = initChart(chartInterview.value)
  if (!chart) return
  const data = dashboardData.value.interviewStatusDistribution || []
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: true,
      label: { show: true, formatter: '{b}\n{d}%' },
      data: data.map((d, i) => ({
        ...d,
        itemStyle: { color: ['#E6A23C', '#67C23A', '#409EFF', '#F56C6C'][i] }
      }))
    }]
  })
}

// 图表4：投递趋势（折线图）
function renderTrend() {
  const chart = initChart(chartTrend.value)
  if (!chart) return
  const data = dashboardData.value.applicationTrend || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '10%', right: '5%', bottom: '15%', top: '10%' },
    xAxis: { type: 'category', data: data.map(d => d.date), axisLabel: { rotate: 45, fontSize: 10 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'line', smooth: true, data: data.map(d => d.count),
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(64,158,255,0.4)' },
        { offset: 1, color: 'rgba(64,158,255,0.05)' }
      ])},
      lineStyle: { color: '#409EFF', width: 2 },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

// 图表5：各部门招聘进度（堆叠条形图）
function renderDeptProgress() {
  const chart = initChart(chartDept.value)
  if (!chart) return
  const data = dashboardData.value.departmentProgress || []
  const depts = data.map(d => d.department)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { bottom: 0, data: ['投递', '面试', '录用', '入职'] },
    grid: { left: '15%', right: '5%', bottom: '15%', top: '10%' },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: depts, axisLabel: { width: 60, overflow: 'truncate' } },
    series: [
      { name: '投递', type: 'bar', stack: 'total', data: data.map(d => d.applicationCount), itemStyle: { color: '#409EFF' } },
      { name: '面试', type: 'bar', stack: 'total', data: data.map(d => d.interviewCount), itemStyle: { color: '#E6A23C' } },
      { name: '录用', type: 'bar', stack: 'total', data: data.map(d => d.offerCount), itemStyle: { color: '#67C23A' } },
      { name: '入职', type: 'bar', stack: 'total', data: data.map(d => d.onboardCount), itemStyle: { color: '#F56C6C' } }
    ]
  })
}

// 图表6：岗位录用率TOP5
function renderTop5() {
  const chart = initChart(chartTop5.value)
  if (!chart) return
  const data = dashboardData.value.topHireRateJobs || []
  const names = data.map(d => d.jobTitle).reverse()
  const rates = data.map(d => d.hireRate).reverse()
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: '{b}<br/>录用率: {c}%' },
    grid: { left: '30%', right: '10%', bottom: '10%', top: '10%' },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: names, axisLabel: { width: 80, overflow: 'truncate' } },
    series: [{
      type: 'bar', data: rates,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#67C23A' }, { offset: 1, color: '#409EFF' }
        ]),
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right', formatter: '{c}%' }
    }]
  })
}

// 导出弹窗
function showExportDialog() {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  exportConfig.fileName = `招聘报表_${dateStr}`
  exportDialogVisible.value = true
}

async function handleExport() {
  exporting.value = true
  try {
    const params = { exportType: exportConfig.exportType }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    const res = await exportReportExcel(params)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${exportConfig.fileName || '招聘报表'}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    exportDialogVisible.value = false
    ElMessage.success('导出成功')
  } catch (err) {
    console.error('导出失败:', err)
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 窗口resize
function handleResize() {
  chartInstances.forEach(c => c && c.resize())
}

onMounted(() => {
  // 初始化默认全部
  filterForm.dateRange = []
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  destroyCharts()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.report-dashboard { padding: 20px; }
.filter-card { margin-bottom: 0; }
.kpi-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 8px 0;
}
.kpi-card:hover { transform: translateY(-4px); }
.kpi-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
}
.kpi-icon { flex-shrink: 0; }
.kpi-content { flex: 1; text-align: left; }
.kpi-value { font-size: 28px; font-weight: bold; line-height: 1.2; }
.kpi-label { font-size: 13px; color: #909399; margin-top: 4px; }
.chart-card { height: 100%; }
.chart-title { font-size: 15px; font-weight: 600; color: #303133; }
.chart-container { height: 300px; width: 100%; }
</style>

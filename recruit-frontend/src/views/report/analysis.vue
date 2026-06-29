<template>
  <div class="report-analysis">
    <!-- 顶部筛选 -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 320px"
          />
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="dateRangeType" size="small" @change="handleDateRangeChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="quarter">本季度</el-radio-button>
            <el-radio-button label="year">最近一年</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadData" :loading="loading">查询</el-button>
          <el-button type="success" icon="Download" @click="showExportDialog">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 核心指标卡 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="8" v-for="(item, idx) in kpiCards" :key="idx">
        <el-card shadow="hover" class="kpi-card" :style="{ borderTop: `3px solid ${item.color}` }">
          <div class="kpi-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="kpi-label">{{ item.label }}</div>
          <div class="kpi-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 维度切换Tab -->
    <el-card shadow="never" style="margin-top: 16px">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 按时间分析 -->
        <el-tab-pane label="按时间分析" name="time">
          <div ref="chartTime" class="chart-container-lg"></div>
          <div class="table-header">
            <span class="table-title">月度明细数据</span>
            <el-button type="primary" link @click="exportTable('time')">
              <el-icon><Download /></el-icon> 导出表格
            </el-button>
          </div>
          <el-table :data="tableData" border stripe style="margin-top: 8px"
                    :default-sort="{ prop: 'period', order: 'descending' }">
            <el-table-column prop="period" label="月份" width="120" sortable />
            <el-table-column prop="applicationCount" label="投递量" width="100" align="center" sortable />
            <el-table-column prop="screeningPassed" label="筛选通过" width="100" align="center" sortable />
            <el-table-column prop="interviewCount" label="面试数" width="100" align="center" sortable />
            <el-table-column prop="hireCount" label="录用数" width="100" align="center" sortable />
            <el-table-column prop="conversionRate" label="转化率" width="100" align="center" sortable />
          </el-table>
        </el-tab-pane>

        <!-- 按岗位分析 -->
        <el-tab-pane label="按岗位分析" name="job">
          <el-row :gutter="16">
            <el-col :span="12">
              <div ref="chartJobComp" class="chart-container"></div>
            </el-col>
            <el-col :span="12">
              <div ref="chartJobCycle" class="chart-container"></div>
            </el-col>
          </el-row>
          <div class="table-header">
            <span class="table-title">岗位维度明细数据</span>
            <el-button type="primary" link @click="exportTable('job')">
              <el-icon><Download /></el-icon> 导出表格
            </el-button>
          </div>
          <el-table :data="tableData" border stripe style="margin-top: 8px"
                    :default-sort="{ prop: 'applicationCount', order: 'descending' }">
            <el-table-column prop="jobTitle" label="岗位名称" min-width="150" show-overflow-tooltip sortable />
            <el-table-column prop="department" label="部门" width="120" sortable />
            <el-table-column prop="applicationCount" label="投递量" width="100" align="center" sortable />
            <el-table-column prop="screeningPassed" label="筛选通过" width="100" align="center" sortable />
            <el-table-column prop="interviewCount" label="面试数" width="100" align="center" sortable />
            <el-table-column prop="hireCount" label="录用数" width="100" align="center" sortable />
            <el-table-column prop="conversionRate" label="转化率" width="100" align="center" sortable />
            <el-table-column prop="avgCycle" label="平均周期(天)" width="120" align="center" sortable />
          </el-table>
        </el-tab-pane>

        <!-- 按渠道分析 -->
        <el-tab-pane label="按渠道分析" name="channel">
          <el-row :gutter="16">
            <el-col :span="12">
              <div ref="chartChannelPie" class="chart-container"></div>
            </el-col>
            <el-col :span="12">
              <div ref="chartChannelBar" class="chart-container"></div>
            </el-col>
          </el-row>
          <div class="table-header">
            <span class="table-title">渠道维度明细数据</span>
            <el-button type="primary" link @click="exportTable('channel')">
              <el-icon><Download /></el-icon> 导出表格
            </el-button>
          </div>
          <el-table :data="tableData" border stripe style="margin-top: 8px"
                    :default-sort="{ prop: 'applicationCount', order: 'descending' }">
            <el-table-column prop="channel" label="渠道名称" min-width="150" sortable />
            <el-table-column prop="applicationCount" label="投递量" width="100" align="center" sortable />
            <el-table-column prop="screeningPassed" label="筛选通过" width="100" align="center" sortable />
            <el-table-column prop="interviewCount" label="面试数" width="100" align="center" sortable />
            <el-table-column prop="hireCount" label="录用数" width="100" align="center" sortable />
            <el-table-column prop="conversionRate" label="转化率" width="100" align="center" sortable />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

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
          <el-input v-model="exportConfig.fileName" placeholder="招聘效果分析_YYYYMMDD" />
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
import { Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getAnalysis, exportReportExcel } from '../../api/report'

// refs
const chartTime = ref(null)
const chartJobComp = ref(null)
const chartJobCycle = ref(null)
const chartChannelPie = ref(null)
const chartChannelBar = ref(null)

let chartInstances = []
const loading = ref(false)
const exporting = ref(false)
const exportDialogVisible = ref(false)
const activeTab = ref('time')
const dateRangeType = ref('all')
const analysisData = ref({})
const tableData = ref([])

const filterForm = reactive({ dateRange: [] })
const exportConfig = reactive({ scope: 'current', fileName: '' })

// KPI 卡片
const kpiCards = computed(() => {
  const kpi = analysisData.value.kpi || {}
  return [
    { label: '平均招聘周期', value: (kpi.avgRecruitmentCycle || '0') + '天', color: '#409EFF', desc: '从投递到录用接受' },
    { label: '简历筛选通过率', value: (kpi.screeningPassRate || '0') + '%', color: '#67C23A', desc: '进入面试/总投递' },
    { label: 'Offer接受率', value: (kpi.offerAcceptRate || '0') + '%', color: '#F56C6C', desc: '接受Offer/发放Offer' }
  ]
})

function formatDate(d) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function handleDateRangeChange(type) {
  const now = new Date()
  if (type === 'all') {
    filterForm.dateRange = []
  } else if (type === 'month') {
    filterForm.dateRange = [formatDate(new Date(now.getFullYear(), now.getMonth(), 1)), formatDate(now)]
  } else if (type === 'quarter') {
    const qMonth = Math.floor(now.getMonth() / 3) * 3
    filterForm.dateRange = [formatDate(new Date(now.getFullYear(), qMonth, 1)), formatDate(now)]
  } else if (type === 'year') {
    filterForm.dateRange = [formatDate(new Date(now.getFullYear() - 1, now.getMonth(), now.getDate())), formatDate(now)]
  }
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params = { dimension: activeTab.value }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    const res = await getAnalysis(params)
    if (res.code === 200) {
      analysisData.value = res.data || {}
      tableData.value = res.data.tableData || []
      await nextTick()
      renderCurrentTabCharts()
    }
  } catch (err) {
    console.error('分析数据加载失败:', err)
    ElMessage.error('数据加载失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  loadData()
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

function renderCurrentTabCharts() {
  destroyCharts()
  if (activeTab.value === 'time') renderTimeCharts()
  else if (activeTab.value === 'job') renderJobCharts()
  else if (activeTab.value === 'channel') renderChannelCharts()
}

// 按时间分析图表
function renderTimeCharts() {
  const chart = initChart(chartTime.value)
  if (!chart) return
  const data = analysisData.value.chartData || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['投递量', '面试量', '录用量'], bottom: 0 },
    grid: { left: '8%', right: '5%', bottom: '15%', top: '10%' },
    xAxis: { type: 'category', data: data.map(d => d.period) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '投递量', type: 'bar', data: data.map(d => d.applicationCount), itemStyle: { color: '#409EFF' } },
      { name: '面试量', type: 'bar', data: data.map(d => d.interviewCount), itemStyle: { color: '#E6A23C' } },
      { name: '录用量', type: 'line', data: data.map(d => d.hireCount), itemStyle: { color: '#67C23A' }, smooth: true }
    ]
  })
}

// 按岗位分析图表
function renderJobCharts() {
  // 分组柱状图
  const chart1 = initChart(chartJobComp.value)
  if (chart1) {
    const data = analysisData.value.comparisonChart || []
    chart1.setOption({
      title: { text: '各岗位投递/面试/录用对比', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis' },
      legend: { data: ['投递量', '面试量', '录用量'], bottom: 0 },
      grid: { left: '10%', right: '5%', bottom: '15%', top: '15%' },
      xAxis: { type: 'category', data: data.map(d => d.jobTitle), axisLabel: { rotate: 30, fontSize: 10 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: '投递量', type: 'bar', data: data.map(d => d.applicationCount), itemStyle: { color: '#409EFF' } },
        { name: '面试量', type: 'bar', data: data.map(d => d.interviewCount), itemStyle: { color: '#E6A23C' } },
        { name: '录用量', type: 'bar', data: data.map(d => d.hireCount), itemStyle: { color: '#67C23A' } }
      ]
    })
  }

  // 招聘周期条形图
  const chart2 = initChart(chartJobCycle.value)
  if (chart2) {
    const data = analysisData.value.cycleChart || []
    const sorted = [...data].sort((a, b) => b.avgCycle - a.avgCycle)
    chart2.setOption({
      title: { text: '各岗位招聘周期对比', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis', formatter: '{b}<br/>平均周期: {c}天' },
      grid: { left: '30%', right: '10%', bottom: '10%', top: '15%' },
      xAxis: { type: 'value', name: '天' },
      yAxis: { type: 'category', data: sorted.map(d => d.jobTitle).reverse(), axisLabel: { width: 80, overflow: 'truncate' } },
      series: [{
        type: 'bar', data: sorted.map(d => d.avgCycle).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#E6A23C' }, { offset: 1, color: '#F56C6C' }
          ]),
          borderRadius: [0, 4, 4, 0]
        },
        label: { show: true, position: 'right', formatter: '{c}天' }
      }]
    })
  }
}

// 按渠道分析图表
function renderChannelCharts() {
  // 饼图
  const chart1 = initChart(chartChannelPie.value)
  if (chart1) {
    const data = analysisData.value.pieData || []
    chart1.setOption({
      title: { text: '各渠道投递量占比', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie', radius: ['35%', '65%'], center: ['50%', '45%'],
        label: { show: true, formatter: '{b}\n{d}%' },
        data: data
      }]
    })
  }

  // 录用人数对比柱状图
  const chart2 = initChart(chartChannelBar.value)
  if (chart2) {
    const data = analysisData.value.tableData || []
    chart2.setOption({
      title: { text: '各渠道录用人数对比', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis' },
      grid: { left: '15%', right: '10%', bottom: '15%', top: '15%' },
      xAxis: { type: 'category', data: data.map(d => d.channel), axisLabel: { rotate: 30, fontSize: 10 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        { name: '投递量', type: 'bar', data: data.map(d => d.applicationCount), itemStyle: { color: '#409EFF' } },
        { name: '录用数', type: 'bar', data: data.map(d => d.hireCount), itemStyle: { color: '#67C23A' } }
      ]
    })
  }
}

// 导出
function showExportDialog() {
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  exportConfig.fileName = `招聘效果分析_${dateStr}`
  exportDialogVisible.value = true
}

async function handleExport() {
  exporting.value = true
  try {
    const params = { exportType: 'analysis' }
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    const res = await exportReportExcel(params)
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${exportConfig.fileName || '招聘效果分析'}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    exportDialogVisible.value = false
    ElMessage.success('导出成功')
  } catch (err) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 前端导出表格
function exportTable(type) {
  if (!tableData.value || tableData.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  // 使用简单CSV导出
  const headers = Object.keys(tableData.value[0])
  const csvContent = [
    headers.join(','),
    ...tableData.value.map(row => headers.map(h => row[h] ?? '').join(','))
  ].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${type}_analysis_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  window.URL.revokeObjectURL(url)
  ElMessage.success('表格导出成功')
}

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
.report-analysis { padding: 20px; }
.filter-card { margin-bottom: 0; }
.kpi-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}
.kpi-card:hover { transform: translateY(-4px); }
.kpi-value { font-size: 28px; font-weight: bold; line-height: 1.3; }
.kpi-label { font-size: 14px; color: #303133; margin-top: 4px; font-weight: 500; }
.kpi-desc { font-size: 12px; color: #909399; margin-top: 2px; }
.chart-container { height: 300px; width: 100%; }
.chart-container-lg { height: 360px; width: 100%; }
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding: 8px 0;
  border-bottom: 1px solid #EBEEF5;
}
.table-title { font-size: 15px; font-weight: 600; color: #303133; }
</style>

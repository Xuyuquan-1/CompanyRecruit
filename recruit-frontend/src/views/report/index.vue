<template>
  <div class="report-page">
    <el-row :gutter="16">
      <el-col :span="6" v-for="item in statCards" :key="item.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <div style="text-align:right;margin-top:12px">
      <el-button type="success" icon="Download" @click="handleExport">导出 Excel</el-button>
    </div>

    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card shadow="never" header="各岗位投递统计">
          <el-table :data="jobStats" border stripe size="small">
            <el-table-column prop="title" label="岗位" />
            <el-table-column prop="department" label="部门" width="100" />
            <el-table-column prop="appCount" label="应聘数" width="80" align="center" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="招聘转化漏斗">
          <div class="funnel">
            <div class="funnel-item" style="width:100%; background:#409EFF">
              总应聘数: {{ progressStats.totalApplications }}
            </div>
            <div class="funnel-item" style="width:75%; background:#67C23A">
              面试数: {{ progressStats.totalInterviews }}
            </div>
            <div class="funnel-item" style="width:45%; background:#E6A23C">
              录用数: {{ progressStats.totalOffers }}
            </div>
            <div class="funnel-item" style="width:25%; background:#F56C6C">
              到岗数: {{ progressStats.hiredCount }}
            </div>
          </div>
          <el-descriptions :column="2" border style="margin-top:16px">
            <el-descriptions-item label="岗位总数">{{ progressStats.totalJobs }}</el-descriptions-item>
            <el-descriptions-item label="招聘中岗位">{{ progressStats.activeJobs }}</el-descriptions-item>
            <el-descriptions-item label="转化率">{{ effectAnalysis.conversionRate }}</el-descriptions-item>
            <el-descriptions-item label="面试通过率">{{ channelStats.passRate }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProgressStats, getEffectAnalysis, getStatsByJob, getStatsByChannel, exportReportExcel } from '../../api/report'

const progressStats = ref({}), effectAnalysis = ref({}), jobStats = ref([]), channelStats = ref({})

const statCards = ref([
  { label: '岗位总数', value: 0, color: '#409EFF' },
  { label: '简历总数', value: 0, color: '#67C23A' },
  { label: '面试总数', value: 0, color: '#E6A23C' },
  { label: '录用总数', value: 0, color: '#F56C6C' }
])

async function loadAll() {
  try {
    const [p, e, j, c] = await Promise.all([getProgressStats(), getEffectAnalysis(), getStatsByJob(), getStatsByChannel()])
    progressStats.value = p.data
    effectAnalysis.value = e.data
    jobStats.value = (j.data && j.data.list) || []
    channelStats.value = c.data
    statCards.value[0].value = p.data?.totalJobs || 0
    statCards.value[1].value = p.data?.totalApplications || 0
    statCards.value[2].value = p.data?.totalInterviews || 0
    statCards.value[3].value = p.data?.totalOffers || 0
  } catch (err) {
    console.error('报表数据加载失败:', err)
  }
}

async function handleExport() {
  try {
    const res = await exportReportExcel()
    const blob = new Blob([res.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '招聘统计报表.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

onMounted(loadAll)
</script>

<style scoped>
.stat-card { text-align:center; cursor:pointer }
.stat-value { font-size:32px; font-weight:bold }
.stat-label { font-size:14px; color:#909399; margin-top:8px }
.funnel { display:flex; flex-direction:column; align-items:center; gap:8px }
.funnel-item { color:#fff; padding:10px; text-align:center; border-radius:4px; font-weight:bold; transition:width 0.5s }
</style>
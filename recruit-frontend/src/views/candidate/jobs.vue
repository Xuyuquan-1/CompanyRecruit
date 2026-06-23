<template>
  <div class="candidate-jobs">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="岗位名称">
          <el-input v-model="queryForm.keyword" placeholder="搜索岗位" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="queryForm.department" placeholder="全部" clearable style="width:150px">
            <el-option label="技术部" value="技术部" />
            <el-option label="产品部" value="产品部" />
            <el-option label="市场部" value="市场部" />
            <el-option label="数据部" value="数据部" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 岗位列表 -->
    <el-card shadow="never" style="margin-top:15px">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column prop="title" label="岗位名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="department" label="部门" width="100" align="center" />
        <el-table-column prop="experience" label="经验要求" width="100" align="center" />
        <el-table-column prop="education" label="学历要求" width="100" align="center" />
        <el-table-column prop="salary" label="薪资" width="120" align="center">
          <template #default="{ row }">{{ formatSalary(row.salary) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="160">
          <template #default="{ row }">{{ formatDate(row.publishTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link icon="View" @click="viewDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 1 && !hasApplied(row.id)"
              type="success"
              link
              icon="Upload"
              @click="applyJob(row)"
            >
              投递简历
            </el-button>
            <el-tag v-if="hasApplied(row.id)" type="success" size="small">已投递</el-tag>
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

    <!-- 岗位详情对话框 -->
    <el-dialog v-model="detailVisible" title="岗位详情" width="700px">
      <el-descriptions :column="2" border v-if="currentJob">
        <el-descriptions-item label="岗位名称" :span="2">{{ currentJob.title }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentJob.department }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ currentJob.headcount || 1 }}</el-descriptions-item>
        <el-descriptions-item label="经验要求">{{ currentJob.experience || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ currentJob.education || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围" :span="2">{{ formatSalary(currentJob.salary) }}</el-descriptions-item>
        <el-descriptions-item label="工作地点" :span="2">{{ currentJob.location || '待定' }}</el-descriptions-item>
        <el-descriptions-item label="岗位职责" :span="2">
          <div style="white-space:pre-wrap">{{ currentJob.description || '暂无' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="任职要求" :span="2">
          <div style="white-space:pre-wrap">{{ currentJob.requirements || '暂无' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentJob?.status === 1 && !hasApplied(currentJob.id)"
          type="primary"
          @click="applyJob(currentJob)"
        >
          投递简历
        </el-button>
      </template>
    </el-dialog>

    <!-- 选择简历对话框 -->
    <el-dialog v-model="resumeVisible" title="选择简历" width="600px">
      <el-alert
        :title="`您正在申请【${currentJob?.title}】岗位，请选择要投递的简历`"
        type="info"
        :closable="false"
        style="margin-bottom:15px"
      />
      <el-table :data="myResumes" border stripe v-loading="resumeLoading" style="width:100%">
        <el-table-column prop="originalFilename" label="简历名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="tags" label="技能标签" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="t in (row.tags||'').split(',').filter(Boolean)" :key="t" size="small" style="margin:2px">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="160">
          <template #default="{ row }">{{ formatDate(row.uploadTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="confirmApply(row)">确认投递</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="resumeVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getJobPage } from '../../api/job'
import { getMyResumes, getMyApplications, submitApplication } from '../../api/application'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  department: ''
})

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const appliedJobs = ref(new Set()) // 已投递的岗位ID集合

// 岗位详情
const detailVisible = ref(false)
const currentJob = ref(null)

// 简历选择
const resumeVisible = ref(false)
const resumeLoading = ref(false)
const myResumes = ref([])

// 加载岗位列表
async function loadData() {
  loading.value = true
  try {
    const res = await getJobPage(queryForm)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0

      // 检查哪些岗位已投递
      checkAppliedJobs()
    }
  } catch (e) {
    console.error('加载失败:', e)
  } finally {
    loading.value = false
  }
}

// 检查已投递的岗位
async function checkAppliedJobs() {
  try {
    const res = await getMyApplications({ pageNum: 1, pageSize: 100 })
    if (res.code === 200) {
      const applications = res.data?.records || []
      appliedJobs.value = new Set(applications.map(app => app.jobId))
    }
  } catch (e) {
    console.error('检查投递状态失败:', e)
  }
}

// 判断是否已投递
function hasApplied(jobId) {
  return appliedJobs.value.has(jobId)
}

// 搜索
function handleSearch() {
  queryForm.pageNum = 1
  loadData()
}

// 重置
function handleReset() {
  queryForm.keyword = ''
  queryForm.department = ''
  queryForm.pageNum = 1
  loadData()
}

// 查看详情
function viewDetail(row) {
  currentJob.value = row
  detailVisible.value = true
}

// 申请职位
async function applyJob(row) {
  // 加载我的简历列表
  resumeLoading.value = true
  try {
    const res = await getMyResumes()
    if (res.code === 200) {
      myResumes.value = res.data || []
      if (myResumes.value.length === 0) {
        ElMessage.warning('您还没有创建简历，请先创建简历')
        return
      }
      currentJob.value = row
      resumeVisible.value = true
    }
  } catch (e) {
    console.error('加载简历失败:', e)
  } finally {
    resumeLoading.value = false
  }
}

// 确认投递
async function confirmApply(resume) {
  try {
    await submitApplication({
      jobId: currentJob.value.id,
      resumeId: resume.id
    })
    ElMessage.success('投递成功！')
    resumeVisible.value = false
    appliedJobs.value.add(currentJob.value.id)
    loadData()
  } catch (e) {
    console.error('投递失败:', e)
  }
}

/** 状态标签类型 */
function statusTagType(status) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

/** 状态文本 */
function statusLabel(status) {
  const map = { 0: '草稿', 1: '已发布', 2: '已关闭' }
  return map[status] || '未知'
}

// 格式化日期
function formatDate(d) {
  return d ? d.replace('T', ' ').substring(0, 16) : ''
}

// 格式化薪资
function formatSalary(salary) {
  if (!salary) return '面议'
  return salary + 'k/月'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.candidate-jobs {
  padding: 20px;
}

.search-card {
  margin-bottom: 15px;
}
</style>
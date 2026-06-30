<template>
  <div class="resume-manage">
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="搜索文件名" clearable style="width:200px" />
        </el-form-item>
        <template v-if="canManage()">
          <el-form-item label="解析状态">
            <el-select v-model="queryForm.parseStatus" placeholder="全部" clearable style="width:150px">
              <el-option label="待解析" :value="0" />
              <el-option label="解析成功" :value="1" />
              <el-option label="解析失败" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="关联岗位">
            <el-select v-model="queryForm.jobId" placeholder="全部岗位" clearable filterable style="width:180px">
              <el-option v-for="j in jobList" :key="j.id" :label="j.title" :value="j.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="技能标签">
            <el-select v-model="queryForm.tag" placeholder="选择标签" clearable filterable style="width:180px">
              <el-option label="Java" value="Java" />
              <el-option label="Spring Boot" value="Spring Boot" />
              <el-option label="Vue.js" value="Vue.js" />
              <el-option label="React" value="React" />
              <el-option label="MySQL" value="MySQL" />
              <el-option label="Redis" value="Redis" />
              <el-option label="Python" value="Python" />
              <el-option label="产品设计" value="产品设计" />
              <el-option label="测试" value="测试" />
              <el-option label="数据分析" value="数据分析" />
            </el-select>
          </el-form-item>
        </template>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadData">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:15px">
      <div class="table-header">
        <!-- 只有有上传权限的用户才能看到上传按钮 -->
        <el-upload v-if="userStore.isCandidate()" :show-file-list="false" :before-upload="handleUpload" accept=".pdf,.doc,.docx" :http-request="()=>{}">
          <el-button type="primary" icon="Upload">上传简历</el-button>
        </el-upload>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top:10px">
        <el-table-column prop="originalFilename" label="文件名" min-width="180" />
        <el-table-column v-if="canManage()" prop="jobTitle" label="关联岗位" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.jobTitle || '未投递' }}
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="文件大小" width="100" align="center">
          <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column v-if="canManage()" prop="parseStatus" label="解析状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="parseStatusType(row.parseStatus)">{{ parseStatusLabel(row.parseStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="uploadTime" label="上传时间" width="170">
          <template #default="{ row }">{{ formatDate(row.uploadTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="canManage()">
              <el-button type="primary" link icon="View" @click="handleView(row)">查看</el-button>
              <el-button type="info" link icon="Download" @click="handleDownload(row)">下载</el-button>
              <el-button type="success" link icon="MagicStick" @click="handleParse(row)">AI解析</el-button>
              <el-button type="warning" link icon="Edit" @click="handleEdit(row)">修正</el-button>
              <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
            </template>
            <!-- 应聘者：仅下载 -->
            <template v-else>
              <el-button type="primary" link icon="Download" @click="handleDownload(row)">下载</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[5,10,20,50]" :total="total" layout="total,sizes,prev,pager,next,jumper"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="viewVisible" title="简历详情" width="700px">
      <el-descriptions :column="2" border v-if="parsedData">
        <el-descriptions-item label="姓名">{{ parsedData.name }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ parsedData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ parsedData.email }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ parsedData.education }}</el-descriptions-item>
        <el-descriptions-item label="毕业院校">{{ parsedData.school }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ parsedData.major }}</el-descriptions-item>
        <el-descriptions-item label="毕业年份">{{ parsedData.graduationYear }}</el-descriptions-item>
        <el-descriptions-item label="当前职位">{{ parsedData.currentPosition }}</el-descriptions-item>
        <el-descriptions-item label="工作年限">{{ parsedData.experience }}</el-descriptions-item>
        <el-descriptions-item label="期望薪资">{{ parsedData.expectedSalary }}</el-descriptions-item>
        <el-descriptions-item label="技能" :span="2">{{ parsedData.skills }}</el-descriptions-item>
        <el-descriptions-item label="关联岗位" :span="2">{{ currentResume.jobTitle || '未投递' }}</el-descriptions-item>
        <el-descriptions-item label="应聘状态" :span="2" v-if="currentResume.applicationId">
          <el-tag :type="applicationStatusType(currentResume.applicationStatus)">
            {{ applicationStatusLabel(currentResume.applicationStatus) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="尚未解析或数据为空" />
      
      <!-- 如果已投递到岗位且状态为待筛选，显示筛选操作按钮 -->
      <div v-if="currentResume.applicationId && currentResume.applicationStatus === 0" style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee">
        <h4 style="margin: 0 0 15px 0">筛选操作</h4>
        <el-button type="success" icon="Select" @click="handlePass(currentResume)">通过筛选</el-button>
        <el-button type="danger" icon="CloseBold" @click="handleReject(currentResume)">不通过</el-button>
      </div>
      
      <!-- 如果已投递但不在待筛选状态，显示状态提示 -->
      <div v-else-if="currentResume.applicationId && currentResume.applicationStatus !== 0" style="margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee">
        <el-alert type="info" :closable="false" show-icon>
          <template #title>
            该简历当前状态为：<el-tag :type="applicationStatusType(currentResume.applicationStatus)" size="small">
              {{ applicationStatusLabel(currentResume.applicationStatus) }}
            </el-tag>
            ，无需重复筛选
          </template>
        </el-alert>
      </div>
    </el-dialog>

    <!-- 修正对话框 -->
    <el-dialog v-model="editVisible" title="修正解析数据" width="600px" destroy-on-close @closed="resetEditForm">
      <el-form ref="editFormRef" :model="editForm" :rules="editFormRules" label-width="90px">
        <el-form-item label="姓名"><el-input v-model="editForm.name" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
        <el-form-item label="学历">
          <el-select v-model="editForm.education" style="width:100%">
            <el-option label="博士" value="博士" /><el-option label="硕士" value="硕士" />
            <el-option label="本科" value="本科" /><el-option label="大专" value="大专" />
          </el-select>
        </el-form-item>
        <el-form-item label="毕业院校"><el-input v-model="editForm.school" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="editForm.major" /></el-form-item>
        <el-form-item label="毕业年份"><el-input v-model="editForm.graduationYear" /></el-form-item>
        <el-form-item label="工作年限"><el-input v-model="editForm.experience" /></el-form-item>
        <el-form-item label="期望薪资"><el-input v-model="editForm.expectedSalary" /></el-form-item>
        <el-form-item label="当前职位"><el-input v-model="editForm.currentPosition" /></el-form-item>
        <el-form-item label="技能标签"><el-input v-model="editForm.skills" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getResumePage, uploadResume, parseResume, updateParsedData, deleteResume, downloadResume } from '../../api/resume'
import { getPublishedJobs } from '../../api/job'
import { passApplication, rejectApplication } from '../../api/application'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
// 判断是否为管理员或招聘人员（有管理权限）
const canManage = () => userStore.hasPermission('resume:manage')

const loading = ref(false), tableData = ref([]), total = ref(0), jobList = ref([])
const queryForm = reactive({ keyword: '', parseStatus: null, jobId: null, tag: null, pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try { const res = await getResumePage(queryForm); tableData.value = res.data.records; total.value = res.data.total } catch {} finally { loading.value = false }
}
function resetQuery() { queryForm.keyword = ''; queryForm.parseStatus = null; queryForm.jobId = null; queryForm.tag = null; queryForm.pageNum = 1; loadData() }

async function handleUpload(file) {
  const allowed = ['.pdf', '.doc', '.docx'];
  const ext = '.' + file.name.split('.').pop().toLowerCase();
  if (!allowed.includes(ext)) { ElMessage.error('仅支持PDF或Word格式'); return false }
  if (file.size > 10 * 1024 * 1024) { ElMessage.error('文件不能超过10MB'); return false }
  try {
    await uploadResume(file)
    ElMessage.success('上传成功')
    loadData()
  } catch (e) { ElMessage.error('上传失败，请稍后重试') }
  return false
}

async function handleParse(row) {
  try {
    await parseResume(row.id)
    ElMessage.success('解析完成')
    loadData()
  } catch {}
}

// 下载简历文件
async function handleDownload(row) {
  try {
    // 统一通过后端接口下载（支持OSS和本地存储）
    const res = await downloadResume(row.id)
    // 创建 blob URL 并触发下载
    const blob = new Blob([res.data], { type: 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = row.originalFilename || `resume_${row.id}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (e) {
    console.error('下载失败:', e)
    ElMessage.error('下载失败')
  }
}

// 查看
const viewVisible = ref(false), parsedData = ref(null), currentResume = ref({})
function handleView(row) {
  console.log('查看简历:', row)  // 调试日志
  currentResume.value = row  // 保存当前简历信息
  
  if (!row.parsedJson) {
    parsedData.value = null
  } else {
    try {
      const raw = JSON.parse(row.parsedJson)
      // 从 education 数组中提取第一条学历信息
      const edu = Array.isArray(raw.education) && raw.education.length > 0 ? raw.education[0] : (raw.education || {})
      const school = raw.school || edu.school || ''
      const major = raw.major || edu.major || ''
      const degree = raw.degree || edu.degree || (typeof raw.education === 'string' ? raw.education : '')
      const graduationYear = raw.graduationYear || edu.endDate || ''
      // skills 数组转字符串
      const skills = Array.isArray(raw.skills) ? raw.skills.join('、') : (raw.skills || '')
      parsedData.value = {
        ...raw,
        education: degree,
        school,
        major,
        graduationYear,
        skills
      }
    } catch (e) {
      console.error('解析简历数据失败:', e)
      parsedData.value = null
    }
  }
  viewVisible.value = true
}

// 修正
const editVisible = ref(false), editId = ref(null)
const editForm = reactive({
  name: '', phone: '', email: '', education: '',
  school: '', major: '', graduationYear: '',
  experience: '', expectedSalary: '', currentPosition: '', skills: ''
})

const editFormRules = {
  name: [{ required: true, message: '姓名和电话为必填项', trigger: 'blur' }],
  phone: [{ required: true, message: '姓名和电话为必填项', trigger: 'blur' }]
}
function handleEdit(row) {
  editId.value = row.id
  const raw = row.parsedJson ? JSON.parse(row.parsedJson) : {}
  const edu = Array.isArray(raw.education) && raw.education.length > 0 ? raw.education[0] : (raw.education || {})
  const work = Array.isArray(raw.work_experience) && raw.work_experience.length > 0 ? raw.work_experience[0] : (raw.work_experience || {})
  const skills = Array.isArray(raw.skills) ? raw.skills.join('、') : (raw.skills || '')
  editForm.name = raw.name || ''
  editForm.phone = raw.phone || ''
  editForm.email = raw.email || ''
  editForm.education = raw.degree || edu.degree || ''
  editForm.school = raw.school || edu.school || ''
  editForm.major = raw.major || edu.major || ''
  editForm.graduationYear = raw.graduationYear || edu.endDate || ''
  editForm.experience = raw.experience || ''
  editForm.expectedSalary = raw.expectedSalary || ''
  editForm.currentPosition = raw.currentPosition || (work.position || '')
  editForm.skills = skills
  editVisible.value = true
}
function resetEditForm() {
  editForm.name = ''
  editForm.phone = ''
  editForm.email = ''
  editForm.education = ''
  editForm.school = ''
  editForm.major = ''
  editForm.graduationYear = ''
  editForm.experience = ''
  editForm.expectedSalary = ''
  editForm.currentPosition = ''
  editForm.skills = ''
  editId.value = null
}
async function handleSaveEdit() {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await updateParsedData(editId.value, { ...editForm })
    ElMessage.success('修正已保存')
    editVisible.value = false
    loadData()
  } catch {}
}

async function handlePass(row) {
  if (!row.applicationId) { ElMessage.warning('该简历尚未投递到岗位，无法操作'); return }
  try {
    await ElMessageBox.confirm('确认通过该简历筛选？', '提示', { type: 'warning' })
    await passApplication(row.applicationId)
    ElMessage.success('已通过筛选')
    viewVisible.value = false  // 关闭详情弹窗
    loadData()
  } catch {}
}

async function handleReject(row) {
  if (!row.applicationId) { ElMessage.warning('该简历尚未投递到岗位，无法操作'); return }
  try {
    const { value: refuseType } = await ElMessageBox.prompt(
      '请选择失败原因：1-简历淘汰 2-面试淘汰 3-候选人拒Offer 4-审批不通过 5-岗位关闭终止',
      '不录用原因',
      {
        inputPlaceholder: '输入原因编号（1-5）',
        inputPattern: /^[1-5]$/,
        inputErrorMessage: '请输入1-5之间的数字'
      }
    )
    await rejectApplication(row.applicationId, parseInt(refuseType))
    ElMessage.success('已标记为不录用')
    viewVisible.value = false  // 关闭详情弹窗
    loadData()
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该简历？', '提示', { type: 'warning' })
  try { await deleteResume(row.id); ElMessage.success('删除成功'); loadData() } catch {}
}

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 19) : '' }
function formatSize(s) { return s ? (s > 1048576 ? (s/1048576).toFixed(1)+'MB' : (s/1024).toFixed(0)+'KB') : '-' }
function parseStatusType(s) { return s === 1 ? 'success' : s === 2 ? 'danger' : 'info' }
function parseStatusLabel(s) { return s === 1 ? '已解析' : s === 2 ? '失败' : '待解析' }

// 应聘状态类型
function applicationStatusType(s) {
  const map = { 0: 'info', 1: 'warning', 2: '', 3: 'warning', 4: 'danger', 5: 'success', 6: 'success', 7: 'info' }
  return map[s] || 'info'
}

// 应聘状态标签
function applicationStatusLabel(s) {
  const map = {
    0: '待筛选',
    1: '通过筛选',
    2: '面试中',
    3: '待确认Offer',
    4: '不录用',
    5: '已接受Offer(待入职)',
    6: '已入职'
  }
  return map[s] || '未知'
}

async function loadJobList() {
  try { const res = await getPublishedJobs(); jobList.value = res.data || [] } catch {}
}

onMounted(() => { loadData(); loadJobList() })
</script>

<style scoped>
.table-header { display:flex; justify-content:space-between; align-items:center }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:15px }
</style>

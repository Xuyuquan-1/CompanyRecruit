<template>
  <div class="my-offers">
    <el-card shadow="never" class="header-card">
      <h3 style="margin:0">我的录用通知</h3>
      <p style="color:#999;margin:8px 0 0">查看Offer详情、接受/拒绝、提交入职资料</p>
    </el-card>

    <el-card shadow="never" style="margin-top:15px">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="jobTitle" label="岗位" min-width="150" show-overflow-tooltip />
        <el-table-column prop="offerTime" label="发送时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.offerTime) }}</template>
        </el-table-column>
        <el-table-column prop="salary" label="薪资" width="120" align="center">
          <template #default="{ row }">{{ formatSalary(row.salary) }}</template>
        </el-table-column>
        <el-table-column prop="expectedJoinDate" label="期望入职日期" width="120" align="center">
          <template #default="{ row }">{{ formatDate(row.expectedJoinDate) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">
              {{ statusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 待确认状态 -->
            <template v-if="row.status === 0">
              <el-button type="success" link icon="Check" @click="handleAcceptOffer(row)">接受Offer</el-button>
              <el-button type="danger" link icon="Close" @click="handleRejectOffer(row)">拒绝Offer</el-button>
            </template>
            <!-- 已接受状态 -->
            <el-button 
              v-if="row.status === 1 && !row.docsSubmitted" 
              type="primary" 
              link 
              icon="Upload" 
              @click="openSubmitDocs(row)"
            >
              提交入职资料
            </el-button>
            <el-button type="info" link icon="View" @click="viewDetail(row)">详情</el-button>
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

    <!-- Offer详情对话框 -->
    <el-dialog v-model="detailVisible" title="Offer详情" width="700px">
      <el-descriptions :column="2" border v-if="currentOffer">
        <el-descriptions-item label="岗位" :span="2">{{ currentOffer.jobTitle }}</el-descriptions-item>
        <el-descriptions-item label="发送时间">{{ formatDateTime(currentOffer.offerTime) }}</el-descriptions-item>
        <el-descriptions-item label="期望入职日期">{{ formatDate(currentOffer.expectedJoinDate) }}</el-descriptions-item>
        <el-descriptions-item label="薪资待遇" :span="2">{{ formatSalary(currentOffer.salary) }}</el-descriptions-item>
        <el-descriptions-item label="福利补贴" :span="2">
          {{ currentOffer.benefits || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">
          {{ currentOffer.remark || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="statusType(currentOffer.status)">
            {{ statusLabel(currentOffer.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 提交入职资料对话框 -->
    <el-dialog v-model="submitDocsVisible" title="提交入职资料" width="650px">
      <el-alert 
        title="请提交以下入职资料，招聘者审核通过后将为您办理入职手续" 
        type="info" 
        :closable="false"
        style="margin-bottom:15px"
      />
      <el-form :model="submitDocsForm" label-width="110px">
        <el-form-item label="身份证正面">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'idCardFront')"
            :before-upload="beforeUpload"
            accept="image/*,.pdf"
            :limit="1"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png/pdf格式，不超过10MB</div>
            </template>
          </el-upload>
          <div v-if="submitDocsForm.idCardFrontUrl" style="color:#67c23a;margin-top:5px">
            ✓ 已上传
          </div>
        </el-form-item>
        <el-form-item label="身份证反面">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'idCardBack')"
            :before-upload="beforeUpload"
            accept="image/*,.pdf"
            :limit="1"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png/pdf格式，不超过10MB</div>
            </template>
          </el-upload>
          <div v-if="submitDocsForm.idCardBackUrl" style="color:#67c23a;margin-top:5px">
            ✓ 已上传
          </div>
        </el-form-item>
        
        <el-form-item label="体检报告">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'medicalReport')"
            :before-upload="beforeUpload"
            accept="image/*,.pdf"
            :limit="1"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png/pdf格式，不超过10MB</div>
            </template>
          </el-upload>
          <div v-if="submitDocsForm.medicalReportUrl" style="color:#67c23a;margin-top:5px">
            ✓ 已上传
          </div>
        </el-form-item>
        
        <el-form-item label="劳动合同">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'contract')"
            :before-upload="beforeUpload"
            accept=".pdf"
            :limit="1"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持pdf格式，不超过10MB</div>
            </template>
          </el-upload>
          <div v-if="submitDocsForm.contractUrl" style="color:#67c23a;margin-top:5px">
            ✓ 已上传
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDocsVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handlesubmitDocuments">提交资料</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyOffers, acceptOffer, rejectOffer, submitDocuments } from '../../api/offer'
import { uploadFile } from '../../api/oss'

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
const currentOffer = ref(null)

// 提交资料
const submitDocsVisible = ref(false)
const uploading = ref(false)
const submitDocsForm = reactive({
  offerId: null,
  idCardFrontUrl: '',
  idCardBackUrl: '',
  medicalReportUrl: '',
  contractUrl: ''
})

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const res = await getMyOffers(queryForm)
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
  currentOffer.value = row
  detailVisible.value = true
}

// 接受Offer
async function handleAcceptOffer(row) {
  await ElMessageBox.confirm('确认接受此录用通知？', '提示', { type: 'success' })
  try {
    await acceptOffer(row.id)
    ElMessage.success('已接受Offer，请提交入职资料')
    loadData()
  } catch (e) {
    console.error('接受失败:', e)
  }
}

// 拒绝Offer
async function handleRejectOffer(row) {
  const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因（可选）', '拒绝Offer', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  try {
    await rejectOffer(row.id, reason || '')
    ElMessage.success('已拒绝Offer')
    loadData()
  } catch (e) {
    console.error('拒绝失败:', e)
  }
}

// 打开提交资料对话框
function openSubmitDocs(row) {
  submitDocsForm.offerId = row.id
  submitDocsForm.idCardFrontUrl = ''
  submitDocsForm.idCardBackUrl = ''
  submitDocsForm.medicalReportUrl = ''
  submitDocsForm.contractUrl = ''
  submitDocsVisible.value = true
}

// 文件上传前验证
function beforeUpload(file) {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB！')
    return false
  }
  return true
}

// 文件选择后自动上传
async function handleFileChange(file, type) {
  try {
    const res = await uploadFile(file.raw)
    console.log('上传返回结果:', res)
    console.log('res.data:', res.data)
    console.log('res.data类型:', typeof res.data)
    
    if (res.code === 200) {
      // 确保 res.data 是字符串
      let url = res.data
      console.log('处理前的url:', url, '类型:', typeof url)
      
      if (typeof url === 'object') {
        // 如果是对象，尝试获取 url 字段
        console.log('res.data 是对象，尝试提取URL字段')
        url = url.url || url.downloadUrl || url.fileUrl || ''
        console.log('提取后的url:', url)
      }
      
      // 最终确认是字符串
      if (typeof url !== 'string') {
        console.error('url 不是字符串类型！', typeof url, url)
        url = String(url || '')
      }
      
      // 根据类型设置对应的URL
      if (type === 'idCardFront') {
        submitDocsForm.idCardFrontUrl = url
        console.log('设置 idCardFrontUrl:', url)
      } else if (type === 'idCardBack') {
        submitDocsForm.idCardBackUrl = url
        console.log('设置 idCardBackUrl:', url)
      } else if (type === 'medicalReport') {
        submitDocsForm.medicalReportUrl = url
        console.log('设置 medicalReportUrl:', url)
      } else if (type === 'contract') {
        submitDocsForm.contractUrl = url
        console.log('设置 contractUrl:', url)
      }
      ElMessage.success('文件上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e) {
    console.error('上传失败:', e)
    ElMessage.error('文件上传失败')
  }
}

// 提交资料
async function handlesubmitDocuments() {
  if (!submitDocsForm.idCardFrontUrl && !submitDocsForm.idCardBackUrl && !submitDocsForm.medicalReportUrl && !submitDocsForm.contractUrl) {
    ElMessage.warning('请至少提交一项入职资料')
    return
  }
  uploading.value = true
  try {
    // 确保所有字段都是字符串，undefined 传空字符串
    const data = {
      idCardFrontUrl: submitDocsForm.idCardFrontUrl || '',
      idCardBackUrl: submitDocsForm.idCardBackUrl || '',
      medicalReportUrl: submitDocsForm.medicalReportUrl || '',
      contractUrl: submitDocsForm.contractUrl || ''
    }
    console.log('提交资料数据:', data)  // 调试信息
    await submitDocuments(submitDocsForm.offerId, data)
    ElMessage.success('资料提交成功，等待招聘者审核')
    submitDocsVisible.value = false
    loadData()
  } catch (e) {
    console.error('提交失败:', e)
  } finally {
    uploading.value = false
  }
}

// 格式化日期
function formatDate(d) {
  return d ? d.replace('T', ' ').substring(0, 10) : ''
}

// 格式化日期时间
function formatDateTime(d) {
  return d ? d.replace('T', ' ').substring(0, 16) : ''
}

// 格式化薪资
function formatSalary(salary) {
  if (!salary) return '面议'
  return salary + 'k/月'
}

// 状态类型
function statusType(s) {
  const map = { 0: 'warning', 1: 'success', 2: 'danger', 3: '' }
  return map[s] || 'info'
}

// 状态标签
function statusLabel(s) {
  const map = { 0: '待确认', 1: '已接受', 2: '已拒绝', 3: '已入职' }
  return map[s] || '未知'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-offers {
  padding: 20px;
}

.header-card {
  margin-bottom: 15px;
}

.upload-demo {
  width: 100%;
}

.el-upload__tip {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}
</style>
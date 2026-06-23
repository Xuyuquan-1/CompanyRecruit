<template>
  <div class="offer-manage">
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:150px">
            <el-option label="待确认" :value="0" />
            <el-option label="已接受" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已入职" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadData">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:15px" class="table-card">
      <el-table :data="tableData" border stripe v-loading="loading" style="width:100%">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column prop="applicationId" label="应聘ID" width="90" />
        <el-table-column prop="candidateName" label="应聘者" min-width="100" show-overflow-tooltip />
        <el-table-column prop="jobTitle" label="岗位" min-width="120" show-overflow-tooltip />
        <el-table-column prop="offerTime" label="发送日期" width="160">
          <template #default="{ row }">{{ formatDateTime(row.offerTime) }}</template>
        </el-table-column>
        <el-table-column prop="expectedJoinDate" label="预计入职" width="120">
          <template #default="{ row }">{{ formatDate(row.expectedJoinDate) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 招聘者/管理员操作 -->
            <template v-if="!userStore.isCandidate()">
              <el-button v-if="row.status===1" type="success" link icon="Check" @click="handleOnboard(row)">确认入职</el-button>
              <el-button v-if="row.status===0" type="danger" link icon="Close" @click="handleReject(row)">拒绝</el-button>
              <el-button v-if="!row.offerTime" type="warning" link icon="Send" @click="openSendOffer(row)">发送Offer</el-button>
            </template>
            <!-- 应聘者操作 -->
            <template v-else>
              <el-button v-if="row.status===0" type="success" link icon="Check" @click="handleAcceptOffer(row)">接受Offer</el-button>
              <el-button v-if="row.status===0" type="danger" link icon="Close" @click="handleRejectOffer(row)">拒绝Offer</el-button>
              <el-button v-if="row.status===1" type="primary" link icon="Upload" @click="openSubmitDocs(row)">提交入职资料</el-button>
            </template>
            <el-button type="info" link icon="View" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[5,10,20,50]" :total="total" layout="total,sizes,prev,pager,next,jumper"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 确认入职对话框 -->
    <el-dialog v-model="onboardVisible" title="确认入职" width="450px">
      <el-form label-width="110px">
        <el-form-item label="应聘ID">{{ onboardForm.applicationId }}</el-form-item>
        <el-form-item label="实际入职日期">
          <el-date-picker v-model="onboardForm.joinDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="onboardVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOnboard">确认入职</el-button>
      </template>
    </el-dialog>

    <!-- 发送Offer对话框 -->
    <el-dialog v-model="sendOfferVisible" title="发送录用通知" width="550px">
      <el-form :model="sendOfferForm" label-width="110px">
        <el-form-item label="应聘者">{{ sendOfferForm.candidateName }}</el-form-item>
        <el-form-item label="岗位">{{ sendOfferForm.jobTitle }}</el-form-item>
        <el-form-item label="预计入职日期">
          <el-date-picker v-model="sendOfferForm.expectedJoinDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="薪资待遇">
          <el-input v-model="sendOfferForm.salary" placeholder="例如：15K-20K·13薪" />
        </el-form-item>
        <el-form-item label="福利说明">
          <el-input v-model="sendOfferForm.benefits" type="textarea" :rows="3" placeholder="例如：五险一金、带薪年假、节日福利" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="sendOfferForm.remark" type="textarea" :rows="2" placeholder="其他说明信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sendOfferVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSendOffer">发送Offer</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="录用详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应聘ID">{{ detail.applicationId }}</el-descriptions-item>
        <el-descriptions-item label="发送日期">{{ formatDateTime(detail.offerTime) }}</el-descriptions-item>
        <el-descriptions-item label="预计入职">{{ formatDate(detail.expectedJoinDate) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="薪资待遇" :span="2">{{ detail.salary || '-' }}</el-descriptions-item>
        <el-descriptions-item label="福利说明" :span="2">{{ detail.benefits || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入职资料" :span="2">
          <template v-if="parsedDocs.length">
            <el-tag
              v-for="doc in parsedDocs"
              :key="doc"
              type="success"
              style="margin-right:6px;margin-bottom:4px"
            >{{ doc }}</el-tag>
          </template>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 提交入职资料对话框（应聘者） -->
    <el-dialog v-model="submitDocsVisible" title="提交入职资料" width="650px">
      <el-alert 
        title="请提交以下入职资料，招聘者审核通过后将为您办理入职手续" 
        type="info" 
        :closable="false"
        style="margin-bottom:15px"
      />
      <el-form :model="submitDocsForm" label-width="110px">
        <el-form-item label="身份证">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'idCard')"
            :before-upload="beforeUpload"
            accept="image/*,.pdf"
            :limit="1"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png/pdf格式，不超过10MB</div>
            </template>
          </el-upload>
          <div v-if="submitDocsForm.idCardUrl" style="margin-top:8px;color:#67c23a">
            <el-icon><Check /></el-icon> 已上传
          </div>
        </el-form-item>
        <el-form-item label="体检报告">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'medicalReport')"
            :before-upload="beforeUpload"
            accept=".pdf,image/*"
            :limit="1"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">支持PDF或图片格式，不超过10MB</div>
            </template>
          </el-upload>
          <div v-if="submitDocsForm.medicalReportUrl" style="margin-top:8px;color:#67c23a">
            <el-icon><Check /></el-icon> 已上传
          </div>
        </el-form-item>
        <el-form-item label="劳动合同">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="(file) => handleFileChange(file, 'contract')"
            :before-upload="beforeUpload"
            accept=".pdf,image/*"
            :limit="1"
          >
            <el-button type="primary" size="small">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">上传已签署的合同扫描件，不超过10MB</div>
            </template>
          </el-upload>
          <div v-if="submitDocsForm.contractUrl" style="margin-top:8px;color:#67c23a">
            <el-icon><Check /></el-icon> 已上传
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDocsVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitDocuments" :loading="uploading">提交资料</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { getOfferPage, confirmOnboard, rejectOffer, sendOffer, acceptOffer, submitDocuments } from '../../api/offer'
import { uploadFile } from '../../api/oss'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const loading = ref(false), tableData = ref([]), total = ref(0)
const queryForm = reactive({ status: null, pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try { const res = await getOfferPage(queryForm); tableData.value = res.data.records; total.value = res.data.total } catch {} finally { loading.value = false }
}
function resetQuery() { queryForm.status = null; queryForm.pageNum = 1; loadData() }

const onboardVisible = ref(false), onboardForm = reactive({ id: null, applicationId: '', joinDate: '' })
function handleOnboard(row) {
  onboardForm.id = row.id; onboardForm.applicationId = row.applicationId
  onboardForm.joinDate = ''; onboardVisible.value = true
}
async function submitOnboard() {
  if (!onboardForm.joinDate) { ElMessage.warning('请选择入职日期'); return }
  try { await confirmOnboard(onboardForm.id, onboardForm.joinDate); ElMessage.success('入职确认成功'); onboardVisible.value = false; loadData() } catch {}
}

// 发送Offer相关
const sendOfferVisible = ref(false)
const sendOfferForm = reactive({
  applicationId: null,
  candidateName: '',
  jobTitle: '',
  expectedJoinDate: '',
  salary: '',
  benefits: '',
  remark: ''
})

function openSendOffer(row) {
  sendOfferForm.applicationId = row.applicationId || row.id
  sendOfferForm.candidateName = row.candidateName || ''
  sendOfferForm.jobTitle = row.jobTitle || ''
  sendOfferForm.expectedJoinDate = ''
  sendOfferForm.salary = ''
  sendOfferForm.benefits = ''
  sendOfferForm.remark = ''
  sendOfferVisible.value = true
}

async function submitSendOffer() {
  if (!sendOfferForm.expectedJoinDate) {
    ElMessage.warning('请选择预计入职日期')
    return
  }
  try {
    await sendOffer({
      applicationId: sendOfferForm.applicationId,
      expectedJoinDate: sendOfferForm.expectedJoinDate,
      salary: sendOfferForm.salary,
      benefits: sendOfferForm.benefits,
      remark: sendOfferForm.remark
    })
    ElMessage.success('录用通知已发送')
    sendOfferVisible.value = false
    loadData()
  } catch (e) {
    console.error('发送失败:', e)
  }
}

async function handleReject(row) {
  await ElMessageBox.confirm('确认拒绝该录用？', '提示', { type: 'warning' })
  try {
    await rejectOffer(row.id, '')
    ElMessage.success('已拒绝录用')
    loadData()
  } catch {}
}

// 应聘者接受Offer
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

// 应聘者拒绝Offer
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

// 提交入职资料相关
const submitDocsVisible = ref(false)
const uploading = ref(false)
const submitDocsForm = reactive({
  offerId: null,
  idCardUrl: '',
  medicalReportUrl: '',
  contractUrl: ''
})

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
    if (res.code === 200) {
      // 根据类型设置对应的URL
      if (type === 'idCard') {
        submitDocsForm.idCardUrl = res.data
      } else if (type === 'medicalReport') {
        submitDocsForm.medicalReportUrl = res.data
      } else if (type === 'contract') {
        submitDocsForm.contractUrl = res.data
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

function openSubmitDocs(row) {
  submitDocsForm.offerId = row.id
  submitDocsForm.idCardUrl = ''
  submitDocsForm.medicalReportUrl = ''
  submitDocsForm.contractUrl = ''
  submitDocsVisible.value = true
}

async function handleSubmitDocuments() {
  if (!submitDocsForm.idCardUrl && !submitDocsForm.medicalReportUrl && !submitDocsForm.contractUrl) {
    ElMessage.warning('请至少提交一项入职资料')
    return
  }
  uploading.value = true
  try {
    await submitDocuments(submitDocsForm.offerId, {
      idCardUrl: submitDocsForm.idCardUrl,
      medicalReportUrl: submitDocsForm.medicalReportUrl,
      contractUrl: submitDocsForm.contractUrl
    })
    ElMessage.success('资料提交成功，等待招聘者审核')
    submitDocsVisible.value = false
    loadData()
  } catch (e) {
    console.error('提交失败:', e)
  } finally {
    uploading.value = false
  }
}

const detailVisible = ref(false), detail = ref({})
const parsedDocs = ref([])
function viewDetail(row) {
  detail.value = row
  // 解析入职资料字段，支持字符串JSON或对象，提取值为true的条目
  try {
    let docs = row.docsSubmitted
    if (typeof docs === 'string') docs = JSON.parse(docs)
    if (docs && typeof docs === 'object') {
      parsedDocs.value = Object.entries(docs)
        .filter(([, v]) => v === true || v === 'true' || v === 1)
        .map(([k]) => k)
    } else {
      parsedDocs.value = []
    }
  } catch {
    parsedDocs.value = []
  }
  detailVisible.value = true
}

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 10) : '' }
function formatDateTime(d) { return d ? d.replace('T', ' ').substring(0, 16) : '' }
function statusType(s) { const map = {0:'info',1:'warning',2:'danger',3:'success'}; return map[s]||'info' }
function statusLabel(s) { const map = {0:'待确认',1:'已接受',2:'已拒绝',3:'已入职'}; return map[s]||'未知' }
onMounted(loadData)
</script>

<style scoped>
.table-card { overflow-x: auto }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:15px }
</style>
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
        <el-table-column label="资料提交" width="180" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 1 && !userStore.isCandidate()">
              <el-tag v-if="isDocSubmitted(row, 'idCard')" type="success" size="small" style="margin:2px">身份证</el-tag>
              <el-tag v-else type="info" size="small" style="margin:2px">身份证</el-tag>
              <el-tag v-if="isDocSubmitted(row, 'contract')" type="success" size="small" style="margin:2px">合同</el-tag>
              <el-tag v-else type="info" size="small" style="margin:2px">合同</el-tag>
              <el-tag v-if="isDocSubmitted(row, 'medicalReport')" type="success" size="small" style="margin:2px">体检</el-tag>
              <el-tag v-else type="info" size="small" style="margin:2px">体检</el-tag>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="350" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 招聘者/管理员操作 -->
            <template v-if="!userStore.isCandidate()">
              <el-button v-if="row.status===1" type="primary" link icon="View" @click="viewDocuments(row)">查看资料</el-button>
              <el-button v-if="row.status===1" type="success" link icon="Check" @click="handleOnboard(row)">确认入职</el-button>
              <el-button v-if="row.status===0" type="danger" link icon="Close" @click="handleReject(row)">拒绝</el-button>
            </template>
            <!-- 应聘者操作 -->
            <template v-else>
              <el-button v-if="row.status===0" type="success" link icon="Check" @click="handleAcceptOffer(row)">接受Offer</el-button>
              <el-button v-if="row.status===0" type="danger" link icon="Close" @click="handleRejectOffer(row)">拒绝Offer</el-button>
              <el-button v-if="row.status===1 && !row.docsSubmitted" type="primary" link icon="Upload" @click="openSubmitDocs(row)">提交入职资料</el-button>
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
    <el-dialog v-model="onboardVisible" title="确认入职" width="550px">
      <el-form label-width="110px">
        <el-form-item label="应聘者">{{ onboardForm.candidateName }}</el-form-item>
        <el-form-item label="岗位">{{ onboardForm.jobTitle }}</el-form-item>
        <el-form-item label="实际入职日期">
          <el-date-picker v-model="onboardForm.joinDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      
      <el-alert 
        title="确认入职前请检查：" 
        type="warning" 
        :closable="false"
        style="margin-top:10px"
      >
        <template #default>
          <div style="line-height:1.8">
            <div>✓ 应聘者已接受Offer</div>
            <div>✓ 已提交身份证、劳动合同、体检报告</div>
            <div>✓ 资料审核通过</div>
          </div>
        </template>
      </el-alert>
      
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

    <!-- 查看应聘者提交的资料 -->
    <el-dialog v-model="viewDocsVisible" title="应聘者提交的入职资料" width="700px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="应聘者">{{ currentOffer.candidateName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ currentOffer.jobTitle || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top:20px">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
          <h4 style="margin:0">资料清单</h4>
          <el-button 
            v-if="isAllDocsSubmitted(currentOffer)"
            type="primary" 
            size="small" 
            icon="Download"
            @click="downloadAllDocs"
          >
            下载所有资料
          </el-button>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="身份证正面">
            <template v-if="currentOffer.docsSubmitted && isDocSubmitted(currentOffer, 'idCardFront')">
              <el-tag type="success">已提交</el-tag>
              <el-button type="primary" link size="small" @click="previewDoc(currentOffer, 'idCardFront')" style="margin-left:10px">
                查看
              </el-button>
              <el-button type="success" link size="small" @click="downloadDoc(currentOffer, 'idCardFront', '身份证正面')" style="margin-left:5px">
                下载
              </el-button>
            </template>
            <el-tag v-else type="info">未提交</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="身份证反面">
            <template v-if="currentOffer.docsSubmitted && isDocSubmitted(currentOffer, 'idCardBack')">
              <el-tag type="success">已提交</el-tag>
              <el-button type="primary" link size="small" @click="previewDoc(currentOffer, 'idCardBack')" style="margin-left:10px">
                查看
              </el-button>
              <el-button type="success" link size="small" @click="downloadDoc(currentOffer, 'idCardBack', '身份证反面')" style="margin-left:5px">
                下载
              </el-button>
            </template>
            <el-tag v-else type="info">未提交</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="劳动合同">
            <template v-if="currentOffer.docsSubmitted && isDocSubmitted(currentOffer, 'contract')">
              <el-tag type="success">已提交</el-tag>
              <el-button type="primary" link size="small" @click="previewDoc(currentOffer, 'contract')" style="margin-left:10px">
                查看
              </el-button>
              <el-button type="success" link size="small" @click="downloadDoc(currentOffer, 'contract', '劳动合同')" style="margin-left:5px">
                下载
              </el-button>
            </template>
            <el-tag v-else type="info">未提交</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="体检报告">
            <template v-if="currentOffer.docsSubmitted && isDocSubmitted(currentOffer, 'medicalReport')">
              <el-tag type="success">已提交</el-tag>
              <el-button type="primary" link size="small" @click="previewDoc(currentOffer, 'medicalReport')" style="margin-left:10px">
                查看
              </el-button>
              <el-button type="success" link size="small" @click="downloadDoc(currentOffer, 'medicalReport', '体检报告')" style="margin-left:5px">
                下载
              </el-button>
            </template>
            <el-tag v-else type="info">未提交</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-alert 
          v-if="isAllDocsSubmitted(currentOffer)"
          title="资料已齐全，可以确认入职" 
          type="success" 
          :closable="false"
          style="margin-top:15px"
        />
        <el-alert 
          v-else-if="currentOffer.status === 1"
          title="资料未齐全，请等待应聘者提交完整资料" 
          type="warning" 
          :closable="false"
          style="margin-top:15px"
        />
      </div>
      
      <template #footer>
        <el-button @click="viewDocsVisible = false">关闭</el-button>
        <el-button v-if="isAllDocsSubmitted(currentOffer)" type="primary" @click="handleOnboardFromView(currentOffer)">
          确认入职
        </el-button>
      </template>
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
          <div v-if="submitDocsForm.idCardFrontUrl" style="margin-top:8px;color:#67c23a">
            <el-icon><Check /></el-icon> 已上传
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
          <div v-if="submitDocsForm.idCardBackUrl" style="margin-top:8px;color:#67c23a">
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
import { uploadFile, getDownloadUrl } from '../../api/oss'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const loading = ref(false), tableData = ref([]), total = ref(0)
const queryForm = reactive({ status: null, pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try { const res = await getOfferPage(queryForm); tableData.value = res.data.records; total.value = res.data.total } catch {} finally { loading.value = false }
}
function resetQuery() { queryForm.status = null; queryForm.pageNum = 1; loadData() }

const onboardVisible = ref(false), onboardForm = reactive({ id: null, applicationId: '', joinDate: '', candidateName: '', jobTitle: '' })
function handleOnboard(row) {
  onboardForm.id = row.id 
  onboardForm.applicationId = row.applicationId
  onboardForm.candidateName = row.candidateName || ''
  onboardForm.jobTitle = row.jobTitle || ''
  onboardForm.joinDate = ''
  onboardVisible.value = true
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
  idCardFrontUrl: '',
  idCardBackUrl: '',
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
    console.log('上传返回结果:', res)
    console.log('res.data:', res.data)
    console.log('res.data类型:', typeof res.data)
    
    if (res.code === 200) {
      // 确保 res.data 是字符串
      let url = res.data
      if (typeof url === 'object') {
        // 如果是对象，尝试获取 url 字段
        url = url.url || url.downloadUrl || ''
      }
      
      // 根据类型设置对应的URL
      if (type === 'idCardFront') {
        submitDocsForm.idCardFrontUrl = url
      } else if (type === 'idCardBack') {
        submitDocsForm.idCardBackUrl = url
      } else if (type === 'medicalReport') {
        submitDocsForm.medicalReportUrl = url
      } else if (type === 'contract') {
        submitDocsForm.contractUrl = url
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
  submitDocsForm.idCardFrontUrl = ''
  submitDocsForm.idCardBackUrl = ''
  submitDocsForm.medicalReportUrl = ''
  submitDocsForm.contractUrl = ''
  submitDocsVisible.value = true
}

async function handleSubmitDocuments() {
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

// 查看资料提交情况
const viewDocsVisible = ref(false)
const currentOffer = ref({})

function viewDocuments(row) {
  // 使用 JSON.parse 确保是响应式对象
  const offerData = JSON.parse(JSON.stringify(row))
  currentOffer.value = offerData
  
  console.log('查看资料 - docsSubmitted:', offerData.docsSubmitted)
  console.log('查看资料 - docsSubmitted类型:', typeof offerData.docsSubmitted)
  
  // 尝试解析看看
  try {
    const docs = typeof offerData.docsSubmitted === 'string' ? JSON.parse(offerData.docsSubmitted) : offerData.docsSubmitted
    console.log('解析后的docs对象:', docs)
    console.log('idCardFront:', docs.idCardFront)
    console.log('idCardBack:', docs.idCardBack)
    console.log('isDocSubmitted(idCardFront):', isDocSubmitted(offerData, 'idCardFront'))
    console.log('isDocSubmitted(idCardBack):', isDocSubmitted(offerData, 'idCardBack'))
  } catch (e) {
    console.error('解析失败:', e)
  }
  
  viewDocsVisible.value = true
}

// 检查某项资料是否已提交
function isDocSubmitted(row, docType) {
  if (!row.docsSubmitted) return false
  try {
    const docs = typeof row.docsSubmitted === 'string' ? JSON.parse(row.docsSubmitted) : row.docsSubmitted
    
    // 身份证特殊处理：需要正面和反面都提交
    if (docType === 'idCard') {
      return (docs['idCardFront'] === true || docs['idCardFront'] === 'true' || docs['idCardFront'] === 1)
          && (docs['idCardBack'] === true || docs['idCardBack'] === 'true' || docs['idCardBack'] === 1)
    }
    
    return docs[docType] === true || docs[docType] === 'true' || docs[docType] === 1
  } catch {
    return false
  }
}

// 检查所有资料是否齐全
function isAllDocsSubmitted(row) {
  return isDocSubmitted(row, 'idCardFront') && isDocSubmitted(row, 'idCardBack') && isDocSubmitted(row, 'contract') && isDocSubmitted(row, 'medicalReport')
}

// 预览资料文件
async function previewDoc(row, docType) {
  try {
    const docs = typeof row.docsSubmitted === 'string' ? JSON.parse(row.docsSubmitted) : row.docsSubmitted
    const urlKey = docType + 'Url'
    const ossUrl = docs[urlKey]
    
    if (!ossUrl) {
      ElMessage.warning('文件URL不存在')
      return
    }
    
    // 如果是OSS地址，需要获取签名URL
    if (ossUrl.includes('.aliyuncs.com/')) {
      // 提取objectName（从 .com/ 后面的部分）
      let objectName = ossUrl.substring(ossUrl.indexOf('.aliyuncs.com/') + 13)
      // 去除可能的前导斜杠
      if (objectName.startsWith('/')) {
        objectName = objectName.substring(1)
      }
      const res = await getDownloadUrl(objectName)
      if (res.code === 200 && res.data.downloadUrl) {
        window.open(res.data.downloadUrl, '_blank')
      } else {
        ElMessage.error('获取下载链接失败')
      }
    } else {
      // 本地存储直接打开
      window.open(ossUrl, '_blank')
    }
  } catch (e) {
    console.error('预览失败:', e)
    ElMessage.error('预览失败')
  }
}

// 下载单个资料文件
async function downloadDoc(row, docType, fileName) {
  try {
    const docs = typeof row.docsSubmitted === 'string' ? JSON.parse(row.docsSubmitted) : row.docsSubmitted
    const urlKey = docType + 'Url'
    const ossUrl = docs[urlKey]
    
    if (!ossUrl) {
      ElMessage.warning('文件URL不存在')
      return
    }
    
    let downloadUrl = ossUrl
    
    // 如果是OSS地址，需要获取签名URL
    if (ossUrl.includes('.aliyuncs.com/')) {
      // 提取objectName（从 .com/ 后面的部分）
      let objectName = ossUrl.substring(ossUrl.indexOf('.aliyuncs.com/') + 13)
      // 去除可能的前导斜杠
      if (objectName.startsWith('/')) {
        objectName = objectName.substring(1)
      }
      const res = await getDownloadUrl(objectName)
      if (res.code === 200 && res.data.downloadUrl) {
        downloadUrl = res.data.downloadUrl
      } else {
        ElMessage.error('获取下载链接失败')
        return
      }
    }
    
    // 创建隐藏的a标签进行下载
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName // 设置下载文件名
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    ElMessage.success(`开始下载: ${fileName}`)
  } catch (e) {
    console.error('下载失败:', e)
    ElMessage.error('下载失败')
  }
}

// 一键下载所有资料
async function downloadAllDocs() {
  try {
    const docs = typeof currentOffer.value.docsSubmitted === 'string' 
      ? JSON.parse(currentOffer.value.docsSubmitted) 
      : currentOffer.value.docsSubmitted
    
    // 依次下载所有文件
    const files = [
      { type: 'idCardFront', name: '身份证正面' },
      { type: 'idCardBack', name: '身份证反面' },
      { type: 'contract', name: '劳动合同' },
      { type: 'medicalReport', name: '体检报告' }
    ]
    
    let downloadCount = 0
    for (const file of files) {
      const urlKey = file.type + 'Url'
      const ossUrl = docs[urlKey]
      
      if (ossUrl) {
        let downloadUrl = ossUrl
        
        // 如果是OSS地址，需要获取签名URL
        if (ossUrl.includes('.aliyuncs.com/')) {
          let objectName = ossUrl.substring(ossUrl.indexOf('.aliyuncs.com/') + 13)
          // 去除可能的前导斜杠
          if (objectName.startsWith('/')) {
            objectName = objectName.substring(1)
          }
          const res = await getDownloadUrl(objectName)
          if (res.code === 200 && res.data.downloadUrl) {
            downloadUrl = res.data.downloadUrl
          } else {
            console.error(`获取${file.name}下载链接失败`)
            continue
          }
        }
        
        const link = document.createElement('a')
        link.href = downloadUrl
        link.download = `${currentOffer.value.candidateName || '应聘者'}_${file.name}`
        link.target = '_blank'
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        downloadCount++
        // 每个下载间隔200ms，避免浏览器拦截
        await new Promise(resolve => setTimeout(resolve, 200))
      }
    }
    
    ElMessage.success(`已下载 ${downloadCount} 个文件`)
  } catch (e) {
    console.error('下载失败:', e)
    ElMessage.error('下载失败')
  }
}

// 从查看资料对话框确认入职
function handleOnboardFromView(row) {
  viewDocsVisible.value = false
  handleOnboard(row)
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
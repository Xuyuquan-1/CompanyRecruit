<template>
  <div class="application-manage">
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="岗位">
          <el-input v-model="queryForm.keyword" placeholder="搜索岗位名称" clearable style="width:200px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:140px">
            <el-option label="待筛选" :value="0" />
            <el-option label="通过筛选" :value="1" />
            <el-option label="面试中" :value="2" />
            <el-option label="待确认Offer" :value="3" />
            <el-option label="不录用" :value="4" />
            <el-option label="已接受Offer(待入职)" :value="5" />
            <el-option label="已入职" :value="6" />
            <el-option label="候选人撤回" :value="7" />
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
        <el-table-column prop="candidateName" label="应聘者姓名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="resumeName" label="应聘简历" min-width="120" show-overflow-tooltip />
        <el-table-column prop="jobTitle" label="应聘岗位" min-width="130" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="投递时间" width="160">
          <template #default="{ row }">{{ formatDate(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link icon="View" @click="handleView(row)">查看</el-button>
            <!-- 只有招聘者/管理员才能看到审核按钮 -->
            <template v-if="!userStore.isCandidate()">
              <el-button v-if="row.status===0" type="success" link icon="Select" @click="handlePass(row)">通过</el-button>
              <el-button v-if="row.status===0" type="danger" link icon="CloseBold" @click="handleReject(row)">不通过</el-button>
              <el-button v-if="row.status===1" type="primary" link icon="ChatDotSquare" @click="openArrange(row)">安排面试</el-button>
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

    <!-- 详情/标记对话框 -->
    <el-dialog v-model="detailVisible" title="应聘详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应聘者姓名">{{ currentApp.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentApp.phone }}</el-descriptions-item>
        <el-descriptions-item label="应聘简历">{{ currentApp.resumeName }}</el-descriptions-item>
        <el-descriptions-item label="应聘岗位">{{ currentApp.jobTitle }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(currentApp.status) }}</el-descriptions-item>
      </el-descriptions>
      <el-form style="margin-top:15px" label-width="80px">
        <el-form-item label="标签">
          <el-input v-model="currentApp.tags" placeholder="逗号分隔，如：211,985,Java" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="currentApp.remark" type="textarea" :rows="3" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRemark">保存</el-button>
      </template>
    </el-dialog>

    <!-- 安排面试对话框 -->
    <el-dialog v-model="arrangeVisible" title="安排面试" width="500px" destroy-on-close>
      <el-form ref="arrangeFormRef" :model="arrangeForm" label-width="100px">
        <el-form-item label="应聘者">{{ arrangeTarget.resumeName }}</el-form-item>
        <el-form-item label="应聘岗位">{{ arrangeTarget.jobTitle }}</el-form-item>
        <el-form-item label="面试时间" required>
          <el-date-picker v-model="arrangeForm.interviewTime" type="datetime" placeholder="选择面试时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="面试地点">
          <el-input v-model="arrangeForm.location" placeholder="会议室/线上链接" />
        </el-form-item>
        <el-form-item label="面试官">
          <el-input v-model="arrangeForm.interviewerName" placeholder="面试官姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arrangeVisible=false">取消</el-button>
        <el-button type="primary" @click="submitArrange">确定安排</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplicationPage, passApplication, rejectApplication, updateApplicationRemark } from '../../api/application'
import { arrangeInterview } from '../../api/interview'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const loading = ref(false), tableData = ref([]), total = ref(0)
const queryForm = reactive({ keyword: '', status: null, pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try { const res = await getApplicationPage(queryForm); tableData.value = res.data.records; total.value = res.data.total } catch {} finally { loading.value = false }
}
function resetQuery() { queryForm.keyword = ''; queryForm.status = null; queryForm.pageNum = 1; loadData() }

async function handlePass(row) {
  await ElMessageBox.confirm('确认通过该应聘者？', '提示', { type: 'warning' })
  try { await passApplication(row.id); ElMessage.success('已通过'); loadData() } catch {}
}
async function handleReject(row) {
  // 选择失败原因
  try {
    const { value: refuseType } = await ElMessageBox.prompt(
      '请选择失败原因：1-简历淘汰 2-面试淘汰 3-候选人拒Offer 4-材料不合格 5-录用审批驳回 6-候选人主动撤回 7-岗位关闭终止',
      '不录用原因',
      {
        inputPlaceholder: '输入原因编号（1-7）',
        inputPattern: /^[1-7]$/,
        inputErrorMessage: '请输入1-7之间的数字'
      }
    )
    await rejectApplication(row.id, parseInt(refuseType))
    ElMessage.success('已拒绝')
    loadData()
  } catch {}
}

const detailVisible = ref(false), currentApp = ref({})
function handleView(row) { currentApp.value = { ...row }; detailVisible.value = true }
async function saveRemark() {
  try {
    await updateApplicationRemark(currentApp.value.id, currentApp.value.tags, currentApp.value.remark)
    ElMessage.success('保存成功'); detailVisible.value = false; loadData()
  } catch {}
}

const arrangeVisible = ref(false), arrangeTarget = ref({})
const arrangeForm = reactive({ interviewTime: '', location: '', interviewerName: '' })
function openArrange(row) {
  arrangeTarget.value = row
  arrangeForm.interviewTime = ''
  arrangeForm.location = ''
  arrangeForm.interviewerName = ''
  arrangeVisible.value = true
}
async function submitArrange() {
  if (!arrangeForm.interviewTime) { ElMessage.warning('请选择面试时间'); return }
  if (new Date(arrangeForm.interviewTime).getTime() <= Date.now()) { ElMessage.warning('面试时间必须晚于当前时间'); return }
  try {
    await arrangeInterview({
      applicationId: arrangeTarget.value.id,
      interviewTime: arrangeForm.interviewTime,
      location: arrangeForm.location,
      interviewerName: arrangeForm.interviewerName
    })
    ElMessage.success('面试安排成功')
    arrangeVisible.value = false
    loadData()
  } catch {}
}

async function submitOffer() {
  if (!offerForm.expectedJoinDate) { ElMessage.warning('请选择预计入职日期'); return }
  try {
    await sendOffer({
      applicationId: offerTarget.value.id,
      expectedJoinDate: offerForm.expectedJoinDate,
      salary: offerForm.salary,
      benefits: offerForm.benefits,
      remark: offerForm.remark
    })
    ElMessage.success('Offer发放成功')
    offerVisible.value = false
    loadData()
  } catch {}
}

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 16) : '' }
function statusType(s) { 
  const map = {0:'info',1:'warning',2:'',3:'primary',4:'danger',5:'success',6:'success',7:'info'}
  return map[s]||'info' 
}
function statusLabel(s) { 
  const map = {
    0:'待筛选',
    1:'通过筛选',
    2:'面试中',
    3:'待确认Offer',
    4:'不录用',
    5:'已接受Offer(待入职)',
    6:'已入职',
    7:'候选人撤回'
  }
  return map[s]||'未知' 
}

onMounted(loadData)
</script>

<style scoped>
.table-card { overflow-x: auto }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:15px }
</style>
<template>
  <div class="interview-manage">
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="面试状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:150px">
            <el-option label="待面试" :value="0" />
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
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
        <el-table-column prop="candidateName" label="应聘者" min-width="100" show-overflow-tooltip />
        <el-table-column prop="jobTitle" label="岗位" min-width="120" show-overflow-tooltip />
        <el-table-column prop="interviewTime" label="面试时间" width="160">
          <template #default="{ row }">{{ formatDate(row.interviewTime) }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="120" show-overflow-tooltip />
        <el-table-column prop="interviewerName" label="面试官" width="100" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.result===1" type="success">通过</el-tag>
            <el-tag v-else-if="row.result===2" type="danger">不通过</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 招聘者/管理员可以评价和取消 -->
            <template v-if="!userStore.isCandidate()">
              <el-button v-if="row.status===0" type="primary" link icon="Edit" @click="openEvaluate(row)">评价</el-button>
              <el-button v-if="row.status===0" type="danger" link icon="Close" @click="handleCancel(row)">取消</el-button>
            </template>
            <!-- 所有人都可以查看评价 -->
            <el-button type="success" link icon="View" @click="viewEvaluation(row)">查看评价</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
          :page-sizes="[5,10,20,50]" :total="total" layout="total,sizes,prev,pager,next,jumper"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 评价对话框 -->
    <el-dialog v-model="evalVisible" title="面试评价" width="550px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="面试结果">
          <el-radio-group v-model="evalForm.result">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">不通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评价意见">
          <el-input v-model="evalForm.evaluation" type="textarea" :rows="5" placeholder="请填写面试评价..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEval">提交</el-button>
      </template>
    </el-dialog>

    <!-- 查看评价 -->
    <el-dialog v-model="viewEvalVisible" title="面试评价详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="应聘者">{{ evalView.candidateName }}</el-descriptions-item>
        <el-descriptions-item label="岗位">{{ evalView.jobTitle }}</el-descriptions-item>
        <el-descriptions-item label="面试官">{{ evalView.interviewerName }}</el-descriptions-item>
        <el-descriptions-item label="面试时间">{{ formatDate(evalView.interviewTime) }}</el-descriptions-item>
        <el-descriptions-item label="面试地点" :span="2">{{ evalView.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试结果" :span="2">
          <el-tag v-if="evalView.result===1" type="success" size="large">通过</el-tag>
          <el-tag v-else-if="evalView.result===2" type="danger" size="large">不通过</el-tag>
          <span v-else>待评价</span>
        </el-descriptions-item>
        <el-descriptions-item label="评价意见" :span="2">
          <div style="white-space:pre-wrap;min-height:50px">{{ evalView.evaluation || '暂无评价' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInterviewPage, cancelInterview, evaluateInterview } from '../../api/interview'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const loading = ref(false), tableData = ref([]), total = ref(0)
const queryForm = reactive({ status: null, pageNum: 1, pageSize: 10 })

async function loadData() {
  loading.value = true
  try { const res = await getInterviewPage(queryForm); tableData.value = res.data.records; total.value = res.data.total } catch {} finally { loading.value = false }
}
function resetQuery() { queryForm.status = null; queryForm.pageNum = 1; loadData() }

async function handleCancel(row) {
  await ElMessageBox.confirm('确认取消该面试？', '提示', { type: 'warning' })
  try { await cancelInterview(row.id); ElMessage.success('已取消'); loadData() } catch {}
}

const evalVisible = ref(false), evalForm = reactive({ result: 1, evaluation: '' }), evalId = ref(null)
function openEvaluate(row) { evalId.value = row.id; evalForm.result = 1; evalForm.evaluation = ''; evalVisible.value = true }
async function submitEval() {
  try {
    await evaluateInterview(evalId.value, evalForm.evaluation, evalForm.result)
    ElMessage.success('评价提交成功'); evalVisible.value = false; loadData()
  } catch {}
}

const viewEvalVisible = ref(false), evalView = ref({})
function viewEvaluation(row) { evalView.value = row; viewEvalVisible.value = true }

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 16) : '' }
function statusTagType(s) { const map = {0:'warning',1:'success',2:'info'}; return map[s]||'info' }
function statusLabel(s) { const map = {0:'待面试',1:'已完成',2:'已取消'}; return map[s]||'未知' }

onMounted(loadData)
</script>

<style scoped>
.table-card { overflow-x: auto }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:15px }
</style>
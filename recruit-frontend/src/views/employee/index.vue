<template>
  <div class="employee-manage">
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="姓名">
          <el-input v-model="searchName" placeholder="搜索姓名" clearable style="width:180px" @keyup.enter="doSearch" />
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="searchDept" placeholder="搜索部门" clearable style="width:180px" @keyup.enter="doSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="doSearch">搜索</el-button>
          <el-button icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:15px" class="table-card">
      <div class="table-header">
        <span class="title">正式员工列表</span>
      </div>
      <el-table :data="pagedData" border stripe style="margin-top:10px;width:100%">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column prop="name" label="姓名" min-width="100" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="department" label="部门" min-width="100" />
        <el-table-column prop="position" label="职位" min-width="110" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="expectedJoinDate" label="预计入职" width="110">
          <template #default="{ row }">{{ formatDate(row.expectedJoinDate) }}</template>
        </el-table-column>
        <el-table-column prop="actualJoinDate" label="实际入职" width="110">
          <template #default="{ row }">{{ formatDate(row.actualJoinDate) || '-' }}</template>
        </el-table-column>
        <el-table-column prop="idCardStatus" label="身份证" width="80" align="center">
          <template #default="{ row }"><el-tag :type="docType(row.idCardStatus)" size="small">{{ docLabel(row.idCardStatus) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="contractStatus" label="合同" width="80" align="center">
          <template #default="{ row }"><el-tag :type="docType(row.contractStatus)" size="small">{{ docLabel(row.contractStatus) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="medicalReportStatus" label="体检报告" width="90" align="center">
          <template #default="{ row }"><el-tag :type="docType(row.medicalReportStatus)" size="small">{{ docLabel(row.medicalReportStatus) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="入职进度" width="150" align="center">
          <template #default="{ row }">
            <el-progress 
              :percentage="calculateProgress(row)" 
              :status="getProgressStatus(row)"
              :stroke-width="8"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-dropdown @command="(cmd) => updateDoc(row, cmd)" style="margin-right:8px">
              <el-button type="primary" link icon="Edit">资料状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="idCard:1">身份证 · 已提交</el-dropdown-item>
                  <el-dropdown-item command="idCard:0">身份证 · 未提交</el-dropdown-item>
                  <el-dropdown-item command="contract:1" divided>合同 · 已提交</el-dropdown-item>
                  <el-dropdown-item command="contract:0">合同 · 未提交</el-dropdown-item>
                  <el-dropdown-item command="medicalReport:1" divided>体检报告 · 已提交</el-dropdown-item>
                  <el-dropdown-item command="medicalReport:0">体检报告 · 未提交</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="success" link icon="View" @click="viewDetail(row)">详情</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :page-sizes="[5,10,20,50]" :total="filteredData.length" layout="total,sizes,prev,pager,next,jumper"
          @size-change="pageNum=1" @current-change="()=>{}" />
      </div>
    </el-card>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="员工详情" width="550px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detail.department }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ detail.position }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="预计入职">{{ formatDate(detail.expectedJoinDate) }}</el-descriptions-item>
        <el-descriptions-item label="实际入职">{{ formatDate(detail.actualJoinDate) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证">{{ docLabel(detail.idCardStatus) }}</el-descriptions-item>
        <el-descriptions-item label="合同">{{ docLabel(detail.contractStatus) }}</el-descriptions-item>
        <el-descriptions-item label="体检报告">{{ docLabel(detail.medicalReportStatus) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEmployeeList, updateEmployeeDocument, deleteEmployee } from '../../api/employee'

const tableData = ref([])
const searchName = ref('')
const searchDept = ref('')
const pageNum = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  let list = tableData.value
  if (searchName.value) list = list.filter(e => e.name && e.name.includes(searchName.value))
  if (searchDept.value) list = list.filter(e => e.department && e.department.includes(searchDept.value))
  return list
})
const pagedData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})
function doSearch() { pageNum.value = 1 }
function resetSearch() { searchName.value = ''; searchDept.value = ''; pageNum.value = 1 }

async function loadData() {
  try { const res = await getEmployeeList(); tableData.value = res.data || [] } catch {}
}

async function updateDoc(row, cmd) {
  const [type, status] = cmd.split(':')
  try { await updateEmployeeDocument(row.id, type, parseInt(status)); ElMessage.success('更新成功'); loadData() } catch {}
}

const detailVisible = ref(false), detail = ref({})
function viewDetail(row) { detail.value = row; detailVisible.value = true }

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  try { await deleteEmployee(row.id); ElMessage.success('删除成功'); loadData() } catch {}
}

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 10) : '' }
function docType(s) { return s === 1 ? 'success' : 'danger' }
function docLabel(s) { return s === 1 ? '已提交' : '未提交' }
function statusType(s) { const map = {0:'danger',1:'success',2:'warning'}; return map[s]||'info' }
function statusLabel(s) { const map = {0:'离职',1:'在职',2:'入职中'}; return map[s]||'未知' }

// 计算入职进度百分比
function calculateProgress(row) {
  let completed = 0
  if (row.idCardStatus === 1) completed++
  if (row.contractStatus === 1) completed++
  if (row.medicalReportStatus === 1) completed++
  return Math.round((completed / 3) * 100)
}

// 获取进度条状态
function getProgressStatus(row) {
  const progress = calculateProgress(row)
  if (progress === 100) return 'success'
  if (progress > 0) return ''
  return 'exception'
}

onMounted(loadData)
</script>

<style scoped>
.table-card { overflow-x: auto }
.table-header { display:flex; justify-content:space-between; align-items:center }
.title { font-size:16px; font-weight:bold }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:15px }
</style>
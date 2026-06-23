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
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="success" link icon="View" @click="viewDetail(row)">详情</el-button>
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
    <el-dialog v-model="detailVisible" title="员工详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ detail.email }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ detail.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职位">{{ detail.position || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="预计入职">{{ formatDate(detail.expectedJoinDate) }}</el-descriptions-item>
        <el-descriptions-item label="实际入职">{{ formatDate(detail.actualJoinDate) || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top:20px">
        <h4>入职资料状态</h4>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="身份证">
            <el-tag type="success">已提交并审核通过</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="劳动合同">
            <el-tag type="success">已提交并审核通过</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="体检报告">
            <el-tag type="success">已提交并审核通过</el-tag>
          </el-descriptions-item>
        </el-descriptions>
        <el-alert 
          title="该员工已完成所有入职手续，资料已审核通过" 
          type="success" 
          :closable="false"
          style="margin-top:15px"
        />
      </div>
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
  try { 
    const res = await getEmployeeList()
    // 只显示已入职的员工（status=1）
    tableData.value = (res.data || []).filter(e => e.status === 1)
  } catch { 
    tableData.value = [] 
  }
}

const detailVisible = ref(false), detail = ref({})
function viewDetail(row) { detail.value = row; detailVisible.value = true }

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 10) : '' }
function statusType(s) { const map = {0:'danger',1:'success',2:'warning'}; return map[s]||'info' }
function statusLabel(s) { const map = {0:'离职',1:'在职',2:'入职中'}; return map[s]||'未知' }

onMounted(loadData)
</script>

<style scoped>
.table-card { overflow-x: auto }
.table-header { display:flex; justify-content:space-between; align-items:center }
.title { font-size:16px; font-weight:bold }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:15px }
</style>
<template>
  <div class="user-manage">
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="用户名">
          <el-input v-model="searchKey" placeholder="搜索用户名/姓名" clearable style="width:200px" @keyup.enter="doSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="doSearch">搜索</el-button>
          <el-button icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:15px" class="table-card">
      <div class="table-header">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增用户</el-button>
      </div>
      <el-table :data="pagedData" border stripe v-loading="loading" style="margin-top:10px;width:100%">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" show-overflow-tooltip />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize"
          :page-sizes="[5,10,20,50]" :total="filteredData.length" layout="total,sizes,prev,pager,next,jumper"
          @size-change="pageNum=1" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑用户':'新增用户'" width="500px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="用户名" required><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="密码" v-if="!isEdit" required><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser } from '../../api/system'

const loading = ref(false), tableData = ref([]), dialogVisible = ref(false), isEdit = ref(false), editId = ref(null)
const searchKey = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const form = reactive({ username:'', realName:'', password:'', phone:'', email:'', status: 1 })

const filteredData = computed(() => {
  if (!searchKey.value) return tableData.value
  const kw = searchKey.value.toLowerCase()
  return tableData.value.filter(u =>
    (u.username && u.username.toLowerCase().includes(kw)) ||
    (u.realName && u.realName.toLowerCase().includes(kw))
  )
})
const pagedData = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})
function doSearch() { pageNum.value = 1 }
function resetSearch() { searchKey.value = ''; pageNum.value = 1 }

async function loadData() {
  loading.value = true
  try { const res = await getUserList(); tableData.value = res.data || [] } catch {} finally { loading.value = false }
}

function handleAdd() { isEdit.value = false; dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; Object.assign(form, row); form.password = ''; dialogVisible.value = true }
async function handleSubmit() {
  try {
    if (isEdit.value) { await updateUser(editId.value, { ...form }) } else { await addUser({ ...form }) }
    ElMessage.success('成功'); dialogVisible.value = false; loadData()
  } catch {}
}
function resetForm() { Object.keys(form).forEach(k => form[k] = ''); form.status = 1; editId.value = null }

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  try { await deleteUser(row.id); ElMessage.success('已删除'); loadData() } catch {}
}

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 16) : '' }
onMounted(loadData)
</script>

<style scoped>
.table-card { overflow-x: auto }
.table-header { display:flex; justify-content:space-between; align-items:center }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:15px }
</style>
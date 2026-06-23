<template>
  <div class="role-manage">
    <el-card shadow="never" class="search-card">
      <el-form inline>
        <el-form-item label="角色名">
          <el-input v-model="searchKey" placeholder="搜索角色名/标识" clearable style="width:200px" @keyup.enter="doSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="doSearch">搜索</el-button>
          <el-button icon="Refresh" @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top:15px" class="table-card">
      <div class="table-header">
        <el-button type="primary" icon="Plus" @click="handleAdd">新增角色</el-button>
      </div>
      <el-table :data="pagedData" border stripe v-loading="loading" style="margin-top:10px;width:100%">
        <el-table-column prop="id" label="ID" width="65" align="center" />
        <el-table-column prop="roleName" label="角色名" width="120" />
        <el-table-column prop="roleCode" label="角色标识" min-width="140" show-overflow-tooltip />
        <el-table-column prop="description" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link icon="Menu" @click="openMenuAssign(row)">分配菜单</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑角色':'新增角色'" width="500px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="角色名" required><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="标识" required><el-input v-model="form.roleCode" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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

    <el-dialog v-model="menuVisible" title="分配菜单权限" width="450px">
      <el-tree ref="menuTreeRef" :data="menuList" show-checkbox node-key="id"
        :props="{label:'name',children:'children'}"
        default-expand-all highlight-current />
      <template #footer>
        <el-button @click="menuVisible=false">取消</el-button>
        <el-button type="primary" @click="saveMenuAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, assignRoleMenus, getMenuList } from '../../api/system'
import request from '../../utils/request'

const loading = ref(false), tableData = ref([]), dialogVisible = ref(false), isEdit = ref(false), editId = ref(null)
const searchKey = ref('')
const pageNum = ref(1), pageSize = ref(10)
const form = reactive({ roleName:'', roleCode:'', description:'', status: 1 })

const filteredData = computed(() => {
  if (!searchKey.value) return tableData.value
  const kw = searchKey.value.toLowerCase()
  return tableData.value.filter(r =>
    (r.roleName && r.roleName.toLowerCase().includes(kw)) ||
    (r.roleCode && r.roleCode.toLowerCase().includes(kw))
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
  try { const res = await getRoleList(); tableData.value = res.data || [] } catch {} finally { loading.value = false }
}

function handleAdd() { isEdit.value = false; dialogVisible.value = true }
function handleEdit(row) { isEdit.value = true; editId.value = row.id; Object.assign(form, row); form.roleCode = row.roleCode || ''; form.description = row.description || ''; form.status = row.status ?? 1; dialogVisible.value = true }
async function handleSubmit() {
  try {
    const data = { roleName: form.roleName, roleCode: form.roleCode, description: form.description, status: form.status }
    if (isEdit.value) { await updateRole(editId.value, data) } else { await addRole(data) }
    ElMessage.success('成功'); dialogVisible.value = false; loadData()
  } catch {}
}
function resetForm() { Object.keys(form).forEach(k => form[k] = ''); form.status = 1; editId.value = null }
async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  try { await deleteRole(row.id); ElMessage.success('已删除'); loadData() } catch {}
}

const menuVisible = ref(false), menuList = ref([]), menuTreeRef = ref(null), assignRoleId = ref(null)
async function openMenuAssign(row) {
  assignRoleId.value = row.id
  try {
    const [menuRes, idsRes] = await Promise.all([
      getMenuList(),
      request({ url: `/api/system/role/${row.id}/menu-ids`, method: 'get' }).catch(() => ({ data: [] }))
    ])
    menuList.value = menuRes.data || []
    await nextTick()
    if (menuTreeRef.value && idsRes.data) {
      menuTreeRef.value.setCheckedKeys(idsRes.data)
    }
  } catch {}
  menuVisible.value = true
}
async function saveMenuAssign() {
  const ids = menuTreeRef.value ? menuTreeRef.value.getCheckedKeys().concat(menuTreeRef.value.getHalfCheckedKeys()) : []
  try { await assignRoleMenus(assignRoleId.value, ids); ElMessage.success('权限已保存'); menuVisible.value = false } catch {}
}

function formatDate(d) { return d ? d.replace('T', ' ').substring(0, 16) : '' }
onMounted(loadData)
</script>

<style scoped>
.table-card { overflow-x: auto }
.table-header { display:flex; justify-content:space-between; align-items:center }
.pagination-wrapper { display:flex; justify-content:flex-end; margin-top:15px }
</style>
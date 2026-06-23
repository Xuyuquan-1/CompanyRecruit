<template>
  <div class="menu-manage">
    <el-card shadow="never" class="table-card">
      <div class="table-header">
        <el-button type="primary" icon="Plus" @click="handleAdd({})">新增菜单</el-button>
      </div>
      <el-table :data="menuList" border stripe row-key="id" default-expand-all style="margin-top:10px;width:100%">
        <el-table-column prop="name" label="菜单名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="['','success','info'][row.type]||'info'" size="small">{{ ['','目录','菜单','按钮'][row.type]||'未知' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="160" show-overflow-tooltip />
        <el-table-column prop="icon" label="图标" width="80" />
        <el-table-column prop="permission" label="权限标识" min-width="150" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link icon="Plus" @click="handleAdd(row)">添加子菜单</el-button>
            <el-button type="success" link icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑菜单':'新增菜单'" width="550px" destroy-on-close @closed="resetForm">
      <el-form :model="form" label-width="90px">
        <el-form-item label="菜单名称" required><el-input v-model="form.name" placeholder="如：用户管理" /></el-form-item>
        <el-form-item label="父级菜单"><el-input :value="parentMenuName" disabled /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路由路径"><el-input v-model="form.path" placeholder="如：/system/user" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" placeholder="如：Setting" /></el-form-item>
        <el-form-item label="权限标识"><el-input v-model="form.permission" placeholder="如：system:user" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuList, addMenu, updateMenu, deleteMenu } from '../../api/system'

const menuList = ref([])
async function loadData() {
  try { const res = await getMenuList(); menuList.value = res.data || [] } catch {}
}

const dialogVisible = ref(false), isEdit = ref(false), editId = ref(null), parentId = ref(null), parentMenuName = ref('根目录')
const form = reactive({ name:'', type:1, path:'', icon:'', permission:'', sortOrder:0, status:1 })

function handleAdd(row) {
  isEdit.value = false
  parentId.value = row.id || 0
  parentMenuName.value = row.name || '根目录'
  dialogVisible.value = true
}
function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  parentId.value = row.parentId || 0
  parentMenuName.value = row.parentId ? (menuList.value.find(m => m.id === row.parentId) || {}).name || '未知' : '根目录'
  Object.assign(form, {
    name: row.name, type: row.type ?? 1, path: row.path, icon: row.icon,
    permission: row.permission, sortOrder: row.sortOrder ?? 0, status: row.status ?? 1
  })
  dialogVisible.value = true
}
async function handleSubmit() {
  const data = { ...form, parentId: parentId.value }
  try {
    if (isEdit.value) { await updateMenu(editId.value, data) } else { await addMenu(data) }
    ElMessage.success('成功'); dialogVisible.value = false; loadData()
  } catch {}
}
function resetForm() {
  Object.keys(form).forEach(k => form[k] = ''); form.type = 1; form.sortOrder = 0; form.status = 1
  editId.value = null; parentId.value = null
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
  try { await deleteMenu(row.id); ElMessage.success('已删除'); loadData() } catch {}
}

onMounted(loadData)
</script>

<style scoped>
.table-card { overflow-x: auto }
.table-header { display:flex; justify-content:space-between; align-items:center }
</style>
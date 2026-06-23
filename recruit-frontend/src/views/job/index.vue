<template>
  <div class="job-manage">
    <!-- ========== 搜索区域 ========== -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="岗位名称">
          <el-input
            v-model="queryForm.title"
            placeholder="请输入岗位名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="queryForm.department" placeholder="请选择部门" clearable style="width: 150px">
            <el-option label="技术部" value="技术部" />
            <el-option label="产品部" value="产品部" />
            <el-option label="市场部" value="市场部" />
            <el-option label="数据部" value="数据部" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 130px">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadData">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ========== 操作按钮 + 数据表格 ========== -->
    <el-card shadow="never" style="margin-top: 15px" class="table-card">
      <!-- 操作按钮区域 -->
      <div class="table-header">
        <el-button
          v-if="userStore.hasPermission('job:add')"
          type="primary"
          icon="Plus"
          @click="handleAdd"
        >
          新增岗位
        </el-button>
      </div>

      <!-- 数据表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 10px; width:100%">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="岗位名称" min-width="150" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="headcount" label="招聘人数" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="userStore.hasPermission('job:edit')"
              type="primary"
              link
              icon="Edit"
              @click="handleEdit(row)"
            >编辑</el-button>
            <el-button
              v-if="userStore.hasPermission('job:edit') && row.status === 0"
              type="success"
              link
              icon="Promotion"
              @click="handlePublish(row)"
            >发布</el-button>
            <el-button
              v-if="userStore.hasPermission('job:edit') && row.status === 1"
              type="warning"
              link
              icon="CloseBold"
              @click="handleClose(row)"
            >关闭</el-button>
            <el-button
              v-if="userStore.hasPermission('job:delete')"
              type="danger"
              link
              icon="Delete"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- ========== 新增/编辑对话框 ========== -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="岗位名称" prop="title">
          <el-input v-model="formData.title" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="所属部门" prop="department">
          <el-select v-model="formData.department" placeholder="请选择部门" style="width: 100%">
            <el-option label="技术部" value="技术部" />
            <el-option label="产品部" value="产品部" />
            <el-option label="市场部" value="市场部" />
            <el-option label="数据部" value="数据部" />
          </el-select>
        </el-form-item>
        <el-form-item label="招聘人数" prop="headcount">
          <el-input-number v-model="formData.headcount" :min="1" :max="999" />
        </el-form-item>
        <el-form-item label="岗位职责" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入岗位职责描述"
          />
        </el-form-item>
        <el-form-item label="岗位要求" prop="requirements">
          <el-input
            v-model="formData.requirements"
            type="textarea"
            :rows="4"
            placeholder="请输入岗位要求"
          />
        </el-form-item>
        <el-form-item label="保存状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="0">保存为草稿</el-radio>
            <el-radio :value="1">直接发布</el-radio>
            <el-radio :value="2">已关闭</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确 定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../store/user'
import {
  getJobPage,
  addJob,
  updateJob,
  deleteJob,
  publishJob,
  closeJob
} from '../../api/job'

const userStore = useUserStore()

// ============ 查询相关 ============
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  title: '',
  department: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

/** 加载列表数据 */
async function loadData() {
  loading.value = true
  try {
    const res = await getJobPage(queryForm)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

/** 重置搜索条件 */
function resetQuery() {
  queryForm.title = ''
  queryForm.department = ''
  queryForm.status = null
  queryForm.pageNum = 1
  loadData()
}

// ============ 表单相关 ============
const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位')
const submitLoading = ref(false)
const formRef = ref(null)
const editId = ref(null)  // 编辑时保存当前岗位ID

const formData = reactive({
  title: '',
  department: '',
  headcount: 1,
  description: '',
  requirements: '',
  status: 0
})

const formRules = {
  title: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  department: [{ required: true, message: '请选择部门', trigger: 'change' }],
  headcount: [{ required: true, message: '请输入招聘人数', trigger: 'blur' }]
}

/** 打开新增对话框 */
function handleAdd() {
  dialogTitle.value = '新增岗位'
  editId.value = null
  dialogVisible.value = true
}

/** 打开编辑对话框 */
function handleEdit(row) {
  dialogTitle.value = '编辑岗位'
  editId.value = row.id
  // 把当前行数据填入表单
  Object.assign(formData, {
    title: row.title,
    department: row.department,
    headcount: row.headcount,
    description: row.description,
    requirements: row.requirements,
    status: row.status
  })
  dialogVisible.value = true
}

/** 提交表单 */
async function handleSubmit() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    if (editId.value) {
      await updateJob(editId.value, formData)
      ElMessage.success('修改成功')
    } else {
      await addJob(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    submitLoading.value = false
  }
}

/** 重置表单 */
function resetForm() {
  formData.title = ''
  formData.department = ''
  formData.headcount = 1
  formData.description = ''
  formData.requirements = ''
  formData.status = 0
  editId.value = null
  formRef.value?.resetFields()
}

// ============ 操作按钮 ============

/** 发布岗位 */
async function handlePublish(row) {
  try {
    await ElMessageBox.confirm(`确定要发布岗位【${row.title}】吗？`, '提示', {
      type: 'warning'
    })
    await publishJob(row.id)
    ElMessage.success('发布成功')
    loadData()
  } catch (error) {
    // 取消或错误
  }
}

/** 关闭岗位 */
async function handleClose(row) {
  try {
    await ElMessageBox.confirm(`确定要关闭岗位【${row.title}】吗？`, '提示', {
      type: 'warning'
    })
    await closeJob(row.id)
    ElMessage.success('关闭成功')
    loadData()
  } catch (error) {}
}

/** 删除岗位 */
async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除岗位【${row.title}】吗？此操作不可恢复！`, '警告', {
      type: 'warning',
      confirmButtonText: '确定删除',
      confirmButtonClass: 'el-button--danger'
    })
    await deleteJob(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {}
}

// ============ 工具方法 ============

/** 状态标签类型 */
function statusTagType(status) {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

/** 状态文本 */
function statusLabel(status) {
  const map = { 0: '草稿', 1: '已发布', 2: '已关闭' }
  return map[status] || '未知'
}

/** 日期格式化 */
function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.replace('T', ' ').substring(0, 19)
}

// ============ 页面加载时获取数据 ============
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.job-manage {
  padding: 0;
}
.search-card {
  margin-bottom: 0;
}
.table-card {
  overflow-x: auto;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}
</style>
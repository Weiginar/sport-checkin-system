<template>
  <div class="sport-manage">
    <el-card shadow="never">
      <!-- Top Button -->
      <div class="top-bar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增运动项目</el-button>
      </div>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 16px">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="名称" min-width="120" />
        <el-table-column prop="icon" label="图标" width="80" align="center" />
        <el-table-column prop="caloriesPerHour" label="每小时卡路里" width="130" align="center" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- Add / Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑运动项目' : '新增运动项目'"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="运动名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入运动名称" />
        </el-form-item>
        <el-form-item label="图标标识" prop="icon">
          <el-input v-model="formData.icon" placeholder="请输入图标标识" />
        </el-form-item>
        <el-form-item label="每小时卡路里" prop="caloriesPerHour">
          <el-input-number v-model="formData.caloriesPerHour" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :precision="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSportList, addSport, updateSport, deleteSport } from '../api'

const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  id: undefined,
  name: '',
  icon: '',
  caloriesPerHour: 0,
  description: '',
  sortOrder: 0,
  status: 1
})

const formData = reactive(defaultForm())

const formRules = {
  name: [{ required: true, message: '请输入运动名称', trigger: 'blur' }],
  caloriesPerHour: [{ required: true, message: '请输入每小时卡路里', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSportList({ pageNum: pageNum.value, pageSize: pageSize.value })
    const data = res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (err) {
    ElMessage.error('获取运动列表失败')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(formData, defaultForm())
}

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    icon: row.icon,
    caloriesPerHour: row.caloriesPerHour,
    description: row.description,
    sortOrder: row.sortOrder,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateSport(formData)
      ElMessage.success('更新成功')
    } else {
      const { id, ...payload } = formData
      await addSport(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (err) {
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除运动项目「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteSport(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (err) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.sport-manage {
  padding: 20px;
}
.top-bar {
  display: flex;
  justify-content: flex-end;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

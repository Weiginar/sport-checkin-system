<template>
  <div class="user-manage">
    <el-card shadow="never">
      <!-- Search Bar -->
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名/昵称/手机号"
          clearable
          style="width: 240px"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="statusFilter" placeholder="用户状态" clearable style="width: 140px; margin-left: 12px">
          <el-option label="全部" value="" />
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" style="margin-left: 12px" @click="handleSearch">搜索</el-button>
      </div>

      <!-- Table -->
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%; margin-top: 16px">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="points" label="积分" width="90" align="center" />
        <el-table-column prop="totalCheckins" label="累计打卡" width="100" align="center" />
        <el-table-column prop="consecutiveDays" label="连续天数" width="100" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
              inline-prompt
              active-text="启"
              inactive-text="禁"
            />
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, updateUserStatus } from '../api'

const keyword = ref('')
const statusFilter = ref('')
const tableData = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value !== '' ? statusFilter.value : undefined
    }
    const res = await getUserList(params)
    const data = res.data || {}
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (err) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadData()
}

const handleStatusChange = async (row, val) => {
  const newStatus = val ? 1 : 0
  try {
    await updateUserStatus({ userId: row.id, status: newStatus })
    row.status = newStatus
    ElMessage.success('状态更新成功')
  } catch (err) {
    ElMessage.error('状态更新失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}
.search-bar {
  display: flex;
  align-items: center;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

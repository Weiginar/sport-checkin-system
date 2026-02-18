<template>
  <div class="points-manage">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="用户ID">
          <el-input v-model="filters.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="全部" clearable>
            <el-option label="全部" value="" />
            <el-option label="打卡" value="CHECKIN" />
            <el-option label="连续打卡" value="CONSECUTIVE" />
            <el-option label="成就奖励" value="ACHIEVEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column label="积分变动" width="110" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.points >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
              {{ row.points >= 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type]?.color || 'info'" size="small">
              {{ typeTagMap[row.type]?.label || row.type }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="记录时间" width="170" />
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPointsList } from '../api'

const loading = ref(false)
const tableData = ref([])

const filters = reactive({
  userId: '',
  type: '',
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

const typeTagMap = {
  CHECKIN: { label: '打卡', color: 'success' },
  CONSECUTIVE: { label: '连续打卡', color: 'warning' },
  ACHIEVEMENT: { label: '成就奖励', color: '' },
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      userId: filters.userId || undefined,
      type: filters.type || undefined,
    }
    const res = await getPointsList(params)
    const data = res.data || {}
    tableData.value = data.records || []
    pagination.total = data.total || 0
  } catch (err) {
    ElMessage.error('加载积分记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.points-manage {
  padding: 20px;
}
.filter-card {
  margin-bottom: 16px;
}
.table-card {
  margin-bottom: 16px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>

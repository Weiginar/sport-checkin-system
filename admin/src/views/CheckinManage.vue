<template>
  <div class="checkin-manage">
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="用户ID">
          <el-input v-model="filters.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="运动类型">
          <el-select v-model="filters.sportTypeId" placeholder="全部类型" clearable>
            <el-option
              v-for="item in sportOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" width="110" />
        <el-table-column prop="nickname" label="昵称" width="110" />
        <el-table-column prop="sportTypeName" label="运动类型" width="100" />
        <el-table-column prop="checkinDate" label="打卡日期" width="120" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="distance" label="距离(km)" width="100" align="center" />
        <el-table-column prop="calories" label="卡路里" width="90" align="center" />
        <el-table-column prop="pointsEarned" label="获得积分" width="90" align="center" />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="90" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
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
import { ElMessageBox, ElMessage } from 'element-plus'
import { getCheckinList, deleteCheckin, getSportList } from '../api'

const loading = ref(false)
const tableData = ref([])

const filters = reactive({
  userId: '',
  sportTypeId: '',
  dateRange: null,
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0,
})

const sportOptions = ref([])

const loadSportOptions = async () => {
  try {
    const res = await getSportList({ pageNum: 1, pageSize: 100 })
    const records = res.data?.records || res.data || []
    sportOptions.value = records.map(item => ({ label: item.name, value: item.id }))
  } catch (err) {
    console.error('加载运动类型失败', err)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      userId: filters.userId || undefined,
      sportTypeId: filters.sportTypeId || undefined,
      startDate: filters.dateRange?.[0] || undefined,
      endDate: filters.dateRange?.[1] || undefined,
    }
    const res = await getCheckinList(params)
    const data = res.data || res || {}
    tableData.value = data.records || []
    pagination.total = data.total || 0
  } catch (err) {
    ElMessage.error('加载打卡记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadData()
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除该打卡记录 (ID: ${row.id}) 吗？`,
      '确认删除',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteCheckin(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadSportOptions()
  loadData()
})
</script>

<style scoped>
.checkin-manage {
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

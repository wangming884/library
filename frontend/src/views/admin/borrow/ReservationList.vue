<template>
  <div class="reservation-list">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="读者ID">
          <el-input v-model="searchForm.readerId" placeholder="读者ID" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="书名/读者名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="readerName" label="读者" width="120" show-overflow-tooltip />
        <el-table-column prop="bookTitle" label="图书" min-width="180" show-overflow-tooltip />
        <el-table-column prop="reserveDate" label="预约日期" width="120" />
        <el-table-column prop="expireDate" label="过期日期" width="120" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notifyTime" label="通知时间" width="170" />
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchReservations"
        @current-change="fetchReservations"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getReservations } from '../../../api/modules/borrow'

// ---------- 状态选项 ----------
const statusOptions = [
  { value: 1, label: '等待' },
  { value: 2, label: '就绪' },
  { value: 3, label: '已取消' },
  { value: 4, label: '已完成' },
  { value: 5, label: '已过期' }
]
const statusLabel = (val) => statusOptions.find(s => s.value === val)?.label || '未知'
const statusTagType = (val) => {
  const map = { 1: 'warning', 2: 'primary', 3: 'info', 4: 'success', 5: 'danger' }
  return map[val] || 'info'
}

// ---------- 搜索 ----------
const searchForm = reactive({ readerId: '', status: '', keyword: '' })
const handleSearch = () => { pagination.page = 1; fetchReservations() }
const resetSearch = () => {
  searchForm.readerId = ''
  searchForm.status = ''
  searchForm.keyword = ''
  handleSearch()
}

// ---------- 表格 & 分页 ----------
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchReservations = async () => {
  loading.value = true
  try {
    const res = await getReservations({
      page: pagination.page,
      size: pagination.pageSize,
      readerId: searchForm.readerId || undefined,
      status: searchForm.status || undefined,
      keyword: searchForm.keyword || undefined
    })
    tableData.value = res.data.records || res.data.list || res.data
    pagination.total = res.data.total || 0
  } catch {
  } finally {
    loading.value = false
  }
}

// ---------- 初始化 ----------
onMounted(() => {
  fetchReservations()
})
</script>

<style scoped>
.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>

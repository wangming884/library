<template>
  <div class="borrow-records">
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

    <!-- 操作栏 + 表格 -->
    <el-card shadow="never" style="margin-top: 16px">
      <div style="margin-bottom: 16px; display: flex; gap: 8px">
        <el-button type="warning" @click="handleCheckOverdue">检测逾期</el-button>
        <el-button type="success" @click="handleSendReminders">批量催还</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="readerName" label="读者" width="100" show-overflow-tooltip />
        <el-table-column prop="bookTitle" label="图书" min-width="160" show-overflow-tooltip />
        <el-table-column prop="borrowDate" label="借出日期" width="110" />
        <el-table-column prop="dueDate" label="应还日期" width="110" />
        <el-table-column prop="returnDate" label="归还日期" width="110">
          <template #default="{ row }">
            {{ row.returnDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="renewCount" label="续借次数" width="90" align="center" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :disabled="row.status !== 1" @click="handleRenew(row)">
              续借
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchRecords"
        @current-change="fetchRecords"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBorrowRecords, renewBook, checkOverdue, sendReminders } from '../../../api/modules/borrow'

// ---------- 状态选项 ----------
const statusOptions = [
  { value: 1, label: '借阅中' },
  { value: 2, label: '已归还' },
  { value: 3, label: '已逾期' },
  { value: 4, label: '已续借' }
]
const statusLabel = (val) => statusOptions.find(s => s.value === val)?.label || '未知'
const statusTagType = (val) => {
  const map = { 1: 'primary', 2: 'success', 3: 'danger', 4: 'warning' }
  return map[val] || 'info'
}

// ---------- 搜索 ----------
const searchForm = reactive({ readerId: '', status: '', keyword: '' })
const handleSearch = () => { pagination.page = 1; fetchRecords() }
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

const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getBorrowRecords({
      page: pagination.page,
      pageSize: pagination.pageSize,
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

// ---------- 续借 ----------
const handleRenew = (row) => {
  ElMessageBox.confirm(`确认为读者「${row.readerName}」续借图书《${row.bookTitle}》？`, '续借确认', { type: 'info' }).then(async () => {
    try {
      await renewBook(row.id)
      ElMessage.success('续借成功')
      fetchRecords()
    } catch {}
  }).catch(() => {})
}

// ---------- 检测逾期 ----------
const handleCheckOverdue = async () => {
  try {
    await checkOverdue()
    ElMessage.success('逾期检测完成')
    fetchRecords()
  } catch {}
}

// ---------- 批量催还 ----------
const handleSendReminders = () => {
  ElMessageBox.confirm('确认向所有逾期读者发送催还通知？', '批量催还', { type: 'warning' }).then(async () => {
    try {
      await sendReminders()
      ElMessage.success('催还通知已发送')
    } catch {}
  }).catch(() => {})
}

// ---------- 初始化 ----------
onMounted(() => {
  fetchRecords()
})
</script>

<style scoped>
.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>

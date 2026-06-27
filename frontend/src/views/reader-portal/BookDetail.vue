<template>
  <div class="book-detail-container" v-loading="loading">
    <el-card v-if="book">
      <el-row :gutter="30">
        <!-- 封面 -->
        <el-col :span="6">
          <el-image
            :src="book.cover"
            fit="cover"
            style="width: 100%; border-radius: 8px;"
          >
            <template #error>
              <div class="cover-placeholder">
                <el-icon size="64"><Picture /></el-icon>
                <span>暂无封面</span>
              </div>
            </template>
          </el-image>
        </el-col>

        <!-- 图书信息 -->
        <el-col :span="18">
          <h1 class="book-title">{{ book.title }}</h1>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="作者">{{ book.author }}</el-descriptions-item>
            <el-descriptions-item label="ISBN">{{ book.isbn }}</el-descriptions-item>
            <el-descriptions-item label="出版社">{{ book.publisher }}</el-descriptions-item>
            <el-descriptions-item label="出版日期">{{ book.pubDate }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ book.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="价格">{{ book.price ? `¥${book.price}` : '-' }}</el-descriptions-item>
            <el-descriptions-item label="总馆藏">
              <span>{{ book.totalCount || 0 }} 册</span>
            </el-descriptions-item>
            <el-descriptions-item label="可借数量">
              <el-tag :type="book.availableCount > 0 ? 'success' : 'danger'" size="large">
                {{ book.availableCount || 0 }} 册
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <div class="book-desc" v-if="book.description">
            <h3>内容简介</h3>
            <p>{{ book.description }}</p>
          </div>

          <div class="action-bar">
            <el-button
              v-if="book.availableCount > 0"
              type="primary"
              size="large"
              :loading="borrowing"
              @click="handleBorrow"
            >
              <el-icon><Collection /></el-icon> 借阅此书
            </el-button>
            <el-button
              v-else
              type="warning"
              size="large"
              :loading="reserving"
              @click="handleReserve"
            >
              <el-icon><Clock /></el-icon> 预约此书
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 可借副本 -->
    <el-card class="copies-card" v-if="book && book.availableCount > 0">
      <template #header><span>可借副本位置</span></template>
      <el-table :data="availableCopies" v-loading="copyLoading" stripe border>
        <el-table-column prop="id" label="副本ID" width="90" />
        <el-table-column prop="barcode" label="条码号" width="140" />
        <el-table-column prop="location" label="馆藏位置" min-width="150" />
        <el-table-column prop="floor" label="楼层" width="90" />
        <el-table-column prop="shelf" label="书架" width="120" />
      </el-table>
      <el-empty v-if="!copyLoading && availableCopies.length === 0" description="暂无可借副本" />
    </el-card>

    <!-- 借阅历史 -->
    <el-card class="history-card" v-if="book">
      <template #header><span>借阅记录</span></template>
      <el-table :data="book.borrowRecords || []" stripe border>
        <el-table-column prop="readerName" label="借阅人" width="120" />
        <el-table-column prop="borrowDate" label="借阅日期" width="170" />
        <el-table-column prop="dueDate" label="应还日期" width="170" />
        <el-table-column prop="returnDate" label="归还日期" width="170">
          <template #default="{ row }">{{ row.returnDate || '未归还' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="borrowStatusType(row.status)">{{ borrowStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!book.borrowRecords?.length" description="暂无借阅记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAvailableCopies, getBook } from '../../api/modules/book'
import { readerBorrowBook, reserveBook } from '../../api/modules/borrow'

const route = useRoute()
const loading = ref(false)
const copyLoading = ref(false)
const borrowing = ref(false)
const reserving = ref(false)
const book = ref(null)
const availableCopies = ref([])

const borrowStatusType = (status) => {
  const map = { 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}

const borrowStatusLabel = (status) => {
  const map = { 1: '借阅中', 2: '已归还', 3: '已逾期', 4: '丢失' }
  return map[status] || '未知'
}

const fetchBook = async () => {
  loading.value = true
  try {
    const res = await getBook(route.params.id)
    book.value = res.data
    fetchAvailableCopies()
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const fetchAvailableCopies = async () => {
  copyLoading.value = true
  try {
    const res = await getAvailableCopies(route.params.id)
    availableCopies.value = res.data || []
  } catch {
    availableCopies.value = []
  } finally {
    copyLoading.value = false
  }
}

const handleBorrow = () => {
  ElMessageBox.confirm('确定借阅此书？系统会自动选择一个在馆副本。', '借阅确认', {
    type: 'info',
    confirmButtonText: '确定借阅',
    cancelButtonText: '取消'
  }).then(async () => {
    borrowing.value = true
    try {
      const res = await readerBorrowBook({ bookId: book.value.id })
      const record = res.data || {}
      const location = [record.copyLocation, record.copyFloor, record.copyShelf].filter(Boolean).join(' / ')
      ElMessage.success(`借阅成功，应还日期：${record.dueDate || '-'}${record.copyBarcode ? `，副本：${record.copyBarcode}` : ''}${location ? `，位置：${location}` : ''}`)
      fetchBook()
    } catch {
      // handled
    } finally {
      borrowing.value = false
    }
  }).catch(() => {})
}

const handleReserve = () => {
  ElMessageBox.confirm('确定预约此书？有可借副本时将通知您。', '预约确认', {
    type: 'info',
    confirmButtonText: '确定预约',
    cancelButtonText: '取消'
  }).then(async () => {
    reserving.value = true
    try {
      await reserveBook({ bookId: book.value.id })
      ElMessage.success('预约成功，请等待通知')
      fetchBook()
    } catch {
      // handled
    } finally {
      reserving.value = false
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchBook()
})
</script>

<style scoped>
.book-detail-container {
  max-width: 1000px;
  margin: 0 auto;
}
.book-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
  font-weight: 600;
}
.cover-placeholder {
  width: 100%;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  color: #c0c4cc;
  gap: 12px;
  font-size: 14px;
}
.book-desc {
  margin-top: 20px;
}
.book-desc h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}
.book-desc p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
}
.action-bar {
  margin-top: 24px;
}
.history-card {
  margin-top: 20px;
}
.copies-card {
  margin-top: 20px;
}
</style>

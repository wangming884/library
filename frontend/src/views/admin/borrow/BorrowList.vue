<template>
  <div class="borrow-list">
    <el-row :gutter="16">
      <!-- 借书办理 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight: 600">借书办理</span>
          </template>
          <el-form ref="borrowFormRef" :model="borrowForm" :rules="borrowRules" label-width="80px">
            <el-form-item label="读者ID" prop="readerId">
              <el-input v-model.number="borrowForm.readerId" placeholder="请输入读者ID" />
            </el-form-item>
            <el-form-item label="图书ID" prop="bookId">
              <el-input v-model.number="borrowForm.bookId" placeholder="输入图书ID可查询可借副本">
                <template #append>
                  <el-button :loading="copyLoading" @click="fetchAvailableCopies">查询</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="副本ID" prop="copyId">
              <el-input v-model.number="borrowForm.copyId" placeholder="请输入副本ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="borrowLoading" @click="handleBorrow">确认借书</el-button>
            </el-form-item>
          </el-form>

          <el-table
            v-if="availableCopies.length > 0 || copyLoading"
            :data="availableCopies"
            v-loading="copyLoading"
            border
            stripe
            size="small"
            style="margin-top: 12px"
          >
            <el-table-column prop="id" label="副本ID" width="80" />
            <el-table-column prop="barcode" label="条码号" min-width="130" />
            <el-table-column prop="bookTitle" label="书名" min-width="140" show-overflow-tooltip />
            <el-table-column label="位置" min-width="160">
              <template #default="{ row }">{{ copyLocationText(row) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="selectCopy(row)">选择</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-divider />
          <el-descriptions v-if="borrowResult" title="借书结果" :column="1" border size="small">
            <el-descriptions-item label="借阅记录ID">{{ borrowResult.id }}</el-descriptions-item>
            <el-descriptions-item label="读者ID">{{ borrowResult.readerId }}</el-descriptions-item>
            <el-descriptions-item label="图书">{{ borrowResult.bookTitle }}</el-descriptions-item>
            <el-descriptions-item label="副本条码">{{ borrowResult.copyBarcode }}</el-descriptions-item>
            <el-descriptions-item label="副本位置">{{ copyLocationText(borrowResult) }}</el-descriptions-item>
            <el-descriptions-item label="借出日期">{{ borrowResult.borrowDate }}</el-descriptions-item>
            <el-descriptions-item label="应还日期">{{ borrowResult.dueDate }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 还书办理 -->
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <span style="font-weight: 600">还书办理</span>
          </template>
          <el-form ref="returnFormRef" :model="returnForm" :rules="returnRules" label-width="100px">
            <el-form-item label="借阅记录ID" prop="borrowRecordId">
              <el-input v-model.number="returnForm.borrowRecordId" placeholder="请输入借阅记录ID" />
            </el-form-item>
            <el-form-item>
              <el-button type="success" :loading="returnLoading" @click="handleReturn">确认还书</el-button>
            </el-form-item>
          </el-form>

          <el-divider />
          <el-descriptions v-if="returnResult" title="还书结果" :column="1" border size="small">
            <el-descriptions-item label="借阅记录ID">{{ returnResult.id }}</el-descriptions-item>
            <el-descriptions-item label="读者ID">{{ returnResult.readerId }}</el-descriptions-item>
            <el-descriptions-item label="图书">{{ returnResult.bookTitle }}</el-descriptions-item>
            <el-descriptions-item label="副本条码">{{ returnResult.copyBarcode }}</el-descriptions-item>
            <el-descriptions-item label="副本位置">{{ copyLocationText(returnResult) }}</el-descriptions-item>
            <el-descriptions-item label="借出日期">{{ returnResult.borrowDate }}</el-descriptions-item>
            <el-descriptions-item label="归还日期">{{ returnResult.returnDate }}</el-descriptions-item>
            <el-descriptions-item label="是否逾期">
              <el-tag :type="returnResult.overdue ? 'danger' : 'success'" size="small">
                {{ returnResult.overdue ? '逾期' : '正常' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="returnResult.fine" label="罚款金额">
              {{ returnResult.fine }} 元
            </el-descriptions-item>
            <el-descriptions-item v-if="returnResult.remark" label="处理结果">
              {{ returnResult.remark }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { borrowBook, returnBook } from '../../../api/modules/borrow'
import { getCopies } from '../../../api/modules/book'

const copyLocationText = (row) => {
  if (!row) return '-'
  const location = row.copyLocation || row.location
  const floor = row.copyFloor || row.floor
  const shelf = row.copyShelf || row.shelf
  return [location, floor, shelf].filter(Boolean).join(' / ') || '-'
}

// ---------- 借书 ----------
const borrowFormRef = ref(null)
const borrowLoading = ref(false)
const copyLoading = ref(false)
const borrowResult = ref(null)
const availableCopies = ref([])

const validateBorrowTarget = (_rule, _value, callback) => {
  if (!borrowForm.copyId && !borrowForm.bookId) {
    callback(new Error('请输入副本ID，或输入图书ID后自动选择副本'))
    return
  }
  callback()
}

const borrowForm = reactive({ readerId: '', bookId: '', copyId: '' })
const borrowRules = {
  readerId: [{ required: true, message: '请输入读者ID', trigger: 'blur' }],
  copyId: [{ validator: validateBorrowTarget, trigger: 'blur' }]
}

const fetchAvailableCopies = async () => {
  if (!borrowForm.bookId) {
    ElMessage.warning('请先输入图书ID')
    return
  }
  copyLoading.value = true
  try {
    const res = await getCopies({
      page: 1,
      size: 50,
      bookId: borrowForm.bookId,
      status: 1
    })
    availableCopies.value = res.data.records || res.data.list || res.data || []
    if (availableCopies.value.length === 0) {
      ElMessage.warning('该图书暂无在馆副本')
    }
  } catch {
    availableCopies.value = []
  } finally {
    copyLoading.value = false
  }
}

const selectCopy = (row) => {
  borrowForm.bookId = row.bookId
  borrowForm.copyId = row.id
  ElMessage.success(`已选择副本 ${row.barcode || row.id}`)
}

const handleBorrow = async () => {
  if (!borrowFormRef.value) return
  await borrowFormRef.value.validate()
  borrowLoading.value = true
  borrowResult.value = null
  try {
    const res = await borrowBook({
      readerId: borrowForm.readerId,
      bookId: borrowForm.bookId || undefined,
      copyId: borrowForm.copyId || undefined
    })
    borrowResult.value = res.data
    ElMessage.success('借书成功')
    borrowForm.readerId = ''
    borrowForm.bookId = ''
    borrowForm.copyId = ''
    availableCopies.value = []
  } catch {
  } finally {
    borrowLoading.value = false
  }
}

// ---------- 还书 ----------
const returnFormRef = ref(null)
const returnLoading = ref(false)
const returnResult = ref(null)

const returnForm = reactive({ borrowRecordId: '' })
const returnRules = {
  borrowRecordId: [{ required: true, message: '请输入借阅记录ID', trigger: 'blur' }]
}

const handleReturn = async () => {
  if (!returnFormRef.value) return
  await returnFormRef.value.validate()
  returnLoading.value = true
  returnResult.value = null
  try {
    const res = await returnBook(returnForm.borrowRecordId)
    returnResult.value = res.data
    ElMessage.success('还书成功')
    returnForm.borrowRecordId = ''
  } catch {
  } finally {
    returnLoading.value = false
  }
}
</script>

<style scoped>
</style>

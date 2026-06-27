<template>
  <div class="feedback-container">
    <el-card>
      <template #header><span>留言反馈管理</span></template>

      <!-- 筛选 -->
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable @clear="fetchData">
            <el-option label="未回复" :value="0" />
            <el-option label="已回复" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 列表 -->
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="readerName" label="读者" width="120" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            {{ feedbackTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '已回复' : '未回复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openReply(row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复反馈" width="550px" destroy-on-close>
      <div class="feedback-detail">
        <p><strong>读者：</strong>{{ currentFeedback.readerName }}</p>
        <p><strong>标题：</strong>{{ currentFeedback.title }}</p>
        <p><strong>内容：</strong>{{ currentFeedback.content }}</p>
      </div>
      <el-divider />
      <el-form ref="replyFormRef" :model="replyForm" :rules="replyRules" label-width="60px">
        <el-form-item label="回复" prop="reply">
          <el-input
            v-model="replyForm.reply"
            type="textarea"
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="confirmReply">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFeedbackList, replyFeedback } from '../../../api/modules/system'

const loading = ref(false)
const replyLoading = ref(false)
const replyDialogVisible = ref(false)
const replyFormRef = ref(null)
const tableData = ref([])
const currentFeedback = ref({})

const filters = reactive({
  status: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const replyForm = reactive({
  reply: ''
})

const replyRules = {
  reply: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

const feedbackTypeLabel = (type) => {
  const map = { 1: '购书建议', 2: '图书丢失', 3: '投诉', 4: '其他' }
  return map[type] || '其他'
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      status: filters.status !== '' ? filters.status : undefined
    }
    const res = await getFeedbackList(params)
    tableData.value = res.data.records || res.data.list || res.data
    pagination.total = res.data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.status = ''
  pagination.page = 1
  fetchData()
}

const openReply = (row) => {
  currentFeedback.value = { ...row }
  replyForm.reply = ''
  replyDialogVisible.value = true
}

const confirmReply = async () => {
  const valid = await replyFormRef.value.validate().catch(() => false)
  if (!valid) return

  replyLoading.value = true
  try {
    await replyFeedback(currentFeedback.value.id, { reply: replyForm.reply })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    fetchData()
  } catch {
    // handled
  } finally {
    replyLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.feedback-container {
  padding: 0;
}
.filter-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.feedback-detail p {
  margin: 6px 0;
  font-size: 14px;
  color: #606266;
}
</style>

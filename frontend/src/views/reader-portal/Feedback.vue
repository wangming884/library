<template>
  <div class="feedback-container">
    <el-row :gutter="24">
      <!-- 提交反馈 Form -->
      <el-col :xs="24" :md="10" class="compose-col">
        <el-card shadow="never" class="feedback-form-card">
          <template #header>
            <div class="card-header-title">
              <el-icon color="#409EFF"><EditPen /></el-icon>
              <span>读者留言与诉求提交</span>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="反馈类型" prop="type">
              <el-radio-group v-model="form.type" class="type-radio-group">
                <el-radio-button :label="1">📚 购书建议</el-radio-button>
                <el-radio-button :label="2">🔍 图书丢失</el-radio-button>
                <el-radio-button :label="3">📢 服务建议</el-radio-button>
                <el-radio-button :label="4">💬 其他诉求</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="留言标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="简明扼要概括您的诉求 (例如：建议采购《深入理解计算机系统》第4版)"
                maxlength="80"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="详细说明" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="6"
                placeholder="请详细描述具体情况，如书名、ISBN、遇到故障的设备位置或意见细节，图书馆管理员将尽快查阅并回复您..."
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>

            <el-form-item class="form-btn-row">
              <el-button type="primary" :loading="submitting" @click="handleSubmit">
                确认提交反馈
              </el-button>
              <el-button @click="resetForm">重置表单</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 我的反馈记录 -->
      <el-col :xs="24" :md="14" class="list-col">
        <el-card shadow="never" class="feedback-list-card">
          <template #header>
            <div class="list-header-row">
              <div class="card-header-title">
                <el-icon color="#67C23A"><ChatLineRound /></el-icon>
                <span>我的历史留言与答复</span>
              </div>
              
              <!-- 状态筛选 -->
              <el-radio-group v-model="statusFilter" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="replied">已回复</el-radio-button>
                <el-radio-button label="pending">处理中</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <div class="feedback-list-body" v-loading="loading">
            <el-empty v-if="filteredList.length === 0" description="暂无符合条件的留言记录" />

            <div v-for="item in filteredList" :key="item.id" class="feedback-card-item">
              <div class="feedback-top-line">
                <el-tag size="small" :type="feedbackTagColor(item.type)" effect="light">
                  {{ feedbackTypeLabel(item.type) }}
                </el-tag>
                <h4 class="feedback-headline" :title="item.title">{{ item.title }}</h4>
                <el-tag :type="item.status === 1 ? 'success' : 'warning'" size="small">
                  {{ item.status === 1 ? '已答复' : '待处理' }}
                </el-tag>
              </div>

              <p class="feedback-desc-text">{{ item.content }}</p>

              <!-- 管理员回复卡片 -->
              <div class="admin-reply-box" v-if="item.reply">
                <div class="reply-badge">
                  <el-icon><Service /></el-icon>
                  <span>图书馆管理员回复</span>
                  <span class="reply-time-str" v-if="item.replyTime">{{ item.replyTime }}</span>
                </div>
                <div class="reply-content-text">{{ item.reply }}</div>
              </div>

              <div class="feedback-footer-line">
                <span>提交时间：{{ item.createTime || '-' }}</span>
              </div>
            </div>
          </div>

          <el-pagination
            class="pagination"
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchFeedback"
            @current-change="fetchFeedback"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { EditPen, ChatLineRound, Service } from '@element-plus/icons-vue'
import { submitFeedback, myFeedback } from '../../api/modules/system'

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const feedbackList = ref([])
const statusFilter = ref('all')

const form = reactive({
  type: 1,
  title: '',
  content: ''
})

const feedbackTypeLabel = (type) => {
  const map = { 1: '购书建议', 2: '图书丢失', 3: '服务建议', 4: '其他' }
  return map[type] || '其他'
}

const feedbackTagColor = (type) => {
  const map = { 1: 'primary', 2: 'danger', 3: 'warning', 4: 'info' }
  return map[type] || 'info'
}

const rules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入反馈标题', trigger: 'blur' }],
  content: [{ required: true, message: '请详细描述诉求内容', trigger: 'blur' }]
}

const pagination = reactive({
  page: 1,
  pageSize: 5,
  total: 0
})

const filteredList = computed(() => {
  if (statusFilter.value === 'replied') {
    return feedbackList.value.filter(item => item.status === 1)
  }
  if (statusFilter.value === 'pending') {
    return feedbackList.value.filter(item => item.status !== 1)
  }
  return feedbackList.value
})

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await submitFeedback({
      type: form.type,
      title: form.title,
      content: form.content
    })
    ElMessage.success('反馈已成功提交，工作人员将尽快回复！')
    resetForm()
    fetchFeedback()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.type = 1
  form.title = ''
  form.content = ''
  formRef.value?.resetFields()
}

const fetchFeedback = async () => {
  loading.value = true
  try {
    const res = await myFeedback({
      page: pagination.page,
      size: pagination.pageSize
    })
    feedbackList.value = res.data.records || res.data.list || res.data || []
    pagination.total = res.data.total || 0
  } catch {
    feedbackList.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFeedback()
})
</script>

<style scoped>
.feedback-container {
  width: 100%;
}

.feedback-form-card,
.feedback-list-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.type-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.form-btn-row {
  margin-top: 20px;
  margin-bottom: 0;
}

.list-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.feedback-list-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feedback-card-item {
  background: #fdfdfd;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 16px;
  transition: all 0.2s;
}

.feedback-card-item:hover {
  background: #f9fafb;
  border-color: #dcdfe6;
}

.feedback-top-line {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.feedback-headline {
  flex: 1;
  margin: 0;
  font-size: 15px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.feedback-desc-text {
  margin: 0 0 12px;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

.admin-reply-box {
  background: #eff6ff;
  border-left: 4px solid #3b82f6;
  border-radius: 0 8px 8px 0;
  padding: 12px 14px;
  margin-bottom: 10px;
}

.reply-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #1d4ed8;
  margin-bottom: 6px;
}

.reply-time-str {
  margin-left: auto;
  font-size: 11px;
  font-weight: normal;
  color: #93c5fd;
}

.reply-content-text {
  font-size: 13px;
  color: #1e40af;
  line-height: 1.6;
}

.feedback-footer-line {
  font-size: 12px;
  color: #9ca3af;
}

.pagination {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}
</style>

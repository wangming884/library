<template>
  <div class="feedback-container">
    <el-row :gutter="20">
      <!-- 提交反馈 -->
      <el-col :span="10">
        <el-card>
          <template #header><span>提交反馈</span></template>
          <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
            <el-form-item label="类型" prop="type">
              <el-select v-model="form.type" placeholder="请选择反馈类型" style="width: 100%;">
                <el-option label="建议" value="建议" />
                <el-option label="投诉" value="投诉" />
                <el-option label="咨询" value="咨询" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="标题" prop="title">
              <el-input v-model="form.title" placeholder="请输入标题" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="内容" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="6"
                placeholder="请详细描述您的反馈内容"
                maxlength="1000"
                show-word-limit
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">提交反馈</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 我的反馈 -->
      <el-col :span="14">
        <el-card>
          <template #header><span>我的反馈</span></template>
          <div class="feedback-list" v-loading="loading">
            <el-empty v-if="feedbackList.length === 0" description="暂无反馈记录" />
            <div v-for="item in feedbackList" :key="item.id" class="feedback-item">
              <div class="feedback-header">
                <el-tag size="small" type="info">{{ item.type }}</el-tag>
                <span class="feedback-title">{{ item.title }}</span>
                <el-tag :type="item.status === 1 ? 'success' : 'warning'" size="small">
                  {{ item.status === 1 ? '已回复' : '未回复' }}
                </el-tag>
              </div>
              <div class="feedback-content">{{ item.content }}</div>
              <div class="feedback-reply" v-if="item.reply">
                <el-divider content-position="left">管理员回复</el-divider>
                <p>{{ item.reply }}</p>
              </div>
              <div class="feedback-time">
                <span>提交时间：{{ item.createTime }}</span>
                <span v-if="item.replyTime">回复时间：{{ item.replyTime }}</span>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { submitFeedback, myFeedback } from '../../api/modules/system'

const formRef = ref(null)
const submitting = ref(false)
const loading = ref(false)
const feedbackList = ref([])

const form = reactive({
  type: '',
  title: '',
  content: ''
})

const rules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
}

const pagination = reactive({
  page: 1,
  pageSize: 5,
  total: 0
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
    ElMessage.success('反馈提交成功')
    resetForm()
    fetchFeedback()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.type = ''
  form.title = ''
  form.content = ''
  formRef.value?.resetFields()
}

const fetchFeedback = async () => {
  loading.value = true
  try {
    const res = await myFeedback({
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    feedbackList.value = res.data.records || res.data.list || res.data
    pagination.total = res.data.total || 0
  } catch {
    // handled
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
  max-width: 1100px;
  margin: 0 auto;
}
.feedback-item {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}
.feedback-item:last-child {
  border-bottom: none;
}
.feedback-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.feedback-title {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.feedback-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 4px;
}
.feedback-reply {
  margin-top: 4px;
}
.feedback-reply p {
  font-size: 14px;
  color: #409EFF;
  background: #ecf5ff;
  padding: 10px 14px;
  border-radius: 4px;
  margin: 0;
}
.feedback-time {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

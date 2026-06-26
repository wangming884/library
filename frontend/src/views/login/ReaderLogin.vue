<template>
  <div class="login-wrapper">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>读者登录</h2>
          <p>Library Management System</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="cardNo">
          <el-input
            v-model="form.cardNo"
            placeholder="请输入借书证号"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <router-link to="/register">还没有账号？立即注册</router-link>
        <span class="divider">|</span>
        <router-link to="/login">管理员登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { readerLogin } from '../../api/modules/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  cardNo: '',
  password: ''
})

const rules = reactive({
  cardNo: [{ required: true, message: '请输入借书证号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await readerLogin({
      cardNo: form.cardNo,
      password: form.password
    })
    userStore.setLogin(res.data)
    ElMessage.success('登录成功')
    router.push('/portal/home')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.login-card {
  width: 420px;
  border-radius: 12px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 8px;
  color: #303133;
  font-size: 24px;
}

.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.login-btn {
  width: 100%;
}

.card-footer {
  text-align: center;
  margin-top: 12px;
  font-size: 14px;
}

.card-footer a {
  color: #409eff;
  text-decoration: none;
}

.card-footer a:hover {
  text-decoration: underline;
}

.card-footer .divider {
  margin: 0 8px;
  color: #dcdfe6;
}
</style>

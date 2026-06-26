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
  background: linear-gradient(-45deg, #12c2e9, #c471ed, #f64f59);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
}

@keyframes gradientBG {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.login-card {
  width: 420px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 15px 35px rgba(31, 38, 135, 0.15);
  transition: transform 0.4s ease, box-shadow 0.4s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 20px 40px rgba(31, 38, 135, 0.2);
}

.card-header {
  text-align: center;
  padding-top: 10px;
}

.card-header h2 {
  margin: 0 0 10px;
  color: #303133;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 1px;
}

.card-header p {
  margin: 0;
  color: #606266;
  font-size: 15px;
  letter-spacing: 0.5px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  letter-spacing: 2px;
  background: linear-gradient(to right, #4facfe 0%, #00f2fe 100%);
  border: none;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 8px 15px rgba(0, 242, 254, 0.4);
}

.card-footer {
  text-align: center;
  margin-top: 15px;
  font-size: 14px;
}

.card-footer a {
  color: #409eff;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.3s;
}

.card-footer a:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.card-footer .divider {
  margin: 0 12px;
  color: #dcdfe6;
}

/* 覆盖 el-card 的一些默认样式 */
:deep(.el-card__header) {
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}
:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 0 1px rgba(0,0,0,0.05) inset;
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}
</style>

<template>
  <div class="login-wrapper">
    <div class="login-glow-orb orb-1"></div>
    <div class="login-glow-orb orb-2"></div>

    <div class="login-card">
      <div class="card-header">
        <div class="login-logo-icon">
          <el-icon size="28" color="#fff"><Setting /></el-icon>
        </div>
        <h2>管理运维中心登录</h2>
        <p>智慧图书馆运营与业务管控后台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
        @keyup.enter="handleLogin"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入管理员用户名 / 工号"
            :prefix-icon="User"
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入管理员密码"
            :prefix-icon="Lock"
            show-password
            clearable
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录 管 理 后 台
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        <router-link to="/reader-login">切换为读者登录</router-link>
        <span class="divider">|</span>
        <router-link to="/register">读者证注册</router-link>
        <span class="divider">|</span>
        <router-link to="/">返回网站首页</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { adminLogin } from '../../api/modules/auth'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = reactive({
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await adminLogin({
      username: form.username.trim(),
      password: form.password
    })
    userStore.setLogin(res.data)
    ElMessage.success('管理员登录成功，正在进入运营后台...')
    router.push('/admin/dashboard')
  } catch (err) {
    // handled by request interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #091a32 0%, #172554 45%, #1e3a8a 100%);
  overflow: hidden;
  padding: 20px;
}

.login-glow-orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
  filter: blur(80px);
}

.orb-1 {
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.25) 0%, transparent 70%);
}

.orb-2 {
  bottom: -100px;
  left: -100px;
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.2) 0%, transparent 70%);
}

.login-card {
  position: relative;
  z-index: 1;
  width: 440px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.3);
  padding: 40px 36px;
}

.card-header {
  text-align: center;
  margin-bottom: 28px;
}

.login-logo-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(220, 38, 38, 0.3);
  margin-bottom: 12px;
}

.card-header h2 {
  margin: 0 0 6px;
  color: #0f172a;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.3px;
}

.card-header p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.login-form {
  margin-top: 10px;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 700;
  border-radius: 10px;
  letter-spacing: 1px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border: none;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
  transition: all 0.25s;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.4);
}

.card-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 13px;
}

.card-footer a {
  color: #2563eb;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.2s;
}

.card-footer a:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.card-footer .divider {
  margin: 0 10px;
  color: #cbd5e1;
}
</style>

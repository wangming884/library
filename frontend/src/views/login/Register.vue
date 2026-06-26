<template>
  <div class="register-wrapper">
    <el-card class="register-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>读者注册</h2>
          <p>创建您的借书证账户</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        size="large"
        label-position="right"
      >
        <el-form-item label="借书证号" prop="cardNo">
          <el-input v-model="form.cardNo" placeholder="请输入借书证号" clearable />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" clearable />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" clearable />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" clearable />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="院系" prop="dept">
          <el-input v-model="form.dept" placeholder="请输入院系" clearable />
        </el-form-item>

        <el-form-item label="读者类型" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择读者类型" style="width: 100%">
            <el-option
              v-for="item in typeOptions"
              :key="item.id"
              :label="item.typeName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister">
            注 册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="card-footer">
        已有账号？
        <router-link to="/reader-login">立即登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, getReaderTypes } from '../../api/modules/reader'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const typeOptions = ref([])

const form = reactive({
  cardNo: '',
  name: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  gender: 1,
  dept: '',
  typeId: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = reactive({
  cardNo: [
    { required: true, message: '请输入借书证号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  dept: [
    { required: true, message: '请输入院系', trigger: 'blur' }
  ],
  typeId: [
    { required: true, message: '请选择读者类型', trigger: 'change' }
  ]
})

const fetchTypes = async () => {
  try {
    const res = await getReaderTypes()
    typeOptions.value = res.data || []
  } catch {
    // error handled by interceptor
  }
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { confirmPassword, ...data } = form
    await register(data)
    ElMessage.success('注册成功，请登录')
    router.push('/reader-login')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTypes()
})
</script>

<style scoped>
.register-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  padding: 40px 0;
}

.register-card {
  width: 520px;
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

.register-btn {
  width: 100%;
}

.card-footer {
  text-align: center;
  margin-top: 12px;
  font-size: 14px;
  color: #909399;
}

.card-footer a {
  color: #409eff;
  text-decoration: none;
}

.card-footer a:hover {
  text-decoration: underline;
}
</style>

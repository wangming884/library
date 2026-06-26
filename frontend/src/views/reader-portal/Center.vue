<template>
  <div class="center-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 我的借阅 -->
      <el-tab-pane label="我的借阅" name="borrow">
        <el-table :data="borrowRecords" v-loading="borrowLoading" stripe border>
          <el-table-column prop="bookName" label="图书名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="borrowDate" label="借阅日期" width="170" />
          <el-table-column prop="dueDate" label="应还日期" width="170" />
          <el-table-column prop="returnDate" label="归还日期" width="170">
            <template #default="{ row }">{{ row.returnDate || '-' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="borrowStatusType(row.status)">{{ borrowStatusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 0"
                size="small"
                type="primary"
                :loading="row._renewing"
                @click="handleRenew(row)"
              >
                续借
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pagination"
          v-model:current-page="borrowPagination.page"
          v-model:page-size="borrowPagination.pageSize"
          :total="borrowPagination.total"
          :page-sizes="[10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchBorrowRecords"
          @current-change="fetchBorrowRecords"
        />
      </el-tab-pane>

      <!-- 我的预约 -->
      <el-tab-pane label="我的预约" name="reservation">
        <el-table :data="reservations" v-loading="reservationLoading" stripe border>
          <el-table-column prop="bookName" label="图书名称" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="预约时间" width="170" />
          <el-table-column prop="expireTime" label="过期时间" width="170" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'warning' : row.status === 1 ? 'success' : 'info'">
                {{ row.status === 0 ? '等待中' : row.status === 1 ? '已到书' : '已取消' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 0"
                size="small"
                type="danger"
                @click="handleCancelReservation(row)"
              >
                取消
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pagination"
          v-model:current-page="reservationPagination.page"
          v-model:page-size="reservationPagination.pageSize"
          :total="reservationPagination.total"
          :page-sizes="[10, 20]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchReservations"
          @current-change="fetchReservations"
        />
      </el-tab-pane>

      <!-- 我的罚款 -->
      <el-tab-pane label="我的罚款" name="fine">
        <el-table :data="fines" v-loading="fineLoading" stripe border>
          <el-table-column prop="bookName" label="图书名称" min-width="180" show-overflow-tooltip />
          <el-table-column prop="amount" label="罚款金额" width="120">
            <template #default="{ row }">
              <span style="color: #F56C6C; font-weight: bold;">{{ row.amount?.toFixed(2) }} 元</span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="fineTypeMap[row.type]?.type">{{ fineTypeMap[row.type]?.label || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '已缴' : '未缴' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="产生时间" width="170" />
        </el-table>
        <div class="unpaid-summary" v-if="unpaidAmount > 0">
          <el-alert
            :title="`您有未缴罚款共计 ${unpaidAmount.toFixed(2)} 元，请前往图书馆缴纳`"
            type="warning"
            show-icon
            :closable="false"
          />
        </div>
      </el-tab-pane>

      <!-- 个人信息 -->
      <el-tab-pane label="个人信息" name="profile">
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="100px"
          style="max-width: 600px;"
          v-loading="profileLoading"
        >
          <el-form-item label="读者证号">
            <el-input v-model="profileForm.cardNo" disabled />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="profileForm.name" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="profileForm.gender">
              <el-radio :label="1">男</el-radio>
              <el-radio :label="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="profileForm.email" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="profileForm.phone" />
          </el-form-item>
          <el-form-item label="院系">
            <el-input v-model="profileForm.department" disabled />
          </el-form-item>
          <el-form-item label="读者类型">
            <el-input v-model="profileForm.typeName" disabled />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane label="修改密码" name="password">
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          style="max-width: 500px;"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="passwordSaving" @click="handleChangePassword">确认修改</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myBorrowRecords, myReservations, cancelReservation, readerRenew } from '../../api/modules/borrow'
import { myFines, myUnpaidAmount } from '../../api/modules/fine'
import { getProfile, updateProfile, changeReaderPassword } from '../../api/modules/reader'

const activeTab = ref('borrow')

// === 借阅记录 ===
const borrowLoading = ref(false)
const borrowRecords = ref([])
const borrowPagination = reactive({ page: 1, pageSize: 10, total: 0 })

const borrowStatusType = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info')
const borrowStatusLabel = (s) => ({ 0: '借阅中', 1: '已归还', 2: '已逾期' }[s] || '未知')

const fetchBorrowRecords = async () => {
  borrowLoading.value = true
  try {
    const res = await myBorrowRecords({
      page: borrowPagination.page,
      pageSize: borrowPagination.pageSize
    })
    borrowRecords.value = res.data.records || res.data.list || res.data
    borrowPagination.total = res.data.total || 0
  } catch {
    // handled
  } finally {
    borrowLoading.value = false
  }
}

const handleRenew = async (row) => {
  try {
    await ElMessageBox.confirm('确认续借此书？', '续借确认')
    row._renewing = true
    await readerRenew(row.id)
    ElMessage.success('续借成功')
    fetchBorrowRecords()
  } catch {
    // handled
  } finally {
    row._renewing = false
  }
}

// === 预约记录 ===
const reservationLoading = ref(false)
const reservations = ref([])
const reservationPagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchReservations = async () => {
  reservationLoading.value = true
  try {
    const res = await myReservations({
      page: reservationPagination.page,
      pageSize: reservationPagination.pageSize
    })
    reservations.value = res.data.records || res.data.list || res.data
    reservationPagination.total = res.data.total || 0
  } catch {
    // handled
  } finally {
    reservationLoading.value = false
  }
}

const handleCancelReservation = (row) => {
  ElMessageBox.confirm('确定取消此预约？', '提示', { type: 'warning' }).then(async () => {
    try {
      await cancelReservation(row.id)
      ElMessage.success('已取消预约')
      fetchReservations()
    } catch {
      // handled
    }
  }).catch(() => {})
}

// === 罚款 ===
const fineLoading = ref(false)
const fines = ref([])
const unpaidAmount = ref(0)

const fineTypeMap = {
  1: { label: '逾期', type: 'warning' },
  2: { label: '损毁', type: 'danger' },
  3: { label: '丢失', type: 'info' }
}

const fetchFines = async () => {
  fineLoading.value = true
  try {
    const [finesRes, unpaidRes] = await Promise.all([myFines(), myUnpaidAmount()])
    fines.value = finesRes.data.records || finesRes.data.list || finesRes.data
    unpaidAmount.value = unpaidRes.data?.amount || unpaidRes.data || 0
  } catch {
    // handled
  } finally {
    fineLoading.value = false
  }
}

// === 个人信息 ===
const profileLoading = ref(false)
const profileSaving = ref(false)
const profileFormRef = ref(null)
const profileForm = reactive({
  cardNo: '',
  name: '',
  gender: 1,
  email: '',
  phone: '',
  department: '',
  typeName: ''
})

const profileRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入正确的手机号', trigger: 'blur' }]
}

const fetchProfile = async () => {
  profileLoading.value = true
  try {
    const res = await getProfile()
    const data = res.data || {}
    Object.assign(profileForm, {
      cardNo: data.cardNo || '',
      name: data.name || '',
      gender: data.gender || 1,
      email: data.email || '',
      phone: data.phone || '',
      department: data.department || '',
      typeName: data.typeName || ''
    })
  } catch {
    // handled
  } finally {
    profileLoading.value = false
  }
}

const handleSaveProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return
  profileSaving.value = true
  try {
    await updateProfile({
      name: profileForm.name,
      gender: profileForm.gender,
      email: profileForm.email,
      phone: profileForm.phone
    })
    ElMessage.success('保存成功')
  } catch {
    // handled
  } finally {
    profileSaving.value = false
  }
}

// === 修改密码 ===
const passwordSaving = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码不少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleChangePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  passwordSaving.value = true
  try {
    await changeReaderPassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch {
    // handled
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  fetchBorrowRecords()
  fetchReservations()
  fetchFines()
  fetchProfile()
})
</script>

<style scoped>
.center-container {
  max-width: 1000px;
  margin: 0 auto;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.unpaid-summary {
  margin-top: 16px;
}
</style>

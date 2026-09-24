<template>
  <div class="center-container">
    <!-- 顶部读者概览卡片 Banner -->
    <div class="reader-summary-banner">
      <div class="reader-profile-card">
        <el-avatar :size="64" class="reader-avatar-large">
          <el-icon :size="32"><UserFilled /></el-icon>
        </el-avatar>
        <div class="reader-info-box">
          <div class="name-row">
            <h2>{{ profileForm.name || userStore.userInfo.name || '读者' }}</h2>
            <el-tag type="success" effect="light" size="small">{{ profileForm.typeName || '读者' }}</el-tag>
            <el-tag type="info" effect="plain" size="small">卡号: {{ profileForm.cardNo || userStore.userInfo.cardNo || '-' }}</el-tag>
          </div>
          <p class="dept-text">
            <span>{{ profileForm.department || '校内读者' }}</span>
            <span class="divider">•</span>
            <span>读者状态：<el-tag type="success" size="small">正常</el-tag></span>
          </p>
        </div>
      </div>

      <!-- 快捷指标卡 -->
      <div class="stat-metrics">
        <div class="metric-card" :class="{ 'is-active': activeTab === 'borrow' }" @click="activeTab = 'borrow'">
          <div class="metric-icon borrow-icon">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="metric-info">
            <span class="metric-title">当前借阅中</span>
            <span class="metric-value">{{ activeBorrowCount }}</span>
          </div>
        </div>

        <div class="metric-card" :class="{ 'is-active': activeTab === 'reservation' }" @click="activeTab = 'reservation'">
          <div class="metric-icon reserve-icon">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="metric-info">
            <span class="metric-title">预约待取</span>
            <span class="metric-value">{{ activeReservationCount }}</span>
          </div>
        </div>

        <div class="metric-card" :class="{ 'is-active': activeTab === 'fine' }" @click="activeTab = 'fine'">
          <div class="metric-icon fine-icon">
            <el-icon><Money /></el-icon>
          </div>
          <div class="metric-info">
            <span class="metric-title">待缴罚款</span>
            <span class="metric-value" :class="{ 'has-fine': unpaidAmount > 0 }">
              ¥ {{ money(unpaidAmount) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <el-card shadow="never" class="tabs-card">
      <el-tabs v-model="activeTab" class="modern-tabs">
        <!-- 我的借阅 -->
        <el-tab-pane name="borrow">
          <template #label>
            <span class="tab-label">
              <el-icon><Reading /></el-icon>我的借阅
              <el-badge v-if="activeBorrowCount > 0" :value="activeBorrowCount" class="tab-badge" />
            </span>
          </template>

          <el-table :data="borrowRecords" v-loading="borrowLoading" stripe border>
            <el-table-column prop="bookTitle" label="图书名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="borrowDate" label="借阅日期" width="170" />
            <el-table-column prop="dueDate" label="应还日期" width="170" />
            <el-table-column prop="returnDate" label="归还日期" width="170">
              <template #default="{ row }">{{ row.returnDate || '-' }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="borrowStatusType(row.status)">{{ borrowStatusLabel(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="190" fixed="right">
              <template #default="{ row }">
                <template v-if="row.status === 1 || row.status === 3">
                  <el-button
                    v-if="row.status === 1"
                    size="small"
                    type="primary"
                    :loading="row._renewing"
                    @click="handleRenew(row)"
                  >
                    续借
                  </el-button>
                  <el-button
                    size="small"
                    type="success"
                    :loading="row._returning"
                    @click="handleReturn(row)"
                  >
                    提前归还
                  </el-button>
                </template>
                <span v-else class="text-muted">-</span>
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
        <el-tab-pane name="reservation">
          <template #label>
            <span class="tab-label">
              <el-icon><Calendar /></el-icon>我的预约
              <el-badge v-if="activeReservationCount > 0" :value="activeReservationCount" class="tab-badge" type="warning" />
            </span>
          </template>

          <el-table :data="reservations" v-loading="reservationLoading" stripe border>
            <el-table-column prop="bookTitle" label="图书名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="reserveDate" label="预约时间" width="170" />
            <el-table-column prop="expireDate" label="过期时间" width="170" />
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="reservationStatusType(row.status)">
                  {{ reservationStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 1"
                  size="small"
                  type="danger"
                  @click="handleCancelReservation(row)"
                >
                  取消预约
                </el-button>
                <span v-else class="text-muted">-</span>
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
        <el-tab-pane name="fine">
          <template #label>
            <span class="tab-label">
              <el-icon><Money /></el-icon>我的罚款
            </span>
          </template>

          <div class="unpaid-summary" v-if="unpaidAmount > 0">
            <el-alert
              :title="`您当前有未缴罚款共计 ¥ ${money(unpaidAmount)} 元，请尽快前往图书馆服务台处理缴纳。`"
              type="warning"
              show-icon
              :closable="false"
              style="margin-bottom: 16px;"
            />
          </div>

          <el-table :data="fines" v-loading="fineLoading" stripe border>
            <el-table-column prop="bookTitle" label="图书名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="amount" label="罚款金额" width="130">
              <template #default="{ row }">
                <span class="fine-amount">¥ {{ money(row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="type" label="类型" width="110" align="center">
              <template #default="{ row }">
                <el-tag :type="fineTypeMap[row.type]?.type">{{ fineTypeMap[row.type]?.label || '-' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '已缴' : '未缴' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="产生时间" width="170" />
          </el-table>
        </el-tab-pane>

        <!-- 个人资料 -->
        <el-tab-pane name="profile">
          <template #label>
            <span class="tab-label">
              <el-icon><User /></el-icon>个人信息
            </span>
          </template>

          <div class="form-container">
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              style="max-width: 580px;"
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
                <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile">
                  保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 修改密码 -->
        <el-tab-pane name="password">
          <template #label>
            <span class="tab-label">
              <el-icon><Lock /></el-icon>修改密码
            </span>
          </template>

          <div class="form-container">
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
                <el-button type="primary" :loading="passwordSaving" @click="handleChangePassword">
                  确认修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { myBorrowRecords, myReservations, cancelReservation, readerRenew, readerReturnBook } from '../../api/modules/borrow'
import { myFines, myUnpaidAmount } from '../../api/modules/fine'
import { getProfile, updateProfile, changeReaderPassword } from '../../api/modules/reader'

const userStore = useUserStore()
const activeTab = ref('borrow')

// === 借阅记录 ===
const borrowLoading = ref(false)
const borrowRecords = ref([])
const borrowPagination = reactive({ page: 1, pageSize: 10, total: 0 })

const borrowStatusType = (s) => ({ 1: 'warning', 2: 'success', 3: 'danger', 4: 'info' }[s] || 'info')
const borrowStatusLabel = (s) => ({ 1: '借阅中', 2: '已归还', 3: '已逾期', 4: '丢失' }[s] || '未知')

const activeBorrowCount = computed(() => {
  return borrowRecords.value.filter(r => r.status === 1 || r.status === 3).length
})

const fetchBorrowRecords = async () => {
  borrowLoading.value = true
  try {
    const res = await myBorrowRecords({
      page: borrowPagination.page,
      size: borrowPagination.pageSize
    })
    borrowRecords.value = res.data.records || res.data.list || res.data || []
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

const handleReturn = async (row) => {
  try {
    await ElMessageBox.confirm('确认现在归还这本书？归还后该副本会重新变为可借。', '提前归还确认', {
      type: 'warning',
      confirmButtonText: '确认归还',
      cancelButtonText: '取消'
    })
    row._returning = true
    const res = await readerReturnBook(row.id)
    ElMessage.success(res.data?.remark || '归还成功')
    fetchBorrowRecords()
  } catch {
    // handled
  } finally {
    row._returning = false
  }
}

// === 预约记录 ===
const reservationLoading = ref(false)
const reservations = ref([])
const reservationPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const reservationStatusType = (s) => ({ 1: 'warning', 2: 'success', 3: 'info', 4: 'success', 5: 'danger' }[s] || 'info')
const reservationStatusLabel = (s) => ({ 1: '等待中', 2: '已到书', 3: '已取消', 4: '已完成', 5: '已过期' }[s] || '未知')

const activeReservationCount = computed(() => {
  return reservations.value.filter(r => r.status === 1 || r.status === 2).length
})

const fetchReservations = async () => {
  reservationLoading.value = true
  try {
    const res = await myReservations({
      page: reservationPagination.page,
      size: reservationPagination.pageSize
    })
    reservations.value = res.data.records || res.data.list || res.data || []
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

const money = (value) => Number(value || 0).toFixed(2)

const fetchFines = async () => {
  fineLoading.value = true
  try {
    const [finesRes, unpaidRes] = await Promise.all([myFines(), myUnpaidAmount()])
    fines.value = finesRes.data.records || finesRes.data.list || finesRes.data || []
    unpaidAmount.value = Number(unpaidRes.data?.unpaidAmount || unpaidRes.data || 0)
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
      department: data.dept || '',
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
    ElMessage.success('密码修改成功')
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
  width: 100%;
}

.reader-summary-banner {
  background: #fff;
  border-radius: 12px;
  padding: 24px 28px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #ebeef5;
}

.reader-profile-card {
  display: flex;
  align-items: center;
  gap: 18px;
}

.reader-avatar-large {
  background: linear-gradient(135deg, #409EFF, #67C23A);
  color: #fff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.25);
}

.reader-info-box .name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.reader-info-box h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.dept-text {
  margin: 0;
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-metrics {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.metric-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #f8fafc;
  border: 1px solid #eaedf1;
  padding: 12px 18px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.22s;
  min-width: 140px;
}

.metric-card:hover,
.metric-card.is-active {
  background: #ecf5ff;
  border-color: #b3d8ff;
  transform: translateY(-2px);
}

.metric-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.borrow-icon {
  background: #ecf5ff;
  color: #409EFF;
}

.reserve-icon {
  background: #fdf6ec;
  color: #E6A23C;
}

.fine-icon {
  background: #fef0f0;
  color: #F56C6C;
}

.metric-info {
  display: flex;
  flex-direction: column;
}

.metric-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.metric-value {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.metric-value.has-fine {
  color: #F56C6C;
}

.tabs-card {
  border-radius: 12px;
}

.modern-tabs {
  padding: 8px 4px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.tab-badge {
  margin-left: 4px;
}

.fine-amount {
  color: #F56C6C;
  font-weight: 600;
}

.form-container {
  padding: 20px 10px;
}

.pagination {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: #c0c4cc;
}
</style>

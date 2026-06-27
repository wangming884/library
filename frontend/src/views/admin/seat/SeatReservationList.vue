<template>
  <div class="seat-reservation-list">
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="读者ID">
          <el-input v-model="searchForm.readerId" placeholder="读者ID" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="searchForm.date" type="date" value-format="YYYY-MM-DD" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option v-for="s in reservationStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <div v-if="isSuperAdmin" style="margin-bottom: 16px">
        <el-button type="primary" @click="dialogVisible = true">新增座位</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="readerName" label="读者" width="120" show-overflow-tooltip />
        <el-table-column prop="roomName" label="自习室" min-width="140" show-overflow-tooltip />
        <el-table-column prop="seatNo" label="座位号" width="110" />
        <el-table-column prop="reserveDate" label="日期" width="120" />
        <el-table-column label="时间段" width="150">
          <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" width="170">
          <template #default="{ row }">{{ row.checkInTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="reservationStatusType(row.status)">{{ reservationStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="primary" @click="handleCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 1 || row.status === 2" link type="danger" @click="handleRelease(row)">释放</el-button>
            <span v-if="row.status !== 1 && row.status !== 2">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; justify-content: flex-end"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增座位" width="420px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="自习室" prop="roomName">
          <el-input v-model="form.roomName" />
        </el-form-item>
        <el-form-item label="座位号" prop="seatNo">
          <el-input v-model="form.seatNo" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-input v-model="form.floor" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="空闲" :value="1" />
            <el-option label="占用" :value="2" />
            <el-option label="维护" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAddSeat">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addSeat, checkInSeat, getSeatReservations, releaseSeat } from '../../../api/modules/seat'
import { useUserStore } from '../../../stores/user'

const userStore = useUserStore()
const isSuperAdmin = userStore.roleKey === 'super_admin'
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])
const searchForm = reactive({ readerId: '', date: '', status: '' })
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const form = reactive({ roomName: '', seatNo: '', floor: '', status: 1 })
const rules = {
  roomName: [{ required: true, message: '请输入自习室', trigger: 'blur' }],
  seatNo: [{ required: true, message: '请输入座位号', trigger: 'blur' }]
}

const reservationStatusOptions = [
  { value: 1, label: '预约中' },
  { value: 2, label: '已签到' },
  { value: 3, label: '已释放' },
  { value: 4, label: '未签到' }
]
const reservationStatusLabel = (status) => reservationStatusOptions.find(s => s.value === status)?.label || '未知'
const reservationStatusType = (status) => ({ 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }[status] || 'info')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSeatReservations({
      page: pagination.page,
      size: pagination.pageSize,
      readerId: searchForm.readerId || undefined,
      date: searchForm.date || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    })
    tableData.value = res.data.records || res.data.list || res.data || []
    pagination.total = res.data.total || 0
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const resetSearch = () => {
  searchForm.readerId = ''
  searchForm.date = ''
  searchForm.status = ''
  handleSearch()
}

const handleCheckIn = async (row) => {
  await checkInSeat(row.id)
  ElMessage.success('签到成功')
  fetchData()
}

const handleRelease = (row) => {
  ElMessageBox.confirm('确定释放此座位预约？', '释放座位', { type: 'warning' }).then(async () => {
    await releaseSeat(row.id)
    ElMessage.success('已释放')
    fetchData()
  }).catch(() => {})
}

const handleAddSeat = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await addSeat({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    Object.assign(form, { roomName: '', seatNo: '', floor: '', status: 1 })
    fetchData()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>

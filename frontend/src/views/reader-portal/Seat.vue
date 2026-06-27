<template>
  <div class="seat-page">
    <el-card shadow="never" class="filter-card">
      <el-form :model="reserveForm" inline>
        <el-form-item label="自习室">
          <el-input v-model="roomName" placeholder="全部自习室" clearable @keyup.enter="fetchSeats" />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="reserveForm.date" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="开始">
          <el-time-select
            v-model="reserveForm.startTime"
            start="08:00"
            step="00:30"
            end="21:30"
            placeholder="开始时间"
          />
        </el-form-item>
        <el-form-item label="结束">
          <el-time-select
            v-model="reserveForm.endTime"
            start="08:30"
            step="00:30"
            end="22:00"
            placeholder="结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchSeats">查询座位</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header><span>座位列表</span></template>
      <el-table :data="seats" v-loading="seatLoading" border stripe>
        <el-table-column prop="roomName" label="自习室" min-width="140" />
        <el-table-column prop="seatNo" label="座位号" width="120" />
        <el-table-column prop="floor" label="楼层" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="seatStatusType(row.status)">{{ seatStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :disabled="row.status !== 1"
              @click="handleReserve(row)"
            >
              预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="records-card">
      <template #header><span>我的座位预约</span></template>
      <el-table :data="reservations" v-loading="reservationLoading" border stripe>
        <el-table-column prop="roomName" label="自习室" min-width="140" />
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" link type="primary" @click="handleCheckIn(row)">签到</el-button>
            <el-button v-if="row.status === 1 || row.status === 2" link type="danger" @click="handleRelease(row)">释放</el-button>
            <span v-if="row.status !== 1 && row.status !== 2">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchReservations"
        @current-change="fetchReservations"
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { checkInSeat, getAvailableSeats, mySeatReservations, releaseSeat, reserveSeat } from '../../api/modules/seat'

const today = new Date().toISOString().slice(0, 10)
const roomName = ref('')
const seatLoading = ref(false)
const reservationLoading = ref(false)
const seats = ref([])
const reservations = ref([])
const reserveForm = reactive({
  date: today,
  startTime: '08:00',
  endTime: '10:00'
})
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const seatStatusLabel = (status) => ({ 1: '空闲', 2: '占用', 3: '维护' }[status] || '未知')
const seatStatusType = (status) => ({ 1: 'success', 2: 'warning', 3: 'info' }[status] || 'info')
const reservationStatusLabel = (status) => ({ 1: '预约中', 2: '已签到', 3: '已释放', 4: '未签到' }[status] || '未知')
const reservationStatusType = (status) => ({ 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }[status] || 'info')

const fetchSeats = async () => {
  seatLoading.value = true
  try {
    const res = await getAvailableSeats({ roomName: roomName.value || undefined })
    seats.value = res.data || []
  } catch {
    seats.value = []
  } finally {
    seatLoading.value = false
  }
}

const fetchReservations = async () => {
  reservationLoading.value = true
  try {
    const res = await mySeatReservations({
      page: pagination.page,
      size: pagination.pageSize
    })
    reservations.value = res.data.records || res.data.list || res.data || []
    pagination.total = res.data.total || 0
  } catch {
    reservations.value = []
  } finally {
    reservationLoading.value = false
  }
}

const handleReserve = (seat) => {
  ElMessageBox.confirm(`预约 ${seat.roomName} ${seat.seatNo}？`, '座位预约', { type: 'info' }).then(async () => {
    await reserveSeat({
      seatId: seat.id,
      date: reserveForm.date,
      startTime: reserveForm.startTime,
      endTime: reserveForm.endTime
    })
    ElMessage.success('预约成功')
    fetchReservations()
  }).catch(() => {})
}

const handleCheckIn = async (row) => {
  await checkInSeat(row.id)
  ElMessage.success('签到成功')
  fetchReservations()
}

const handleRelease = (row) => {
  ElMessageBox.confirm('确定释放此座位预约？', '释放座位', { type: 'warning' }).then(async () => {
    await releaseSeat(row.id)
    ElMessage.success('已释放')
    fetchReservations()
  }).catch(() => {})
}

onMounted(() => {
  fetchSeats()
  fetchReservations()
})
</script>

<style scoped>
.seat-page {
  max-width: 1000px;
  margin: 0 auto;
}
.filter-card {
  margin-bottom: 16px;
}
.records-card {
  margin-top: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

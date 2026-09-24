<template>
  <div class="seat-page">
    <!-- 预约条件筛选卡片 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-header">
        <div class="filter-title">
          <el-icon><OfficeBuilding /></el-icon>
          <span>自习室与座位预约中心</span>
        </div>
        <div class="view-mode-switch">
          <el-radio-group v-model="viewMode" size="small">
            <el-radio-button label="visual">
              <el-icon><Grid /></el-icon> 可视化平面选座
            </el-radio-button>
            <el-radio-button label="table">
              <el-icon><Menu /></el-icon> 列表模式
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <el-form :model="reserveForm" inline class="filter-form">
        <el-form-item label="自习室">
          <el-input
            v-model="roomName"
            placeholder="搜索自习室名称"
            clearable
            style="width: 170px;"
            @keyup.enter="fetchSeats"
          />
        </el-form-item>
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="reserveForm.date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            :disabled-date="disabledDate"
            style="width: 160px;"
          />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-select
            v-model="reserveForm.startTime"
            start="08:00"
            step="00:30"
            end="21:30"
            placeholder="开始时间"
            style="width: 130px;"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-select
            v-model="reserveForm.endTime"
            start="08:30"
            step="00:30"
            end="22:00"
            placeholder="结束时间"
            style="width: 130px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" :loading="seatLoading" @click="fetchSeats">
            查询空位
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 状态图例 -->
      <div class="legend-row">
        <span class="legend-label">图例说明：</span>
        <div class="legend-item"><span class="dot available"></span> 空闲可约</div>
        <div class="legend-item"><span class="dot occupied"></span> 已被占用</div>
        <div class="legend-item"><span class="dot maintenance"></span> 维护停用</div>
        <div class="legend-tips">💡 提示：点击空闲绿色座位即可快速发起预约</div>
      </div>
    </el-card>

    <!-- 可视化选座模式 -->
    <div v-if="viewMode === 'visual'" class="visual-seats-container" v-loading="seatLoading">
      <el-empty v-if="seats.length === 0" description="暂无可预约的自习室座位" />
      
      <div v-for="room in groupedSeats" :key="room.name" class="room-block">
        <div class="room-header">
          <div class="room-title">
            <span class="room-badge">{{ room.floor || '1F' }}</span>
            <span class="room-name">{{ room.name }}</span>
          </div>
          <div class="room-stats">
            <span>总座席：<strong>{{ room.seats.length }}</strong></span>
            <span class="text-success">空闲：<strong>{{ room.availableCount }}</strong></span>
            <span class="text-warning">占用：<strong>{{ room.occupiedCount }}</strong></span>
          </div>
        </div>

        <div class="seat-grid">
          <div
            v-for="seat in room.seats"
            :key="seat.id"
            class="seat-box"
            :class="{
              'is-available': seat.status === 1,
              'is-occupied': seat.status === 2,
              'is-maintenance': seat.status === 3
            }"
            @click="seat.status === 1 && openReserveDialog(seat)"
          >
            <div class="seat-icon-row">
              <el-icon class="desk-icon"><Monitor /></el-icon>
            </div>
            <div class="seat-number">{{ seat.seatNo }}</div>
            <div class="seat-status-badge">
              {{ seatStatusLabel(seat.status) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表模式 -->
    <el-card v-else shadow="never" class="table-card">
      <el-table :data="seats" v-loading="seatLoading" border stripe>
        <el-table-column prop="roomName" label="自习室" min-width="150" />
        <el-table-column prop="seatNo" label="座位号" width="130" align="center" />
        <el-table-column prop="floor" label="楼层" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="seatStatusType(row.status)">{{ seatStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :disabled="row.status !== 1"
              @click="openReserveDialog(row)"
            >
              预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 我的座位预约记录 -->
    <el-card shadow="never" class="records-card">
      <template #header>
        <div class="records-header">
          <div class="records-title">
            <el-icon><Calendar /></el-icon>
            <span>我的座位预约记录</span>
          </div>
          <el-button link type="primary" size="small" @click="fetchReservations">
            刷新记录
          </el-button>
        </div>
      </template>

      <el-table :data="reservations" v-loading="reservationLoading" border stripe>
        <el-table-column prop="roomName" label="自习室" min-width="140" />
        <el-table-column prop="seatNo" label="座位号" width="110" align="center" />
        <el-table-column prop="reserveDate" label="预约日期" width="130" align="center" />
        <el-table-column label="预约时间段" width="170" align="center">
          <template #default="{ row }">
            <span class="time-range">{{ row.startTime }} - {{ row.endTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="签到时间" width="170" align="center">
          <template #default="{ row }">
            <span v-if="row.checkInTime" class="text-success">{{ row.checkInTime }}</span>
            <span v-else class="text-muted">未签到</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="reservationStatusType(row.status)">
              {{ reservationStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="快捷操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="primary"
              size="small"
              @click="handleCheckIn(row)"
            >
              签到
            </el-button>
            <el-button
              v-if="row.status === 1 || row.status === 2"
              type="danger"
              size="small"
              @click="handleRelease(row)"
            >
              释放
            </el-button>
            <span v-if="row.status !== 1 && row.status !== 2" class="text-muted">-</span>
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

    <!-- 预约确认弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="确认预约自习室座位"
      width="460px"
      append-to-body
      destroy-on-close
    >
      <div v-if="currentSeat" class="reserve-dialog-content">
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="自习室">{{ currentSeat.roomName }}</el-descriptions-item>
          <el-descriptions-item label="座位编号">{{ currentSeat.seatNo }}</el-descriptions-item>
          <el-descriptions-item label="所在楼层">{{ currentSeat.floor || '1楼' }}</el-descriptions-item>
          <el-descriptions-item label="配套设施">标配独立充电插座、护眼台灯</el-descriptions-item>
        </el-descriptions>

        <div class="dialog-form-section">
          <el-form label-width="80px" size="default">
            <el-form-item label="预约日期">
              <el-date-picker
                v-model="reserveForm.date"
                type="date"
                value-format="YYYY-MM-DD"
                :disabled-date="disabledDate"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="开始时间">
              <el-time-select
                v-model="reserveForm.startTime"
                start="08:00"
                step="00:30"
                end="21:30"
                style="width: 100%;"
              />
            </el-form-item>
            <el-form-item label="结束时间">
              <el-time-select
                v-model="reserveForm.endTime"
                start="08:30"
                step="00:30"
                end="22:00"
                style="width: 100%;"
              />
            </el-form-item>
          </el-form>
        </div>

        <el-alert
          type="info"
          show-icon
          :closable="false"
          title="入座须知"
          description="请在预约开始时间后 15 分钟内使用读者账号进行线上签到，超时未签到座位将自动释放。"
        />
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="reserving" @click="confirmReserve">
            确认预约
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { OfficeBuilding, Search, Grid, Menu, Calendar, Monitor } from '@element-plus/icons-vue'
import { checkInSeat, getAvailableSeats, mySeatReservations, releaseSeat, reserveSeat } from '../../api/modules/seat'

const today = new Date().toISOString().slice(0, 10)
const roomName = ref('')
const viewMode = ref('visual')
const seatLoading = ref(false)
const reservationLoading = ref(false)
const reserving = ref(false)
const seats = ref([])
const reservations = ref([])
const dialogVisible = ref(false)
const currentSeat = ref(null)

const reserveForm = reactive({
  date: today,
  startTime: '08:00',
  endTime: '12:00'
})

const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁止选择过去日期
}

const seatStatusLabel = (status) => ({ 1: '空闲', 2: '占用', 3: '维护' }[status] || '未知')
const seatStatusType = (status) => ({ 1: 'success', 2: 'warning', 3: 'info' }[status] || 'info')
const reservationStatusLabel = (status) => ({ 1: '预约中', 2: '已签到', 3: '已释放', 4: '未签到' }[status] || '未知')
const reservationStatusType = (status) => ({ 1: 'warning', 2: 'success', 3: 'info', 4: 'danger' }[status] || 'info')

// 自动根据自习室名称聚类分组
const groupedSeats = computed(() => {
  const map = {}
  seats.value.forEach(s => {
    const key = s.roomName || '公共自习区'
    if (!map[key]) {
      map[key] = {
        name: key,
        floor: s.floor || '1F',
        seats: [],
        availableCount: 0,
        occupiedCount: 0
      }
    }
    map[key].seats.push(s)
    if (s.status === 1) map[key].availableCount++
    if (s.status === 2) map[key].occupiedCount++
  })
  return Object.values(map)
})

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

const openReserveDialog = (seat) => {
  currentSeat.value = seat
  dialogVisible.value = true
}

const confirmReserve = async () => {
  if (!currentSeat.value) return
  reserving.value = true
  try {
    await reserveSeat({
      seatId: currentSeat.value.id,
      date: reserveForm.date,
      startTime: reserveForm.startTime,
      endTime: reserveForm.endTime
    })
    ElMessage.success(`成功预约 ${currentSeat.value.roomName} ${currentSeat.value.seatNo}`)
    dialogVisible.value = false
    fetchSeats()
    fetchReservations()
  } catch {
    // handled
  } finally {
    reserving.value = false
  }
}

const handleCheckIn = async (row) => {
  try {
    await checkInSeat(row.id)
    ElMessage.success('签到成功，祝您学习愉快！')
    fetchReservations()
  } catch {
    // handled
  }
}

const handleRelease = (row) => {
  ElMessageBox.confirm('确定释放此座位预约？释放后他人可重新预约。', '释放座位', {
    type: 'warning',
    confirmButtonText: '确认释放',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await releaseSeat(row.id)
      ElMessage.success('座位已释放')
      fetchSeats()
      fetchReservations()
    } catch {
      // handled
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchSeats()
  fetchReservations()
})
</script>

<style scoped>
.seat-page {
  width: 100%;
}

.filter-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.filter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.filter-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.legend-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 10px;
  padding-top: 12px;
  border-top: 1px dashed #e4e7ed;
  font-size: 13px;
  flex-wrap: wrap;
}

.legend-label {
  color: #909399;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.available { background: #67C23A; }
.dot.occupied { background: #E6A23C; }
.dot.maintenance { background: #909399; }

.legend-tips {
  margin-left: auto;
  color: #409EFF;
  font-size: 12px;
}

/* 可视化选座网格 */
.visual-seats-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 20px;
}

.room-block {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #ebeef5;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
}

.room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f6fc;
}

.room-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.room-badge {
  background: #409EFF;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 10px;
}

.room-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.room-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #606266;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(110px, 1fr));
  gap: 14px;
}

.seat-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 14px 8px;
  border-radius: 10px;
  border: 1.5px solid #dcdfe6;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.22s ease;
  user-select: none;
}

.seat-box.is-available {
  background: #f0f9eb;
  border-color: #b3e19d;
  color: #67c23a;
}

.seat-box.is-available:hover {
  transform: translateY(-4px) scale(1.03);
  box-shadow: 0 8px 16px rgba(103, 194, 58, 0.2);
  border-color: #67c23a;
}

.seat-box.is-occupied {
  background: #fdf6ec;
  border-color: #f5dab1;
  color: #e6a23c;
  cursor: not-allowed;
  opacity: 0.85;
}

.seat-box.is-maintenance {
  background: #f4f4f5;
  border-color: #e9e9eb;
  color: #909399;
  cursor: not-allowed;
  opacity: 0.7;
}

.desk-icon {
  font-size: 26px;
  margin-bottom: 6px;
}

.seat-number {
  font-size: 14px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.seat-status-badge {
  font-size: 11px;
}

/* 列表模式 */
.table-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

/* 记录区 */
.records-card {
  border-radius: 12px;
}

.records-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.records-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.time-range {
  font-weight: 500;
  color: #409EFF;
}

.dialog-form-section {
  margin: 16px 0;
}

.text-success { color: #67c23a; }
.text-warning { color: #e6a23c; }
.text-muted { color: #c0c4cc; }

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

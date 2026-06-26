<template>
  <div class="fine-container">
    <!-- 财务概览卡片 -->
    <el-row :gutter="20" class="summary-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-inner">
            <div class="card-info">
              <div class="label">罚款总额</div>
              <div class="value">{{ summary.totalAmount?.toFixed(2) || '0.00' }}</div>
            </div>
            <el-icon size="40" color="#409EFF"><Money /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-inner">
            <div class="card-info">
              <div class="label">已缴金额</div>
              <div class="value paid">{{ summary.paidAmount?.toFixed(2) || '0.00' }}</div>
            </div>
            <el-icon size="40" color="#67C23A"><CircleCheck /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-inner">
            <div class="card-info">
              <div class="label">未缴金额</div>
              <div class="value unpaid">{{ summary.unpaidAmount?.toFixed(2) || '0.00' }}</div>
            </div>
            <el-icon size="40" color="#F56C6C"><WarningFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="summary-card">
          <div class="card-inner">
            <div class="card-info">
              <div class="label">罚款笔数</div>
              <div class="value">{{ summary.totalCount || 0 }}</div>
            </div>
            <el-icon size="40" color="#E6A23C"><DataLine /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="读者ID">
          <el-input v-model="filters.readerId" placeholder="请输入读者ID" clearable @clear="fetchData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable @clear="fetchData">
            <el-option label="未缴" :value="0" />
            <el-option label="已缴" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="filters.type" placeholder="全部" clearable @clear="fetchData">
            <el-option label="逾期" :value="1" />
            <el-option label="损毁" :value="2" />
            <el-option label="丢失" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 罚款列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="readerId" label="读者ID" width="100" />
        <el-table-column prop="readerName" label="读者姓名" width="120" />
        <el-table-column prop="bookName" label="图书名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="amount" label="罚款金额" width="110">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold;">{{ row.amount?.toFixed(2) }} 元</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="typeTagMap[row.type]?.type">{{ typeTagMap[row.type]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '已缴' : '未缴' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="payTime" label="缴费时间" width="170">
          <template #default="{ row }">{{ row.payTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              type="success"
              size="small"
              @click="handlePay(row)"
            >
              缴费
            </el-button>
            <span v-else style="color: #909399;">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <!-- 缴费弹窗 -->
    <el-dialog v-model="payDialogVisible" title="罚款缴费" width="420px">
      <div class="pay-info">
        <p><strong>读者：</strong>{{ currentFine.readerName }}</p>
        <p><strong>图书：</strong>{{ currentFine.bookName }}</p>
        <p><strong>金额：</strong><span style="color: #F56C6C; font-weight: bold;">{{ currentFine.amount?.toFixed(2) }} 元</span></p>
      </div>
      <el-form :model="payForm" label-width="80px">
        <el-form-item label="缴费方式" required>
          <el-radio-group v-model="payForm.payMethod">
            <el-radio label="cash">现金</el-radio>
            <el-radio label="wechat">微信</el-radio>
            <el-radio label="alipay">支付宝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="payLoading" @click="confirmPay">确认缴费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFines, payFine, getFinancialSummary } from '../../../api/modules/fine'

const loading = ref(false)
const payLoading = ref(false)
const payDialogVisible = ref(false)
const tableData = ref([])
const summary = ref({})
const currentFine = ref({})

const filters = reactive({
  readerId: '',
  status: '',
  type: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const payForm = reactive({
  payMethod: 'cash'
})

const typeTagMap = {
  1: { label: '逾期', type: 'warning' },
  2: { label: '损毁', type: 'danger' },
  3: { label: '丢失', type: 'info' }
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...filters
    }
    const res = await getFines(params)
    tableData.value = res.data.records || res.data.list || res.data
    pagination.total = res.data.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const fetchSummary = async () => {
  try {
    const res = await getFinancialSummary()
    summary.value = res.data || {}
  } catch {
    // handled by interceptor
  }
}

const resetFilters = () => {
  filters.readerId = ''
  filters.status = ''
  filters.type = ''
  pagination.page = 1
  fetchData()
}

const handlePay = (row) => {
  currentFine.value = { ...row }
  payForm.payMethod = 'cash'
  payDialogVisible.value = true
}

const confirmPay = async () => {
  payLoading.value = true
  try {
    await payFine(currentFine.value.id, { payMethod: payForm.payMethod })
    ElMessage.success('缴费成功')
    payDialogVisible.value = false
    fetchData()
    fetchSummary()
  } catch {
    // handled by interceptor
  } finally {
    payLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchSummary()
})
</script>

<style scoped>
.fine-container {
  padding: 0;
}
.summary-cards {
  margin-bottom: 20px;
}
.summary-card .card-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.summary-card .label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.summary-card .value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.summary-card .value.paid {
  color: #67C23A;
}
.summary-card .value.unpaid {
  color: #F56C6C;
}
.filter-card {
  margin-bottom: 20px;
}
.table-card {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
.pay-info p {
  margin: 8px 0;
  font-size: 15px;
}
</style>

<template>
  <div class="log-container">
    <el-card>
      <template #header><span>操作日志</span></template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="操作人">
          <el-input v-model="filters.operator" placeholder="请输入操作人" clearable @clear="fetchData" />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-input v-model="filters.operation" placeholder="请输入操作类型" clearable @clear="fetchData" />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志表格 -->
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="operator" label="操作人" width="120" />
        <el-table-column prop="operation" label="操作类型" width="150" />
        <el-table-column prop="method" label="请求方法" width="100" />
        <el-table-column prop="url" label="请求地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLogs } from '../../../api/modules/system'

const loading = ref(false)
const tableData = ref([])

const filters = reactive({
  operator: '',
  operation: '',
  dateRange: null
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      operator: filters.operator || undefined,
      operation: filters.operation || undefined,
      startDate: filters.dateRange?.[0] || undefined,
      endDate: filters.dateRange?.[1] || undefined
    }
    const res = await getLogs(params)
    tableData.value = res.data.records || res.data.list || res.data
    pagination.total = res.data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.operator = ''
  filters.operation = ''
  filters.dateRange = null
  pagination.page = 1
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.log-container {
  padding: 0;
}
.filter-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

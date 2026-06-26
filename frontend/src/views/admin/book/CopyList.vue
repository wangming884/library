<template>
  <div class="copy-list">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="图书ID">
          <el-input v-model="searchForm.bookId" placeholder="图书ID" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable>
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never" style="margin-top: 16px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAdd">新增副本</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="bookId" label="图书ID" width="80" />
        <el-table-column prop="bookTitle" label="书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="barcode" label="条形码" width="140" />
        <el-table-column prop="location" label="存放位置" width="120" />
        <el-table-column prop="floor" label="楼层" width="70" align="center" />
        <el-table-column prop="shelf" label="书架" width="80" />
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(cmd) => handleChangeStatus(row, cmd)">
              <el-button link type="primary" size="small">变更状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="s in statusOptions" :key="s.value" :command="s.value"
                    :disabled="row.status === s.value">{{ s.label }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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
        @size-change="fetchCopies"
        @current-change="fetchCopies"
      />
    </el-card>

    <!-- 新增副本弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增副本" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="图书ID" prop="bookId">
          <el-input v-model.number="form.bookId" placeholder="请输入图书ID" />
        </el-form-item>
        <el-form-item label="条形码" prop="barcode">
          <el-input v-model="form.barcode" placeholder="请输入条形码" />
        </el-form-item>
        <el-form-item label="存放位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入存放位置" />
        </el-form-item>
        <el-form-item label="楼层" prop="floor">
          <el-input v-model.number="form.floor" placeholder="楼层" />
        </el-form-item>
        <el-form-item label="书架" prop="shelf">
          <el-input v-model="form.shelf" placeholder="书架编号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCopies, addCopy, updateCopyStatus } from '../../../api/modules/book'

// ---------- 状态选项 ----------
const statusOptions = [
  { value: 1, label: '在馆' },
  { value: 2, label: '借出' },
  { value: 3, label: '丢失' },
  { value: 4, label: '维修' },
  { value: 5, label: '封存' }
]
const statusLabel = (val) => statusOptions.find(s => s.value === val)?.label || '未知'
const statusTagType = (val) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info', 5: '' }
  return map[val] || 'info'
}

// ---------- 搜索 ----------
const searchForm = reactive({ bookId: '', status: '' })
const handleSearch = () => { pagination.page = 1; fetchCopies() }
const resetSearch = () => {
  searchForm.bookId = ''
  searchForm.status = ''
  handleSearch()
}

// ---------- 表格 & 分页 ----------
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchCopies = async () => {
  loading.value = true
  try {
    const res = await getCopies({
      page: pagination.page,
      pageSize: pagination.pageSize,
      bookId: searchForm.bookId || undefined,
      status: searchForm.status || undefined
    })
    tableData.value = res.data.records || res.data.list || res.data
    pagination.total = res.data.total || 0
  } catch {
  } finally {
    loading.value = false
  }
}

// ---------- 状态变更 ----------
const handleChangeStatus = (row, newStatus) => {
  const label = statusLabel(newStatus)
  ElMessageBox.confirm(`将副本 [${row.barcode}] 状态变更为「${label}」？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await updateCopyStatus(row.id, newStatus)
      ElMessage.success('状态变更成功')
      fetchCopies()
    } catch {}
  }).catch(() => {})
}

// ---------- 新增副本弹窗 ----------
const dialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const defaultForm = () => ({ bookId: '', barcode: '', location: '', floor: '', shelf: '' })
const form = reactive(defaultForm())

const rules = {
  bookId: [{ required: true, message: '请输入图书ID', trigger: 'blur' }],
  barcode: [{ required: true, message: '请输入条形码', trigger: 'blur' }]
}

const handleAdd = () => {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    await addCopy({ ...form })
    ElMessage.success('新增成功')
    dialogVisible.value = false
    fetchCopies()
  } catch {
  } finally {
    submitLoading.value = false
  }
}

// ---------- 初始化 ----------
onMounted(() => {
  fetchCopies()
})
</script>

<style scoped>
.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>

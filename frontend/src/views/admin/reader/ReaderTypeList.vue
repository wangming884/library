<template>
  <div class="reader-type-list">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增类型</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="typeName" label="类型名称" min-width="130" />
        <el-table-column prop="typeKey" label="类型标识" min-width="130" />
        <el-table-column prop="maxBorrow" label="最大借阅" width="110" align="center" />
        <el-table-column prop="maxDays" label="借阅天数" width="110" align="center" />
        <el-table-column prop="renewTimes" label="续借次数" width="110" align="center" />
        <el-table-column prop="canBorrowRare" label="珍稀图书" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.canBorrowRare === 1 ? 'success' : 'info'">
              {{ row.canBorrowRare === 1 ? '允许' : '不允许' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑读者类型' : '新增读者类型'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="类型名称" prop="typeName">
          <el-input v-model="form.typeName" />
        </el-form-item>
        <el-form-item label="类型标识" prop="typeKey">
          <el-input v-model="form.typeKey" placeholder="如 student、teacher" />
        </el-form-item>
        <el-form-item label="最大借阅" prop="maxBorrow">
          <el-input-number v-model="form.maxBorrow" :min="1" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="借阅天数" prop="maxDays">
          <el-input-number v-model="form.maxDays" :min="1" :max="365" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续借次数" prop="renewTimes">
          <el-input-number v-model="form.renewTimes" :min="0" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="珍稀图书">
          <el-switch v-model="form.canBorrowRare" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { addReaderType, getAdminReaderTypes, updateReaderType } from '../../../api/modules/reader'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const editingId = ref(null)

const defaultForm = () => ({
  typeName: '',
  typeKey: '',
  maxBorrow: 5,
  maxDays: 30,
  renewTimes: 0,
  canBorrowRare: 0,
  status: 1,
  description: ''
})
const form = reactive(defaultForm())
const rules = {
  typeName: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  typeKey: [{ required: true, message: '请输入类型标识', trigger: 'blur' }],
  maxBorrow: [{ required: true, message: '请输入最大借阅数量', trigger: 'change' }],
  maxDays: [{ required: true, message: '请输入借阅天数', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAdminReaderTypes()
    tableData.value = res.data || []
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(form, defaultForm())
  editingId.value = null
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, {
    typeName: row.typeName || '',
    typeKey: row.typeKey || '',
    maxBorrow: row.maxBorrow || 5,
    maxDays: row.maxDays || 30,
    renewTimes: row.renewTimes ?? 0,
    canBorrowRare: row.canBorrowRare ?? 0,
    status: row.status ?? 1,
    description: row.description || ''
  })
  editingId.value = row.id
  isEdit.value = true
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateReaderType(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await addReaderType({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
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
.toolbar {
  margin-bottom: 16px;
}
</style>

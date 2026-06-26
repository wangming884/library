<template>
  <div>
    <!-- 搜索栏 -->
    <el-card style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="姓名/证号/手机号" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="全部" style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="挂失" :value="2" />
            <el-option label="冻结" :value="3" />
            <el-option label="黑名单" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.typeId" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="t in readerTypes" :key="t.id" :label="t.typeName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button type="success" @click="showAdd">新增读者</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column prop="cardNo" label="证号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="typeName" label="类型" width="80" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="dept" label="院系" min-width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="80">
          <template #default="{ row }">{{ row.balance ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" @click="handleLoss(row)">挂失</el-button>
            <el-button v-if="row.status === 2" size="small" type="success" @click="handleReissue(row)">补办</el-button>
            <el-button v-if="row.status === 1" size="small" type="danger" @click="handleFreeze(row)">冻结</el-button>
            <el-button v-if="row.status === 3" size="small" type="success" @click="handleUnfreeze(row)">解冻</el-button>
            <el-button v-if="row.status !== 4" size="small" type="danger" plain @click="handleBlacklist(row)">拉黑</el-button>
            <el-button v-if="row.status === 4" size="small" type="success" plain @click="handleRemoveBlacklist(row)">移出</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="formTitle" v-model="dialogVisible" width="600px" destroy-on-close>
      <el-form :model="form" label-width="90px" :rules="rules" ref="formRef">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="证号" prop="cardNo">
              <el-input v-model="form.cardNo" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="!isEdit">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类型" prop="typeId">
              <el-select v-model="form.typeId" style="width: 100%">
                <el-option v-for="t in readerTypes" :key="t.id" :label="t.typeName" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="院系">
              <el-input v-model="form.dept" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="证件号">
              <el-input v-model="form.idCard" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReaders, addReader, updateReader, reportLoss, reissue, freezeReader, unfreezeReader, addBlacklist, removeBlacklist, getReaderTypes } from '../../../api/modules/reader'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const total = ref(0)
const readerTypes = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formTitle = ref('新增读者')
const formRef = ref(null)

const query = reactive({ page: 1, size: 10, keyword: '', status: null, typeId: null })
const form = reactive({ id: null, cardNo: '', name: '', password: '', typeId: null, gender: 1, phone: '', email: '', dept: '', idCard: '', remark: '' })

const rules = {
  cardNo: [{ required: true, message: '请输入证号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择类型', trigger: 'change' }],
  phone: [{ pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

const statusText = (s) => ({ 1: '正常', 2: '挂失', 3: '冻结', 4: '黑名单' }[s] || '未知')
const statusType = (s) => ({ 1: 'success', 2: 'warning', 3: 'info', 4: 'danger' }[s] || 'info')

const loadData = async () => {
  loading.value = true
  try {
    const res = await getReaders(query)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

const loadTypes = async () => {
  const res = await getReaderTypes()
  readerTypes.value = res.data || []
}

const resetForm = () => {
  Object.assign(form, { id: null, cardNo: '', name: '', password: '', typeId: null, gender: 1, phone: '', email: '', dept: '', idCard: '', remark: '' })
}

const showAdd = () => {
  resetForm()
  isEdit.value = false
  formTitle.value = '新增读者'
  dialogVisible.value = true
}

const showEdit = (row) => {
  Object.assign(form, { ...row, password: '' })
  isEdit.value = true
  formTitle.value = '编辑读者'
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateReader(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await addReader(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally { submitting.value = false }
}

const handleLoss = (row) => {
  ElMessageBox.confirm(`确定将读者 ${row.name} 挂失？`, '提示', { type: 'warning' }).then(async () => {
    await reportLoss(row.id)
    ElMessage.success('挂失成功')
    loadData()
  }).catch(() => {})
}

const handleReissue = (row) => {
  ElMessageBox.prompt('请输入新的读者证号', '补办', { inputValue: 'R' + Date.now().toString().slice(-8) }).then(async ({ value }) => {
    await reissue(row.id, { newCardNo: value })
    ElMessage.success('补办成功')
    loadData()
  }).catch(() => {})
}

const handleFreeze = (row) => {
  ElMessageBox.confirm(`确定冻结读者 ${row.name}？`, '提示', { type: 'warning' }).then(async () => {
    await freezeReader(row.id)
    ElMessage.success('冻结成功')
    loadData()
  }).catch(() => {})
}

const handleUnfreeze = async (row) => {
  await unfreezeReader(row.id)
  ElMessage.success('解冻成功')
  loadData()
}

const handleBlacklist = (row) => {
  ElMessageBox.confirm(`确定将读者 ${row.name} 加入黑名单？`, '提示', { type: 'warning' }).then(async () => {
    await addBlacklist(row.id)
    ElMessage.success('已加入黑名单')
    loadData()
  }).catch(() => {})
}

const handleRemoveBlacklist = async (row) => {
  await removeBlacklist(row.id)
  ElMessage.success('已移出黑名单')
  loadData()
}

onMounted(() => {
  loadData()
  loadTypes()
})
</script>

<template>
  <div class="category-list">
    <el-card shadow="never">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAdd(null)">新增顶级分类</el-button>
      </div>

      <el-table :data="treeData" v-loading="loading" border row-key="id"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }" default-expand-all>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAdd(row)">添加子分类</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-input :model-value="parentName" disabled placeholder="无（顶级分类）" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategories, addCategory, updateCategory, deleteCategory } from '../../../api/modules/book'

// ---------- 树形数据 ----------
const loading = ref(false)
const flatData = ref([])
const treeData = ref([])

const buildTree = (list) => {
  const map = {}
  const roots = []
  list.forEach(item => { map[item.id] = { ...item, children: [] } })
  list.forEach(item => {
    if (item.parentId && map[item.parentId]) {
      map[item.parentId].children.push(map[item.id])
    } else {
      roots.push(map[item.id])
    }
  })
  // remove empty children arrays
  const clean = (nodes) => nodes.forEach(n => {
    if (n.children.length === 0) delete n.children
    else clean(n.children)
  })
  clean(roots)
  return roots
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getCategories()
    flatData.value = res.data || []
    treeData.value = buildTree(flatData.value)
  } catch {
  } finally {
    loading.value = false
  }
}

// ---------- 弹窗表单 ----------
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const editingId = ref(null)
const parentId = ref(null)

const parentName = computed(() => {
  if (!parentId.value) return ''
  const p = flatData.value.find(c => c.id === parentId.value)
  return p ? p.name : ''
})

const defaultForm = () => ({ name: '', sort: 0, description: '' })
const form = reactive(defaultForm())

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const handleAdd = (parent) => {
  Object.assign(form, defaultForm())
  isEdit.value = false
  editingId.value = null
  parentId.value = parent ? parent.id : null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, { name: row.name, sort: row.sort ?? 0, description: row.description || '' })
  isEdit.value = true
  editingId.value = row.id
  parentId.value = row.parentId || null
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const payload = { ...form, parentId: parentId.value || null }
    if (isEdit.value) {
      await updateCategory(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await addCategory(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchCategories()
  } catch {
  } finally {
    submitLoading.value = false
  }
}

// ---------- 删除 ----------
const handleDelete = (row) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该分类下存在子分类，请先删除子分类')
    return
  }
  ElMessageBox.confirm(`确定删除分类「${row.name}」？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteCategory(row.id)
      ElMessage.success('删除成功')
      fetchCategories()
    } catch {}
  }).catch(() => {})
}

// ---------- 初始化 ----------
onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
</style>

<template>
  <div class="book-list">
    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="书名/作者/ISBN" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable>
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="searchForm.isbn" placeholder="ISBN" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 + 表格 -->
    <el-card shadow="never" style="margin-top: 16px">
      <div style="margin-bottom: 16px">
        <el-button type="primary" @click="handleAdd">新增图书</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="title" label="书名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
        <el-table-column prop="pubDate" label="出版日期" width="110" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="price" label="价格" width="80" />
        <el-table-column prop="isRare" label="珍稀" width="70" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRare ? 'danger' : 'info'" size="small">{{ row.isRare ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
        @size-change="fetchBooks"
        @current-change="fetchBooks"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑图书' : '新增图书'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="出版日期" prop="pubDate">
          <el-date-picker v-model="form.pubDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="关键字" prop="keywords">
          <el-input v-model="form.keywords" placeholder="多个关键字用逗号分隔" />
        </el-form-item>
        <el-form-item label="珍稀图书">
          <el-switch v-model="form.isRare" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入简介" />
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
import { getBooks, addBook, updateBook, deleteBook, getCategories } from '../../../api/modules/book'

// ---------- 搜索 ----------
const searchForm = reactive({ keyword: '', categoryId: '', isbn: '' })
const handleSearch = () => { pagination.page = 1; fetchBooks() }
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = ''
  searchForm.isbn = ''
  handleSearch()
}

// ---------- 表格 & 分页 ----------
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const fetchBooks = async () => {
  loading.value = true
  try {
    const res = await getBooks({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      categoryId: searchForm.categoryId || undefined,
      isbn: searchForm.isbn || undefined
    })
    tableData.value = res.data.records || res.data.list || res.data
    pagination.total = res.data.total || 0
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

// ---------- 分类 ----------
const categories = ref([])
const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch {}
}

// ---------- 弹窗表单 ----------
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const editingId = ref(null)

const defaultForm = () => ({
  isbn: '', title: '', author: '', publisher: '', pubDate: '',
  categoryId: '', price: 0, description: '', keywords: '', isRare: 0
})
const form = reactive(defaultForm())

const rules = {
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const resetForm = () => {
  Object.assign(form, defaultForm())
  editingId.value = null
}

const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    isbn: row.isbn, title: row.title, author: row.author,
    publisher: row.publisher, pubDate: row.pubDate,
    categoryId: row.categoryId, price: row.price,
    description: row.description, keywords: row.keywords,
    isRare: row.isRare || 0
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateBook(editingId.value, { ...form })
      ElMessage.success('更新成功')
    } else {
      await addBook({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchBooks()
  } catch {
    // handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

// ---------- 删除 ----------
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除图书《${row.title}》？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteBook(row.id)
      ElMessage.success('删除成功')
      fetchBooks()
    } catch {}
  }).catch(() => {})
}

// ---------- 初始化 ----------
onMounted(() => {
  fetchCategories()
  fetchBooks()
})
</script>

<style scoped>
.search-card :deep(.el-form-item) {
  margin-bottom: 0;
}
</style>

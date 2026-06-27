<template>
  <div class="search-container">
    <!-- 高级搜索表单 -->
    <el-card class="search-form-card">
      <el-form :model="searchForm" :inline="true" label-width="70px">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="书名/ISBN/关键词"
            clearable
            style="width: 220px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 160px;">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="searchForm.author" placeholder="作者" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="searchForm.publisher" placeholder="出版社" clearable style="width: 180px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 搜索结果 -->
    <div class="result-header" v-if="searched">
      <span>共找到 <strong>{{ total }}</strong> 本图书</span>
    </div>

    <div class="book-grid" v-loading="loading">
      <el-empty v-if="searched && bookList.length === 0" description="暂无搜索结果" />
      <el-row :gutter="20">
        <el-col
          v-for="book in bookList"
          :key="book.id"
          :xs="24" :sm="12" :md="8" :lg="6"
          class="book-col"
        >
          <el-card class="book-card" shadow="hover" @click="goDetail(book.id)">
            <div class="book-cover">
              <el-image
                :src="book.cover"
                fit="cover"
                style="width: 100%; height: 200px; border-radius: 4px;"
              >
                <template #error>
                  <div class="cover-placeholder">
                    <el-icon size="48"><Picture /></el-icon>
                    <span>暂无封面</span>
                  </div>
                </template>
              </el-image>
            </div>
            <div class="book-info">
              <div class="book-title" :title="book.title">{{ book.title }}</div>
              <div class="book-author">{{ book.author }}</div>
              <div class="book-meta">
                <span class="category">{{ book.categoryName }}</span>
                <el-tag
                  :type="book.availableCount > 0 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ book.availableCount > 0 ? `可借 ${book.availableCount}` : '暂无可借' }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      class="pagination"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="total"
      :page-sizes="[12, 24, 48]"
      layout="total, sizes, prev, pager, next"
      @size-change="fetchBooks"
      @current-change="fetchBooks"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { searchBooks, getCategories } from '../../api/modules/book'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const searched = ref(false)
const bookList = ref([])
const categories = ref([])
const total = ref(0)

const searchForm = reactive({
  keyword: '',
  categoryId: '',
  author: '',
  publisher: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 12
})

const fetchBooks = async () => {
  loading.value = true
  searched.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      keyword: searchForm.keyword || undefined,
      categoryId: searchForm.categoryId || undefined,
      author: searchForm.author || undefined,
      publisher: searchForm.publisher || undefined
    }
    const res = await searchBooks(params)
    bookList.value = res.data.records || res.data.list || res.data
    total.value = res.data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data || []
  } catch {
    // handled
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchBooks()
}

const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.categoryId = ''
  searchForm.author = ''
  searchForm.publisher = ''
  pagination.page = 1
  searched.value = false
  bookList.value = []
  total.value = 0
}

const goDetail = (id) => {
  router.push(`/portal/book/${id}`)
}

onMounted(() => {
  fetchCategories()
  if (route.query.keyword) {
    searchForm.keyword = route.query.keyword
    handleSearch()
  }
})
</script>

<style scoped>
.search-container {
  max-width: 1200px;
  margin: 0 auto;
}
.search-form-card {
  margin-bottom: 20px;
}
.result-header {
  margin-bottom: 16px;
  font-size: 14px;
  color: #606266;
}
.book-col {
  margin-bottom: 20px;
}
.book-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.book-card:hover {
  transform: translateY(-4px);
}
.book-cover {
  margin-bottom: 12px;
}
.cover-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
  color: #c0c4cc;
  gap: 8px;
}
.book-info {
  padding: 0 4px;
}
.book-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.book-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.book-meta .category {
  font-size: 12px;
  color: #909399;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

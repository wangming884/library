<template>
  <div class="search-container">
    <!-- 高级搜索表单卡片 -->
    <el-card shadow="never" class="search-form-card">
      <div class="search-card-header">
        <div class="search-title">
          <el-icon><Search /></el-icon>
          <span>馆藏图书综合检索</span>
        </div>
      </div>

      <el-form :model="searchForm" :inline="true" class="search-form" @keyup.enter="handleSearch">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="书名 / ISBN / 简介关键词"
            clearable
            style="width: 240px;"
          />
        </el-form-item>
        <el-form-item label="图书分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 170px;">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="searchForm.author" placeholder="作者名" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="searchForm.publisher" placeholder="出版社名称" clearable style="width: 170px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">
            搜索图书
          </el-button>
          <el-button @click="resetSearch">重置条件</el-button>
        </el-form-item>
      </el-form>

      <!-- 快捷分类胶囊栏 -->
      <div class="quick-category-bar" v-if="categories.length">
        <span class="quick-label">热门分类：</span>
        <div class="category-tags">
          <span
            class="cat-chip"
            :class="{ active: !searchForm.categoryId }"
            @click="selectCategory('')"
          >
            全部
          </span>
          <span
            v-for="cat in categories.slice(0, 10)"
            :key="cat.id"
            class="cat-chip"
            :class="{ active: searchForm.categoryId === cat.id }"
            @click="selectCategory(cat.id)"
          >
            {{ cat.name }}
          </span>
        </div>
      </div>
    </el-card>

    <!-- 结果栏控制与统计 -->
    <div class="result-toolbar">
      <div class="result-count">
        <span v-if="searched">
          共检索到 <strong class="highlight-total">{{ total }}</strong> 本相关图书
        </span>
        <span v-else class="text-secondary">输入关键词或选择分类开始探索图书</span>
      </div>

      <div class="view-switch" v-if="bookList.length">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="grid">
            <el-icon><Menu /></el-icon> 网格
          </el-radio-button>
          <el-radio-button label="list">
            <el-icon><Fold /></el-icon> 列表
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 搜索结果：网格卡片模式 -->
    <div v-if="viewMode === 'grid'" class="book-grid" v-loading="loading">
      <el-empty v-if="searched && bookList.length === 0" description="未找到符合条件的图书，试试换个关键词吧" />
      <el-row :gutter="20">
        <el-col
          v-for="book in bookList"
          :key="book.id"
          :xs="24" :sm="12" :md="8" :lg="6"
          class="book-col"
        >
          <el-card class="book-card" shadow="hover" @click="goDetail(book.id)">
            <div class="book-cover-box">
              <el-image
                :src="book.cover"
                fit="cover"
                class="book-cover-img"
              >
                <template #error>
                  <div class="cover-placeholder">
                    <el-icon size="40"><Reading /></el-icon>
                    <span>暂无封面</span>
                  </div>
                </template>
              </el-image>
              <div class="cover-badge" v-if="book.isRare">
                <el-tag type="danger" size="small" effect="dark">珍本</el-tag>
              </div>
            </div>

            <div class="book-info">
              <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
              <p class="book-author" :title="book.author">{{ book.author || '未知作者' }}</p>
              
              <div class="book-meta-row">
                <el-tag size="small" type="info" effect="plain">{{ book.categoryName || '未分类' }}</el-tag>
                <span class="book-price" v-if="book.price">¥ {{ Number(book.price).toFixed(2) }}</span>
              </div>

              <div class="book-footer-row">
                <el-tag
                  :type="book.availableCount > 0 ? 'success' : 'danger'"
                  size="small"
                  effect="light"
                >
                  {{ book.availableCount > 0 ? `可借 ${book.availableCount} 本` : '暂无可借' }}
                </el-tag>
                <el-button link type="primary" size="small">详情 →</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索结果：详细表格列表模式 -->
    <div v-else class="book-table-container" v-loading="loading">
      <el-card shadow="never">
        <el-table :data="bookList" stripe border @row-click="(row) => goDetail(row.id)" style="cursor: pointer;">
          <el-table-column prop="cover" label="封面" width="80" align="center">
            <template #default="{ row }">
              <el-image :src="row.cover" style="width: 44px; height: 60px; border-radius: 4px;" fit="cover">
                <template #error>
                  <div class="mini-cover-placeholder"><el-icon><Reading /></el-icon></div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="书名" min-width="180" show-overflow-tooltip />
          <el-table-column prop="author" label="作者" width="130" show-overflow-tooltip />
          <el-table-column prop="isbn" label="ISBN" width="140" />
          <el-table-column prop="publisher" label="出版社" width="150" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="availableCount" label="可借复本" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.availableCount > 0 ? 'success' : 'danger'" size="small">
                {{ row.availableCount > 0 ? `可借 ${row.availableCount}` : '无' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click.stop="goDetail(row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      class="pagination"
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="total"
      :page-sizes="[12, 24, 48]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchBooks"
      @current-change="fetchBooks"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Reading, Menu, Fold } from '@element-plus/icons-vue'
import { searchBooks, getCategories } from '../../api/modules/book'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const searched = ref(false)
const viewMode = ref('grid')
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
    bookList.value = res.data.records || res.data.list || res.data || []
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

const selectCategory = (catId) => {
  searchForm.categoryId = catId
  handleSearch()
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
  } else {
    // 首次进入默认加载一次热门推荐
    fetchBooks()
  }
})
</script>

<style scoped>
.search-container {
  width: 100%;
}

.search-form-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.search-card-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f5;
}

.search-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.quick-category-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
  padding-top: 14px;
  border-top: 1px dashed #e4e7ed;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 13px;
  color: #909399;
}

.category-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.cat-chip {
  padding: 3px 10px;
  font-size: 12px;
  border-radius: 14px;
  background: #f4f4f5;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.cat-chip:hover {
  background: #ecf5ff;
  color: #409EFF;
}

.cat-chip.active {
  background: #409EFF;
  color: #fff;
}

.result-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.result-count {
  font-size: 14px;
  color: #606266;
}

.highlight-total {
  color: #409EFF;
  font-size: 16px;
  margin: 0 4px;
}

.text-secondary {
  color: #909399;
}

.book-col {
  margin-bottom: 20px;
}

.book-card {
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.08);
}

.book-cover-box {
  position: relative;
  width: 100%;
  height: 220px;
  background: #f5f7fa;
  overflow: hidden;
  border-radius: 6px;
  margin-bottom: 12px;
}

.book-cover-img {
  width: 100%;
  height: 100%;
  transition: transform 0.3s ease;
}

.book-card:hover .book-cover-img {
  transform: scale(1.04);
}

.cover-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  font-size: 12px;
}

.book-info {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.book-title {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  margin: 0 0 10px;
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.book-price {
  font-size: 13px;
  font-weight: 600;
  color: #f56c6c;
}

.book-footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px solid #f2f6fc;
}

.mini-cover-placeholder {
  width: 100%;
  height: 100%;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>

<template>
  <div class="search-container">
    <!-- 高级搜索表单卡片 -->
    <el-card shadow="never" class="search-form-card">
      <div class="search-card-header">
        <div class="search-title">
          <div class="title-icon"><el-icon><Search /></el-icon></div>
          <span>馆藏文献综合检索中心</span>
        </div>
        <div class="search-sub-hint">支持书名、著者、标准 ISBN、分类与出版社多条件组合检索</div>
      </div>

      <el-form :model="searchForm" :inline="true" class="search-form" @keyup.enter="handleSearch">
        <el-form-item label="检索词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="书名 / ISBN / 简介关键词"
            clearable
            style="width: 250px;"
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
        <el-form-item label="著作作者">
          <el-input v-model="searchForm.author" placeholder="作者名 / 编者" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item label="出版机构">
          <el-input v-model="searchForm.publisher" placeholder="出版社名称" clearable style="width: 170px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" :loading="loading" @click="handleSearch">
            搜索馆藏
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
            v-for="cat in categories.slice(0, 12)"
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
          共检索到 <strong class="highlight-total">{{ total }}</strong> 本相关馆藏图书
        </span>
        <span v-else class="text-secondary">输入关键词或选择分类开始探索图书</span>
      </div>

      <div class="view-switch" v-if="bookList.length">
        <el-radio-group v-model="viewMode" size="small">
          <el-radio-button label="grid">
            <el-icon><Menu /></el-icon> 卡片画廊
          </el-radio-button>
          <el-radio-button label="list">
            <el-icon><Fold /></el-icon> 紧凑列表
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 搜索结果：网格卡片模式 -->
    <div v-if="viewMode === 'grid'" class="book-grid" v-loading="loading">
      <el-empty v-if="searched && bookList.length === 0" description="未找到符合条件的馆藏图书，建议调整检索词重试" />
      <el-row :gutter="20">
        <el-col
          v-for="book in bookList"
          :key="book.id"
          :xs="24" :sm="12" :md="8" :lg="6"
          class="book-col"
        >
          <div class="book-card" @click="goDetail(book.id)">
            <div class="book-cover-box">
              <div class="book-spine-effect"></div>
              <el-image
                :src="book.cover"
                fit="cover"
                class="book-cover-img"
                lazy
              >
                <template #error>
                  <div class="cover-placeholder">
                    <el-icon size="40"><Reading /></el-icon>
                    <span>暂无封面</span>
                  </div>
                </template>
              </el-image>
              <div class="cover-badge" v-if="book.isRare">
                <el-tag type="danger" size="small" effect="dark">珍藏古籍</el-tag>
              </div>
              <div class="card-hover-mask">
                <span class="view-btn">查看详情 →</span>
              </div>
            </div>

            <div class="book-info">
              <h3 class="book-title" :title="book.title">{{ book.title }}</h3>
              <p class="book-author" :title="book.author">{{ book.author || '未知作者' }}</p>
              
              <div class="book-meta-row">
                <el-tag size="small" type="info" effect="plain">{{ book.categoryName || '文献' }}</el-tag>
                <span class="book-price" v-if="book.price">¥ {{ Number(book.price).toFixed(2) }}</span>
              </div>

              <div class="book-footer-row">
                <el-tag
                  :type="book.availableCount > 0 ? 'success' : 'danger'"
                  size="small"
                  effect="light"
                >
                  {{ book.availableCount > 0 ? `在馆可借 ${book.availableCount} 册` : '暂无空闲副本' }}
                </el-tag>
                <span class="detail-link">借阅预约 →</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索结果：详细表格列表模式 -->
    <div v-else class="book-table-container" v-loading="loading">
      <el-card shadow="never" class="table-card">
        <el-table :data="bookList" stripe border @row-click="(row) => goDetail(row.id)" style="cursor: pointer;">
          <el-table-column prop="cover" label="封面" width="80" align="center">
            <template #default="{ row }">
              <el-image :src="row.cover" style="width: 44px; height: 60px; border-radius: 4px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);" fit="cover">
                <template #error>
                  <div class="mini-cover-placeholder"><el-icon><Reading /></el-icon></div>
                </template>
              </el-image>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="图书题名" min-width="180" show-overflow-tooltip />
          <el-table-column prop="author" label="著者" width="130" show-overflow-tooltip />
          <el-table-column prop="isbn" label="标准 ISBN" width="140" />
          <el-table-column prop="publisher" label="出版机构" width="150" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="文献分类" width="120" />
          <el-table-column prop="availableCount" label="在馆副本" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="row.availableCount > 0 ? 'success' : 'danger'" size="small">
                {{ row.availableCount > 0 ? `可借 ${row.availableCount}` : '已借出' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button size="small" type="primary" link @click.stop="goDetail(row.id)">借阅详情</el-button>
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
  // 支持解析来自首页等外链的所有参数
  if (route.query.keyword) searchForm.keyword = route.query.keyword
  if (route.query.title) searchForm.keyword = route.query.title
  if (route.query.author) searchForm.author = route.query.author
  if (route.query.isbn) searchForm.keyword = route.query.isbn
  if (route.query.categoryId) searchForm.categoryId = Number(route.query.categoryId)

  fetchBooks()
})
</script>

<style scoped>
.search-container {
  width: 100%;
}

.search-form-card {
  border-radius: 14px;
  margin-bottom: 24px;
  border: 1px solid #e2e8f0;
}

.search-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f1f5f9;
  flex-wrap: wrap;
  gap: 10px;
}

.search-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.title-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #eff6ff;
  color: #2563eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.search-sub-hint {
  font-size: 12px;
  color: #64748b;
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
  border-top: 1px dashed #e2e8f0;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.category-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.cat-chip {
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 16px;
  background: #f1f5f9;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
  border: 1px solid transparent;
}

.cat-chip:hover {
  background: #e0f2fe;
  color: #0284c7;
}

.cat-chip.active {
  background: #2563eb;
  color: #fff;
  font-weight: 500;
}

.result-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.result-count {
  font-size: 14px;
  color: #475569;
}

.highlight-total {
  color: #2563eb;
  font-size: 18px;
  margin: 0 4px;
  font-family: 'DIN Alternate', sans-serif;
}

.text-secondary {
  color: #94a3b8;
}

.book-col {
  margin-bottom: 22px;
}

.book-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  height: 100%;
  display: flex;
  flex-direction: column;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

.book-cover-box {
  position: relative;
  width: 100%;
  height: 220px;
  background: #f8fafc;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.book-spine-effect {
  position: absolute;
  left: calc(50% - 64px);
  top: 18px;
  width: 4px;
  height: 184px;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.18), rgba(255, 255, 255, 0.2));
  z-index: 2;
  pointer-events: none;
}

.book-cover-img {
  width: 130px;
  height: 184px;
  border-radius: 6px;
  box-shadow: -3px 0 8px rgba(0, 0, 0, 0.08), 4px 6px 14px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.book-card:hover .book-cover-img {
  transform: scale(1.05);
}

.cover-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 3;
}

.card-hover-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.22s;
  z-index: 4;
}

.book-card:hover .card-hover-mask {
  opacity: 1;
}

.view-btn {
  background: #fff;
  color: #1e293b;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #94a3b8;
  background: #f1f5f9;
  font-size: 12px;
}

.book-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  padding: 14px 16px;
}

.book-title {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  margin: 0 0 10px;
  font-size: 12px;
  color: #64748b;
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
  color: #dc2626;
}

.book-footer-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
}

.detail-link {
  font-size: 12px;
  color: #2563eb;
  font-weight: 500;
}

.table-card {
  border-radius: 14px;
}

.mini-cover-placeholder {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}

.pagination {
  margin-top: 28px;
  display: flex;
  justify-content: center;
}
</style>

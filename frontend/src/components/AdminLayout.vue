<template>
  <el-container class="admin-wrapper">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '230px'" class="aside-container">
      <div class="logo" :class="{ collapsed: isCollapse }">
        <div class="logo-icon-box">
          <el-icon size="22" color="#fff"><Collection /></el-icon>
        </div>
        <span v-show="!isCollapse" class="title">图书馆管理后台</span>
      </div>
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="route.path"
          :collapse="isCollapse"
          router
          background-color="#1f2d3d"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :collapse-transition="false"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>
          <el-sub-menu v-if="canAccess(['super_admin', 'circulation', 'front_desk'])" index="reader-mgmt">
            <template #title><el-icon><User /></el-icon><span>读者管理</span></template>
            <el-menu-item index="/admin/readers">读者列表</el-menu-item>
            <el-menu-item v-if="canAccess(['super_admin'])" index="/admin/reader-types">读者类型</el-menu-item>
          </el-sub-menu>
          <el-sub-menu v-if="canAccess(['super_admin', 'cataloger', 'circulation', 'front_desk'])" index="book-mgmt">
            <template #title><el-icon><Reading /></el-icon><span>图书管理</span></template>
            <el-menu-item v-if="canAccess(['super_admin', 'cataloger'])" index="/admin/books">图书列表</el-menu-item>
            <el-menu-item v-if="canAccess(['super_admin', 'cataloger', 'circulation', 'front_desk'])" index="/admin/copies">副本管理</el-menu-item>
            <el-menu-item v-if="canAccess(['super_admin', 'cataloger'])" index="/admin/categories">分类管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu v-if="canAccess(['super_admin', 'circulation', 'front_desk'])" index="borrow-mgmt">
            <template #title><el-icon><Document /></el-icon><span>借阅管理</span></template>
            <el-menu-item index="/admin/borrow">借还办理</el-menu-item>
            <el-menu-item index="/admin/borrow-records">借阅记录</el-menu-item>
            <el-menu-item index="/admin/reservations">预约管理</el-menu-item>
          </el-sub-menu>
          <el-menu-item v-if="canAccess(['super_admin', 'front_desk'])" index="/admin/seat-reservations">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>座位预约</template>
          </el-menu-item>
          <el-menu-item v-if="canAccess(['super_admin', 'circulation', 'front_desk'])" index="/admin/fines">
            <el-icon><Money /></el-icon>
            <template #title>罚款管理</template>
          </el-menu-item>
          <el-menu-item v-if="canAccess(['super_admin', 'circulation'])" index="/admin/reports">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>统计报表</template>
          </el-menu-item>
          <el-sub-menu v-if="canAccess(['super_admin', 'circulation', 'front_desk'])" index="system-mgmt">
            <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
            <el-menu-item v-if="canAccess(['super_admin'])" index="/admin/config">系统设置</el-menu-item>
            <el-menu-item v-if="canAccess(['super_admin', 'circulation'])" index="/admin/announcements">公告管理</el-menu-item>
            <el-menu-item v-if="canAccess(['super_admin', 'circulation', 'front_desk'])" index="/admin/feedback">留言反馈</el-menu-item>
            <el-menu-item v-if="canAccess(['super_admin'])" index="/admin/logs">操作日志</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 右侧容器 -->
    <el-container class="right-container">
      <!-- 顶部 Header -->
      <el-header class="header">
        <div class="header-left">
          <div class="collapse-btn" @click="isCollapse = !isCollapse">
            <el-icon size="20">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </div>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.path !== '/admin/dashboard'">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          
          <!-- 全局指令面板 (Ctrl + K) -->
          <el-tooltip content="全局功能速寻与跳转 (快捷键 Ctrl + K)" placement="bottom">
            <el-button link class="tool-btn search-cmd-btn" @click="commandPaletteVisible = true">
              <el-icon size="17"><Search /></el-icon>
              <span class="tool-label">快速搜索</span>
              <kbd class="kbd-badge">Ctrl K</kbd>
            </el-button>
          </el-tooltip>
          <!-- 前往读者门户快捷入口 -->
          <el-tooltip content="前往读者门户首页" placement="bottom">
            <el-button link class="tool-btn" @click="goToPortal">
              <el-icon size="18"><Compass /></el-icon>
              <span class="tool-label">读者门户</span>
            </el-button>
          </el-tooltip>

          <!-- 全屏按钮 -->
          <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏显示'" placement="bottom">
            <el-button link class="tool-btn" @click="toggleFullscreen">
              <el-icon size="18">
                <component :is="isFullscreen ? 'Aim' : 'FullScreen'" />
              </el-icon>
            </el-button>
          </el-tooltip>

          <!-- 角色标签 -->
          <el-tag size="small" effect="plain" type="primary" class="role-tag">
            {{ userRoleName }}
          </el-tag>

          <!-- 用户头像与下拉菜单 -->
          <el-dropdown trigger="click">
            <div class="user-profile">
              <el-avatar :size="34" class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="admin-name">{{ userStore.userInfo.realName || userStore.userInfo.username || '管理员' }}</span>
              <el-icon class="arrow-down"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToPortal">
                  <el-icon><Reading /></el-icon>读者门户
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 多标签页导航 (TagsView) -->
      <div class="tags-view-container">
        <el-scrollbar class="tags-scrollbar">
          <div class="tags-inner">
            <div
              v-for="tag in visitedViews"
              :key="tag.path"
              class="tag-item"
              :class="{ active: route.path === tag.path }"
              @click="router.push(tag.path)"
            >
              <span class="tag-dot" v-if="route.path === tag.path"></span>
              <span class="tag-title">{{ tag.title }}</span>
              <el-icon
                v-if="tag.path !== '/admin/dashboard'"
                class="tag-close"
                @click.stop="closeView(tag)"
              >
                <Close />
              </el-icon>
            </div>
          </div>
        </el-scrollbar>

        <el-dropdown trigger="click" class="tags-actions">
          <el-button size="small" text>
            标签选项 <el-icon><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="closeOtherViews">关闭其他</el-dropdown-item>
              <el-dropdown-item @click="closeAllViews">关闭所有</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 全局快捷功能导航弹窗 Command Palette -->
    <el-dialog
      v-model="commandPaletteVisible"
      title="全局功能导航速寻"
      width="540px"
      append-to-body
      destroy-on-close
      class="cmd-palette-modal"
    >
      <div class="cmd-palette-body">
        <el-input
          v-model="cmdSearchQuery"
          placeholder="输入功能名称快速直达 (例如：图书、借还、读者、报表...)"
          :prefix-icon="Search"
          size="large"
          clearable
          autofocus
          class="cmd-search-input"
        />

        <div class="cmd-results-list">
          <div
            v-for="item in filteredMenuItems"
            :key="item.path"
            class="cmd-item"
            @click="handleNavigate(item.path)"
          >
            <div class="cmd-icon-box">
              <el-icon size="18"><component :is="item.icon" /></el-icon>
            </div>
            <div class="cmd-info">
              <span class="cmd-title">{{ item.title }}</span>
              <span class="cmd-path">{{ item.path }}</span>
            </div>
            <el-icon class="cmd-enter-icon"><Right /></el-icon>
          </div>
          <el-empty v-if="filteredMenuItems.length === 0" description="未找到匹配功能，按回车可快速在图书列表中搜索" :image-size="60" />
        </div>
      </div>
      <template #footer>
        <div class="cmd-footer-tips">
          <span>快捷键提示：按 <kbd>ESC</kbd> 退出，按 <kbd>Ctrl + K</kbd> 随时唤起</span>
        </div>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Search, Right, Compass, FullScreen, Aim, UserFilled, SwitchButton,
  Close, ArrowDown, Collection, Odometer, User, Reading, Document,
  OfficeBuilding, Money, DataAnalysis, Setting
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const isFullscreen = ref(false)

const roleMap = {
  super_admin: '超级管理员',
  cataloger: '图书采编员',
  circulation: '流通管理员',
  front_desk: '前台操作员'
}
const userRoleName = computed(() => roleMap[userStore.roleKey] || '系统管理')

const canAccess = (roles) => roles.includes(userStore.roleKey)

// TagsView 多标签页逻辑
const visitedViews = ref([
  { path: '/admin/dashboard', title: '仪表盘', name: 'Dashboard' }
])

watch(
  () => route.path,
  () => {
    if (!route.path.startsWith('/admin')) return
    const title = route.meta?.title
    if (!title) return
    const exists = visitedViews.value.some(v => v.path === route.path)
    if (!exists) {
      visitedViews.value.push({
        path: route.path,
        title,
        name: route.name
      })
    }
  },
  { immediate: true }
)

const closeView = (tag) => {
  if (tag.path === '/admin/dashboard') return
  const index = visitedViews.value.findIndex(v => v.path === tag.path)
  if (index > -1) {
    visitedViews.value.splice(index, 1)
    if (route.path === tag.path) {
      const nextView = visitedViews.value[index] || visitedViews.value[index - 1] || { path: '/admin/dashboard' }
      router.push(nextView.path)
    }
  }
}

const closeOtherViews = () => {
  visitedViews.value = visitedViews.value.filter(v => v.path === '/admin/dashboard' || v.path === route.path)
}

const closeAllViews = () => {
  visitedViews.value = [{ path: '/admin/dashboard', title: '仪表盘', name: 'Dashboard' }]
  router.push('/admin/dashboard')
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().then(() => {
      isFullscreen.value = true
    }).catch(() => {})
  } else {
    document.exitFullscreen().then(() => {
      isFullscreen.value = false
    }).catch(() => {})
  }
}

const goToPortal = () => {
  router.push('/portal/home')
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出当前管理员账号吗？', '提示', {
    confirmButtonText: '确定退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}

// 全局指令面板 (Ctrl + K)
const commandPaletteVisible = ref(false)
const cmdSearchQuery = ref('')

const allNavItems = [
  { title: '仪表盘 (总览)', path: '/admin/dashboard', icon: 'Odometer', roles: ['super_admin', 'cataloger', 'circulation', 'front_desk'] },
  { title: '读者管理 (读者列表)', path: '/admin/readers', icon: 'User', roles: ['super_admin', 'circulation', 'front_desk'] },
  { title: '读者类型设置', path: '/admin/reader-types', icon: 'User', roles: ['super_admin'] },
  { title: '图书管理 (图书列表)', path: '/admin/books', icon: 'Reading', roles: ['super_admin', 'cataloger'] },
  { title: '副本馆藏管理', path: '/admin/copies', icon: 'Reading', roles: ['super_admin', 'cataloger', 'circulation', 'front_desk'] },
  { title: '分类管理 (中图分类)', path: '/admin/categories', icon: 'Reading', roles: ['super_admin', 'cataloger'] },
  { title: '借还办理 (借书/还书)', path: '/admin/borrow', icon: 'Document', roles: ['super_admin', 'circulation', 'front_desk'] },
  { title: '借阅记录检索', path: '/admin/borrow-records', icon: 'Document', roles: ['super_admin', 'circulation', 'front_desk'] },
  { title: '预约管理', path: '/admin/reservations', icon: 'Document', roles: ['super_admin', 'circulation', 'front_desk'] },
  { title: '自习室与座位预约', path: '/admin/seat-reservations', icon: 'OfficeBuilding', roles: ['super_admin', 'front_desk'] },
  { title: '罚款管理与清缴', path: '/admin/fines', icon: 'Money', roles: ['super_admin', 'circulation', 'front_desk'] },
  { title: '业务统计与趋势报表', path: '/admin/reports', icon: 'DataAnalysis', roles: ['super_admin', 'circulation'] },
  { title: '系统参数设置', path: '/admin/config', icon: 'Setting', roles: ['super_admin'] },
  { title: '公告通知发布管理', path: '/admin/announcements', icon: 'Setting', roles: ['super_admin', 'circulation'] },
  { title: '读者留言与诉求反馈', path: '/admin/feedback', icon: 'Setting', roles: ['super_admin', 'circulation', 'front_desk'] },
  { title: '管理员操作日志审计', path: '/admin/logs', icon: 'Setting', roles: ['super_admin'] },
  { title: '前往读者前台门户', path: '/portal/home', icon: 'Compass', roles: ['super_admin', 'cataloger', 'circulation', 'front_desk'] },
]

const filteredMenuItems = computed(() => {
  const query = cmdSearchQuery.value.trim().toLowerCase()
  return allNavItems.filter(item => {
    const hasRole = !item.roles || item.roles.includes(userStore.roleKey)
    if (!hasRole) return false
    if (!query) return true
    return item.title.toLowerCase().includes(query) || item.path.toLowerCase().includes(query)
  })
})

const handleNavigate = (path) => {
  commandPaletteVisible.value = false
  cmdSearchQuery.value = ''
  router.push(path)
}

const handleKeydown = (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
    e.preventDefault()
    commandPaletteVisible.value = !commandPaletteVisible.value
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.admin-wrapper {
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.aside-container {
  background-color: #1f2d3d;
  transition: width 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 21, 41, 0.15);
  z-index: 10;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  background: #18222c;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.logo-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409EFF, #3a7afe);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.logo .title {
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.menu-scrollbar {
  flex: 1;
}

.el-menu {
  border-right: none;
}

.right-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.header {
  height: 56px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
  padding: 0 18px;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  color: #606266;
  transition: all 0.2s;
}

.collapse-btn:hover {
  background: #f2f6fc;
  color: #409EFF;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
}

.tool-btn:hover {
  color: #409EFF;
  background: #f2f6fc;
}

.tool-label {
  font-size: 13px;
}

.role-tag {
  font-weight: 500;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background 0.2s;
}

.user-profile:hover {
  background: #f2f6fc;
}

.user-avatar {
  background: #409EFF;
}

.admin-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.arrow-down {
  font-size: 12px;
  color: #909399;
}

/* 多标签页 TagsView */
.tags-view-container {
  height: 36px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  padding: 0 12px;
  z-index: 8;
}

.tags-scrollbar {
  flex: 1;
  white-space: nowrap;
}

.tags-inner {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 0;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  height: 28px;
  line-height: 26px;
  font-size: 12px;
  color: #606266;
  background: #fafafa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.tag-item:hover {
  background: #f2f6fc;
  color: #409EFF;
  border-color: #c6e2ff;
}

.tag-item.active {
  background: #409EFF;
  color: #fff;
  border-color: #409EFF;
}

.tag-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fff;
}

.tag-close {
  border-radius: 50%;
  padding: 2px;
  transition: all 0.2s;
}

.tag-close:hover {
  background: rgba(0, 0, 0, 0.15);
}

.tag-item.active .tag-close:hover {
  background: rgba(255, 255, 255, 0.25);
}

.tags-actions {
  margin-left: 8px;
  flex-shrink: 0;
}

.main-content {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
  flex: 1;
}

.search-cmd-btn {
  background: #f4f4f5;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.kbd-badge {
  font-size: 11px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 1px 5px;
  color: #909399;
  font-family: inherit;
  margin-left: 6px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.cmd-palette-body {
  padding: 4px 0;
}

.cmd-search-input {
  margin-bottom: 14px;
}

.cmd-results-list {
  max-height: 380px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.cmd-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: all 0.2s;
}

.cmd-item:hover {
  background: #ecf5ff;
  border-color: #b3d8ff;
  transform: translateX(4px);
}

.cmd-icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #e0edff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cmd-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.cmd-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.cmd-path {
  font-size: 11px;
  color: #909399;
}

.cmd-enter-icon {
  color: #c0c4cc;
}

.cmd-item:hover .cmd-enter-icon {
  color: #409eff;
}

.cmd-footer-tips {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.cmd-footer-tips kbd {
  background: #f4f4f5;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
  padding: 1px 4px;
}
</style>

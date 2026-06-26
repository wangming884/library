<template>
  <el-container style="height: 100vh">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" style="transition: width 0.3s">
      <div class="logo" :class="{ collapsed: isCollapse }">
        <el-icon size="24"><Collection /></el-icon>
        <span v-show="!isCollapse" class="title">图书馆管理</span>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-sub-menu index="reader-mgmt">
          <template #title><el-icon><User /></el-icon><span>读者管理</span></template>
          <el-menu-item index="/admin/readers">读者列表</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="book-mgmt">
          <template #title><el-icon><Reading /></el-icon><span>图书管理</span></template>
          <el-menu-item index="/admin/books">图书列表</el-menu-item>
          <el-menu-item index="/admin/copies">副本管理</el-menu-item>
          <el-menu-item index="/admin/categories">分类管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="borrow-mgmt">
          <template #title><el-icon><Document /></el-icon><span>借阅管理</span></template>
          <el-menu-item index="/admin/borrow">借还办理</el-menu-item>
          <el-menu-item index="/admin/borrow-records">借阅记录</el-menu-item>
          <el-menu-item index="/admin/reservations">预约管理</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/admin/fines">
          <el-icon><Money /></el-icon>
          <template #title>罚款管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/reports">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>统计报表</template>
        </el-menu-item>
        <el-sub-menu index="system-mgmt">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/admin/config">系统设置</el-menu-item>
          <el-menu-item index="/admin/announcements">公告管理</el-menu-item>
          <el-menu-item index="/admin/feedback">留言反馈</el-menu-item>
          <el-menu-item index="/admin/logs">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 右侧内容 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" size="20">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/" style="margin-left: 16px">
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="admin-name">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
          <el-dropdown trigger="click">
            <el-avatar :size="32" style="cursor: pointer; background: #409EFF">
              <el-icon><UserFilled /></el-icon>
            </el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background-color: #263445;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
}
.logo.collapsed .title { display: none; }
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
}
.collapse-btn { cursor: pointer; }
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.admin-name { font-size: 14px; color: #606266; }
.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
.el-aside {
  background-color: #304156;
  overflow: hidden;
}
.el-menu {
  border-right: none;
}
</style>

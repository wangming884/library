<template>
  <el-container class="portal-layout">
    <!-- 顶部导航栏 (Sticky + Glassmorphism) -->
    <el-header class="portal-header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo-box" @click="router.push('/portal/home')">
          <div class="logo-icon">
            <el-icon size="22" color="#fff"><Reading /></el-icon>
          </div>
          <div class="logo-text">
            <span class="main-title">智慧图书馆</span>
            <span class="sub-title">READER PORTAL</span>
          </div>
        </div>

        <!-- 导航菜单 -->
        <el-menu
          mode="horizontal"
          :default-active="route.path"
          router
          class="top-menu"
          :ellipsis="false"
        >
          <el-menu-item index="/portal/home">
            <el-icon><House /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/portal/search">
            <el-icon><Search /></el-icon>
            <span>图书检索</span>
          </el-menu-item>
          <el-menu-item index="/portal/seat">
            <el-icon><OfficeBuilding /></el-icon>
            <span>座位预约</span>
          </el-menu-item>
          <el-menu-item index="/portal/center">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item index="/portal/feedback">
            <el-icon><ChatDotRound /></el-icon>
            <span>留言反馈</span>
          </el-menu-item>
        </el-menu>

        <!-- 用户信息区 -->
        <div class="user-action-box">
          <el-dropdown trigger="click">
            <div class="user-badge">
              <el-avatar :size="34" class="reader-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="reader-info-text">
                <span class="reader-name">{{ userStore.userInfo.name || userStore.userInfo.username || '读者' }}</span>
                <span class="reader-card" v-if="userStore.userInfo.cardNo">{{ userStore.userInfo.cardNo }}</span>
              </div>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/portal/center')">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/portal/seat')">
                  <el-icon><OfficeBuilding /></el-icon>座位预约
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin()" @click="router.push('/admin/dashboard')">
                  <el-icon><Setting /></el-icon>管理后台
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- 核心内容区 -->
    <el-main class="portal-main">
      <div class="content-wrapper">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </div>
    </el-main>

    <!-- 底部 Footer -->
    <el-footer class="portal-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <div class="footer-logo">📚 智慧图书馆服务平台</div>
          <p class="footer-desc">倡导书香校园，提供馆藏检索、空间预约、文献借还与智能读者服务。</p>
        </div>
        <div class="footer-meta">
          <span>开馆时间：周一至周日 08:00 - 22:00</span>
          <span>服务咨询：010-88886666</span>
          <span>© 2024-2026 图书馆管理系统 版权所有</span>
        </div>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  ElMessageBox.confirm('确定退出登录？', '提示', {
    type: 'warning',
    confirmButtonText: '退出',
    cancelButtonText: '取消'
  }).then(() => {
    userStore.logout()
    router.push('/reader-login')
  }).catch(() => {})
}
</script>

<style scoped>
.portal-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.portal-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 64px !important;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.03);
  padding: 0;
}

.header-container {
  max-width: 1320px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-box {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: linear-gradient(135deg, #409EFF 0%, #3a7afe 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-text .main-title {
  font-size: 17px;
  font-weight: 700;
  color: #1f2f3d;
  letter-spacing: 0.5px;
}

.logo-text .sub-title {
  font-size: 10px;
  color: #909399;
  letter-spacing: 1px;
}

.top-menu {
  flex: 1;
  max-width: 600px;
  margin: 0 40px;
  border: none !important;
  background: transparent !important;
}

:deep(.top-menu .el-menu-item) {
  font-size: 14px;
  font-weight: 500;
  height: 64px;
  line-height: 64px;
  border-bottom: 2px solid transparent !important;
  transition: all 0.25s;
}

:deep(.top-menu .el-menu-item.is-active) {
  color: #409EFF !important;
  border-bottom: 2px solid #409EFF !important;
  background: transparent !important;
}

.user-action-box {
  display: flex;
  align-items: center;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 10px 4px 4px;
  border-radius: 20px;
  background: #f0f2f5;
  cursor: pointer;
  transition: all 0.2s;
}

.user-badge:hover {
  background: #e4e7ed;
}

.reader-avatar {
  background: linear-gradient(135deg, #409EFF, #67C23A);
}

.reader-info-text {
  display: flex;
  flex-direction: column;
}

.reader-name {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.reader-card {
  font-size: 11px;
  color: #909399;
}

.arrow-icon {
  font-size: 12px;
  color: #909399;
}

.portal-main {
  flex: 1;
  padding: 24px;
}

.content-wrapper {
  max-width: 1320px;
  margin: 0 auto;
}

.portal-footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  padding: 24px 20px;
  height: auto !important;
}

.footer-inner {
  max-width: 1320px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.footer-brand .footer-logo {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.footer-desc {
  font-size: 12px;
  color: #909399;
  margin: 0;
}

.footer-meta {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .top-menu {
    margin: 0 10px;
  }
  .footer-inner {
    flex-direction: column;
    text-align: center;
  }
  .footer-meta {
    justify-content: center;
  }
}
</style>

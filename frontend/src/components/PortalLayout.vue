<template>
  <el-container class="portal-layout">
    <!-- 顶部导航栏 (Sticky + Frosted Glass) -->
    <el-header class="portal-header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo-box" @click="router.push('/portal/home')">
          <div class="logo-icon">
            <el-icon size="22" color="#fff"><Reading /></el-icon>
          </div>
          <div class="logo-text">
            <span class="main-title">智慧图书馆</span>
            <span class="sub-title">SMART LIBRARY SYSTEM</span>
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
          <el-menu-item index="/portal/rank">
            <el-icon><Trophy /></el-icon>
            <span>榜单精选</span>
          </el-menu-item>
          <el-menu-item index="/portal/seat">
            <el-icon><OfficeBuilding /></el-icon>
            <span>自习选座</span>
          </el-menu-item>
          <el-menu-item index="/portal/news">
            <el-icon><Bell /></el-icon>
            <span>馆务资讯</span>
          </el-menu-item>
          <el-menu-item index="/portal/guide">
            <el-icon><Guide /></el-icon>
            <span>服务指南</span>
          </el-menu-item>
          <el-menu-item index="/portal/center">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
          <el-menu-item index="/portal/feedback">
            <el-icon><ChatDotRound /></el-icon>
            <span>读者留言</span>
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
                  <el-icon><User /></el-icon>个人借阅中心
                </el-dropdown-item>
                <el-dropdown-item @click="router.push('/portal/seat')">
                  <el-icon><OfficeBuilding /></el-icon>自习选座记录
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin()" @click="router.push('/admin/dashboard')">
                  <el-icon><Setting /></el-icon>进入管理后台
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>安全退出登录
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

    <!-- 现代化底部 Footer -->
    <el-footer class="portal-footer">
      <div class="footer-inner">
        <div class="footer-top-grid">
          <!-- 平台愿景 -->
          <div class="footer-col brand-col">
            <div class="footer-brand-title">
              <div class="footer-logo-icon"><el-icon><Reading /></el-icon></div>
              <span>智慧图书馆服务平台</span>
            </div>
            <p class="footer-slogan">
              探索真理，浸润书香。依托数字化技术，为师生读者提供全方位文献检索、智能空间预约与个性化阅读服务。
            </p>
          </div>

          <!-- 快速导航 -->
          <div class="footer-col">
            <h4 class="col-title">读者服务</h4>
            <ul class="col-links">
              <li @click="router.push('/portal/search')">馆藏书目检索</li>
              <li @click="router.push('/portal/seat')">自习空间选座</li>
              <li @click="router.push('/portal/center')">借阅续借管理</li>
              <li @click="router.push('/portal/feedback')">图书荐购通道</li>
            </ul>
          </div>

          <!-- 开放指引 -->
          <div class="footer-col">
            <h4 class="col-title">开馆指南</h4>
            <ul class="col-info">
              <li>周一至周日：08:00 - 22:00</li>
              <li>法定节假日：09:00 - 17:00</li>
              <li>静音自习区：全天候静音</li>
              <li>电子阅览大厅：3F开放</li>
            </ul>
          </div>

          <!-- 联系反馈 -->
          <div class="footer-col">
            <h4 class="col-title">联系与支持</h4>
            <ul class="col-info">
              <li>读者咨询：010-88886666</li>
              <li>读者邮箱：library@oneais.dev</li>
              <li>馆舍地址：文献信息中心大楼 1-4F</li>
            </ul>
          </div>
        </div>

        <div class="footer-bottom-bar">
          <span>© 2024-2026 智慧图书馆管理系统 · 保留所有权利</span>
          <span class="tech-badge">Powered by Spring Boot & Vue 3</span>
        </div>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'
import {
  Reading,
  House,
  Search,
  OfficeBuilding,
  User,
  ChatDotRound,
  UserFilled,
  ArrowDown,
  Setting,
  SwitchButton
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  ElMessageBox.confirm('确定退出当前登录状态？', '安全退出提示', {
    type: 'warning',
    confirmButtonText: '确定退出',
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
  background: #f8fafc;
}

.portal-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: 68px !important;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(14px);
  -webkit-backdrop-filter: blur(14px);
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 18px rgba(15, 23, 42, 0.04);
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
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #1e40af 0%, #3b82f6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.28);
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-text .main-title {
  font-size: 18px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.3px;
}

.logo-text .sub-title {
  font-size: 9px;
  color: #64748b;
  letter-spacing: 1.2px;
  font-weight: 600;
}

.top-menu {
  flex: 1;
  max-width: 820px;
  margin: 0 32px;
  border: none !important;
  background: transparent !important;
}

:deep(.top-menu .el-menu-item) {
  font-size: 14px;
  font-weight: 500;
  height: 68px;
  line-height: 68px;
  border-bottom: 2px solid transparent !important;
  color: #475569 !important;
  transition: all 0.22s;
}

:deep(.top-menu .el-menu-item:hover) {
  color: #2563eb !important;
  background: transparent !important;
}

:deep(.top-menu .el-menu-item.is-active) {
  color: #2563eb !important;
  font-weight: 600 !important;
  border-bottom: 2px solid #2563eb !important;
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
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}

.user-badge:hover {
  background: #e2e8f0;
}

.reader-avatar {
  background: linear-gradient(135deg, #2563eb, #06b6d4);
}

.reader-info-text {
  display: flex;
  flex-direction: column;
}

.reader-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
}

.reader-card {
  font-size: 11px;
  color: #64748b;
}

.arrow-icon {
  font-size: 12px;
  color: #94a3b8;
}

.portal-main {
  flex: 1;
  padding: 24px;
}

.content-wrapper {
  max-width: 1320px;
  margin: 0 auto;
}

/* ================= 现代化大 Footer ================= */
.portal-footer {
  background: #0f172a;
  color: #e2e8f0;
  padding: 48px 24px 24px;
  height: auto !important;
  border-top: 1px solid #1e293b;
}

.footer-inner {
  max-width: 1320px;
  margin: 0 auto;
}

.footer-top-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr;
  gap: 36px;
  margin-bottom: 36px;
}

.brand-col {
  padding-right: 24px;
}

.footer-brand-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 17px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
}

.footer-logo-icon {
  width: 28px;
  height: 28px;
  background: #2563eb;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #fff;
}

.footer-slogan {
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.7;
  margin: 0;
}

.col-title {
  font-size: 14px;
  font-weight: 600;
  color: #f8fafc;
  margin: 0 0 16px;
  letter-spacing: 0.5px;
}

.col-links, .col-info {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  font-size: 13px;
  color: #94a3b8;
}

.col-links li {
  cursor: pointer;
  transition: color 0.2s;
}

.col-links li:hover {
  color: #60a5fa;
}

.footer-bottom-bar {
  border-top: 1px solid #1e293b;
  padding-top: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #64748b;
  flex-wrap: wrap;
  gap: 12px;
}

.tech-badge {
  background: #1e293b;
  padding: 4px 10px;
  border-radius: 4px;
  color: #94a3b8;
}

@media (max-width: 900px) {
  .top-menu {
    margin: 0 10px;
  }
  .footer-top-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 600px) {
  .footer-top-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<template>
  <el-container style="height: 100vh">
    <!-- 顶部导航 -->
    <el-header class="portal-header">
      <div class="header-content">
        <div class="logo" @click="router.push('/portal/home')">
          <el-icon size="28" color="#409EFF"><Collection /></el-icon>
          <span class="title">图书馆管理系统</span>
        </div>
        <el-menu mode="horizontal" :default-active="route.path" router class="top-menu">
          <el-menu-item index="/portal/home">首页</el-menu-item>
          <el-menu-item index="/portal/search">图书检索</el-menu-item>
          <el-menu-item index="/portal/center">个人中心</el-menu-item>
          <el-menu-item index="/portal/feedback">留言反馈</el-menu-item>
        </el-menu>
        <div class="user-info">
          <span class="name">{{ userStore.userInfo.name || '读者' }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </el-header>

    <!-- 内容区 -->
    <el-main class="portal-main">
      <router-view />
    </el-main>

    <!-- 底部 -->
    <el-footer class="portal-footer">
      <p>图书馆管理系统 ©2024 All Rights Reserved</p>
    </el-footer>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  router.push('/reader-login')
}
</script>

<style scoped>
.portal-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  padding: 0;
  height: 60px !important;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  margin-right: 40px;
}
.logo .title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}
.top-menu {
  flex: 1;
  border: none !important;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-info .name {
  font-size: 14px;
  color: #606266;
}
.portal-main {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}
.portal-footer {
  text-align: center;
  color: #909399;
  font-size: 13px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  height: 40px !important;
  line-height: 40px;
}
</style>

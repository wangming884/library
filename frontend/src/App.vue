<script setup>
</script>

<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "WenQuanYi Micro Hei", sans-serif;
  background: #f5f7fa;
  color: #303133;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 优雅平滑定制滚动条 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 4px;
  transition: background 0.3s;
}
::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}
::-webkit-scrollbar-track {
  background: transparent;
}

/* 页面切换平滑过渡 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.22s cubic-bezier(0.4, 0, 0.2, 1);
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

/* 全局卡片与阴影细节优化 */
.el-card {
  border-radius: 10px !important;
  border: 1px solid #ebeef5;
  transition: box-shadow 0.25s ease, transform 0.25s ease !important;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
}

.el-button {
  font-weight: 500;
  border-radius: 6px;
}

.el-dialog {
  border-radius: 12px !important;
  overflow: hidden;
}
</style>

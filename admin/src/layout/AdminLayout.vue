<template>
  <el-container style="height: 100vh">
    <el-aside :width="isCollapse ? '64px' : '200px'" style="background: #304156; transition: width 0.3s">
      <div class="logo-area" @click="isCollapse = !isCollapse">
        <span v-if="!isCollapse" class="logo-text">🏃 悦动打卡</span>
        <span v-else class="logo-text">🏃</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="isCollapse" background-color="#304156" text-color="#bfcbd9" active-text-color="#4facfe" router>
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><span>仪表盘</span></el-menu-item>
        <el-menu-item index="/user"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/sport"><el-icon><Trophy /></el-icon><span>运动项目管理</span></el-menu-item>
        <el-menu-item index="/checkin"><el-icon><Calendar /></el-icon><span>打卡记录管理</span></el-menu-item>
        <el-menu-item index="/statistics"><el-icon><DataAnalysis /></el-icon><span>数据统计</span></el-menu-item>
        <el-menu-item index="/points"><el-icon><Coin /></el-icon><span>积分记录管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background: #fff; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,0,0,0.08); padding: 0 20px">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
        </el-breadcrumb>
        <el-dropdown @command="handleCommand">
          <span class="admin-info">{{ adminName }} <el-icon><ArrowDown /></el-icon></span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background: #f0f2f5; padding: 20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onErrorCaptured } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
const adminName = computed(() => localStorage.getItem('nickname') || '管理员')

onErrorCaptured((err) => {
  console.error('页面渲染错误:', err)
  return false
})

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    localStorage.clear()
    router.push('/login')
  }
}
</script>

<style scoped>
.logo-area { height: 60px; display: flex; align-items: center; justify-content: center; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-text { color: #fff; font-size: 18px; font-weight: bold; white-space: nowrap; }
.admin-info { cursor: pointer; display: flex; align-items: center; gap: 4px; color: #333; }
</style>

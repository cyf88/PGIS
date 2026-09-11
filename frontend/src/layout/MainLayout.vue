<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">警用设备数据服务</div>
      <el-menu :default-active="active" router>
        <el-sub-menu index="info">
          <template #title>警用设备基本信息</template>
          <el-menu-item index="/device-info/query">查询</el-menu-item>
          <el-menu-item index="/device-info/by-sjly">按来源检索</el-menu-item>
          <el-menu-item index="/device-info/map">地图聚合</el-menu-item>
          <el-menu-item index="/device-info/import">导入</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="loc">
          <template #title>警用设备定位信息</template>
          <el-menu-item index="/device-location/query">查询</el-menu-item>
          <el-menu-item index="/device-location/import">导入</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">{{ title }}</el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const active = computed(() => route.path)
const titleMap = {
  '/device-info/query': '警用设备基本信息 · 查询',
  '/device-info/by-sjly': '警用设备基本信息 · 按来源检索',
  '/device-info/map': '警用设备基本信息 · 地图聚合',
  '/device-info/import': '警用设备基本信息 · 导入（sb001*.csv）',
  '/device-location/query': '警用设备定位信息 · 查询',
  '/device-location/import': '警用设备定位信息 · 导入（sb002*.csv）'
}
const title = computed(() => titleMap[route.path] || '警用设备数据服务')
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #1f2d3d; }
.logo {
  color: #fff;
  padding: 18px 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.aside :deep(.el-menu) {
  border-right: none;
  background: #1f2d3d;
}
.aside :deep(.el-menu-item),
.aside :deep(.el-sub-menu__title) {
  color: #d3dce6;
}
.header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #ebeef5;
}
</style>

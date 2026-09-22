import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  // 独立地图窗口（无侧边导航，从主界面菜单弹出新窗口打开）
  { path: '/map/device-info', component: () => import('../views/DeviceInfoMap.vue') },
  { path: '/map/alarm', component: () => import('../views/AlarmMap.vue') },
  { path: '/map/case', component: () => import('../views/CaseMap.vue') },
  {
    path: '/',
    component: MainLayout,
    redirect: '/device-info/query',
    children: [
      { path: '/device-info/query', component: () => import('../views/DeviceInfoQuery.vue') },
      { path: '/device-info/by-sjly', component: () => import('../views/DeviceInfoBySjly.vue') },
      { path: '/device-info/map', component: () => import('../views/DeviceInfoMap.vue') },
      { path: '/device-info/import', component: () => import('../views/DeviceInfoImport.vue') },
      { path: '/device-location/query', component: () => import('../views/DeviceLocationQuery.vue') },
      { path: '/device-location/import', component: () => import('../views/DeviceLocationImport.vue') },
      { path: '/alarm/list', component: () => import('../views/AlarmList.vue') },
      { path: '/alarm/map', component: () => import('../views/AlarmMap.vue') },
      { path: '/case/list', component: () => import('../views/CaseList.vue') },
      { path: '/case/map', component: () => import('../views/CaseMap.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 独立地图窗口：根元素加标记类，使地图容器铺满整个视口，并设置窗口标题
const standaloneTitles = {
  '/map/device-info': '警用设备基本信息 · 地图聚合',
  '/map/alarm': '接处警报警信息 · 地图展示',
  '/map/case': '案件信息 · 地图展示'
}
router.afterEach((to) => {
  document.documentElement.classList.toggle('standalone-map', to.path.startsWith('/map/'))
  document.title = standaloneTitles[to.path] || '警用设备数据服务'
})

export default router

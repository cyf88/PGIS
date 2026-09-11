import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
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
      { path: '/device-location/import', component: () => import('../views/DeviceLocationImport.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

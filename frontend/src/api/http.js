import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  timeout: 0
})

http.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body.code === 'number' && body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body
  },
  (err) => {
    const msg = err.response?.data?.message || err.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export function queryDeviceInfo(params) {
  return http.get('/api/device-info', { params })
}

export function queryDeviceLocation(params) {
  return http.get('/api/device-location', { params })
}

export function searchDeviceInfoBySjly(params) {
  return http.get('/api/device-info/search-by-sjly', { params })
}

export function countDeviceInfoBySjly(sjly) {
  return http.get('/api/device-info/count-by-sjly', { params: { sjly } })
}

export function aggregateDeviceInfoInBounds(params) {
  return http.get('/api/device-info/aggregate-in-bounds', { params })
}

export function listAdminRegions() {
  return http.get('/api/device-info/admin-regions')
}

export function fetchRegionBounds(sjly) {
  return http.get('/api/device-info/region-bounds', { params: { sjly } })
}

export function fetchMapConfig() {
  return http.get('/api/map-config')
}

export function importDeviceInfo(files, onUploadProgress) {
  const form = new FormData()
  files.forEach((f) => form.append('files', f))
  return http.post('/api/import/device-info', form, { onUploadProgress })
}

export function importDeviceLocation(files, onUploadProgress) {
  const form = new FormData()
  files.forEach((f) => form.append('files', f))
  return http.post('/api/import/device-location', form, { onUploadProgress })
}

export function queryAlarms(params) {
  return http.get('/api/alarm', { params })
}

export function getAlarm(jjbh) {
  return http.get(`/api/alarm/${jjbh}`)
}

export function queryAlarmMapPoints(params) {
  return http.get('/api/alarm/map-points', { params })
}

export function queryCases(params) {
  return http.get('/api/case', { params })
}

export function getCase(xxzjbh) {
  return http.get(`/api/case/${xxzjbh}`)
}

export function queryCaseMapPoints(params) {
  return http.get('/api/case/map-points', { params })
}

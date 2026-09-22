<template>
  <div class="map-page">
    <div class="toolbar">
      <el-select v-model="provinceCode" clearable filterable placeholder="选择省" style="width: 160px" @change="onProvinceChange" @keyup.enter="searchRegion">
        <el-option v-for="p in provinces" :key="p.code" :label="p.name" :value="p.code" />
      </el-select>
      <el-select v-model="cityCode" clearable filterable placeholder="选择市" style="width: 180px" :disabled="!provinceCode" @keyup.enter="searchRegion">
        <el-option v-for="c in cities" :key="c.code" :label="c.name" :value="c.code" />
      </el-select>
      <el-button type="primary" @click="searchRegion">搜索</el-button>
      <el-radio-group v-if="basemaps.length > 1" v-model="basemapId" size="default" @change="switchBasemap">
        <el-radio-button v-for="item in basemaps" :key="item.id" :label="item.id">{{ item.label }}</el-radio-button>
      </el-radio-group>
      <el-radio-group v-model="clusterMode" size="default" @change="reload">
        <el-radio-button label="region">按行政区</el-radio-button>
        <el-radio-button label="nearby">按邻近点</el-radio-button>
      </el-radio-group>
      <el-button @click="reload">按视野刷新</el-button>
      <span class="meta">模式 {{ modeLabel }}　视野内 {{ total }} 台　缩放 {{ zoom }}　点击气泡可下钻</span>
      <span v-if="clusterMode === 'region'" class="legend">
        <i class="dot province"></i>省
        <i class="dot city"></i>市
        <i class="dot county"></i>县
      </span>
      <span v-else class="legend">
        <i class="dot nearby"></i>邻近点
      </span>
    </div>
    <div class="map-wrap">
      <div ref="mapEl" class="map"></div>
      <div class="stat-panel">
        <div class="province-title">{{ provinceName }}</div>
        <div class="fold-head" @click="typesOpen = !typesOpen">
          <span class="caret">{{ typesOpen ? '▼' : '▶' }}</span>
          <span>装备类型</span>
        </div>
        <div v-show="typesOpen" class="fold-body">
          <div v-for="row in typeStats" :key="row.zblx" class="type-row" :class="{ muted: !row.count }">
            <span class="type-name">{{ row.name }}</span>
            <span class="type-count">{{ row.count || 0 }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { aggregateDeviceInfoInBounds, fetchMapConfig, fetchRegionBounds, listAdminRegions } from '../api/http'

const DEFAULT_BASEMAPS = [
  {
    id: 'gaode',
    label: '高德地图',
    tileUrl: 'https://webst0{s}.is.autonavi.com/appmaptile?style=7&x={x}&y={y}&z={z}',
    tileSubdomains: '1234',
    tileAttribution: '&copy; 高德地图',
    tileMaxZoom: 18
  },
  {
    id: 'intranet',
    label: '内网栅格',
    tileUrl: 'http://10.2.164.43:10000/tileService/raster-3857/Maps/hebei21/EzMap?Service=getImage&Type=RGB&ZoomOffset=0&Col={x}&Row={y}&Zoom={z}&v=0.3',
    tileSubdomains: '',
    tileAttribution: '© 内网地图',
    tileMaxZoom: 18
  }
]

const DEFAULT_MAP = {
  defaultBasemap: 'gaode',
  basemaps: DEFAULT_BASEMAPS,
  tileUrl: DEFAULT_BASEMAPS[0].tileUrl,
  tileSubdomains: DEFAULT_BASEMAPS[0].tileSubdomains,
  tileAttribution: DEFAULT_BASEMAPS[0].tileAttribution,
  tileMaxZoom: 18,
  provinceZoom: 6,
  cityMaxZoom: 11,
  fallbackMinLng: 113.45,
  fallbackMinLat: 36.05,
  fallbackMaxLng: 119.87,
  fallbackMaxLat: 42.62
}

const BASEMAP_STORAGE_KEY = 'pgis:basemap'

let mapCfg = { ...DEFAULT_MAP }

function fallbackBounds() {
  return [[mapCfg.fallbackMinLat, mapCfg.fallbackMinLng], [mapCfg.fallbackMaxLat, mapCfg.fallbackMaxLng]]
}

const mapEl = ref(null)
const provinces = ref([])
const provinceCode = ref('')
const cityCode = ref('')
const sjly = ref('')
const clusterMode = ref('region')
const basemapId = ref('gaode')
const basemaps = ref([...DEFAULT_BASEMAPS])
const mode = ref('')
const total = ref(0)
const zoom = ref(6)
const typeStats = ref([])
const provinceName = ref('全部')
const typesOpen = ref(true)

const cities = computed(() => {
  const p = provinces.value.find((item) => item.code === provinceCode.value)
  return p?.cities || []
})

const modeLabel = computed(() => {
  if (mode.value === 'nearby') return '邻近点聚合'
  if (mode.value === 'region') {
    if (zoom.value <= 6) return '按省聚合'
    if (zoom.value <= 9) return '按市聚合'
    return '按区聚合'
  }
  return mode.value === 'point' ? '撒点' : (mode.value || '-')
})

let map = null
let baseLayer = null
let layer = null
let moveTimer = null
let suppressMoveReload = false

onMounted(async () => {
  mapCfg = await loadMapConfig()
  basemaps.value = filterBasemapsForStandalone(mapCfg.basemaps)
  basemapId.value = pickBasemapId({ ...mapCfg, basemaps: basemaps.value })
  map = L.map(mapEl.value, { zoomControl: true }).setView(regionCenter(fallbackBounds()), mapCfg.provinceZoom)
  applyBasemap(basemapId.value)
  map.on('moveend', scheduleReload)
  try {
    const resp = await listAdminRegions()
    provinces.value = resp.data || []
    if (provinces.value.length) {
      provinceCode.value = provinces.value[0].code
    }
  } catch {
    provinces.value = []
  }
  await searchRegion()
})

onBeforeUnmount(() => {
  if (moveTimer) clearTimeout(moveTimer)
  map?.remove()
})

function scheduleReload() {
  if (suppressMoveReload) return
  if (moveTimer) clearTimeout(moveTimer)
  moveTimer = setTimeout(reload, 300)
}

function onProvinceChange() {
  cityCode.value = ''
}

async function searchRegion() {
  if (!provinceCode.value) {
    ElMessage.warning('请选择省份')
    return
  }
  sjly.value = cityCode.value || provinceCode.value
  const bounds = await resolveBounds(sjly.value)
  suppressMoveReload = true
  if (cityCode.value) {
    map.fitBounds(bounds, { padding: [48, 48], maxZoom: mapCfg.cityMaxZoom, animate: false })
  } else {
    map.setView(regionCenter(bounds), mapCfg.provinceZoom, { animate: false })
  }
  suppressMoveReload = false
  await reload()
}

function regionCenter(bounds) {
  return [
    (bounds[0][0] + bounds[1][0]) / 2,
    (bounds[0][1] + bounds[1][1]) / 2
  ]
}

function isValidLeafletBounds(bounds) {
  if (!Array.isArray(bounds) || bounds.length !== 2) return false
  const south = Number(bounds[0][0])
  const west = Number(bounds[0][1])
  const north = Number(bounds[1][0])
  const east = Number(bounds[1][1])
  return Number.isFinite(south) && Number.isFinite(west) && Number.isFinite(north) && Number.isFinite(east)
    && west >= -180 && east <= 180 && south >= -90 && north <= 90
    && west < east && south < north
}

async function resolveBounds(prefix) {
  try {
    const resp = await fetchRegionBounds(prefix)
    const b = resp.data
    const box = [[b.minLat, b.minLng], [b.maxLat, b.maxLng]]
    if (isValidLeafletBounds(box)) {
      return box
    }
  } catch {
    /* 无定位或范围非法时用河北省范围兜底 */
  }
  return fallbackBounds()
}

async function loadMapConfig() {
  try {
    const resp = await fetchMapConfig()
    return normalizeMapConfig({ ...DEFAULT_MAP, ...(resp.data || {}) })
  } catch {
    return normalizeMapConfig({ ...DEFAULT_MAP })
  }
}

function normalizeMapConfig(raw) {
  const list = Array.isArray(raw.basemaps)
    ? raw.basemaps.filter((item) => item && item.tileUrl)
    : []
  const basemapList = list.length
    ? list.map((item, index) => ({
        id: item.id || `basemap-${index}`,
        label: item.label || `图层${index + 1}`,
        tileUrl: item.tileUrl,
        tileSubdomains: item.tileSubdomains || '',
        tileAttribution: item.tileAttribution || '',
        tileMaxZoom: item.tileMaxZoom || 18
      }))
    : [{
        id: 'default',
        label: '默认底图',
        tileUrl: raw.tileUrl,
        tileSubdomains: raw.tileSubdomains || '',
        tileAttribution: raw.tileAttribution || '',
        tileMaxZoom: raw.tileMaxZoom || 18
      }]
  return { ...raw, basemaps: basemapList, defaultBasemap: raw.defaultBasemap || basemapList[0].id }
}

function pickBasemapId(cfg) {
  const ids = new Set((cfg.basemaps || []).map((item) => item.id))
  try {
    const stored = localStorage.getItem(BASEMAP_STORAGE_KEY)
    if (stored && ids.has(stored)) return stored
  } catch {
    /* 无 localStorage 时用配置默认 */
  }
  if (cfg.defaultBasemap && ids.has(cfg.defaultBasemap)) return cfg.defaultBasemap
  return cfg.basemaps[0]?.id || 'gaode'
}

// 独立地图窗口（/map/* 新窗口）：仅使用内网栅格底图，隐藏切换选项
function filterBasemapsForStandalone(list) {
  if (!location.pathname.startsWith('/map/')) return list
  const intranet = (list || []).filter((item) => item.id === 'intranet')
  return intranet.length ? intranet : (list || []).slice(0, 1)
}

function currentBasemap(id = basemapId.value) {
  return (mapCfg.basemaps || []).find((item) => item.id === id) || mapCfg.basemaps[0]
}

function tileLayerOptions(source) {
  const options = {
    maxZoom: source.tileMaxZoom || 18,
    attribution: source.tileAttribution || ''
  }
  if (source.tileSubdomains) {
    options.subdomains = source.tileSubdomains
  }
  return options
}

function applyBasemap(id) {
  const source = currentBasemap(id)
  if (!map || !source) return
  if (baseLayer) {
    map.removeLayer(baseLayer)
  }
  baseLayer = L.tileLayer(source.tileUrl, tileLayerOptions(source)).addTo(map)
  baseLayer.bringToBack()
}

function switchBasemap(id) {
  basemapId.value = id
  applyBasemap(id)
  try {
    localStorage.setItem(BASEMAP_STORAGE_KEY, id)
  } catch {
    /* ignore */
  }
}

function queryBounds() {
  const b = map.getBounds()
  let minLng = Math.max(-180, Math.min(180, b.getWest()))
  let maxLng = Math.max(-180, Math.min(180, b.getEast()))
  let minLat = Math.max(-90, Math.min(90, b.getSouth()))
  let maxLat = Math.max(-90, Math.min(90, b.getNorth()))
  if (minLng >= maxLng) {
    minLng = -180
    maxLng = 180
  }
  if (minLat >= maxLat) {
    minLat = -90
    maxLat = 90
  }
  return { minLng, minLat, maxLng, maxLat }
}

async function reload() {
  if (!map) return
  const b = queryBounds()
  zoom.value = map.getZoom()
  const resp = await aggregateDeviceInfoInBounds({
    minLng: b.minLng,
    minLat: b.minLat,
    maxLng: b.maxLng,
    maxLat: b.maxLat,
    zoom: zoom.value,
    sjly: sjly.value.trim() || undefined,
    clusterMode: clusterMode.value
  })
  const data = resp.data
  mode.value = data.mode
  total.value = data.total || 0
  typeStats.value = data.typeStats || []
  provinceName.value = data.provinceName || '全部'
  render(data)
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (ch) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[ch]))
}

function formatCount(n) {
  if (n >= 100000) return `${Math.round(n / 10000)}万`
  if (n >= 10000) {
    const wan = n / 10000
    return `${Number.isInteger(wan) ? wan : wan.toFixed(1)}万`
  }
  return String(n)
}

/** 点击气泡跳到下一级行政区，类似链家下钻。 */
function nextDrillZoom(z) {
  if (z <= 6) return 8
  if (z <= 9) return 11
  if (z < 14) return 14
  return Math.min(18, z + 1)
}

function render(data) {
  if (layer) {
    layer.remove()
  }
  layer = L.layerGroup().addTo(map)
  ;(data.clusters || []).forEach((item) => {
    if (item.lng == null || item.lat == null) return
    if (data.mode === 'point') {
      L.circleMarker([item.lat, item.lng], {
        radius: 7,
        color: '#1d4ed8',
        fillColor: '#3b82f6',
        fillOpacity: 0.85,
        weight: 1
      }).bindPopup(`设备 ${item.sbbh || '-'}<br/>警员 ${item.jyxm || '-'}`).addTo(layer)
      return
    }
    const count = item.count || 0
    const nearby = data.mode === 'nearby' || item.regionLevel === 'nearby'
    const region = nearby ? '' : (item.regionName || item.sbbh || '')
    const level = nearby ? 'nearby' : (['province', 'city', 'county'].includes(item.regionLevel) ? item.regionLevel : 'city')
    const html = `<div class="lj-bubble lj-bubble--${level}"><span class="lj-num">${escapeHtml(formatCount(count))}</span>${
      region ? `<span class="lj-sub">${escapeHtml(region)}</span>` : ''
    }</div>`
    const size = nearby ? 36 : 88
    const height = nearby ? 36 : 56
    const icon = L.divIcon({
      className: 'lj-icon',
      html,
      iconSize: [size, height],
      iconAnchor: [size / 2, height / 2]
    })
    L.marker([item.lat, item.lng], { icon, zIndexOffset: count })
      .bindTooltip(`${region || (nearby ? '邻近点' : '区划')} · ${count} 台`, { direction: 'top', offset: [0, nearby ? -18 : -28] })
      .on('click', () => {
        map.setView([item.lat, item.lng], nextDrillZoom(map.getZoom()))
      })
      .addTo(layer)
  })
}
</script>

<style scoped>
.map-page {
  height: calc(100vh - 76px);
  display: flex;
  flex-direction: column;
  margin: -20px;
}
.toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.meta { color: #606266; font-size: 13px; }
.legend {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #606266;
  font-size: 13px;
}
.legend .dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 2px;
}
.legend .dot.province { background: #e85d04; }
.legend .dot.city { background: #2563eb; }
.legend .dot.county { background: #059669; }
.legend .dot.nearby { background: #7c3aed; }
.map-wrap {
  position: relative;
  flex: 1;
  min-height: 360px;
}
.map { height: 100%; min-height: 360px; }
.stat-panel {
  position: absolute;
  right: 12px;
  top: 12px;
  z-index: 500;
  width: 228px;
  max-height: calc(100% - 24px);
  overflow: auto;
  padding: 8px 10px 10px;
  background: rgba(255, 255, 255, 0.94);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.16);
  font-size: 12px;
  color: #303133;
  pointer-events: auto;
}
.province-title {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  padding: 2px 0 6px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 4px;
}
.fold-head {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 0;
  user-select: none;
}
.caret { width: 12px; color: #909399; font-size: 10px; }
.fold-body { padding: 2px 0 4px; }
.type-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  line-height: 1.7;
}
.type-row.muted { color: #c0c4cc; }
.type-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.type-count { font-variant-numeric: tabular-nums; font-weight: 600; flex-shrink: 0; }
</style>

<style>
.lj-icon {
  background: none !important;
  border: none !important;
}
.lj-bubble {
  width: 88px;
  height: 56px;
  border-radius: 28px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.28);
  border: 2px solid #fff;
  cursor: pointer;
  line-height: 1.05;
  padding: 0 6px;
  box-sizing: border-box;
}
.lj-bubble--province {
  background: linear-gradient(180deg, #ff9a4d 0%, #e85d04 100%);
}
.lj-bubble--city {
  background: linear-gradient(180deg, #60a5fa 0%, #2563eb 100%);
}
.lj-bubble--county {
  background: linear-gradient(180deg, #34d399 0%, #059669 100%);
}
.lj-bubble--nearby {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  padding: 0 2px;
  background: linear-gradient(180deg, #a78bfa 0%, #7c3aed 100%);
}
.lj-bubble--nearby .lj-num {
  font-size: 12px;
}
.lj-num {
  font-size: 14px;
  font-weight: 700;
}
.lj-sub {
  font-size: 11px;
  font-weight: 500;
  opacity: 0.95;
  margin-top: 2px;
  max-width: 76px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

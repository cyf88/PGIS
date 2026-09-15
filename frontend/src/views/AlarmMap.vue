<template>
  <div class="map-page">
    <div class="toolbar">
      <el-input v-model="xzqhdm" clearable placeholder="行政区划代码（前缀，可选）" style="width: 220px" @keyup.enter="reload" />
      <el-button type="primary" @click="reload">搜索</el-button>
      <el-radio-group v-if="basemaps.length > 1" v-model="basemapId" size="default" @change="switchBasemap">
        <el-radio-button v-for="item in basemaps" :key="item.id" :label="item.id">{{ item.label }}</el-radio-button>
      </el-radio-group>
      <el-button @click="reload">按视野刷新</el-button>
      <span class="meta">视野内 {{ points.length }} 条报警</span>
    </div>
    <div class="map-wrap">
      <div ref="mapEl" class="map"></div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { fetchMapConfig, queryAlarmMapPoints } from '../api/http'

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

const BASEMAP_STORAGE_KEY = 'pgis:basemap'

const mapEl = ref(null)
const xzqhdm = ref('')
const basemaps = ref([...DEFAULT_BASEMAPS])
const basemapId = ref('gaode')
const points = ref([])

let map = null
let baseLayer = null
let layer = null
let moveTimer = null

onMounted(async () => {
  const cfg = await loadMapConfig()
  basemaps.value = cfg.basemaps
  basemapId.value = pickBasemapId(cfg)
  map = L.map(mapEl.value, { zoomControl: true }).setView([35, 105], 5)
  applyBasemap(basemapId.value)
  map.on('moveend', scheduleReload)
  await reload()
})

onBeforeUnmount(() => {
  if (moveTimer) clearTimeout(moveTimer)
  map?.remove()
})

function scheduleReload() {
  if (moveTimer) clearTimeout(moveTimer)
  moveTimer = setTimeout(reload, 300)
}

async function loadMapConfig() {
  try {
    const resp = await fetchMapConfig()
    return normalizeMapConfig(resp.data || {})
  } catch {
    return normalizeMapConfig({ basemaps: DEFAULT_BASEMAPS })
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
    : DEFAULT_BASEMAPS
  return { basemaps: basemapList, defaultBasemap: raw.defaultBasemap || basemapList[0].id }
}

function pickBasemapId(cfg) {
  const ids = new Set((cfg.basemaps || []).map((item) => item.id))
  try {
    const stored = localStorage.getItem(BASEMAP_STORAGE_KEY)
    if (stored && ids.has(stored)) return stored
  } catch {
    /* ignore */
  }
  if (cfg.defaultBasemap && ids.has(cfg.defaultBasemap)) return cfg.defaultBasemap
  return cfg.basemaps[0]?.id || 'gaode'
}

function currentBasemap(id = basemapId.value) {
  return (basemaps.value || []).find((item) => item.id === id) || basemaps.value[0]
}

function applyBasemap(id) {
  const source = currentBasemap(id)
  if (!map || !source) return
  if (baseLayer) map.removeLayer(baseLayer)
  const options = { maxZoom: source.tileMaxZoom || 18, attribution: source.tileAttribution || '' }
  if (source.tileSubdomains) options.subdomains = source.tileSubdomains
  baseLayer = L.tileLayer(source.tileUrl, options).addTo(map)
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
  if (minLng >= maxLng) { minLng = -180; maxLng = 180 }
  if (minLat >= maxLat) { minLat = -90; maxLat = 90 }
  return { minLng, minLat, maxLng, maxLat }
}

function fmtTime(v) {
  const s = String(v || '')
  if (s.length < 14) return s
  return `${s.slice(0, 4)}-${s.slice(4, 6)}-${s.slice(6, 8)} ${s.slice(8, 10)}:${s.slice(10, 12)}:${s.slice(12, 14)}`
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (ch) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[ch]))
}

function popupHtml(item) {
  return [
    `<strong>${escapeHtml(item.jjbh || '-')}</strong>`,
    item.xzqhmc ? `<br/>行政区划：${escapeHtml(item.xzqhmc)}` : '',
    item.jqdz ? `<br/>警情地址：${escapeHtml(item.jqdz)}` : '',
    item.jjsj ? `<br/>接警时间：${escapeHtml(fmtTime(item.jjsj))}` : '',
    item.bjnr ? `<br/>报警内容：${escapeHtml(item.bjnr.length > 80 ? item.bjnr.slice(0, 80) + '…' : item.bjnr)}` : ''
  ].join('')
}

async function reload() {
  if (!map) return
  const b = queryBounds()
  const resp = await queryAlarmMapPoints({
    minLng: b.minLng,
    minLat: b.minLat,
    maxLng: b.maxLng,
    maxLat: b.maxLat,
    xzqhdm: xzqhdm.value.trim() || undefined
  })
  points.value = resp.data || []
  render()
}

function render() {
  if (layer) layer.remove()
  layer = L.layerGroup().addTo(map)
  points.value.forEach((item) => {
    if (item.lng == null || item.lat == null) return
    L.circleMarker([item.lat, item.lng], {
      radius: 7,
      color: '#dc2626',
      fillColor: '#ef4444',
      fillOpacity: 0.85,
      weight: 1
    }).bindPopup(popupHtml(item)).addTo(layer)
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
.map-wrap {
  position: relative;
  flex: 1;
  min-height: 360px;
}
.map { height: 100%; min-height: 360px; }
</style>

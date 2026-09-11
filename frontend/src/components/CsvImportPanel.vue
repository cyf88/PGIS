<template>
  <div>
    <el-alert :title="hint" type="info" show-icon :closable="false" class="mb" />
    <el-space wrap class="mb">
      <el-button type="primary" @click="fileInput?.click()">选择文件</el-button>
      <el-button @click="folderInput?.click()">选择文件夹</el-button>
      <el-button type="success" :disabled="!accepted.length" :loading="uploading" @click="submit">开始导入</el-button>
      <el-button @click="clearAll">清空列表</el-button>
    </el-space>
    <el-progress v-if="uploading" :percentage="uploadPercent" :status="uploadPhase === 'server' ? 'success' : undefined" class="mb" />
    <p v-if="uploading" class="hint">{{ uploadHint }}</p>
    <input ref="fileInput" type="file" accept=".csv" multiple hidden @change="onFiles" />
    <input ref="folderInput" type="file" webkitdirectory directory multiple hidden @change="onFiles" />

    <el-table :data="accepted" border size="small" class="mb" empty-text="请选择 csv 文件或文件夹">
      <el-table-column prop="name" label="文件名" min-width="260" />
      <el-table-column label="大小" width="120">
        <template #default="{ row }">{{ formatSize(row.size) }}</template>
      </el-table-column>
    </el-table>
    <p v-if="skippedLocal.length" class="skip">已跳过 {{ skippedLocal.length }} 个不符合前缀的文件</p>

    <el-card v-if="result" header="导入结果">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="新增">{{ result.inserted }}</el-descriptions-item>
        <el-descriptions-item label="覆盖">{{ result.updated }}</el-descriptions-item>
        <el-descriptions-item label="失败">{{ result.failed }}</el-descriptions-item>
      </el-descriptions>
      <h4>覆盖主键（最多展示 200 条）</h4>
      <el-table :data="keyRows" border size="small" max-height="240">
        <el-table-column type="index" width="60" />
        <el-table-column prop="key" label="xxzjbh" />
      </el-table>
      <h4>失败明细（最多展示 200 条）</h4>
      <el-table :data="result.failures || []" border size="small" max-height="240">
        <el-table-column prop="file" label="文件" min-width="160" />
        <el-table-column prop="line" label="行号" width="80" />
        <el-table-column prop="reason" label="原因" min-width="220" />
      </el-table>
      <h4>跳过的文件</h4>
      <el-table :data="result.skippedFiles || []" border size="small" max-height="180">
        <el-table-column prop="file" label="文件" min-width="160" />
        <el-table-column prop="reason" label="原因" min-width="220" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  prefix: { type: String, required: true },
  hint: { type: String, required: true },
  importer: { type: Function, required: true }
})

const fileInput = ref(null)
const folderInput = ref(null)
const accepted = ref([])
const skippedLocal = ref([])
const uploading = ref(false)
const uploadPercent = ref(0)
const uploadPhase = ref('idle')
const result = ref(null)

const keyRows = computed(() => (result.value?.updatedKeys || []).map((key) => ({ key })))
const uploadHint = computed(() => {
  if (!uploading.value) return ''
  if (uploadPhase.value === 'server') return '文件已上传，服务端正在解析入库，请等待（大文件可能需较长时间）'
  return `正在上传 ${uploadPercent.value}%`
})

function matches(file) {
  const name = (file.webkitRelativePath || file.name).split(/[/\\]/).pop()
  return name.toLowerCase().endsWith('.csv') && name.toLowerCase().startsWith(props.prefix.toLowerCase())
}

function onFiles(event) {
  const files = Array.from(event.target.files || [])
  const ok = []
  const skip = []
  files.forEach((f) => (matches(f) ? ok.push(f) : skip.push(f)))
  accepted.value = mergeFiles(accepted.value, ok)
  skippedLocal.value = skip
  event.target.value = ''
}

function mergeFiles(oldList, incoming) {
  const map = new Map()
  oldList.concat(incoming).forEach((f) => map.set(f.name + f.size, f))
  return Array.from(map.values())
}

function formatSize(n) {
  if (n < 1024) return n + ' B'
  if (n < 1024 * 1024) return (n / 1024).toFixed(1) + ' KB'
  if (n < 1024 * 1024 * 1024) return (n / 1024 / 1024).toFixed(1) + ' MB'
  return (n / 1024 / 1024 / 1024).toFixed(2) + ' GB'
}

async function submit() {
  if (!accepted.value.length) return
  uploading.value = true
  uploadPercent.value = 0
  uploadPhase.value = 'upload'
  result.value = null
  try {
    const resp = await props.importer(accepted.value, (ev) => {
      if (ev.total) {
        uploadPercent.value = Math.min(100, Math.round((ev.loaded / ev.total) * 100))
        if (ev.loaded >= ev.total) {
          uploadPhase.value = 'server'
        }
      }
    })
    result.value = resp.data
    ElMessage.success(`导入完成：新增 ${resp.data.inserted}，覆盖 ${resp.data.updated}，失败 ${resp.data.failed}`)
  } finally {
    uploading.value = false
  }
}

function clearAll() {
  accepted.value = []
  skippedLocal.value = []
  result.value = null
}
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.skip { color: #e6a23c; }
.hint { color: #909399; margin: 0 0 16px; }
</style>

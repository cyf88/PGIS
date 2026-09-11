<template>
  <div>
    <el-form :inline="true" @submit.prevent="search">
      <el-form-item label="数据来源" required>
        <el-input v-model="sjly" clearable placeholder="必填，6位区划精确匹配" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
      <el-form-item v-if="total !== null">
        <span class="total">设备总数：{{ total }}</span>
      </el-form-item>
    </el-form>
    <el-table :data="records" border stripe height="560" v-loading="loading" empty-text="请输入数据来源后查询">
      <el-table-column prop="sbbh" label="设备编号" min-width="140" />
      <el-table-column prop="jyxm" label="警员姓名" min-width="100" />
      <el-table-column prop="jybh" label="警员编号" min-width="110" />
      <el-table-column prop="sbsyzt" label="使用状态" width="90" />
      <el-table-column prop="sjly" label="数据来源" width="90" />
      <el-table-column prop="jd" label="最近经度" min-width="120" />
      <el-table-column prop="wd" label="最近纬度" min-width="120" />
      <el-table-column prop="sbsyjssj" label="使用结束时间" min-width="150" />
      <el-table-column prop="sbpp" label="品牌" min-width="100" />
      <el-table-column prop="sbxh" label="型号" min-width="100" />
      <el-table-column prop="xxzjbh" label="主键" min-width="160" />
    </el-table>
    <el-pagination
      class="pager"
      background
      layout="total, sizes, prev, pager, next"
      :total="total || 0"
      v-model:page-size="size"
      v-model:current-page="page"
      :page-sizes="[20, 50, 100]"
      @current-change="load"
      @size-change="load"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { countDeviceInfoBySjly, searchDeviceInfoBySjly } from '../api/http'

const sjly = ref('')
const records = ref([])
const total = ref(null)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

function search() {
  page.value = 1
  load()
}

async function load() {
  if (!sjly.value.trim()) {
    ElMessage.warning('请输入数据来源 sjly')
    return
  }
  loading.value = true
  try {
    const [pageResp, countResp] = await Promise.all([
      searchDeviceInfoBySjly({ sjly: sjly.value.trim(), page: page.value, size: size.value }),
      countDeviceInfoBySjly(sjly.value.trim())
    ])
    records.value = pageResp.data.records || []
    total.value = countResp.data ?? pageResp.data.total ?? 0
  } finally {
    loading.value = false
  }
}

function reset() {
  sjly.value = ''
  records.value = []
  total.value = null
  page.value = 1
}
</script>

<style scoped>
.pager { margin-top: 12px; justify-content: flex-end; }
.total { font-weight: 600; color: #303133; }
</style>

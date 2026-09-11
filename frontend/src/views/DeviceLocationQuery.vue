<template>
  <div>
    <el-form :inline="true" @submit.prevent="load">
      <el-form-item label="设备编号">
        <el-input v-model="query.sbbh" clearable placeholder="支持模糊" />
      </el-form-item>
      <el-form-item label="数据来源">
        <el-input v-model="query.sjly" clearable placeholder="6位区划" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="records" border stripe height="560" v-loading="loading">
      <el-table-column prop="sbbh" label="设备编号" min-width="160" />
      <el-table-column prop="jd" label="经度" min-width="130" />
      <el-table-column prop="wd" label="纬度" min-width="130" />
      <el-table-column prop="spsd" label="水平速度" width="100" />
      <el-table-column prop="czsd" label="垂直速度" width="100" />
      <el-table-column prop="hjj" label="航迹角" width="90" />
      <el-table-column prop="gc" label="高程" width="90" />
      <el-table-column prop="jd01" label="精度" width="80" />
      <el-table-column prop="sbsykssj" label="开始时间" min-width="140" />
      <el-table-column prop="sbsyjssj" label="结束时间" min-width="140" />
      <el-table-column prop="sjly" label="数据来源" width="90" />
      <el-table-column prop="xxzjbh" label="主键" min-width="220" />
    </el-table>
    <el-pagination
      class="pager"
      background
      layout="total, sizes, prev, pager, next"
      :total="total"
      v-model:page-size="size"
      v-model:current-page="page"
      :page-sizes="[20, 50, 100]"
      @current-change="load"
      @size-change="load"
    />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { queryDeviceLocation } from '../api/http'

const query = reactive({ sbbh: '', sjly: '' })
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const resp = await queryDeviceLocation({ ...query, page: page.value, size: size.value })
    records.value = resp.data.records || []
    total.value = resp.data.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.sbbh = ''
  query.sjly = ''
  page.value = 1
  load()
}

onMounted(load)
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>

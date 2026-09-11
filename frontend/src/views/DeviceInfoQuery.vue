<template>
  <div>
    <el-form :inline="true" @submit.prevent="load">
      <el-form-item label="设备编号">
        <el-input v-model="query.sbbh" clearable placeholder="支持模糊" />
      </el-form-item>
      <el-form-item label="警员姓名">
        <el-input v-model="query.jyxm" clearable placeholder="支持模糊" />
      </el-form-item>
      <el-form-item label="所属机构代码">
        <el-input v-model="query.ssgajgjgdm" clearable />
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
      <el-table-column prop="sbbh" label="设备编号" min-width="140" />
      <el-table-column prop="sbpp" label="品牌" min-width="100" />
      <el-table-column prop="sbxh" label="型号" min-width="100" />
      <el-table-column prop="zblx" label="装备类型" width="90" />
      <el-table-column prop="ssgajgjgdm" label="所属机构" min-width="140" />
      <el-table-column prop="cphm" label="车牌" min-width="100" />
      <el-table-column prop="jybh" label="警员编号" min-width="110" />
      <el-table-column prop="jyxm" label="警员姓名" min-width="100" />
      <el-table-column prop="lxdh" label="联系电话" min-width="120" />
      <el-table-column prop="sbsyzt" label="使用状态" width="90" />
      <el-table-column prop="sjly" label="数据来源" width="90" />
      <el-table-column prop="lrsj" label="录入时间" min-width="140" />
      <el-table-column prop="xxzjbh" label="主键" min-width="160" />
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
import { queryDeviceInfo } from '../api/http'

const query = reactive({ sbbh: '', jyxm: '', ssgajgjgdm: '', sjly: '' })
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const resp = await queryDeviceInfo({ ...query, page: page.value, size: size.value })
    records.value = resp.data.records || []
    total.value = resp.data.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.sbbh = ''
  query.jyxm = ''
  query.ssgajgjgdm = ''
  query.sjly = ''
  page.value = 1
  load()
}

onMounted(load)
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>

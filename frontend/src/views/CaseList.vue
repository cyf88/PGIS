<template>
  <div>
    <el-form :inline="true" @submit.prevent="load">
      <el-form-item label="案事件编号">
        <el-input v-model="query.asjbh" clearable placeholder="支持模糊" />
      </el-form-item>
      <el-form-item label="案件名称">
        <el-input v-model="query.ajmc" clearable placeholder="支持模糊" />
      </el-form-item>
      <el-form-item label="行政区划代码">
        <el-input v-model="query.ajfsdxzqhdm" clearable placeholder="前缀匹配" />
      </el-form-item>
      <el-form-item label="数据来源">
        <el-input v-model="query.sjly" clearable placeholder="精确匹配" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="records" border stripe height="560" v-loading="loading" @row-click="openDetail">
      <el-table-column prop="asjbh" label="案事件编号" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ajmc" label="案件名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="ajlymc" label="案件来源" min-width="100" show-overflow-tooltip />
      <el-table-column prop="ajfsdxzqhmc" label="行政区划" min-width="140" show-overflow-tooltip />
      <el-table-column label="受理时间" min-width="150">
        <template #default="{ row }">{{ fmtTime(row.slsj) }}</template>
      </el-table-column>
      <el-table-column label="立案日期" min-width="110">
        <template #default="{ row }">{{ fmtTime(row.larq) }}</template>
      </el-table-column>
      <el-table-column prop="badwmc" label="办案单位" min-width="180" show-overflow-tooltip />
      <el-table-column prop="zbrxm" label="主办人" min-width="90" />
      <el-table-column prop="jyaq" label="简要案情" min-width="240" show-overflow-tooltip />
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

    <el-drawer v-model="drawerVisible" :title="current?.ajmc || current?.asjbh || '案件详情'" size="620px">
      <el-descriptions :column="2" border>
        <el-descriptions-item v-for="f in detailFields" :key="f.prop" :label="f.label">
          {{ formatField(f.prop, current?.[f.prop]) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { queryCases } from '../api/http'

const query = reactive({ asjbh: '', ajmc: '', ajfsdxzqhdm: '', sjly: '' })
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const drawerVisible = ref(false)
const current = ref(null)

const detailFields = [
  { prop: 'sjhjAppid', label: '应用ID' },
  { prop: 'sjhjSbpcid', label: '上报批次ID' },
  { prop: 'sjhjSbsj', label: '上报时间' },
  { prop: 'sjhjJmms', label: '加密模式' },
  { prop: 'sjhjXfz', label: '下发值' },
  { prop: 'sjxfsj', label: '数据消费时间' },
  { prop: 'bjajbm', label: '部级案件编码' },
  { prop: 'asjbh', label: '案事件编号' },
  { prop: 'ajmc', label: '案件名称' },
  { prop: 'ajlydm', label: '案件来源代码' },
  { prop: 'ajlymc', label: '案件来源名称' },
  { prop: 'jjdbh', label: '接警单编号' },
  { prop: 'ysajdwmc', label: '移送案件单位名称' },
  { prop: 'ysajdwlxdm', label: '移送案件单位类型代码' },
  { prop: 'ysajdwlxmc', label: '移送案件单位类型名称' },
  { prop: 'jyaq', label: '简要案情' },
  { prop: 'fxasjsj', label: '发现案事件时间' },
  { prop: 'ajfskssj', label: '案件发生开始时间' },
  { prop: 'ajfsjssj', label: '案件发生结束时间' },
  { prop: 'zaxsdm', label: '作案形式代码' },
  { prop: 'zaxsmc', label: '作案形式名称' },
  { prop: 'zasjdm', label: '作案时机代码' },
  { prop: 'zasjmc', label: '作案时机名称' },
  { prop: 'zasjlbbcms', label: '作案时机类别补充描述' },
  { prop: 'ajfsdzdylbdm', label: '案件发生地址地域类别代码' },
  { prop: 'ajfsdzdylbmc', label: '案件发生地址地域类别名称' },
  { prop: 'ajfsdxzqhdm', label: '案件发生地行政区划代码' },
  { prop: 'ajfsdxzqhmc', label: '案件发生地行政区划名称' },
  { prop: 'ajfsdzjd', label: '案件发生地址经度' },
  { prop: 'ajfsdzwd', label: '案件发生地址维度' },
  { prop: 'ajfsdzmc', label: '案件发生地址名称' },
  { prop: 'sacslbdm', label: '涉案场所类别代码' },
  { prop: 'sacslbmc', label: '涉案场所类别名称' },
  { prop: 'sacslbbcms', label: '涉案场所类别补充描述' },
  { prop: 'ajfsdkjbwlbdm', label: '案件发生地空间部位类别代码' },
  { prop: 'ajfsdkjbwlbmc', label: '案件发生地空间部位类别名称' },
  { prop: 'ajfsdkjbwbcms', label: '案件发生地空间部位补充描述' },
  { prop: 'ajfsdfslc', label: '案件发生地发生楼层' },
  { prop: 'ajfsdjzwcs', label: '案件发生地建筑物层数' },
  { prop: 'ajfsdsfjzwn', label: '案件发生地是否建筑物内' },
  { prop: 'ajfsdsfzlzz', label: '案件发生地是否租赁住宅' },
  { prop: 'whcd', label: '危害程度' },
  { prop: 'zars', label: '作案人数' },
  { prop: 'ssrs', label: '受伤人数' },
  { prop: 'swrs', label: '死亡人数' },
  { prop: 'sscwjyqk', label: '损失财物简要情况' },
  { prop: 'ssjz', label: '损失价值' },
  { prop: 'sazz', label: '涉案总值' },
  { prop: 'sjcwjz', label: '收缴财物价值' },
  { prop: 'zatjzhms', label: '作案特征集合描述' },
  { prop: 'xkbh', label: '现勘编号' },
  { prop: 'gtfzxzdm', label: '共同犯罪性质代码' },
  { prop: 'gtfzxzmc', label: '共同犯罪性质名称' },
  { prop: 'swysdm', label: '涉外因素代码' },
  { prop: 'swysmc', label: '涉外因素名称' },
  { prop: 'sjgbhdqdm', label: '涉及国别或地区代码' },
  { prop: 'sjgbhdqmc', label: '涉及国别或地区名称' },
  { prop: 'swajswqk', label: '涉外案件涉外情况' },
  { prop: 'slsj', label: '受理时间' },
  { prop: 'sldwdm', label: '受理单位代码' },
  { prop: 'sldwmc', label: '受理单位名称' },
  { prop: 'larq', label: '立案日期' },
  { prop: 'badwdm', label: '办案单位代码' },
  { prop: 'badwmc', label: '办案单位名称' },
  { prop: 'zbrsfzh', label: '主办人身份证号' },
  { prop: 'zbrlxdh', label: '主办人联系电话' },
  { prop: 'zbrxm', label: '主办人姓名' },
  { prop: 'dbjbdm', label: '督办级别代码' },
  { prop: 'dbjbmc', label: '督办级别名称' },
  { prop: 'zczjzcxwyjms', label: '侦查终结侦查行为依据描述' },
  { prop: 'zczjsj', label: '侦查终结时间' },
  { prop: 'zczjdwdm', label: '侦查终结单位代码' },
  { prop: 'zczjdwmc', label: '侦查终结单位名称' },
  { prop: 'pafsdm', label: '破案方式代码' },
  { prop: 'pafsmc', label: '破案方式名称' },
  { prop: 'ysqsajscfjdmd', label: '移送起诉案件审查返回决定代码' },
  { prop: 'ysqsajscfjdmmc', label: '移送起诉案件审查返回决定名称' },
  { prop: 'zxzajsj', label: '转行政案件时间' },
  { prop: 'zxzajjsdwdm', label: '转行政案件接收单位代码' },
  { prop: 'zxzajjsdwmc', label: '转行政案件接收单位名称' },
  { prop: 'sjczlxdm', label: '数据操作类型代码' },
  { prop: 'sjly', label: '数据来源' },
  { prop: 'lrsj', label: '录入时间' },
  { prop: 'lrdwdm', label: '录入单位代码' },
  { prop: 'lrdwmc', label: '录入单位名称' },
  { prop: 'lrrxm', label: '录入人姓名' },
  { prop: 'lrrsfzh', label: '录入人身份证号' },
  { prop: 'xgsj', label: '修改时间' },
  { prop: 'xgdwdm', label: '修改单位代码' },
  { prop: 'xgdwmc', label: '修改单位名称' },
  { prop: 'xgrxm', label: '修改人姓名' },
  { prop: 'xgrsfzh', label: '修改人身份证号' },
  { prop: 'xxzjbh', label: '信息主键编号' },
  { prop: 'wtsjbh', label: '问题数据编号' },
  { prop: 'pt', label: '分区日期' }
]

const TIME_FIELDS = new Set(['sjhjSbsj', 'fxasjsj', 'ajfskssj', 'ajfsjssj', 'slsj', 'larq', 'zczjsj', 'zxzajsj', 'lrsj', 'xgsj'])

function fmtTime(v) {
  const s = String(v || '')
  if (s.length === 14) {
    return `${s.slice(0, 4)}-${s.slice(4, 6)}-${s.slice(6, 8)} ${s.slice(8, 10)}:${s.slice(10, 12)}:${s.slice(12, 14)}`
  }
  if (s.length === 8) {
    return `${s.slice(0, 4)}-${s.slice(4, 6)}-${s.slice(6, 8)}`
  }
  return s
}

function formatField(prop, value) {
  if (value == null || value === '') return '-'
  return TIME_FIELDS.has(prop) ? fmtTime(value) : String(value)
}

async function load() {
  loading.value = true
  try {
    const resp = await queryCases({ ...query, page: page.value, size: size.value })
    records.value = resp.data.records || []
    total.value = resp.data.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.asjbh = ''
  query.ajmc = ''
  query.ajfsdxzqhdm = ''
  query.sjly = ''
  page.value = 1
  load()
}

function openDetail(row) {
  current.value = row
  drawerVisible.value = true
}

onMounted(load)
</script>

<style scoped>
.pager { margin-top: 16px; justify-content: flex-end; }
</style>

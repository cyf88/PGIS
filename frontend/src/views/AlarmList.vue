<template>
  <div>
    <el-form :inline="true" @submit.prevent="load">
      <el-form-item label="接警编号">
        <el-input v-model="query.jjbh" clearable placeholder="支持模糊" />
      </el-form-item>
      <el-form-item label="行政区划代码">
        <el-input v-model="query.xzqhdm" clearable placeholder="前缀匹配" />
      </el-form-item>
      <el-form-item label="报警人名称">
        <el-input v-model="query.bjrxm" clearable placeholder="支持模糊" />
      </el-form-item>
      <el-form-item label="警情类别代码">
        <el-input v-model="query.jqlbdm" clearable placeholder="精确匹配" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="records" border stripe height="560" v-loading="loading" @row-click="openDetail">
      <el-table-column prop="jjbh" label="接警编号" min-width="220" show-overflow-tooltip />
      <el-table-column prop="xzqhmc" label="行政区划" min-width="120" show-overflow-tooltip />
      <el-table-column prop="jjlx" label="接警类型" width="90" />
      <el-table-column prop="jqlbdm" label="警情类别" width="90" />
      <el-table-column prop="jqlxdm" label="警情类型" width="90" />
      <el-table-column prop="bjrxm" label="报警人" min-width="100" show-overflow-tooltip />
      <el-table-column prop="lxdh" label="联系电话" min-width="120" />
      <el-table-column label="报警时间" min-width="160">
        <template #default="{ row }">{{ fmtTime(row.bjsj) }}</template>
      </el-table-column>
      <el-table-column label="接警时间" min-width="160">
        <template #default="{ row }">{{ fmtTime(row.jjsj) }}</template>
      </el-table-column>
      <el-table-column prop="jqdz" label="警情地址" min-width="180" show-overflow-tooltip />
      <el-table-column prop="bjnr" label="报警内容" min-width="220" show-overflow-tooltip />
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

    <el-drawer v-model="drawerVisible" :title="current?.jjbh || '报警详情'" size="620px">
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
import { queryAlarms } from '../api/http'

const query = reactive({ jjbh: '', xzqhdm: '', bjrxm: '', jqlbdm: '' })
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)
const loading = ref(false)

const drawerVisible = ref(false)
const current = ref(null)

const detailFields = [
  { prop: 'jjbh', label: '接警编号' },
  { prop: 'xzqhdm', label: '行政区划代码' },
  { prop: 'xzqhmc', label: '行政区划名称' },
  { prop: 'jjdwdm', label: '接警单位代码' },
  { prop: 'jjdwmc', label: '接警单位名称' },
  { prop: 'glzjjdbh', label: '关联主接警单编号' },
  { prop: 'jjlx', label: '接警类型' },
  { prop: 'jqlyfs', label: '警情来源方式' },
  { prop: 'lhlx', label: '来话类型' },
  { prop: 'jjlyh', label: '接警录音号' },
  { prop: 'jjybh', label: '接警员编号' },
  { prop: 'jjyxm', label: '接警员姓名' },
  { prop: 'bjsj', label: '报警时间' },
  { prop: 'jjsj', label: '接警时间' },
  { prop: 'jjwcsj', label: '接警完成时间' },
  { prop: 'bjdh', label: '报警电话' },
  { prop: 'bjdhyhm', label: '报警电话用户名' },
  { prop: 'bjdhyhdz', label: '报警电话用户地址' },
  { prop: 'bjrxm', label: '报警人名称' },
  { prop: 'bjrxbdm', label: '报警人性别代码' },
  { prop: 'lxdh', label: '联系电话' },
  { prop: 'bjrzjdm', label: '报警人证件代码' },
  { prop: 'bjrzjhm', label: '报警人证件号码' },
  { prop: 'bjdz', label: '报警地址' },
  { prop: 'jqdz', label: '警情地址' },
  { prop: 'bjnr', label: '报警内容' },
  { prop: 'jcjxtjsdwdm', label: '接处警系统建设单位代码' },
  { prop: 'jcjxtjsdwmc', label: '接处警系统建设单位名称' },
  { prop: 'gxdwdm', label: '管辖单位代码' },
  { prop: 'gxdwmc', label: '管辖单位名称' },
  { prop: 'jqlbdm', label: '警情类别代码' },
  { prop: 'jqlxdm', label: '警情类型代码' },
  { prop: 'jqxldm', label: '警情细类代码' },
  { prop: 'jqzldm', label: '警情子类代码' },
  { prop: 'shlddwdm', label: '社会联动单位代码' },
  { prop: 'tzdbh', label: '特征点编号' },
  { prop: 'zars', label: '作案人数' },
  { prop: 'ywcwq', label: '有无持武器' },
  { prop: 'ywwxwz', label: '有无危险物质' },
  { prop: 'ywbzxl', label: '有无爆炸/泄漏' },
  { prop: 'bkryqksm', label: '被困人员情况说明' },
  { prop: 'ssryqksm', label: '受伤人员情况说明' },
  { prop: 'swryqksm', label: '死亡人员情况说明' },
  { prop: 'sfswybj', label: '是否是外语报警' },
  { prop: 'bjrdwxzb', label: '报警人定位X坐标' },
  { prop: 'bjrdwyzb', label: '报警人定位Y坐标' },
  { prop: 'fxdwxzb', label: '反向定位X坐标' },
  { prop: 'fxdwyzb', label: '反向定位Y坐标' },
  { prop: 'bcjjnr', label: '补充接警内容' },
  { prop: 'jqdjdm', label: '警情等级代码' },
  { prop: 'jqclztdm', label: '警情处理状态代码' },
  { prop: 'yjjydjdm', label: '应急救援等级代码' },
  { prop: 'sjchpzldm', label: '涉及车号牌种类代码' },
  { prop: 'sjcph', label: '涉及车牌号' },
  { prop: 'sfswhcl', label: '是否是危化车辆' },
  { prop: 'rksj', label: '入库时间' },
  { prop: 'gxsj', label: '更新时间' },
  { prop: 'jqbq', label: '警情标签' },
  { prop: 'sfbm', label: '是否保密' },
  { prop: 'bjjjdbm', label: '部级接警单编码' },
  { prop: 'bjglzjjdbm', label: '部级关联主接警单编码' },
  { prop: 'mrsjc', label: '默认时间戳' },
  { prop: 'pt', label: '分区日期' }
]

const TIME_FIELDS = new Set(['bjsj', 'jjsj', 'jjwcsj', 'rksj'])

function fmtTime(v) {
  const s = String(v || '')
  if (s.length < 14) return s
  return `${s.slice(0, 4)}-${s.slice(4, 6)}-${s.slice(6, 8)} ${s.slice(8, 10)}:${s.slice(10, 12)}:${s.slice(12, 14)}`
}

function formatField(prop, value) {
  if (value == null || value === '') return '-'
  return TIME_FIELDS.has(prop) ? fmtTime(value) : String(value)
}

async function load() {
  loading.value = true
  try {
    const resp = await queryAlarms({ ...query, page: page.value, size: size.value })
    records.value = resp.data.records || []
    total.value = resp.data.total || 0
  } finally {
    loading.value = false
  }
}

function reset() {
  query.jjbh = ''
  query.xzqhdm = ''
  query.bjrxm = ''
  query.jqlbdm = ''
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

<template>
  <CsvImportPanel
    prefix="sb001"
    hint="本页仅导入文件名以 sb001 开头的 CSV，对应表 JYSB_SBXX_001。支持选择单个或多个文件，也可选择文件夹批量导入。主键冲突将覆盖已有数据。"
    :importer="importDeviceInfo"
  />
</template>

<script setup>
import CsvImportPanel from '../components/CsvImportPanel.vue'
import { importDeviceInfo } from '../api/http'
</script>

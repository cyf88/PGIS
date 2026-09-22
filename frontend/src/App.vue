<template>
  <router-view />
</template>

<script setup>
</script>

<style>
html, body, #app {
  margin: 0;
  height: 100%;
  font-family: "Microsoft YaHei", sans-serif;
}
/* 独立地图窗口：覆盖地图页在主布局中的高度与负边距，铺满整个视口 */
html.standalone-map .map-page {
  height: 100vh;
  margin: 0;
}
</style>

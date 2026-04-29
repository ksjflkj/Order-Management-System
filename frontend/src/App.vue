<template>
  <router-view/>
  <!-- 智能客服悬浮组件（全局常驻） -->
  <CustomerServiceWidget />
</template>

<script setup>
import CustomerServiceWidget from '@/components/CustomerServiceWidget.vue'
</script>

<style>
:root {
  --el-color-primary: #FFC300;
  --el-color-primary-light-3: #FFD44D;
  --el-color-primary-light-5: #FFE180;
  --el-color-primary-light-7: #FFEDB3;
  --el-color-primary-light-8: #FFF3CC;
  --el-color-primary-light-9: #FFF9E6;
  --el-color-primary-dark-2: #E6B000;
}

.el-button--primary {
  --el-button-text-color: #222222 !important;
  --el-button-hover-text-color: #222222 !important;
  font-weight: bold;
}

html, body, #app {
  margin: 0 !important;
  padding: 0 !important;
  width: 100%;
  height: 100%;
}
</style>

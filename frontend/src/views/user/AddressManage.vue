<template>
  <div class="address-manage-container">
    <router-view />
  </div>
</template>

<script setup>
</script>

<style scoped>
.address-manage-container {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  flex-direction: column;
}
</style>
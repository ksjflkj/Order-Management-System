<template>
  <div class="user-settings">
    <router-view />
  </div>
</template>

<script setup>
</script>

<style scoped>
.user-settings {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: #f5f7fa;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
</style>

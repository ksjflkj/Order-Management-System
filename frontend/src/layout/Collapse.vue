<template>
    <el-icon @click="toggleCollapse" class="collapse-icon">
        <Fold v-if="!isCollapse" />
        <Expand v-else />
    </el-icon>
</template>

<script setup lang="ts">
import { inject } from 'vue'
import { Fold, Expand } from '@element-plus/icons-vue'

const isCollapse = inject('isCollapse')
const toggleCollapse = inject('toggleCollapse') as () => void
</script>

<style scoped>
.collapse-icon {
    font-size: 22px;
    cursor: pointer;
    margin-right: 15px;
}
</style>

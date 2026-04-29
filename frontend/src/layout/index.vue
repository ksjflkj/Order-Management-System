<template>
    <el-container class="container">
        <el-aside width="auto" class="aside">
            <Menu></Menu>
        </el-aside>
        <el-container>
            <el-header class="header">
                <Collapse></Collapse>
                <BreadCrumb></BreadCrumb>
            </el-header>
            <el-main class="main">
                <router-view></router-view>
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup lang="ts">
import Menu from './Menu.vue';
import Collapse from './Collapse.vue';
import BreadCrumb from './BreadCrumb.vue';
import { ref, provide } from 'vue';

const isCollapse = ref(false);
const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value;
};

provide('isCollapse', isCollapse);
provide('toggleCollapse', toggleCollapse);
</script>

<style lang="scss" scoped>
.container {
    height: 100vh;

    .aside {
        background-color: #222222;
        display: flex;
        flex-direction: column;
        transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
        overflow-x: hidden;
        box-shadow: 2px 0 8px rgba(0,0,0,0.15);
        z-index: 10;
    }

    .header {
        background-color: #FFC300;
        display: flex;
        align-items: center;
        color: #222222;
        box-shadow: 0 1px 6px rgba(0,0,0,0.1);
        z-index: 5;
    }

    .main {
        background-color: #f5f7fa;
        padding: 0; /* Let inner views handle padding for true full-width components */
        overflow: auto;
    }
}
</style>

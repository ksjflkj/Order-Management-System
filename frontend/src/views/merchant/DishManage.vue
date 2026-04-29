<template>
    <div class="page-container">
        <div class="page-header">
            <div class="header-content">
                <h2 class="page-title">菜品管理</h2>
                <p class="page-desc">管理您的店铺菜品、库存、价格及分类信息</p>
            </div>
        </div>

        <el-card class="main-card" shadow="never">
            <div class="toolbar">
                <div class="filter-group">
                    <el-select v-model="query.merchantId" placeholder="选择店铺" class="filter-item shop-select" @change="handleSearch">
                        <template #prefix>
                            <span class="prefix-icon">🏪</span>
                        </template>
                        <el-option v-for="shop in shops" :key="shop.id" :label="shop.shopName" :value="shop.id" />
                    </el-select>

                    <el-input v-model="query.name" placeholder="搜索菜品名称..." clearable class="filter-item search-input" @keyup.enter="handleSearch">
                        <template #prefix>
                            <span class="prefix-icon">🔍</span>
                        </template>
                    </el-input>

                    <el-select v-model="query.status" placeholder="状态筛选" clearable class="filter-item status-select" @change="handleSearch">
                        <el-option label="🟢 已上架" :value="1" />
                        <el-option label="🔴 已下架" :value="0" />
                    </el-select>

                    <el-button type="primary" class="search-btn" @click="handleSearch">
                        查询
                    </el-button>
                </div>

                <div class="action-group">
                    <el-button @click="openCategoryManage" class="cat-btn" plain>
                        📁 分类管理
                    </el-button>
                    <el-button type="primary" class="add-btn" @click="openAdd">
                        ➕ 新增菜品
                    </el-button>
                </div>
            </div>

            <el-table :data="tableData" v-loading="loading" class="data-table"
                      :header-cell-style="{ background: '#f8fafc', color: '#475569', fontWeight: '600', height: '54px', borderBottom: '1px solid #e2e8f0' }"
                      :row-style="{ height: '76px' }">
                <!-- 菜品信息列 -->
                <el-table-column label="菜品信息" min-width="260">
                    <template #default="{ row }">
                        <div class="dish-info-cell">
                            <el-image v-if="row.image" :src="row.image" :preview-src-list="[row.image]" preview-teleported
                                class="dish-image" fit="cover">
                                <template #error>
                                    <div class="image-slot">
                                        <span class="placeholder-icon">🍲</span>
                                    </div>
                                </template>
                            </el-image>
                            <div v-else class="dish-image placeholder-img">
                                <span class="placeholder-icon">🍲</span>
                            </div>
                            <div class="dish-details">
                                <span class="dish-name">{{ row.name }}</span>
                                <el-tag size="small" type="info" class="cat-tag" effect="plain" round>{{ getCategoryName(row.categoryId) }}</el-tag>
                            </div>
                        </div>
                    </template>
                </el-table-column>

                <!-- 价格列 -->
                <el-table-column label="价格" width="140" align="right">
                    <template #default="{ row }">
                        <div class="price-cell">
                            <span class="current-price"><span class="price-symbol">¥</span>{{ row.price }}</span>
                            <span v-if="row.originalPrice" class="original-price">¥{{ row.originalPrice }}</span>
                        </div>
                    </template>
                </el-table-column>

                <!-- 描述列 -->
                <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip>
                    <template #default="{ row }">
                        <span class="desc-text">{{ row.description || '-' }}</span>
                    </template>
                </el-table-column>

                <!-- 数据列 -->
                <el-table-column label="数据" width="140" align="center">
                    <template #default="{ row }">
                        <div class="stats-cell">
                            <div class="stat-item" :title="'当前库存: ' + row.stock">
                                <span class="stat-icon">📦</span>
                                <span :class="['stat-val', row.stock <= 10 ? 'low-stock' : '']">{{ row.stock }}</span>
                            </div>
                            <div class="stat-item" :title="'累计销量: ' + row.sales">
                                <span class="stat-icon">🔥</span>
                                <span class="stat-val sales-color">{{ row.sales }}</span>
                            </div>
                        </div>
                    </template>
                </el-table-column>

                <!-- 状态列 -->
                <el-table-column label="状态" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="light" class="status-tag">
                            <span class="status-dot" :class="row.status === 1 ? 'dot-success' : 'dot-danger'"></span>
                            {{ row.status === 1 ? '上架' : '下架' }}
                        </el-tag>
                    </template>
                </el-table-column>

                <!-- 操作列 -->
                <el-table-column label="操作" width="160" align="center" fixed="right">
                    <template #default="{ row }">
                        <div class="row-actions">
                            <el-button link type="primary" class="action-link" @click="openEdit(row)">
                                编辑
                            </el-button>
                            <el-divider direction="vertical" class="action-divider" />
                            <el-button v-if="row.status === 1" link type="warning" class="action-link" @click="handleStatus(row.id, 0)">
                                下架
                            </el-button>
                            <el-button v-if="row.status === 0" link type="success" class="action-link" @click="handleStatus(row.id, 1)">
                                上架
                            </el-button>
                        </div>
                    </template>
                </el-table-column>
                
                <template #empty>
                    <div class="empty-state">
                        <span class="empty-icon">🍽️</span>
                        <p class="empty-text">暂无菜品数据</p>
                    </div>
                </template>
            </el-table>

            <div class="pagination-container">
                <el-pagination 
                    v-model:current-page="query.current"
                    v-model:page-size="query.size"
                    :page-sizes="[10, 20, 50]"
                    background 
                    layout="total, sizes, prev, pager, next, jumper" 
                    :total="total" 
                    @size-change="loadData"
                    @current-change="handlePageChange" />
            </div>
        </el-card>

        <!-- 表单弹窗 -->
        <el-dialog v-model="showForm" :title="form.id ? '编辑菜品' : '新增菜品'" width="580px" destroy-on-close class="custom-dialog" :close-on-click-modal="false">
            <el-form ref="dishFormRef" :model="form" :rules="rules" label-width="90px" label-position="left" class="dish-form">
                <el-form-item label="归属店铺" prop="merchantId">
                    <el-select v-model="form.merchantId" placeholder="请选择店铺" class="full-width" disabled>
                        <el-option v-for="shop in shops" :key="shop.id" :label="shop.shopName" :value="shop.id" />
                    </el-select>
                </el-form-item>

                <div class="form-row">
                    <el-form-item label="菜品名称" prop="name" class="flex-1">
                        <el-input v-model="form.name" placeholder="请输入菜品名称" />
                    </el-form-item>
                    <el-form-item label="所属分类" prop="categoryId" class="flex-1">
                        <el-select v-model="form.categoryId" placeholder="请选择菜品分类" class="full-width" clearable>
                            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
                        </el-select>
                    </el-form-item>
                </div>

                <el-form-item label="菜品图片" prop="image">
                    <div class="dish-upload-row">
                        <el-input v-model="form.image" placeholder="请输入图片 URL 链接" style="flex:1" />
                        <el-upload
                            :show-file-list="false"
                            accept="image/*"
                            :before-upload="handleDishImageUpload"
                        >
                            <el-button type="primary" plain :loading="uploading">
                                <el-icon><UploadFilled /></el-icon>&nbsp;上传图片
                            </el-button>
                        </el-upload>
                    </div>

                    <!-- 实时预览 -->
                    <div class="dish-preview-area">
                        <div v-if="form.image" class="dish-preview-wrapper">
                            <img
                                :src="resolveImageUrl(form.image)"
                                class="dish-preview-img"
                                alt="预览"
                                @load="previewOk = true; previewFail = false"
                                @error="previewFail = true; previewOk = false"
                            />
                            <div v-if="previewOk" class="dish-preview-badge ok">✔ 可正常显示</div>
                            <div v-if="previewFail" class="dish-preview-badge fail">⚠️ 无法显示</div>
                        </div>
                        <div v-else class="dish-preview-placeholder">
                            <span style="font-size:28px">🍲</span>
                            <span>输入地址或上传后自动预览</span>
                        </div>
                    </div>
                </el-form-item>

                <div class="form-row">
                    <el-form-item label="当前售价" prop="price" class="flex-1">
                        <el-input-number v-model="form.price" :min="0" :precision="2" controls-position="right" class="full-width" />
                    </el-form-item>
                    <el-form-item label="原始价格" prop="originalPrice" class="flex-1">
                        <el-input-number v-model="form.originalPrice" :min="0" :precision="2" controls-position="right" class="full-width" />
                    </el-form-item>
                </div>

                <el-form-item label="当前库存" prop="stock">
                    <el-input-number v-model="form.stock" :min="0" :step="1" controls-position="right" style="width: 50%" />
                </el-form-item>

                <el-form-item label="菜品描述" prop="description">
                    <el-input v-model="form.description" type="textarea" :rows="3" placeholder="添加一些诱人的菜品描述..." resize="none" />
                </el-form-item>
            </el-form>

            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="closeForm" class="cancel-btn">取消</el-button>
                    <el-button type="primary" @click="handleSave" class="save-btn" :loading="saving">保存菜品</el-button>
                </div>
            </template>
        </el-dialog>

        <!-- 分类管理弹窗 -->
        <el-dialog v-model="showCategoryDialog" title="菜品分类管理" width="600px" destroy-on-close class="custom-dialog">
            <div class="category-toolbar">
                <el-input v-model="categoryForm.name" placeholder="输入分类名称" class="cat-input" />
                <el-input-number v-model="categoryForm.sortOrder" :min="0" controls-position="right" placeholder="排序号" class="cat-sort" />
                <el-button type="primary" @click="handleSaveCategory" class="cat-save-btn">
                    {{ categoryForm.id ? '更新分类' : '添加分类' }}
                </el-button>
                <el-button v-if="categoryForm.id" @click="resetCategoryForm" plain>取消编辑</el-button>
            </div>

            <el-table :data="categories" border class="category-table" 
                      :header-cell-style="{ background: '#f8fafc', color: '#475569' }">
                <el-table-column prop="name" label="分类名称" />
                <el-table-column prop="sortOrder" label="排序权重" width="100" align="center">
                    <template #default="{ row }">
                        <el-tag type="info" size="small" effect="plain">{{ row.sortOrder }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="160" align="center">
                    <template #default="{ row }">
                        <el-button link type="primary" @click="editCategory(row)">编辑</el-button>
                        <el-divider direction="vertical" />
                        <el-button link type="danger" @click="handleDeleteCategory(row.id)">删除</el-button>
                    </template>
                </el-table-column>
                
                <template #empty>
                    <div class="empty-state-mini">
                        <span class="empty-icon-mini">📂</span>
                        <span>暂无分类</span>
                    </div>
                </template>
            </el-table>
        </el-dialog>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDishPage, saveDish, updateDishStatus, getMyMerchants, getDishCategoryList, saveDishCategory, deleteDishCategory } from '@/api/dish'
import { getToken } from '@/utils/auth'
import { UploadFilled } from '@element-plus/icons-vue'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const uploading = ref(false)
const previewOk = ref(false)
const previewFail = ref(false)
const showForm = ref(false)
const dishFormRef = ref(null)

const resolveImageUrl = (url) => {
    if (!url) return ''
    if (url.startsWith('/static/')) return 'http://localhost:8080' + url
    return url
}

const handleDishImageUpload = async (file) => {
    uploading.value = true
    try {
        const fd = new FormData()
        fd.append('file', file)
        const res = await fetch('http://localhost:8080/api/upload/image', {
            method: 'POST',
            headers: { Authorization: `Bearer ${getToken()}` },
            body: fd
        })
        const data = await res.json()
        if (data.code === 200) {
            form.image = 'http://localhost:8080' + data.data
            previewOk.value = true
            previewFail.value = false
            ElMessage.success('图片上传成功')
        } else {
            ElMessage.error(data.message || '上传失败')
        }
    } catch {
        ElMessage.error('上传失败，请检查网络')
    } finally {
        uploading.value = false
    }
    return false
}

const rules = reactive({
    merchantId: [{ required: true, message: '请选择店铺', trigger: 'change' }],
    name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
    price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
    stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
})

const shops = ref([])
const categories = ref([])

const query = reactive({
    merchantId: null,
    name: '',
    status: null,
    current: 1,
    size: 10
})

const form = reactive({
    id: null,
    merchantId: null,
    categoryId: null,
    name: '',
    image: '',
    price: 0,
    originalPrice: null,
    stock: 0,
    description: ''
})

const showCategoryDialog = ref(false)
const categoryForm = reactive({
    id: null,
    merchantId: null,
    name: '',
    sortOrder: 0
})

const getCategoryName = (id) => {
    if (!id) return '未分类'
    const cat = categories.value.find(c => c.id === id)
    return cat ? cat.name : '未知分类'
}

const resetForm = () => {
    form.id = null
    form.merchantId = query.merchantId
    form.categoryId = null
    form.name = ''
    form.image = ''
    form.price = 0
    form.originalPrice = null
    form.stock = 0
    form.description = ''
}

const resetCategoryForm = () => {
    categoryForm.id = null
    categoryForm.name = ''
    categoryForm.sortOrder = 0
}

// 加载店铺列表
const loadShops = async () => {
    try {
        const res = await getMyMerchants()
        if (res.code === 200) {
            shops.value = res.data || []
            if (shops.value.length > 0) {
                query.merchantId = shops.value[0].id
                form.merchantId = query.merchantId
                handleSearch()
            }
        }
    } catch (error) {
        ElMessage.error('加载店铺失败')
    }
}

const loadCategories = async () => {
    if (!query.merchantId) return
    try {
        const res = await getDishCategoryList({ merchantId: query.merchantId })
        if (res.code === 200) {
            categories.value = res.data || []
        }
    } catch (error) {
        ElMessage.error('加载分类失败')
    }
}

const loadData = async () => {
    if (!query.merchantId) {
        tableData.value = []
        total.value = 0
        return
    }

    loading.value = true
    try {
        const res = await getDishPage({ ...query })
        if (res.code === 200) {
            tableData.value = res.data.records || []
            total.value = res.data.total || 0
        } else {
            ElMessage.error(res.message || '查询失败')
        }
    } catch (error) {
        ElMessage.error(error.message || '查询请求失败')
    } finally {
        loading.value = false
    }
}

const openAdd = () => {
    if (!query.merchantId) {
        ElMessage.warning('请先选择店铺')
        return
    }
    resetForm()
    form.merchantId = query.merchantId
    showForm.value = true
}

const openEdit = (item) => {
    form.id = item.id
    form.merchantId = item.merchantId
    form.categoryId = item.categoryId
    form.name = item.name
    form.image = item.image
    form.price = Number(item.price)
    form.originalPrice = item.originalPrice == null ? null : Number(item.originalPrice)
    form.stock = item.stock
    form.description = item.description
    showForm.value = true
}

const closeForm = () => {
    showForm.value = false
    resetForm()
}

const handleSave = async () => {
    if (!dishFormRef.value) return
    try {
        const valid = await dishFormRef.value.validate()
        if (!valid) return
        saving.value = true
        const payload = {
            ...form,
            originalPrice: form.originalPrice === null ? null : Number(form.originalPrice)
        }
        const res = await saveDish(payload)
        if (res.code === 200) {
            ElMessage.success(res.message || '保存成功')
            closeForm()
            loadData()
        } else {
            ElMessage.error(res.message || '保存失败')
        }
    } catch (error) {
        if (error !== false) {
            ElMessage.error(error.message || '保存失败')
        }
    } finally {
        saving.value = false
    }
}

const handleStatus = async (id, status) => {
    try {
        const res = await updateDishStatus({ id, status })
        if (res.code === 200) {
            ElMessage.success(status === 1 ? '上架成功' : '下架成功')
            loadData()
        } else {
            ElMessage.error(res.message || '状态更新失败')
        }
    } catch (error) {
        ElMessage.error(error.message || '状态更新失败')
    }
}

const handlePageChange = (page) => {
    query.current = page
    loadData()
}

const handleSearch = () => {
    query.current = 1
    loadCategories()
    loadData()
}

const openCategoryManage = () => {
    if (!query.merchantId) {
        ElMessage.warning('请先选择店铺')
        return
    }
    resetCategoryForm()
    categoryForm.merchantId = query.merchantId
    showCategoryDialog.value = true
}

const editCategory = (cat) => {
    categoryForm.id = cat.id
    categoryForm.merchantId = cat.merchantId
    categoryForm.name = cat.name
    categoryForm.sortOrder = cat.sortOrder
}

const handleSaveCategory = async () => {
    if (!categoryForm.name) {
        ElMessage.warning('请输入分类名称')
        return
    }
    try {
        const res = await saveDishCategory(categoryForm)
        if (res.code === 200) {
            ElMessage.success(categoryForm.id ? '更新分类成功' : '添加分类成功')
            resetCategoryForm()
            categoryForm.merchantId = query.merchantId
            loadCategories()
        } else {
            ElMessage.error(res.message || '保存失败')
        }
    } catch (error) {
        ElMessage.error('保存失败')
    }
}

const handleDeleteCategory = (id) => {
    ElMessageBox.confirm('这会导致该分类下的菜品没有分类，确认删除?', '删除警告', {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
    }).then(async () => {
        try {
            const res = await deleteDishCategory(id)
            if (res.code === 200) {
                ElMessage.success('删除成功')
                loadCategories()
            } else {
                ElMessage.error(res.message || '删除失败')
            }
        } catch (error) {
            ElMessage.error('删除失败')
        }
    }).catch(() => {})
}

onMounted(() => {
    loadShops()
})
</script>

<style scoped>
.page-container {
    padding: 24px;
    background-color: #f1f5f9;
    min-height: calc(100vh - 84px);
    box-sizing: border-box;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.page-header {
    margin-bottom: 20px;
}

.header-content h2 {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 600;
    color: #1e293b;
}

.page-desc {
    margin: 0;
    font-size: 14px;
    color: #64748b;
}

.main-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
}

.toolbar {
    display: flex;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 20px;
}

.filter-group {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
    align-items: center;
}

.action-group {
    display: flex;
    gap: 12px;
    align-items: center;
}

.filter-item {
    width: 200px;
}

.shop-select { width: 180px; }
.search-input { width: 240px; }
.status-select { width: 140px; }

:deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: 0 0 0 1px #e2e8f0 inset;
}

:deep(.el-input__wrapper:hover) {
    box-shadow: 0 0 0 1px #cbd5e1 inset;
}

:deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.5) inset !important;
}

.search-btn, .add-btn, .cat-btn {
    border-radius: 8px;
    font-weight: 500;
    padding: 8px 16px;
    height: 34px;
    border: none;
}

.search-btn {
    background: #3b82f6;
    color: white;
}

.search-btn:hover {
    background: #2563eb;
}

.add-btn {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
    box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
}

.add-btn:hover {
    background: linear-gradient(135deg, #059669 0%, #047857 100%);
    box-shadow: 0 6px 14px rgba(16, 185, 129, 0.3);
}

.cat-btn {
    color: #6366f1;
    background: #eef2ff;
    border: 1px solid #c7d2fe;
}

.cat-btn:hover {
    background: #e0e7ff;
    border-color: #a5b4fc;
}

/* Data Table styling */
.data-table {
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
}

.dish-info-cell {
    display: flex;
    align-items: center;
    gap: 16px;
}

.dish-image {
    width: 56px;
    height: 56px;
    border-radius: 10px;
    object-fit: cover;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    background-color: #f1f5f9;
    flex-shrink: 0;
    border: 1px solid #f1f5f9;
}

.placeholder-img {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
}

.image-slot {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: #f1f5f9;
    color: #cbd5e1;
}

.placeholder-icon {
    font-size: 24px;
}

.dish-details {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.dish-name {
    font-weight: 600;
    color: #1e293b;
    font-size: 15px;
    line-height: 1.2;
}

.cat-tag {
    align-self: flex-start;
    border: none;
    background-color: #f1f5f9;
    color: #64748b;
    padding: 0 8px;
    height: 22px;
}

.price-cell {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
}

.current-price {
    font-size: 18px;
    font-weight: 700;
    color: #ef4444;
}

.price-symbol {
    font-size: 14px;
    margin-right: 2px;
}

.original-price {
    font-size: 13px;
    color: #94a3b8;
    text-decoration: line-through;
}

.desc-text {
    color: #64748b;
    font-size: 13px;
    line-height: 1.5;
}

.stats-cell {
    display: flex;
    flex-direction: column;
    gap: 6px;
    align-items: center;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #475569;
}

.stat-icon {
    font-size: 14px;
}

.stat-val {
    font-weight: 600;
}

.sales-color {
    color: #f59e0b;
}

.low-stock {
    color: #ef4444;
}

.status-tag {
    border: none;
    font-weight: 500;
    padding: 0 12px;
    border-radius: 20px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
}

.status-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
}

.dot-success { background-color: #10b981; }
.dot-danger { background-color: #ef4444; }

.row-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
}

.action-link {
    font-weight: 500;
}

.action-divider {
    margin: 0 4px;
    border-color: #e2e8f0;
}

.empty-state, .empty-state-mini {
    padding: 40px 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #94a3b8;
}

.empty-state-mini { padding: 20px 0; }

.empty-icon { font-size: 48px; margin-bottom: 12px; opacity: 0.5; }
.empty-icon-mini { font-size: 24px; margin-bottom: 8px; opacity: 0.6; }

.empty-text { font-size: 15px; }

.pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
    align-items: center;
}

/* Dialog Styling */
:deep(.custom-dialog) {
    border-radius: 12px;
    overflow: hidden;
}

:deep(.custom-dialog .el-dialog__header) {
    margin-right: 0;
    padding: 20px 24px;
    border-bottom: 1px solid #f1f5f9;
    font-weight: 600;
}

:deep(.custom-dialog .el-dialog__body) {
    padding: 24px;
}

:deep(.custom-dialog .el-dialog__footer) {
    padding: 16px 24px;
    border-top: 1px solid #f1f5f9;
}

.full-width { width: 100%; }

.form-row {
    display: flex;
    gap: 20px;
}

.flex-1 { flex: 1; }

/* 菜品图片上传行 */
.dish-upload-row {
    display: flex;
    gap: 10px;
    align-items: center;
    width: 100%;
}

/* 菜品图片预览 */
.dish-preview-area {
    margin-top: 10px;
}
.dish-preview-wrapper {
    position: relative;
    display: inline-block;
}
.dish-preview-img {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 8px;
    border: 2px solid #e2e8f0;
    display: block;
}
.dish-preview-badge {
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
    white-space: nowrap;
    font-size: 11px;
    padding: 2px 8px;
    border-radius: 999px;
    font-weight: 600;
}
.dish-preview-badge.ok  { background:#f0f9eb; color:#67c23a; border:1px solid #b3e19d; }
.dish-preview-badge.fail{ background:#fef0f0; color:#f56c6c; border:1px solid #fbc4c4; }
.dish-preview-placeholder {
    width: 100px;
    height: 100px;
    border-radius: 8px;
    border: 2px dashed #e2e8f0;
    background: #f8fafc;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    color: #94a3b8;
    font-size: 12px;
}

.dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
}

.cancel-btn, .save-btn {
    border-radius: 6px;
    padding: 8px 20px;
}

.category-toolbar {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    align-items: center;
}

.cat-input { flex: 1; }
.cat-sort { width: 130px; }

.cat-save-btn {
    background: #8b5cf6;
    border-color: #8b5cf6;
}

.cat-save-btn:hover {
    background: #7c3aed;
    border-color: #7c3aed;
}

.category-table {
    border-radius: 8px;
    overflow: hidden;
}

:deep(.el-form-item__label) {
    font-weight: 500;
    color: #475569;
}
</style>
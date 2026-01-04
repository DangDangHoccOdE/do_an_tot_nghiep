<template>
    <div class="grid">
        <SectionCard eyebrow="Quản lý" title="Khách hàng">
            <template #actions>
                <div class="action-row">
                    <el-input v-model="customerSearch" placeholder="Tìm kiếm..." size="small" class="search-input"
                        clearable />
                    <el-button type="primary" size="small" @click="goCreate">{{ t('admin.actions.add') }}</el-button>
                    <el-button size="small" @click="fetchCustomers()">{{ t('admin.actions.refresh') }}</el-button>
                </div>
            </template>

            <div class="meta-row" v-if="customerStats.total">
                <div class="pill">
                    <span class="pill-label">Tổng khách hàng</span>
                    <strong>{{ customerStats.total }}</strong>
                </div>
            </div>

            <el-table :data="filteredCustomers" stripe :empty-text="t('admin.empty')" style="width: 100%">
                <el-table-column label="Avatar" width="80">
                    <template #default="scope">
                        <div class="avatar-cell" v-if="scope.row.avatar">
                            <img :src="scope.row.avatar" :alt="scope.row.firstName" class="avatar-img" />
                        </div>
                        <div class="avatar-cell empty" v-else>
                            --
                        </div>
                    </template>
                </el-table-column>
                <el-table-column :label="t('admin.table.name')" min-width="180">
                    <template #default="scope">
                        <div class="title-col">
                            <span class="title">{{ scope.row.firstName }} {{ scope.row.lastName }}</span>
                            <span class="subtitle">{{ scope.row.email }}</span>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="Email" min-width="180">
                    <template #default="scope">
                        {{ scope.row.email }}
                    </template>
                </el-table-column>
                <el-table-column label="Số điện thoại" width="140">
                    <template #default="scope">
                        {{ scope.row.phone || '--' }}
                    </template>
                </el-table-column>
                <el-table-column label="Trạng thái" width="120">
                    <template #default="scope">
                        <el-tag :type="scope.row.deleteFlag ? 'danger' : 'success'" effect="dark" size="small">
                            {{ scope.row.deleteFlag ? 'Vô hiệu' : 'Hoạt động' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column width="200" :label="t('admin.actions.view')">
                    <template #default="scope">
                        <el-space wrap size="4">
                            <el-button text size="small" type="primary" @click="goEdit(scope.row.id)">
                                {{ t('admin.actions.edit') }}
                            </el-button>
                            <el-popconfirm :title="t('admin.confirm.deleteMessage')"
                                @confirm="deleteCustomer(scope.row.id)">
                                <template #reference>
                                    <el-button text size="small" type="danger">{{ t('admin.actions.delete')
                                        }}</el-button>
                                </template>
                            </el-popconfirm>
                        </el-space>
                    </template>
                </el-table-column>
            </el-table>
            <div class="pagination">
                <el-pagination background layout="prev, pager, next" :current-page="customerPage.page"
                    :page-size="customerPage.size" :total="customerPage.total" @current-change="handleCustomerPage" />
            </div>
        </SectionCard>
    </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiUsers } from '@/services/apiUsers'

const { t } = useI18n()
const router = useRouter()

const customers = ref([])
const customerSearch = ref('')
const isLoading = ref(false)

const customerPage = reactive({
    page: 1,
    size: 10,
    total: 0
})

onMounted(() => {
    fetchCustomers()
})

const fetchCustomers = async () => {
    isLoading.value = true
    try {
        const params = {
            page: customerPage.page - 1,
            size: customerPage.size
        }
        const data = await apiUsers.listCustomers(params)
        customers.value = data.data?.content || []
        customerPage.total = data.data?.totalElements || 0
    } catch (error) {
        console.error('Failed to fetch customers:', error)
        ElMessage.error('Lỗi khi tải danh sách khách hàng')
    } finally {
        isLoading.value = false
    }
}

const filteredCustomers = computed(() => {
    let result = customers.value

    if (customerSearch.value) {
        const search = customerSearch.value.toLowerCase()
        result = result.filter(c =>
            c.firstName?.toLowerCase().includes(search) ||
            c.lastName?.toLowerCase().includes(search) ||
            c.email?.toLowerCase().includes(search) ||
            c.phone?.includes(search)
        )
    }

    return result
})

const customerStats = computed(() => {
    return {
        total: customers.value.length
    }
})

const handleCustomerPage = (page) => {
    customerPage.page = page
    fetchCustomers()
}

const goCreate = () => {
    router.push({ name: 'admin-customers-new' })
}

const goEdit = (id) => {
    router.push({ name: 'admin-customers-edit', params: { id } })
}

const deleteCustomer = async (id) => {
    try {
        await apiUsers.removeCustomer(id)
        ElMessage.success('Xóa khách hàng thành công')
        fetchCustomers()
    } catch (error) {
        console.error('Failed to delete customer:', error)
        ElMessage.error('Lỗi khi xóa khách hàng')
    }
}
</script>

<style scoped>
.grid {
    display: grid;
    gap: 16px;
}

.action-row {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    align-items: center;
}

.search-input {
    width: 200px;
}

.meta-row {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    flex-wrap: wrap;
}

.pill {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 14px;
    background: #f0f7ff;
    border: 1px solid #b3d8ff;
    border-radius: 8px;
    font-size: 12px;
}

.pill-label {
    color: #666;
}

.pill strong {
    font-size: 16px;
    color: #0066cc;
}

.title-col {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.title {
    font-weight: 600;
    color: #111;
}

.subtitle {
    font-size: 12px;
    color: #666;
}

.avatar-cell {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 50px;
}

.avatar-cell.empty {
    color: #ccc;
}

.avatar-img {
    width: 45px;
    height: 45px;
    border-radius: 4px;
    object-fit: cover;
    border: 1px solid #ddd;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 16px;
}
</style>

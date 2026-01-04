<template>
    <div class="page">
        <SectionCard>
            <div class="page-header">
                <div class="title-group">
                    <h2 class="page-title">{{ t('admin.menu.users') }}</h2>
                </div>
                <div class="header-actions">
                    <el-button size="small" @click="fetchUsers()">{{ t('admin.actions.refresh') }}</el-button>
                    <el-button type="primary" size="small" @click="goCreate">{{ t('admin.actions.add') }}</el-button>
                </div>
            </div>

            <div class="filters-row">
                <el-input v-model="userSearch" :placeholder="t('admin.filters.search')" size="small"
                    class="search-input" clearable />
                <el-select v-model="roleFilter" size="small" class="filter-select" clearable
                    :placeholder="t('admin.filters.filterByRole')">
                    <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
                </el-select>
            </div>

            <div class="meta-row" v-if="userStats.total">
                <div class="pill">
                    <span class="pill-label">{{ t('admin.stats.totalUsers') }}</span>
                    <strong>{{ userStats.total }}</strong>
                </div>
                <div class="pill info">
                    <span class="pill-label">{{ t('admin.stats.admins') }}</span>
                    <strong>{{ userStats.admin }}</strong>
                </div>
                <div class="pill success">
                    <span class="pill-label">{{ t('admin.stats.staff') }}</span>
                    <strong>{{ userStats.staff }}</strong>
                </div>
                <div class="pill warning">
                    <span class="pill-label">{{ t('admin.stats.users') }}</span>
                    <strong>{{ userStats.user }}</strong>
                </div>
            </div>

            <div class="table-wrapper">
                <el-table :data="filteredUsers" stripe :empty-text="t('admin.empty')" style="width: 100%">
                    <el-table-column :label="t('admin.table.avatar')" width="80">
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
                    <el-table-column :label="t('admin.table.email')" min-width="180">
                        <template #default="scope">
                            {{ scope.row.email }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="t('admin.table.phone')" width="140">
                        <template #default="scope">
                            {{ scope.row.phone || '--' }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="t('admin.table.role')" width="140">
                        <template #default="scope">
                            <el-tag :type="getRoleType(scope.row.roleId)" effect="dark" size="small">
                                {{ getRoleName(scope.row.roleId) }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column :label="t('admin.table.status')" width="120">
                        <template #default="scope">
                            <el-tag :type="scope.row.deleteFlag ? 'danger' : 'success'" effect="dark" size="small">
                                {{ scope.row.deleteFlag ? t('admin.table.inactive') : t('admin.table.active') }}
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
                                    @confirm="deleteUser(scope.row.id)">
                                    <template #reference>
                                        <el-button text size="small" type="danger">{{ t('admin.actions.delete')
                                        }}</el-button>
                                    </template>
                                </el-popconfirm>
                            </el-space>
                        </template>
                    </el-table-column>
                </el-table>
            </div>

            <div class="pagination">
                <el-pagination background layout="prev, pager, next" :current-page="userPage.page"
                    :page-size="userPage.size" :total="userPage.total" @current-change="handleUserPage" />
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
import { apiRoles } from '@/services/apiRoles'


const { t } = useI18n()
const router = useRouter()

const users = ref([])
const roles = ref([])
const userSearch = ref('')
const roleFilter = ref(null)
const isLoading = ref(false)

const userPage = reactive({
    page: 1,
    size: 10,
    total: 0
})

onMounted(() => {
    fetchRoles()
    fetchUsers()
})

const fetchRoles = async () => {
    try {
        const data = await apiRoles.list()
        roles.value = data || []
    } catch (error) {
        console.error('Failed to fetch roles:', error)
    }
}

const fetchUsers = async () => {
    isLoading.value = true
    try {
        const params = {
            page: userPage.page - 1,
            size: userPage.size
        }
        const data = await apiUsers.list(params)
        users.value = data?.content || []
        userPage.total = data?.totalElements || 0
    } catch (error) {
        console.error('Failed to fetch users:', error)
        ElMessage.error(t('admin.messages.loadUsersError'))
    } finally {
        isLoading.value = false
    }
}

const filteredUsers = computed(() => {
    let result = users.value

    if (userSearch.value) {
        const search = userSearch.value.toLowerCase()
        result = result.filter(u =>
            u.firstName?.toLowerCase().includes(search) ||
            u.lastName?.toLowerCase().includes(search) ||
            u.email?.toLowerCase().includes(search) ||
            u.phone?.includes(search)
        )
    }

    if (roleFilter.value) {
        result = result.filter(u => u.roleId === roleFilter.value)
    }

    return result
})

const userStats = computed(() => {
    return {
        total: users.value.length,
        admin: users.value.filter(u => getRoleName(u.roleId) === 'ROLE_ADMIN').length,
        staff: users.value.filter(u => getRoleName(u.roleId) === 'ROLE_STAFF').length,
        user: users.value.filter(u => getRoleName(u.roleId) === 'ROLE_USER').length
    }
})

const getRoleName = (roleId) => {
    const role = roles.value.find(r => r.id === roleId)
    return role?.name || 'Unknown'
}

const getRoleType = (roleId) => {
    const roleName = getRoleName(roleId)
    const typeMap = {
        'ROLE_ADMIN': 'danger',
        'ROLE_PM': 'warning',
        'ROLE_STAFF': 'success',
        'ROLE_USER': 'info'
    }
    return typeMap[roleName] || 'info'
}

const handleUserPage = (page) => {
    userPage.page = page
    fetchUsers()
}

const goCreate = () => {
    router.push({ name: 'admin-users-new' })
}

const goEdit = (id) => {
    router.push({ name: 'admin-users-edit', params: { id } })
}

const deleteUser = async (id) => {
    try {
        await apiUsers.remove(id)
        ElMessage.success(t('admin.messages.deleteUserSuccess'))
        fetchUsers()
    } catch (error) {
        console.error('Failed to delete user:', error)
        ElMessage.error(t('admin.messages.deleteUserFailed'))
    }
}
</script>

<style scoped>
.page {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
    gap: 12px;
}

.page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
}

.title-group {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.eyebrow {
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.6px;
    color: #6b7280;
    margin: 0;
}

.page-title {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
    color: #0f172a;
}

.header-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.filters-row {
    display: flex;
    gap: 10px;
    align-items: center;
    flex-wrap: wrap;
    margin: 12px 0 16px;
}

.search-input {
    flex: 1;
    min-width: 240px;
    max-width: 360px;
}

.filter-select {
    min-width: 180px;
}

.table-wrapper {
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    overflow-x: auto;
    overflow-y: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.meta-row {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    gap: 12px;
    margin-bottom: 16px;
    padding: 16px 0;
}

.pill {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
    padding: 14px 16px;
    background: #f0f7ff;
    border: 1px solid #b3d8ff;
    border-radius: 8px;
    font-size: 12px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.pill:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.pill-label {
    color: #666;
    font-weight: 500;
    font-size: 11px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.pill strong {
    font-size: 20px;
    color: #0066cc;
}

.pill.success {
    background: #f6ffed;
    border-color: #b7eb8f;
}

.pill.success strong {
    color: #52c41a;
}

.pill.warning {
    background: #fffbe6;
    border-color: #ffe58f;
}

.pill.warning strong {
    color: #faad14;
}

.pill.info {
    background: #f6f8fc;
    border-color: #d9d9d9;
}

.pill.info strong {
    color: #1890ff;
}

.title-col {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.title {
    font-weight: 600;
    color: #111;
    font-size: 14px;
}

.subtitle {
    font-size: 12px;
    color: #999;
}

.avatar-cell {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 50px;
}

.avatar-cell.empty {
    color: #ccc;
    font-weight: 600;
}

.avatar-img {
    width: 45px;
    height: 45px;
    border-radius: 6px;
    object-fit: cover;
    border: 1px solid #ddd;
    background: #f5f5f5;
}

.muted {
    color: #ccc;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #e5e7eb;
}

:deep(.el-table) {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    overflow: hidden;
}

:deep(.el-table th) {
    background: #f9f9fb !important;
    font-weight: 600;
    color: #333;
    border-color: #e5e7eb;
}

:deep(.el-table tr) {
    transition: background-color 0.2s ease;
}

:deep(.el-table tbody tr:hover) {
    background-color: #f9f9fb !important;
}

:deep(.el-table td) {
    border-color: #e5e7eb;
}

:deep(.el-tag) {
    border-radius: 4px;
    padding: 4px 8px;
}

@media (max-width: 1024px) {
    .search-input {
        max-width: 250px;
    }
}

@media (max-width: 768px) {
    .action-row {
        flex-direction: column;
        align-items: stretch;
        gap: 10px;
    }

    .search-input {
        max-width: 100%;
        width: 100%;
    }

    .filter-select {
        width: 100%;
        min-width: auto;
    }

    :deep(.el-button) {
        width: 100%;
    }

    :deep(.el-table) {
        font-size: 12px;
    }

    :deep(.el-table th.el-table__cell) {
        padding: 10px 8px;
        font-size: 12px;
    }

    :deep(.el-table td.el-table__cell) {
        padding: 12px 8px;
    }

    :deep(.el-table-column--selection .el-table__cell) {
        padding: 8px 4px;
    }

    .meta-row {
        grid-template-columns: repeat(2, 1fr);
        gap: 10px;
        margin-bottom: 12px;
        padding: 12px 0;
    }

    .pill {
        padding: 12px 14px;
        font-size: 11px;
    }

    .pill strong {
        font-size: 18px;
    }

    .avatar-img {
        width: 40px;
        height: 40px;
    }

    .subtitle {
        font-size: 11px;
    }

    :deep(.el-space) {
        width: 100%;
    }

    :deep(.el-space__item) {
        width: 100%;
    }

    :deep(.el-button--text) {
        width: 100%;
        text-align: center;
    }
}

@media (max-width: 480px) {
    .action-row {
        gap: 8px;
    }

    .meta-row {
        grid-template-columns: 1fr;
    }

    .pill {
        padding: 10px 12px;
    }

    :deep(.el-table) {
        font-size: 11px;
    }

    :deep(.el-table th.el-table__cell) {
        padding: 8px 6px;
        font-size: 11px;
    }

    :deep(.el-table td.el-table__cell) {
        padding: 10px 6px;
    }
}
</style>

<template>
    <div class="grid">
        <SectionCard eyebrow="Quản lý" title="Người dùng">
            <template #actions>
                <div class="action-row">
                    <el-input v-model="userSearch" placeholder="Tìm kiếm..." size="small" class="search-input"
                        clearable />
                    <el-select v-model="roleFilter" size="small" class="filter-select" clearable
                        placeholder="Lọc theo role">
                        <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
                    </el-select>
                    <el-button type="primary" size="small" @click="goCreate">{{ t('admin.actions.add') }}</el-button>
                    <el-button size="small" @click="fetchUsers()">{{ t('admin.actions.refresh') }}</el-button>
                </div>
            </template>

            <div class="meta-row" v-if="userStats.total">
                <div class="pill">
                    <span class="pill-label">Tổng người dùng</span>
                    <strong>{{ userStats.total }}</strong>
                </div>
                <div class="pill info">
                    <span class="pill-label">Quản trị viên</span>
                    <strong>{{ userStats.admin }}</strong>
                </div>
                <div class="pill success">
                    <span class="pill-label">Nhân viên</span>
                    <strong>{{ userStats.staff }}</strong>
                </div>
                <div class="pill warning">
                    <span class="pill-label">Người dùng</span>
                    <strong>{{ userStats.user }}</strong>
                </div>
            </div>

            <el-table :data="filteredUsers" stripe :empty-text="t('admin.empty')" style="width: 100%">
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
                <el-table-column label="Role" width="140">
                    <template #default="scope">
                        <el-tag :type="getRoleType(scope.row.roleId)" effect="dark" size="small">
                            {{ getRoleName(scope.row.roleId) }}
                        </el-tag>
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
        ElMessage.error('Lỗi khi tải danh sách người dùng')
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
        ElMessage.success('Xóa người dùng thành công')
        fetchUsers()
    } catch (error) {
        console.error('Failed to delete user:', error)
        ElMessage.error('Lỗi khi xóa người dùng')
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

.filter-select {
    width: 150px;
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

.muted {
    color: #ccc;
}

.pagination {
    display: flex;
    justify-content: center;
    margin-top: 16px;
}
</style>

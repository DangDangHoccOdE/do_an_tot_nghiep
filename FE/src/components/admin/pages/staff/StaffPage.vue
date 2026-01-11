<template>
    <div class="page">
        <SectionCard>
            <div class="page-header">
                <div class="title-group">
                    <h2 class="page-title">{{ t('admin.entities.staff') }}</h2>
                </div>
                <div class="header-actions">
                    <UiButton variant="refresh" @click="fetchStaff()" />
                    <UiButton variant="add" @click="goCreate" />
                </div>
            </div>

            <div class="search-section">
                <div class="search-controls">
                    <el-input v-model="staffSearch" :placeholder="t('admin.filters.search')" class="search-input"
                        clearable size="large">
                        <template #prefix>
                            <el-icon>
                                <Search />
                            </el-icon>
                        </template>
                    </el-input>
                </div>
                <div class="search-actions">
                    <UiButton variant="delete" size="large" :label="t('admin.actions.deleteSelected')"
                        :disabled="!selectedStaffIds.length" @click="() => handleBulkDelete(selectedStaffIds)" />
                </div>
            </div>

            <TableListView :data="filteredStaff" :columns="tableColumns" :total="staffPage.total"
                :current-page="staffPage.page" :page-size="staffPage.size" :loading="isLoading" :selectable="true"
                height="430px" @update:current-page="(page) => staffPage.page = page"
                @update:page-size="(size) => staffPage.size = size" @page-change="handleStaffPage"
                @bulk-delete="handleBulkDelete" @selection-change="onStaffSelectionChange" @row-click="handleRowClick">
                <template #avatar="{ row }">
                    <div class="avatar-cell">
                        <img v-if="row.avatar" :src="row.avatar" :alt="row.firstName" class="avatar-img" />
                        <div v-else class="avatar-default">
                            {{ getInitials(row.firstName, row.lastName) }}
                        </div>
                    </div>
                </template>

                <template #name="{ row }">
                    <div class="title-col">
                        <span class="title">{{ row.firstName }} {{ row.lastName }}</span>
                        <span class="subtitle">{{ row.email }}</span>
                    </div>
                </template>

                <template #itRole="{ row }">
                    <el-tag v-if="row.itRole" type="info" size="small">
                        {{ formatITRole(row.itRole) }}
                    </el-tag>
                    <span v-else>--</span>
                </template>

                <template #skills="{ row }">
                    <div class="skills-display" v-if="row.skills && row.skills.length > 0">
                        <div class="skill-badge" v-for="skill in row.skills.slice(0, 2)" :key="skill.id">
                            {{ skill.skillName }} <br />
                            <span class="skill-meta">{{ formatSkillLevel(skill.level) }}</span>
                        </div>
                        <div v-if="row.skills.length > 2" class="skill-badge more">
                            +{{ row.skills.length - 2 }} more
                        </div>
                    </div>
                    <span v-else class="no-skills">--</span>
                </template>

                <template #status="{ row }">
                    <el-tag :type="row.deleteFlag ? 'danger' : 'success'" effect="dark" size="small">
                        {{ row.deleteFlag ? t('admin.table.inactive') : t('admin.table.active') }}
                    </el-tag>
                </template>

                <template #actions="{ row }">
                    <el-space wrap size="4">
                        <UiButton variant="edit" size="small" @click.stop="goEdit(row.id)" />
                        <UiButton variant="delete" size="small" @click.stop="confirmDelete(row.id)" />
                    </el-space>
                </template>
            </TableListView>
        </SectionCard>
    </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, ElTag, ElSpace } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import SectionCard from '@/components/admin/SectionCard.vue'
import TableListView from '@/components/common/TableListView.vue'
import UiButton from '@/components/common/UiButton.vue'
import { apiUsers } from '@/services/apiUsers'

const { t } = useI18n()
const router = useRouter()

const staff = ref([])
const staffSearch = ref('')
const isLoading = ref(false)

const staffPage = reactive({
    page: 1,
    size: 10,
    total: 0
})
const selectedStaffIds = ref([])

onMounted(() => {
    fetchStaff()
})

const fetchStaff = async () => {
    isLoading.value = true
    try {
        const params = {
            page: staffPage.page - 1,
            size: staffPage.size
        }
        const data = await apiUsers.listStaff(params)
        staff.value = data?.content || []
        staffPage.total = data?.totalElements || 0
    } catch (error) {
        console.error('Failed to fetch staff:', error)
        ElMessage.error(t('admin.messages.loadStaffError'))
    } finally {
        isLoading.value = false
    }
}

const filteredStaff = computed(() => {
    let result = staff.value

    if (staffSearch.value) {
        const search = staffSearch.value.toLowerCase()
        result = result.filter(s => {
            const matchBasicInfo =
                s.firstName?.toLowerCase().includes(search) ||
                s.lastName?.toLowerCase().includes(search) ||
                s.email?.toLowerCase().includes(search) ||
                s.phone?.includes(search)

            // Search in skills
            const matchSkills = s.skills && s.skills.some(skill =>
                skill.skillName?.toLowerCase().includes(search) ||
                skill.level?.toLowerCase().includes(search)
            )

            return matchBasicInfo || matchSkills
        })
    }

    return result
})

const tableColumns = computed(() => [
    {
        prop: 'avatar',
        label: t('admin.table.avatar'),
        minWidth: 80,
        slot: 'avatar'
    },
    {
        prop: 'name',
        label: t('admin.table.name'),
        minWidth: 180,
        slot: 'name'
    },
    {
        prop: 'phone',
        label: t('admin.table.phone'),
        minWidth: 120,
        formatter: (row) => row.phone || '--'
    },
    {
        prop: 'itRole',
        label: t('admin.table.itRole'),
        minWidth: 120,
        slot: 'itRole'
    },
    {
        prop: 'skills',
        label: t('admin.table.skills'),
        minWidth: 250,
        slot: 'skills'
    },
    {
        prop: 'deleteFlag',
        label: t('admin.table.status'),
        minWidth: 120,
        slot: 'status'
    },
    {
        prop: 'actions',
        label: t('admin.actions.view'),
        minWidth: 200,
        slot: 'actions'
    }
])

const staffStats = computed(() => {
    return {
        total: staff.value.length
    }
})

const getInitials = (firstName = '', lastName = '') => {
    const first = firstName.trim()[0]?.toUpperCase() || ''
    const last = lastName.trim()[0]?.toUpperCase() || ''
    return `${first}${last}` || '??'
}

const handleStaffPage = (page) => {
    staffPage.page = page
    fetchStaff()
}

const handleBulkDelete = async (selectedIds) => {
    if (!selectedIds?.length) return
    try {
        await ElMessageBox.confirm(
            t('message.MSG0101', { count: selectedIds.length, entity: t('admin.entities.staff') }),
            t('confirm'),
            {
                confirmButtonText: t('admin.actions.delete'),
                cancelButtonText: t('admin.actions.cancel'),
                type: 'warning'
            }
        )

        isLoading.value = true
        await Promise.all(selectedIds.map(id => apiUsers.removeStaff(id)))
        ElMessage.success(t('message.MSG0102', { count: selectedIds.length, entity: t('admin.entities.staff') }))
        selectedStaffIds.value = []
        await fetchStaff()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error(t('message.ERR011', { entity: t('admin.entities.staff') }))
        }
    } finally {
        isLoading.value = false
    }
}

const onStaffSelectionChange = (ids) => {
    selectedStaffIds.value = ids
}

const handleRowClick = (row) => {
    goEdit(row.id)
}

const goCreate = () => {
    router.push({ name: 'admin-staff-new' })
}

const goEdit = (id) => {
    router.push({ name: 'admin-staff-edit', params: { id } })
}

const deleteStaff = async (id) => {
    try {
        await apiUsers.removeStaff(id)
        ElMessage.success(t('admin.messages.deleteStaffSuccess'))
        fetchStaff()
    } catch (error) {
        console.error('Failed to delete staff:', error)
        ElMessage.error(t('admin.messages.deleteStaffFailed'))
    }
}

const confirmDelete = async (id) => {
    try {
        await ElMessageBox.confirm(
            t('admin.messages.confirmDelete'),
            t('admin.actions.delete'),
            {
                confirmButtonText: t('admin.actions.delete'),
                cancelButtonText: t('admin.actions.close'),
                type: 'warning'
            }
        )
        await deleteStaff(id)
    } catch (error) {
        // user cancelled
    }
}

// Helper functions to format enums
const formatITRole = (role) => {
    const roleMap = {
        'FRONTEND_DEVELOPER': 'Frontend Dev',
        'BACKEND_DEVELOPER': 'Backend Dev',
        'FULLSTACK_DEVELOPER': 'Fullstack Dev',
        'MOBILE_DEVELOPER': 'Mobile Dev',
        'DEVOPS_ENGINEER': 'DevOps',
        'QA_MANUAL': 'QA Manual',
        'QA_AUTOMATION': 'QA Automation',
        'BUSINESS_ANALYST': 'BA',
        'PRODUCT_OWNER': 'PO',
        'PROJECT_MANAGER': 'PM',
        'SCRUM_MASTER': 'Scrum Master',
        'TECH_LEAD': 'Tech Lead',
        'SOLUTION_ARCHITECT': 'Architect',
        'UI_UX_DESIGNER': 'UI/UX Designer',
        'DATA_ENGINEER': 'Data Engineer',
        'DATA_ANALYST': 'Data Analyst',
        'SECURITY_ENGINEER': 'Security',
        'DATABASE_ADMINISTRATOR': 'DBA',
        'SYSTEM_ADMINISTRATOR': 'System Admin'
    }
    return roleMap[role] || role
}

const formatSkillLevel = (level) => {
    const levelMap = {
        'INTERN': 'Intern',
        'FRESHER': 'Fresher',
        'JUNIOR': 'Junior',
        'MIDDLE': 'Middle',
        'SENIOR': 'Senior',
        'LEAD': 'Lead',
        'PRINCIPAL': 'Principal',
        'ARCHITECT': 'Architect',
        'EXPERT': 'Expert'
    }
    return levelMap[level] || level
}
</script>

<style scoped>
.page {
    display: grid;
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

.search-section {
    display: flex;
    gap: 12px;
    align-items: center;
    margin: 16px 0 20px;
    flex-wrap: wrap;
}

.search-controls {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
    flex: 1;
    min-width: 240px;
}

.search-input {
    max-width: 600px;
    flex: 1;
    min-width: 240px;
}

.search-actions {
    margin-left: auto;
    display: flex;
    align-items: center;
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

.avatar-img {
    width: 45px;
    height: 45px;
    border-radius: 6px;
    object-fit: cover;
    border: 1px solid #ddd;
    background: #f5f5f5;
}

.avatar-default {
    width: 45px;
    height: 45px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #eff6ff, #dbeafe);
    color: #1d4ed8;
    font-weight: 700;
    border: 1px solid #cbd5e1;
}

.skills-display {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
}

.skill-badge {
    display: inline-block;
    padding: 6px 10px;
    background-color: #e6f7ff;
    border: 1px solid #91d5ff;
    border-radius: 4px;
    font-size: 12px;
    line-height: 1.4;
    color: #0066cc;
    transition: all 0.2s ease;
}

.skill-badge:hover {
    background-color: #bae7ff;
    border-color: #69c0ff;
}

.skill-badge.more {
    background: #f5f5f5;
    border-color: #ddd;
    color: #666;
}

.skill-meta {
    font-size: 11px;
    opacity: 0.8;
    display: block;
    margin-top: 2px;
}

.no-skills {
    color: #ccc;
}
</style>

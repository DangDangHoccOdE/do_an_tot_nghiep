<template>
    <div class="page">
        <SectionCard>
            <div class="page-header">
                <div class="title-group">
                    <h2 class="page-title">{{ t('admin.entities.staff') }}</h2>
                </div>
                <div class="header-actions">
                    <el-button size="small" @click="fetchStaff()">{{ t('admin.actions.refresh') }}</el-button>
                    <el-button type="primary" size="small" @click="goCreate">{{ t('admin.actions.add') }}</el-button>
                </div>
            </div>

            <div class="filters-row">
                <el-input v-model="staffSearch" :placeholder="t('admin.filters.search')" size="small"
                    class="search-input" clearable />
            </div>

            <div class="meta-row" v-if="staffStats.total">
                <div class="pill">
                    <span class="pill-label">{{ t('admin.stats.totalStaff') }}</span>
                    <strong>{{ staffStats.total }}</strong>
                </div>
            </div>

            <div class="table-wrapper">
                <el-table :data="filteredStaff" stripe :empty-text="t('admin.empty')" style="width: 100%">
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
                    <el-table-column :label="t('admin.table.itRole')" width="180">
                        <template #default="scope">
                            <el-tag v-if="scope.row.itRole" type="info" size="small">
                                {{ formatITRole(scope.row.itRole) }}
                            </el-tag>
                            <span v-else>--</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="t('admin.table.skills')" min-width="250">
                        <template #default="scope">
                            <div class="skills-display" v-if="scope.row.skills && scope.row.skills.length > 0">
                                <div class="skill-badge" v-for="(skill, idx) in scope.row.skills.slice(0, 2)"
                                    :key="skill.id">
                                    {{ skill.skillName }} <br />
                                    <span class="skill-meta">{{ formatSkillLevel(skill.level) }}</span>
                                </div>
                                <div v-if="scope.row.skills.length > 2" class="skill-badge more">
                                    +{{ scope.row.skills.length - 2 }} more
                                </div>
                            </div>
                            <span v-else class="no-skills">--</span>
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
                                    @confirm="deleteStaff(scope.row.id)">
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
                <el-pagination background layout="prev, pager, next" :current-page="staffPage.page"
                    :page-size="staffPage.size" :total="staffPage.total" @current-change="handleStaffPage" />
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

const staff = ref([])
const staffSearch = ref('')
const isLoading = ref(false)

const staffPage = reactive({
    page: 1,
    size: 10,
    total: 0
})

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
        staff.value = data.data?.content || []
        staffPage.total = data.data?.totalElements || 0
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

const staffStats = computed(() => {
    return {
        total: staff.value.length
    }
})

const handleStaffPage = (page) => {
    staffPage.page = page
    fetchStaff()
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

    .skills-display {
        flex-direction: column;
    }

    .skill-badge {
        width: 100%;
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

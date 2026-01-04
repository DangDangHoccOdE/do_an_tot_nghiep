<template>
    <div class="grid">
        <SectionCard eyebrow="Quản lý" title="Nhân viên">
            <template #actions>
                <div class="action-row">
                    <el-input v-model="staffSearch" placeholder="Tìm kiếm..." size="small" class="search-input"
                        clearable />
                    <el-button type="primary" size="small" @click="goCreate">{{ t('admin.actions.add') }}</el-button>
                    <el-button size="small" @click="fetchStaff()">{{ t('admin.actions.refresh') }}</el-button>
                </div>
            </template>

            <div class="meta-row" v-if="staffStats.total">
                <div class="pill">
                    <span class="pill-label">Tổng nhân viên</span>
                    <strong>{{ staffStats.total }}</strong>
                </div>
            </div>

            <el-table :data="filteredStaff" stripe :empty-text="t('admin.empty')" style="width: 100%">
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
                <el-table-column label="Vai trò IT" width="180">
                    <template #default="scope">
                        <el-tag v-if="scope.row.itRole" type="info" size="small">
                            {{ formatITRole(scope.row.itRole) }}
                        </el-tag>
                        <span v-else>--</span>
                    </template>
                </el-table-column>
                <el-table-column label="Kỹ năng" min-width="250">
                    <template #default="scope">
                        <div class="skills-display" v-if="scope.row.skills && scope.row.skills.length > 0">
                            <div class="skill-badge" v-for="skill in scope.row.skills" :key="skill.id">
                                {{ skill.skillName }} <br />
                                <span class="skill-meta">{{ formatSkillLevel(skill.level) }} • {{
                                    skill.yearsOfExperience }} năm</span>
                            </div>
                        </div>
                        <span v-else class="no-skills">--</span>
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
        ElMessage.error('Lỗi khi tải danh sách nhân viên')
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
        ElMessage.success('Xóa nhân viên thành công')
        fetchStaff()
    } catch (error) {
        console.error('Failed to delete staff:', error)
        ElMessage.error('Lỗi khi xóa nhân viên')
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

.skills-display {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
}

.skill-badge {
    display: inline-block;
    padding: 6px 10px;
    background-color: #e6f7ff;
    border: 1px solid #91d5ff;
    border-radius: 4px;
    font-size: 12px;
    line-height: 1.5;
}

.skill-meta {
    color: #666;
    font-size: 11px;
}

.no-skills {
    color: #ccc;
}
</style>

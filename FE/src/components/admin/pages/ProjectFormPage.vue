<template>
    <div class="form-page">
        <div class="form-shell">
            <SectionCard :eyebrow="pageTitle" :title="pageTitle" full>
                <template #actions>
                    <el-space>
                        <el-button @click="goBack">{{ t('admin.actions.back') }}</el-button>
                        <el-button v-if="!isView" type="primary" :loading="submitting" @click="submitForm">
                            {{ isCreate ? t('admin.actions.create') : t('admin.actions.save') }}
                        </el-button>
                    </el-space>
                </template>

                <div class="form-meta">
                    <el-tag type="info" effect="dark">{{ pageTitle }}</el-tag>
                    <span class="form-hint">{{ t('admin.formHints.project') }}</span>
                </div>

                <div class="project-info-section">
                    <div class="section-header">
                        <span class="section-title">THÔNG TIN DỰ ÁN</span>
                    </div>

                    <el-form ref="formRef" :model="form" :rules="rules" label-width="160px" label-position="left"
                        :disabled="loading || isView">
                        <el-row :gutter="16">
                            <el-col :xs="24" :md="12">
                                <el-form-item :label="t('admin.form.projectName')" prop="projectName" required>
                                    <el-input v-model="form.projectName" maxlength="200" show-word-limit />
                                </el-form-item>
                            </el-col>
                            <el-col :xs="24" :md="12">
                                <el-form-item :label="t('admin.filters.selectClient')" prop="clientId" required>
                                    <el-select v-model="form.clientId" clearable filterable remote
                                        :remote-method="searchClients" :loading="clientsLoading"
                                        :placeholder="t('admin.filters.selectClient')" style="width: 100%">
                                        <el-option v-for="client in clientsList" :key="client.id"
                                            :label="clientLabel(client)" :value="client.id" />
                                    </el-select>
                                </el-form-item>
                            </el-col>
                        </el-row>

                        <el-row :gutter="16">
                            <el-col :xs="24" :md="12">
                                <el-form-item :label="t('admin.form.budgetEstimated')" prop="budgetEstimated">
                                    <el-input-number v-model="form.budgetEstimated" :min="0" :step="1000000"
                                        controls-position="right" class="full-input" />
                                </el-form-item>
                            </el-col>
                            <el-col :xs="24" :md="12">
                                <el-form-item :label="t('admin.form.currencyUnit')" prop="currencyUnit">
                                    <el-select v-model="form.currencyUnit" clearable filterable style="width: 100%">
                                        <el-option v-for="opt in currencyOptions" :key="opt.value" :label="opt.label"
                                            :value="opt.value" />
                                    </el-select>
                                </el-form-item>
                            </el-col>
                        </el-row>

                        <el-row :gutter="16">
                            <el-col :xs="24" :md="12">
                                <el-form-item :label="t('admin.form.status')" prop="status">
                                    <el-select v-model="form.status" clearable filterable style="width: 100%">
                                        <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label"
                                            :value="opt.value" />
                                    </el-select>
                                </el-form-item>
                            </el-col>
                            <el-col :xs="24" :md="12">
                                <el-form-item :label="t('admin.form.timelineEstimated')" prop="timelineEstimated">
                                    <el-input-number v-model="form.timelineEstimated" :min="1" :step="1"
                                        controls-position="right" class="full-input" />
                                </el-form-item>
                            </el-col>
                        </el-row>

                        <el-row :gutter="16">
                            <el-col :xs="24" :md="12">
                                <el-form-item :label="t('admin.form.startDate')" prop="startDate">
                                    <el-date-picker v-model="form.startDate" type="date" format="DD/MM/YYYY"
                                        value-format="YYYY-MM-DD" placeholder="DD/MM/YYYY" style="width: 100%" />
                                </el-form-item>
                            </el-col>
                            <el-col :xs="24" :md="12">
                                <el-form-item :label="t('admin.form.endDate')" prop="endDate">
                                    <el-date-picker v-model="form.endDate" type="date" format="DD/MM/YYYY"
                                        value-format="YYYY-MM-DD" placeholder="DD/MM/YYYY" style="width: 100%" />
                                </el-form-item>
                            </el-col>
                        </el-row>

                        <el-form-item :label="t('admin.form.description')" prop="description">
                            <el-input v-model="form.description" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }"
                                maxlength="1000" show-word-limit />
                        </el-form-item>
                    </el-form>
                </div>

                <div class="member-section">
                    <div class="member-header">
                        <div class="member-title">
                            <span class="eyebrow">{{ t('admin.sections.projectMembers') }}</span>
                            <p class="member-hint">{{ t('admin.formHints.projectMembers') }}</p>
                        </div>
                        <div class="member-actions" v-if="!isView">
                            <el-input v-model="memberSearch" class="member-search"
                                :placeholder="t('admin.filters.search')" clearable @clear="loadStaff" @input="loadStaff"
                                @keyup.enter="loadStaff">
                                <template #prefix>
                                    <el-icon>
                                        <Search />
                                    </el-icon>
                                </template>
                            </el-input>
                            <el-select v-model="memberRoleFilter" class="member-filter" clearable filterable
                                :placeholder="t('admin.filters.filterByRole')" @change="loadStaff">
                                <el-option v-for="opt in itRoleOptions" :key="opt.value" :label="opt.label"
                                    :value="opt.value" />
                            </el-select>
                            <el-button type="primary" :loading="staffLoading" @click="loadStaff">
                                {{ t('admin.actions.refresh') }}
                            </el-button>
                        </div>
                    </div>

                    <el-table v-if="!isView" :data="staffList" size="small" stripe :empty-text="t('admin.empty')"
                        class="member-table">
                        <el-table-column :label="t('admin.table.name')" min-width="200">
                            <template #default="scope">
                                <div class="member-name">
                                    <span class="member-full">{{ scope.row.fullName || scope.row.email }}</span>
                                    <span class="member-email" v-if="scope.row.fullName">{{ scope.row.email }}</span>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="t('admin.table.itRole')" width="180">
                            <template #default="scope">{{ roleLabel(scope.row.itRole) }}</template>
                        </el-table-column>
                        <el-table-column :label="t('admin.table.phone')" width="140">
                            <template #default="scope">{{ scope.row.phone || '--' }}</template>
                        </el-table-column>
                        <el-table-column width="100" align="right">
                            <template #default="scope">
                                <el-button text size="small" type="primary" @click="addMember(scope.row)">
                                    {{ t('admin.actions.add') }}
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>

                    <el-divider />

                    <div class="member-header">
                        <div class="member-title">
                            <span class="eyebrow">{{ t('admin.sections.selectedMembers') }}</span>
                            <p class="member-hint">{{ t('admin.formHints.selectedMembers') }}</p>
                        </div>
                    </div>

                    <el-table :data="selectedMembers" size="small" stripe :empty-text="t('admin.empty')"
                        class="member-table">
                        <el-table-column :label="t('admin.table.name')" min-width="200">
                            <template #default="scope">
                                <div class="member-name">
                                    <span class="member-full">{{ scope.row.fullName || scope.row.email }}</span>
                                    <span class="member-email" v-if="scope.row.fullName">{{ scope.row.email }}</span>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="t('admin.table.itRole')" width="180">
                            <template #default="scope">{{ roleLabel(scope.row.itRole) }}</template>
                        </el-table-column>
                        <el-table-column :label="t('admin.table.phone')" width="140">
                            <template #default="scope">{{ scope.row.phone || '--' }}</template>
                        </el-table-column>
                        <el-table-column width="180" align="right">
                            <template #default="scope">
                                <el-space wrap>
                                    <el-button text size="small" @click="goStaffDetail(scope.row.userId)">
                                        {{ t('admin.actions.view') }}
                                    </el-button>
                                    <el-button v-if="!isView" text size="small" type="danger"
                                        @click="removeMember(scope.row.userId)">
                                        {{ t('admin.actions.delete') }}
                                    </el-button>
                                </el-space>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </SectionCard>
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Search } from '@element-plus/icons-vue'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiProjects } from '@/services/apiProjects'
import { apiUsers } from '@/services/apiUsers'
import { handleError, handleSuccess } from '@/utils/handleMessage'
import { createProjectRules } from '@/validations/projectRules'

const props = defineProps({
    id: { type: String, default: null },
    mode: { type: String, default: 'edit' }
})

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
    projectName: '',
    clientId: '',
    description: '',
    budgetEstimated: null,
    currencyUnit: 'VND',
    timelineEstimated: null,
    status: 'PENDING',
    startDate: '',
    endDate: ''
})

const statusOptions = computed(() => [
    { label: t('admin.projectStatus.PENDING'), value: 'PENDING' },
    { label: t('admin.projectStatus.APPROVED'), value: 'APPROVED' },
    { label: t('admin.projectStatus.IN_PROGRESS'), value: 'IN_PROGRESS' },
    { label: t('admin.projectStatus.DONE'), value: 'DONE' },
    { label: t('admin.projectStatus.CANCELLED'), value: 'CANCELLED' }
])

const currencyOptions = [
    { label: 'VND (Vietnam)', value: 'VND' },
    { label: 'USD (US Dollar)', value: 'USD' },
    { label: 'JPY (Japanese Yen)', value: 'JPY' },
    { label: 'EUR (Euro)', value: 'EUR' }
]

const itRoleOptions = [
    { label: t('admin.itRoles.frontend'), value: 'FRONTEND_DEVELOPER' },
    { label: t('admin.itRoles.backend'), value: 'BACKEND_DEVELOPER' },
    { label: t('admin.itRoles.fullstack'), value: 'FULLSTACK_DEVELOPER' },
    { label: t('admin.itRoles.mobile'), value: 'MOBILE_DEVELOPER' },
    { label: t('admin.itRoles.qa'), value: 'QA_ENGINEER' },
    { label: t('admin.itRoles.ba'), value: 'BUSINESS_ANALYST' },
    { label: t('admin.itRoles.po'), value: 'PRODUCT_OWNER' },
    { label: t('admin.itRoles.pm'), value: 'PROJECT_MANAGER' },
    { label: t('admin.itRoles.scrum'), value: 'SCRUM_MASTER' },
    { label: t('admin.itRoles.techLead'), value: 'TECH_LEAD' },
    { label: t('admin.itRoles.architect'), value: 'SOLUTION_ARCHITECT' }
]

const memberRoleFilter = ref()
const memberSearch = ref('')
const staffLoading = ref(false)
const staffList = ref([])
const selectedMembers = ref([])

const clientsLoading = ref(false)
const clientsList = ref([])

const FORM_STORAGE_KEY = 'project_form_data'

const isView = computed(() => props.mode === 'view')
const isCreate = computed(() => props.mode === 'create')
const pageTitle = computed(() => {
    if (isCreate.value) return t('admin.actions.add') + ' ' + t('admin.menu.projects')
    if (isView.value) return t('admin.actions.view') + ' ' + t('admin.menu.projects')
    return t('admin.actions.edit') + ' ' + t('admin.menu.projects')
})

const rules = computed(() => createProjectRules(t, form, props.id))

const roleLabel = (role) => itRoleOptions.find((opt) => opt.value === role)?.label || role || '--'

const clientLabel = (client) => {
    const name = client?.fullName || `${client?.firstName || ''} ${client?.lastName || ''}`.trim()
    return name || client?.email || client?.id || '--'
}

const normalizeMember = (user) => {
    const userId = user?.userId || user?.id
    const firstName = user?.firstName?.trim() || ''
    const lastName = user?.lastName?.trim() || ''
    const fullName = user?.fullName?.trim() || `${firstName} ${lastName}`.trim()
    return {
        userId,
        fullName: fullName || null,
        email: user?.email || '',
        phone: user?.phone || '',
        avatar: user?.avatar || '',
        itRole: user?.itRole || null
    }
}

const addMember = (user) => {
    const member = normalizeMember(user)
    if (!member.userId) return
    const exists = selectedMembers.value.some((item) => item.userId === member.userId)
    if (!exists) {
        selectedMembers.value.push(member)
    }
}

const removeMember = (userId) => {
    selectedMembers.value = selectedMembers.value.filter((item) => item.userId !== userId)
}

const goStaffDetail = (userId) => {
    if (!userId) return
    saveFormData()
    router.push({ name: 'admin-staff-edit', params: { id: userId }, query: { mode: 'view', from: 'project' } })
}

const saveFormData = () => {
    const formData = {
        ...form,
        memberIds: selectedMembers.value.map((m) => m.userId),
        selectedMembers: selectedMembers.value
    }
    sessionStorage.setItem(FORM_STORAGE_KEY, JSON.stringify(formData))
}

const restoreFormData = () => {
    const savedData = sessionStorage.getItem(FORM_STORAGE_KEY)
    if (savedData) {
        try {
            const data = JSON.parse(savedData)
            Object.assign(form, {
                projectName: data.projectName || '',
                clientId: data.clientId || '',
                description: data.description || '',
                budgetEstimated: data.budgetEstimated ?? null,
                currencyUnit: data.currencyUnit || 'VND',
                timelineEstimated: data.timelineEstimated ?? null,
                status: data.status || 'PENDING',
                startDate: data.startDate || '',
                endDate: data.endDate || ''
            })
            if (data.selectedMembers) {
                selectedMembers.value = data.selectedMembers
            }
            sessionStorage.removeItem(FORM_STORAGE_KEY)
        } catch (e) {
            console.error('Failed to restore form data:', e)
        }
    }
}

const searchClients = async (query) => {
    clientsLoading.value = true
    try {
        const params = {
            page: 0,
            size: 50,
            keyword: query || undefined
        }
        const data = await apiUsers.list(params)
        clientsList.value = data?.content || []
    } catch (error) {
        handleError(error, t, t('admin.messages.loadFail'))
    } finally {
        clientsLoading.value = false
    }
}

const loadClients = async () => {
    await searchClients('')
}

const loadStaff = async () => {
    staffLoading.value = true
    try {
        const params = {
            page: 0,
            size: 100,
            itRole: memberRoleFilter.value || undefined,
            keyword: memberSearch.value || undefined
        }
        const data = await apiUsers.listStaff(params)
        staffList.value = data?.content || []
    } catch (error) {
        handleError(error, t, t('admin.messages.loadStaffError'))
    } finally {
        staffLoading.value = false
    }
}

const loadDetail = async () => {
    if (!props.id) return
    loading.value = true
    try {
        const data = await apiProjects.detail(props.id)
        Object.assign(form, {
            projectName: data.projectName || '',
            clientId: data.clientId || '',
            description: data.description || '',
            budgetEstimated: data.budgetEstimated ?? null,
            currencyUnit: data.currencyUnit || 'VND',
            timelineEstimated: data.timelineEstimated ?? null,
            status: data.status || 'PENDING',
            startDate: data.startDate || '',
            endDate: data.endDate || ''
        })
        selectedMembers.value = (data.members || []).map(normalizeMember)
    } catch (error) {
        handleError(error, t, t('admin.messages.loadFail'))
    } finally {
        loading.value = false
    }
}

const redirectToList = () => {
    const bucket = route.query.from || route.query.bucket || 'current'
    const name = bucket === 'future' ? 'admin-projects-future' : 'admin-projects-current'
    router.push({ name })
}

const submitForm = () => {
    if (isView.value) return
    formRef.value?.validate(async (valid) => {
        if (!valid) return
        submitting.value = true
        try {
            const payload = { ...form, memberIds: selectedMembers.value.map((m) => m.userId) }
            if (isCreate.value) {
                const bucket = route.query.bucket === 'future' ? 'future' : 'current'
                if (bucket === 'future') {
                    await apiProjects.createFuture(payload)
                } else {
                    await apiProjects.create(payload)
                }
                handleSuccess(t('admin.messages.created', { entity: t('admin.entities.project') }))
            } else {
                await apiProjects.update(props.id, payload)
                handleSuccess(t('admin.messages.updated', { entity: t('admin.entities.project') }))
            }
            redirectToList()
        } catch (error) {
            handleError(error, t, t('admin.messages.saveFail'))
        } finally {
            submitting.value = false
        }
    })
}

const goBack = () => redirectToList()

onMounted(() => {
    const savedData = sessionStorage.getItem(FORM_STORAGE_KEY)
    if (savedData && isCreate.value) {
        restoreFormData()
    } else if (!isCreate.value) {
        loadDetail()
    }

    if (!isView.value) {
        loadStaff()
        loadClients()
    }
})
</script>

<style scoped>
.form-page {
    background: radial-gradient(circle at 20% 20%, #fff5f5, #ffffff 45%);
}

.full-input {
    width: 100%;
}

.form-meta {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 10px;
}

.form-hint {
    color: #6b7280;
    font-size: 13px;
}

.project-info-section {
    margin-top: 20px;
    padding: 24px;
    background: linear-gradient(to bottom, #f9fafb, #ffffff);
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.section-header {
    padding-bottom: 12px;
    margin-bottom: 20px;
    border-bottom: 2px solid #e5e7eb;
}

.section-title {
    font-size: 14px;
    font-weight: 700;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: #374151;
    background: linear-gradient(135deg, #3b82f6, #8b5cf6);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.member-section {
    margin-top: 32px;
    padding: 24px;
    background: linear-gradient(to bottom, #f9fafb, #ffffff);
    border-radius: 12px;
    border: 1px solid #e5e7eb;
    display: flex;
    flex-direction: column;
    gap: 20px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.member-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 12px;
    flex-wrap: wrap;
    padding-bottom: 12px;
    border-bottom: 2px solid #e5e7eb;
}

.member-title .eyebrow {
    display: inline-block;
    font-size: 14px;
    font-weight: 700;
    letter-spacing: 0.05em;
    text-transform: uppercase;
    color: #374151;
    background: linear-gradient(135deg, #3b82f6, #8b5cf6);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.member-hint {
    margin: 6px 0 0;
    color: #6b7280;
    font-size: 13px;
    font-weight: 400;
    line-height: 1.4;
}

.member-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
}

.member-search {
    min-width: 220px;
}

.member-filter {
    width: 200px;
}

.member-table {
    margin-top: 12px;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    border: 1px solid #e5e7eb;
}

.member-name {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.member-full {
    font-weight: 600;
    color: #111827;
    font-size: 14px;
}

.member-email {
    color: #6b7280;
    font-size: 12px;
    font-weight: 400;
}

/* Table Header Styles */
.member-table :deep(.el-table__header-wrapper) th {
    font-size: 14px;
    font-weight: 700;
    color: #374151;
    background: #f9fafb;
    padding: 14px 0;
}

.member-table :deep(.el-table__header-wrapper) .cell {
    font-weight: 700;
}

/* Required Field Indicator - Red Asterisk */
:deep(.el-form-item.is-required:not(.is-no-asterisk))>.el-form-item__label:before {
    content: '*';
    color: #f56c6c;
    margin-right: 4px;
    font-size: 16px;
    font-weight: 700;
    vertical-align: middle;
}

:deep(.el-form-item.is-required) .el-form-item__label {
    font-weight: 500;
}
</style>

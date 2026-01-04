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

                <el-form ref="formRef" :model="form" :rules="rules" label-width="160px" label-position="left"
                    :disabled="loading || isView">
                    <el-row :gutter="16">
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.form.projectName')" prop="projectName">
                                <el-input v-model="form.projectName" maxlength="200" show-word-limit />
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.form.clientId')" prop="clientId">
                                <el-input v-model="form.clientId" placeholder="UUID" />
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="16">
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.form.teamId')" prop="teamId">
                                <el-input v-model="form.teamId" placeholder="UUID" clearable />
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.form.status')" prop="status">
                                <el-select v-model="form.status" clearable filterable>
                                    <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label"
                                        :value="opt.value" />
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
                            <el-form-item :label="t('admin.form.timelineEstimated')" prop="timelineEstimated">
                                <el-input-number v-model="form.timelineEstimated" :min="1" :step="1"
                                    controls-position="right" class="full-input" />
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="16">
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.form.startDate')" prop="startDate">
                                <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD"
                                    placeholder="YYYY-MM-DD" style="width: 100%" />
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.form.endDate')" prop="endDate">
                                <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD"
                                    placeholder="YYYY-MM-DD" style="width: 100%" />
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-form-item :label="t('admin.form.description')" prop="description">
                        <el-input v-model="form.description" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }"
                            maxlength="1000" show-word-limit />
                    </el-form-item>
                </el-form>
            </SectionCard>
        </div>
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiProjects } from '@/services/apiProjects'
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
    teamId: '',
    description: '',
    budgetEstimated: null,
    timelineEstimated: null,
    status: 'PENDING',
    startDate: '',
    endDate: ''
})

const statusOptions = [
    { label: 'Pending', value: 'PENDING' },
    { label: 'Approved', value: 'APPROVED' },
    { label: 'In progress', value: 'IN_PROGRESS' },
    { label: 'Done', value: 'DONE' },
    { label: 'Cancelled', value: 'CANCELLED' }
]

const isView = computed(() => props.mode === 'view')
const isCreate = computed(() => props.mode === 'create')
const pageTitle = computed(() => {
    if (isCreate.value) return t('admin.actions.add') + ' ' + t('admin.menu.projects')
    if (isView.value) return t('admin.actions.view') + ' ' + t('admin.menu.projects')
    return t('admin.actions.edit') + ' ' + t('admin.menu.projects')
})

const rules = computed(() => createProjectRules(t, form))

const loadDetail = async () => {
    if (!props.id) return
    loading.value = true
    try {
        const data = await apiProjects.detail(props.id)
        Object.assign(form, {
            projectName: data.projectName || '',
            clientId: data.clientId || '',
            teamId: data.teamId || '',
            description: data.description || '',
            budgetEstimated: data.budgetEstimated ?? null,
            timelineEstimated: data.timelineEstimated ?? null,
            status: data.status || 'PENDING',
            startDate: data.startDate || '',
            endDate: data.endDate || ''
        })
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
            const payload = { ...form }
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
    if (!isCreate.value) {
        loadDetail()
    }
})
</script>

<style scoped>
.form-page {
    padding: 18px;
    background: radial-gradient(circle at 20% 20%, #fff5f5, #ffffff 45%);
}

.full-input {
    width: 100%;
}

.form-shell {
    max-width: 1080px;
    margin: 0 auto;
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
</style>

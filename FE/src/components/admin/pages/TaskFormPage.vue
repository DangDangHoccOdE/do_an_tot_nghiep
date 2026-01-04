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
                    <span class="form-hint">{{ t('admin.formHints.task') }}</span>
                </div>

                <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" :disabled="loading || isView">
                    <el-row :gutter="16">
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.filters.projectId')" prop="projectId">
                                <el-input v-model="form.projectId" placeholder="Project UUID" />
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.table.title')" prop="title">
                                <el-input v-model="form.title" maxlength="200" show-word-limit />
                            </el-form-item>
                        </el-col>
                    </el-row>

                    <el-row :gutter="16">
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.table.assignee')" prop="assignedToUserId">
                                <el-input v-model="form.assignedToUserId" placeholder="User UUID" clearable />
                            </el-form-item>
                        </el-col>
                        <el-col :xs="24" :md="12">
                            <el-form-item :label="t('admin.form.status')" prop="status">
                                <el-select v-model="form.status" filterable clearable>
                                    <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label"
                                        :value="opt.value" />
                                </el-select>
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
                            <el-form-item :label="t('admin.table.dueDate')" prop="dueDate">
                                <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD"
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
import { apiTasks } from '@/services/apiTasks'
import { handleError, handleSuccess } from '@/utils/handleMessage'
import { createTaskRules } from '@/validations/taskRules'

const props = defineProps({
    id: { type: String, default: null },
    mode: { type: String, default: 'edit' },
    projectId: { type: String, default: null }
})

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)

const form = reactive({
    projectId: '',
    title: '',
    description: '',
    status: 'PENDING',
    assignedToUserId: '',
    startDate: '',
    dueDate: ''
})

const statusOptions = [
    { label: 'Pending', value: 'PENDING' },
    { label: 'Todo', value: 'TODO' },
    { label: 'In progress', value: 'IN_PROGRESS' },
    { label: 'Review', value: 'REVIEW' },
    { label: 'Done', value: 'DONE' }
]

const isView = computed(() => props.mode === 'view')
const isCreate = computed(() => props.mode === 'create')
const pageTitle = computed(() => {
    if (isCreate.value) return t('admin.actions.add') + ' ' + t('admin.menu.tasks')
    if (isView.value) return t('admin.actions.view') + ' ' + t('admin.menu.tasks')
    return t('admin.actions.edit') + ' ' + t('admin.menu.tasks')
})

const rules = computed(() => createTaskRules(t, form))

const loadDetail = async () => {
    if (!props.id) return
    loading.value = true
    try {
        const data = await apiTasks.detail(props.id)
        Object.assign(form, {
            projectId: data.projectId || form.projectId,
            title: data.title || '',
            description: data.description || '',
            status: data.status || 'PENDING',
            assignedToUserId: data.assignedToUserId || '',
            startDate: data.startDate || '',
            dueDate: data.dueDate || ''
        })
    } catch (error) {
        handleError(error, t, t('admin.messages.loadFail'))
    } finally {
        loading.value = false
    }
}

const redirectToList = () => router.push({ name: 'admin-tasks' })

const submitForm = () => {
    if (isView.value) return
    formRef.value?.validate(async (valid) => {
        if (!valid) return
        submitting.value = true
        try {
            const payload = { ...form }
            if (isCreate.value) {
                await apiTasks.create(form.projectId, payload)
                handleSuccess(t('admin.messages.created', { entity: t('admin.entities.task') }))
            } else {
                await apiTasks.update(props.id, payload)
                handleSuccess(t('admin.messages.updated', { entity: t('admin.entities.task') }))
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
    if (props.projectId) {
        form.projectId = props.projectId
    } else if (route.query.projectId) {
        form.projectId = route.query.projectId
    }
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

.form-shell {
    max-width: 960px;
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

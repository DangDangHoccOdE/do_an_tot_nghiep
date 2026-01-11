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
                    <span class="form-hint">{{ t('admin.formHints.team') }}</span>
                </div>

                <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" :disabled="loading || isView">
                    <el-form-item :label="t('admin.form.projectName')" prop="projectId">
                        <el-select v-model="form.projectId" :placeholder="t('admin.form.selectProject')" filterable
                            clearable style="width: 100%">
                            <el-option v-for="project in projects" :key="project.id" :label="project.projectName"
                                :value="project.id" />
                        </el-select>
                    </el-form-item>

                    <el-form-item :label="t('admin.form.teamName')" prop="name">
                        <el-input v-model="form.name" maxlength="150" show-word-limit />
                    </el-form-item>

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
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiTeams } from '@/services/apiTeams'
import { apiProjects } from '@/services/apiProjects'
import { handleError, handleSuccess } from '@/utils/handleMessage'
import { createTeamRules } from '@/validations/teamRules'

const props = defineProps({
    id: { type: String, default: null },
    mode: { type: String, default: 'edit' }
})

const { t } = useI18n()
const router = useRouter()

const formRef = ref()
const loading = ref(false)
const submitting = ref(false)
const projects = ref([])

const form = reactive({
    name: '',
    description: '',
    projectId: null
})

const isView = computed(() => props.mode === 'view')
const isCreate = computed(() => props.mode === 'create')
const pageTitle = computed(() => {
    if (isCreate.value) return t('admin.actions.add') + ' ' + t('admin.menu.teams')
    if (isView.value) return t('admin.actions.view') + ' ' + t('admin.menu.teams')
    return t('admin.actions.edit') + ' ' + t('admin.menu.teams')
})

const rules = computed(() => createTeamRules(t))

const fetchProjects = async () => {
    try {
        // Fetch all projects (current and future)
        const [currentData, futureData] = await Promise.all([
            apiProjects.list({ status: 'current', page: 0, size: 1000 }),
            apiProjects.list({ status: 'future', page: 0, size: 1000 })
        ])
        projects.value = [...(currentData.content || []), ...(futureData.content || [])]
    } catch (error) {
        handleError(error, t, t('admin.messages.loadFail'))
    }
}

const loadDetail = async () => {
    if (!props.id) return
    loading.value = true
    try {
        const data = await apiTeams.detail(props.id)
        Object.assign(form, {
            name: data.name || '',
            description: data.description || '',
            projectId: data.projectId || null
        })
    } catch (error) {
        handleError(error, t, t('admin.messages.loadFail'))
    } finally {
        loading.value = false
    }
}

const redirectToList = () => router.push({ name: 'admin-teams' })

const submitForm = () => {
    if (isView.value) return
    formRef.value?.validate(async (valid) => {
        if (!valid) return
        submitting.value = true
        try {
            const payload = { ...form }
            if (isCreate.value) {
                await apiTeams.create(payload)
                handleSuccess(t('admin.messages.created', { entity: t('admin.entities.team') }))
            } else {
                await apiTeams.update(props.id, payload)
                handleSuccess(t('admin.messages.updated', { entity: t('admin.entities.team') }))
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

onMounted(async () => {
    await fetchProjects()
    if (!isCreate.value) {
        loadDetail()
    }
})
</script>

<style scoped>
.form-page {
    background: radial-gradient(circle at 20% 20%, #fff5f5, #ffffff 45%);
}

.form-shell {
    max-width: 900px;
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

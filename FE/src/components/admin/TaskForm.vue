<template>
    <div class="task-form">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="140px">
            <el-form-item :label="$t('admin.daily.taskTitle')" prop="title">
                <el-input v-model="formData.title" :placeholder="$t('admin.daily.taskTitlePlaceholder')" />
            </el-form-item>

            <el-form-item :label="$t('admin.daily.description')" prop="description">
                <el-input v-model="formData.description" type="textarea" rows="4"
                    :placeholder="$t('admin.daily.descriptionPlaceholder')" />
            </el-form-item>

            <el-form-item :label="$t('admin.daily.project')" prop="projectId">
                <el-select v-model="formData.projectId" :placeholder="$t('admin.daily.selectProject')">
                    <el-option v-for="project in projects" :key="project.id" :label="project.projectName"
                        :value="project.id" />
                </el-select>
            </el-form-item>

            <el-form-item :label="$t('admin.daily.assignedTo')" prop="assignedTo">
                <el-select v-model="formData.assignedTo" :placeholder="$t('admin.daily.selectAssignee')">
                    <el-option v-for="user in users" :key="user.id" :label="user.fullName" :value="user.id" />
                </el-select>
            </el-form-item>

            <el-form-item :label="$t('admin.daily.taskDate')" prop="taskDate">
                <el-date-picker v-model="formData.taskDate" type="date" :placeholder="$t('admin.daily.selectDate')" />
            </el-form-item>

            <el-form-item :label="$t('admin.daily.priority')" prop="priority">
                <el-select v-model="formData.priority" :placeholder="$t('admin.daily.selectPriority')">
                    <el-option label="LOW" value="LOW" />
                    <el-option label="MEDIUM" value="MEDIUM" />
                    <el-option label="HIGH" value="HIGH" />
                    <el-option label="CRITICAL" value="CRITICAL" />
                </el-select>
            </el-form-item>

            <el-form-item :label="$t('admin.daily.estimatedHours')" prop="estimatedHours">
                <el-input-number v-model="formData.estimatedHours" :min="0.5" :max="24" :step="0.5" />
            </el-form-item>

            <el-form-item :label="$t('admin.daily.status')" prop="status">
                <el-select v-model="formData.status" :placeholder="$t('admin.daily.selectStatus')">
                    <el-option label="PENDING" value="PENDING" />
                    <el-option label="IN_PROGRESS" value="IN_PROGRESS" />
                    <el-option label="COMPLETED" value="COMPLETED" />
                    <el-option label="BLOCKED" value="BLOCKED" />
                    <el-option label="CANCELLED" value="CANCELLED" />
                </el-select>
            </el-form-item>

            <div class="form-actions">
                <el-button type="primary" @click="submitForm">{{ $t('common.submit') }}</el-button>
                <el-button @click="$emit('cancel')">{{ $t('common.cancel') }}</el-button>
            </div>
        </el-form>
    </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import * as apiDailyTasks from '@/services/apiDailyTasks'
import * as apiProjects from '@/services/apiProjects'
import * as apiUsers from '@/services/apiUsers'

const { t } = useI18n()

const props = defineProps({
    task: Object,
    isEdit: Boolean
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref(null)
const projects = ref([])
const users = ref([])

const formData = ref({
    title: '',
    description: '',
    projectId: null,
    assignedTo: null,
    taskDate: null,
    priority: 'MEDIUM',
    estimatedHours: 8,
    status: 'PENDING'
})

const rules = {
    title: [
        { required: true, message: t('admin.daily.titleRequired'), trigger: 'blur' },
        { min: 5, max: 255, message: t('admin.daily.titleLength'), trigger: 'blur' }
    ],
    projectId: [
        { required: true, message: t('admin.daily.projectRequired'), trigger: 'change' }
    ],
    assignedTo: [
        { required: true, message: t('admin.daily.assignedToRequired'), trigger: 'change' }
    ],
    taskDate: [
        { required: true, message: t('admin.daily.dateRequired'), trigger: 'change' }
    ],
    priority: [
        { required: true, message: t('admin.daily.priorityRequired'), trigger: 'change' }
    ],
    estimatedHours: [
        { required: true, message: t('admin.daily.estimatedHoursRequired'), trigger: 'blur' }
    ]
}

onMounted(async () => {
    await loadProjects()
    await loadUsers()
    if (props.task && props.isEdit) {
        formData.value = { ...props.task }
    }
})

const loadProjects = async () => {
    try {
        const response = await apiProjects.getAllProjects()
        projects.value = response.data || []
    } catch (error) {
        console.error('Failed to load projects', error)
    }
}

const loadUsers = async () => {
    try {
        const response = await apiUsers.getAllUsers()
        users.value = response.data || []
    } catch (error) {
        console.error('Failed to load users', error)
    }
}

const submitForm = async () => {
    try {
        await formRef.value.validate()
        if (props.isEdit) {
            await apiDailyTasks.updateDailyTask(props.task.id, formData.value)
            ElMessage.success(t('common.updateSuccess'))
        } else {
            await apiDailyTasks.createDailyTask(formData.value)
            ElMessage.success(t('common.createSuccess'))
        }
        emit('submit')
    } catch (error) {
        console.error('Submit error', error)
        ElMessage.error(t('common.submitFailed'))
    }
}

watch(
    () => props.task,
    (newTask) => {
        if (newTask && props.isEdit) {
            formData.value = { ...newTask }
        }
    },
    { deep: true }
)
</script>

<style scoped>
.form-actions {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    margin-top: 20px;
}
</style>

<template>
    <div class="task-form">
        <div class="section-header">
            <span class="section-title">{{ $t('admin.daily.createTask') }}</span>
        </div>

        <el-form ref="formRef" :model="formData" :rules="rules" label-width="140px">
            <el-form-item prop="title" required>
                <template #label>
                    <span>{{ $t('admin.daily.taskTitle') }}</span>
                </template>
                <el-input v-model="formData.title" :placeholder="$t('admin.daily.taskTitlePlaceholder')" />
            </el-form-item>

            <el-form-item :label="$t('admin.daily.description')" prop="description">
                <el-input v-model="formData.description" type="textarea" rows="4"
                    :placeholder="$t('admin.daily.descriptionPlaceholder')" />
            </el-form-item>

            <el-form-item prop="projectId" required>
                <template #label>
                    <span>{{ $t('admin.daily.project') }}</span>
                </template>
                <el-select v-model="formData.projectId" :placeholder="$t('admin.daily.selectProject')"
                    @change="onProjectChange">
                    <el-option v-for="project in projects" :key="project.id" :label="project.projectName"
                        :value="project.id" />
                </el-select>
            </el-form-item>

            <el-form-item>
                <template #label>
                    <span>{{ $t('admin.daily.roleFilter') }}</span>
                </template>
                <el-select v-model="selectedRoleFilter" :placeholder="$t('admin.daily.selectRole')" clearable
                    @change="onRoleFilterChange">
                    <el-option v-for="role in availableRoles" :key="role" :label="getRoleLabel(role)" :value="role" />
                </el-select>
            </el-form-item>

            <el-form-item prop="assignedTo" required>
                <template #label>
                    <span>{{ $t('admin.daily.assignedTo') }}</span>
                </template>
                <el-select v-model="formData.assignedTo" :placeholder="$t('admin.daily.selectAssignee')">
                    <el-option v-for="user in getFilteredUsers()" :key="user.userId" :label="user.fullName"
                        :value="user.userId" />
                </el-select>
            </el-form-item>

            <el-form-item prop="taskDate" required>
                <template #label>
                    <span>{{ $t('admin.daily.taskDate') }}</span>
                </template>
                <el-date-picker v-model="formData.taskDate" type="date" format="DD/MM/YYYY" value-format="YYYY-MM-DD"
                    :placeholder="$t('admin.daily.selectDate')" style="width: 100%" />
            </el-form-item>

            <el-form-item prop="priority" required>
                <template #label>
                    <span>{{ $t('admin.daily.priority') }}</span>
                </template>
                <el-select v-model="formData.priority" :placeholder="$t('admin.daily.selectPriority')">
                    <el-option :label="$t('admin.enums.priority.LOW')" value="LOW" />
                    <el-option :label="$t('admin.enums.priority.MEDIUM')" value="MEDIUM" />
                    <el-option :label="$t('admin.enums.priority.HIGH')" value="HIGH" />
                    <el-option :label="$t('admin.enums.priority.CRITICAL')" value="CRITICAL" />
                </el-select>
            </el-form-item>

            <el-form-item prop="estimatedHours" required>
                <template #label>
                    <span>{{ $t('admin.daily.estimatedHours') }}</span>
                </template>
                <el-input-number v-model="formData.estimatedHours" :min="0.5" :max="24" :step="0.5" />
            </el-form-item>

            <el-form-item :label="$t('admin.daily.status')" prop="status">
                <el-select v-model="formData.status" :placeholder="$t('admin.daily.selectStatus')">
                    <el-option :label="$t('admin.enums.status.TODO')" value="TODO" />
                    <el-option :label="$t('admin.enums.status.IN_PROGRESS')" value="IN_PROGRESS" />
                    <el-option :label="$t('admin.enums.status.COMPLETED')" value="COMPLETED" />
                    <el-option :label="$t('admin.enums.status.BLOCKED')" value="BLOCKED" />
                    <el-option :label="$t('admin.enums.status.CANCELLED')" value="CANCELLED" />
                </el-select>
            </el-form-item>

            <div class="form-actions">
                <el-button type="primary" @click="submitForm">{{ $t('submit') }}</el-button>
                <el-button @click="$emit('cancel')">{{ $t('cancel') }}</el-button>
            </div>
        </el-form>
    </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { apiDailyTasks } from '@/services/apiDailyTasks'
import { apiProjects } from '@/services/apiProjects'
import { apiUsers } from '@/services/apiUsers'
import { createDailyTaskRules } from '@/validations/dailyTaskRules'

const { t } = useI18n()

const props = defineProps({
    task: Object,
    isEdit: Boolean
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref(null)
const projects = ref([])
const users = ref([])
const selectedRoleFilter = ref(null)

const availableRoles = computed(() => {
    const roles = new Set()
    users.value.forEach(user => {
        if (user.itRole) {
            // Convert itRole to lowercase format for i18n key (e.g., FRONTEND_DEVELOPER -> frontend_developer)
            const roleKey = user.itRole.toLowerCase().replace(/_/g, '_')
            roles.add(roleKey)
        }
    })
    return Array.from(roles).sort()
})

const getRoleLabel = (role) => {
    // Try to get the translation, with fallback to raw role name
    const label = t(`admin.itRoles.${role}`, '')
    return label || role.split('_').map(word => word.charAt(0).toUpperCase() + word.slice(1)).join(' ')
}

const filteredRoles = computed(() => {
    if (selectedRoleFilter.value) {
        return [selectedRoleFilter.value]
    }
    return availableRoles.value
})

const filteredUsersByRole = (role) => {
    return users.value.filter(user => {
        if (!user.itRole) return false
        const userRoleKey = user.itRole.toLowerCase().replace(/_/g, '_')
        return userRoleKey === role
    })
}

const getFilteredUsers = () => {
    if (selectedRoleFilter.value) {
        return filteredUsersByRole(selectedRoleFilter.value)
    }
    return users.value
}

const onRoleFilterChange = () => {
    // Reset assignedTo when role filter changes
    formData.value.assignedTo = null
}

const formData = ref({
    title: '',
    description: '',
    projectId: null,
    assignedTo: null,
    taskDate: null,
    priority: 'MEDIUM',
    estimatedHours: 8,
    status: 'TODO'
})

const rules = createDailyTaskRules(t)

onMounted(async () => {
    await loadProjects()
    if (props.task && props.isEdit) {
        formData.value = { ...props.task }
        await loadUsers()
    }
})

const loadProjects = async () => {
    try {
        const response = await apiProjects.list()
        projects.value = response.content || []
    } catch (error) {
        console.error('Failed to load projects', error)
    }
}

const loadUsers = async () => {
    try {
        // If projectId is selected, load project members; otherwise load all staff
        if (formData.value.projectId) {
            const response = await apiProjects.getMembers(formData.value.projectId)
            users.value = response || []
        } else {
            const response = await apiUsers.listStaff()
            users.value = response || []
        }
    } catch (error) {
        console.error('Failed to load users', error)
    }
}

const onProjectChange = async () => {
    await loadUsers()
    formData.value.assignedTo = null
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
        // Display backend error message if available
        const errorMessage = error?.response?.data?.message || error?.message || t('common.submitFailed')
        ElMessage.error(errorMessage)
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
.task-form {
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

.form-actions {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
    margin-top: 20px;
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

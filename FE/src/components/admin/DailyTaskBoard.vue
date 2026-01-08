<template>
    <div class="daily-task-board">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <span>{{ $t('admin.daily.taskBoard') }}</span>
                    <el-button type="primary" @click="handleShowForm" v-if="canCreate">
                        {{ $t('common.add') }}
                    </el-button>
                </div>
            </template>

            <!-- Filters -->
            <div class="filters">
                <el-select v-model="selectedProject" :placeholder="$t('admin.daily.selectProject')" @change="loadTasks"
                    clearable>
                    <el-option v-for="project in projects" :key="project.id" :label="project.projectName"
                        :value="project.id" />
                </el-select>

                <el-date-picker v-model="selectedDate" type="date" :placeholder="$t('admin.daily.selectDate')"
                    @change="loadTasks" />

                <el-select v-model="selectedStatus" :placeholder="$t('admin.daily.selectStatus')" @change="loadTasks"
                    clearable>
                    <el-option label="PENDING" value="PENDING" />
                    <el-option label="IN_PROGRESS" value="IN_PROGRESS" />
                    <el-option label="COMPLETED" value="COMPLETED" />
                    <el-option label="BLOCKED" value="BLOCKED" />
                    <el-option label="CANCELLED" value="CANCELLED" />
                </el-select>
            </div>

            <!-- Task List -->
            <el-table :data="tasks" stripe style="width: 100%">
                <el-table-column prop="title" :label="$t('admin.daily.taskTitle')" width="200" />
                <el-table-column prop="assignedToUserName" :label="$t('admin.daily.assignedTo')" width="150" />
                <el-table-column prop="priority" :label="$t('admin.daily.priority')" width="100">
                    <template #default="{ row }">
                        <el-tag :type="getPriorityType(row.priority)">{{ row.priority }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="status" :label="$t('admin.daily.status')" width="120">
                    <template #default="{ row }">
                        <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="estimatedHours" :label="$t('admin.daily.estimatedHours')" width="120" />
                <el-table-column prop="taskDate" :label="$t('admin.daily.taskDate')" width="120" />
                <el-table-column :label="$t('common.actions')" width="150">
                    <template #default="{ row }">
                        <el-button link type="primary" size="small" @click="handleEdit(row)">
                            {{ $t('common.edit') }}
                        </el-button>
                        <el-button link type="danger" size="small" @click="handleDelete(row)">
                            {{ $t('common.delete') }}
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </el-card>

        <!-- Task Form Modal -->
        <el-dialog v-model="showFormDialog"
            :title="isEditMode ? $t('admin.daily.editTask') : $t('admin.daily.createTask')" width="600px">
            <task-form :task="selectedTask" :isEdit="isEditMode" @submit="handleFormSubmit"
                @cancel="showFormDialog = false" />
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import TaskForm from './TaskForm.vue'
import * as apiDailyTasks from '@/services/apiDailyTasks'
import * as apiProjects from '@/services/apiProjects'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const { t } = useI18n()
const authStore = useAuthStore()

const tasks = ref([])
const projects = ref([])
const selectedProject = ref(null)
const selectedDate = ref(null)
const selectedStatus = ref(null)
const showFormDialog = ref(false)
const selectedTask = ref(null)
const isEditMode = ref(false)

const canCreate = computed(() => {
    return authStore.hasRole('ROLE_PROJECT_MANAGER') || authStore.hasRole('ROLE_ADMIN')
})

onMounted(async () => {
    await loadProjects()
    await loadTasks()
})

const loadProjects = async () => {
    try {
        const response = await apiProjects.getAllProjects()
        projects.value = response.data || []
    } catch (error) {
        console.error('Failed to load projects', error)
    }
}

const loadTasks = async () => {
    try {
        let response
        if (selectedProject.value && selectedDate.value) {
            response = await apiDailyTasks.getTasksByProjectAndDate(
                selectedProject.value,
                selectedDate.value.toISOString().split('T')[0]
            )
        } else if (selectedProject.value) {
            response = await apiDailyTasks.getTasksByProject(selectedProject.value)
        } else {
            response = await apiDailyTasks.getTasksByProject(projects.value[0]?.id)
        }

        let filteredTasks = response.data || []
        if (selectedStatus.value) {
            filteredTasks = filteredTasks.filter(t => t.status === selectedStatus.value)
        }
        tasks.value = filteredTasks
    } catch (error) {
        console.error('Failed to load tasks', error)
        ElMessage.error(t('common.loadFailed'))
    }
}

const handleShowForm = () => {
    selectedTask.value = null
    isEditMode.value = false
    showFormDialog.value = true
}

const handleEdit = (task) => {
    selectedTask.value = { ...task }
    isEditMode.value = true
    showFormDialog.value = true
}

const handleDelete = async (task) => {
    try {
        await ElMessageBox.confirm(
            t('common.confirmDelete'),
            t('common.warning'),
            { confirmButtonText: t('common.confirm'), cancelButtonText: t('common.cancel') }
        )
        await apiDailyTasks.deleteDailyTask(task.id)
        ElMessage.success(t('common.deleteSuccess'))
        await loadTasks()
    } catch (error) {
        console.error('Delete error', error)
    }
}

const handleFormSubmit = async () => {
    showFormDialog.value = false
    await loadTasks()
}

const getPriorityType = (priority) => {
    const types = {
        LOW: 'info',
        MEDIUM: 'warning',
        HIGH: 'danger',
        CRITICAL: 'danger'
    }
    return types[priority] || 'info'
}

const getStatusType = (status) => {
    const types = {
        PENDING: 'info',
        IN_PROGRESS: 'warning',
        COMPLETED: 'success',
        BLOCKED: 'danger',
        CANCELLED: 'danger'
    }
    return types[status] || 'info'
}
</script>

<style scoped>
.daily-task-board {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.filters {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
}

.filters>* {
    flex: 1;
    min-width: 150px;
}
</style>

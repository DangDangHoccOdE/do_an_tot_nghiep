<template>
    <div class="daily-task-board">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <span>{{ $t('admin.daily.taskBoard') }}</span>
                    <UiButton v-if="canCreate" variant="add" @click="handleShowForm" />
                </div>
            </template>

            <!-- Filters -->
            <div class="filters">
                <el-select v-model="selectedProject" :placeholder="$t('admin.daily.selectProject')" @change="loadTasks"
                    clearable>
                    <el-option v-for="project in projects" :key="project.id" :label="project.projectName"
                        :value="project.id" />
                </el-select>

                <el-date-picker v-model="selectedDate" type="date" format="DD/MM/YYYY" value-format="YYYY-MM-DD"
                    :placeholder="$t('admin.daily.selectDate')" @change="loadTasks" />

                <el-select v-model="selectedStatus" :placeholder="$t('admin.daily.selectStatus')" @change="loadTasks"
                    clearable>
                    <el-option :label="$t('admin.enums.status.TODO')" value="TODO" />
                    <el-option :label="$t('admin.enums.status.IN_PROGRESS')" value="IN_PROGRESS" />
                    <el-option :label="$t('admin.enums.status.COMPLETED')" value="COMPLETED" />
                    <el-option :label="$t('admin.enums.status.BLOCKED')" value="BLOCKED" />
                    <el-option :label="$t('admin.enums.status.CANCELLED')" value="CANCELLED" />
                </el-select>
            </div>

            <!-- Project Selection Hint -->
            <div class="project-hint-box" v-if="!selectedProject">
                <div class="hint-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                        stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="10"></circle>
                        <line x1="12" y1="16" x2="12" y2="12"></line>
                        <line x1="12" y1="8" x2="12.01" y2="8"></line>
                    </svg>
                </div>
                <span class="hint-text">{{ t('selectProjectHint') }}</span>
            </div>

            <!-- Task List -->
            <TableListView :data="tasks" :columns="taskColumns" :total="tasks.length" :current-page="1" :page-size="50"
                :loading="false" :selectable="false" @row-click="handleRowClick">
                <template #assignedToUserName="{ row }">
                    <span>{{ row.assignedToUserName || '--' }}</span>
                </template>

                <template #priority="{ row }">
                    <el-tag :type="getPriorityType(row.priority)">{{ row.priority }}</el-tag>
                </template>

                <template #status="{ row }">
                    <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
                </template>

                <template #estimatedHours="{ row }">
                    <span>{{ row.estimatedHours || '--' }}</span>
                </template>

                <template #taskDate="{ row }">
                    <span>{{ row.taskDate || '--' }}</span>
                </template>

                <template #actions="{ row }">
                    <div class="action-buttons">
                        <UiButton variant="edit" size="small" @click.stop="handleEdit(row)" />
                        <UiButton variant="delete" size="small" @click.stop="handleDelete(row)" />
                    </div>
                </template>
            </TableListView>
        </el-card>

        <!-- Task Form Modal -->
        <el-dialog v-model="showFormDialog" width="600px">
            <task-form :task="selectedTask" :isEdit="isEditMode" @submit="handleFormSubmit"
                @cancel="showFormDialog = false" />
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import TaskForm from '../tasks/TaskForm.vue'
import UiButton from '@/components/common/UiButton.vue'
import TableListView from '@/components/common/TableListView.vue'
import { apiDailyTasks } from '@/services/apiDailyTasks'
import { apiProjects } from '@/services/apiProjects'
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
    return authStore.canManage
})

const emptyText = computed(() => {
    return t('noData')
})

const taskColumns = computed(() => [
    {
        prop: 'title',
        label: t('admin.daily.taskTitle'),
        minWidth: 200
    },
    {
        prop: 'assignedToUserName',
        label: t('admin.daily.assignedTo'),
        minWidth: 150,
        slot: 'assignedToUserName'
    },
    {
        prop: 'priority',
        label: t('admin.daily.priority'),
        minWidth: 100,
        slot: 'priority'
    },
    {
        prop: 'status',
        label: t('admin.daily.status'),
        minWidth: 100,
        slot: 'status'
    },
    {
        prop: 'estimatedHours',
        label: t('admin.daily.estimatedHours'),
        minWidth: 120,
        slot: 'estimatedHours'
    },
    {
        prop: 'taskDate',
        label: t('admin.daily.taskDate'),
        minWidth: 120,
        slot: 'taskDate'
    },
    {
        prop: 'actions',
        label: t('admin.actions.view'),
        minWidth: 150,
        slot: 'actions'
    }
])

onMounted(async () => {
    await loadProjects()
    await loadTasks()
})

const loadProjects = async () => {
    try {
        const response = await apiProjects.list()
        console.log('Projects response:', response)
        projects.value = response.content || []
        console.log('Loaded projects:', projects.value)
    } catch (error) {
        console.error('Failed to load projects', error)
    }
}

const loadTasks = async () => {
    try {
        // Yêu cầu chọn dự án trước khi tải task
        if (!selectedProject.value) {
            tasks.value = []
            return
        }

        let response
        if (selectedProject.value && selectedDate.value) {
            // selectedDate is already formatted as YYYY-MM-DD from date-picker value-format
            const taskDate = typeof selectedDate.value === 'string'
                ? selectedDate.value
                : selectedDate.value.toISOString().split('T')[0]
            response = await apiDailyTasks.byProjectAndDate(
                selectedProject.value,
                taskDate
            )
        } else if (selectedProject.value) {
            response = await apiDailyTasks.byProject(selectedProject.value)
        }

        let filteredTasks = response || []
        if (selectedStatus.value) {
            filteredTasks = filteredTasks.filter(t => t.status === selectedStatus.value)
        }
        tasks.value = filteredTasks
        console.log('Loaded tasks:', tasks.value)
    } catch (error) {
        console.error('Failed to load tasks', error)
        // Display backend error message if available
        const errorMessage = error?.response?.data?.message || error?.message || t('common.loadFailed')
        ElMessage.error(errorMessage)
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

const handleRowClick = (row) => {
    // View task details in read-only mode
    selectedTask.value = { ...row }
    isEditMode.value = false
    showFormDialog.value = true
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

.project-hint-box {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 18px;
    margin-bottom: 20px;
    background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
    border: 2px solid #93c5fd;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
    animation: pulse-hint 2s ease-in-out infinite;
    width: 50vh;
}

.hint-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #2563eb;
    flex-shrink: 0;
}

.hint-text {
    color: #1e40af;
    font-size: 14px;
    font-weight: 600;
    letter-spacing: 0.02em;
}

@keyframes pulse-hint {

    0%,
    100% {
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
        border-color: #93c5fd;
    }

    50% {
        box-shadow: 0 4px 20px rgba(59, 130, 246, 0.3);
        border-color: #60a5fa;
    }
}

.action-buttons {
    display: flex;
    gap: 4px;
    align-items: center;
    justify-content: center;
}
</style>

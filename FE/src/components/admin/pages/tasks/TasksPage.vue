<template>
  <div class="grid">
    <SectionCard full :eyebrow="t('admin.menu.tasks')" :title="t('admin.menu.tasks')">
      <template #actions>
        <div class="action-row">
          <el-input v-model="projectIdFilter" :placeholder="t('admin.filters.projectId')" size="small"
            class="project-input" clearable />
          <el-input v-model="taskSearch" :placeholder="t('admin.filters.search')" size="small" class="search-input"
            clearable />
          <el-select v-model="statusFilter" size="small" class="filter-select">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-button size="small" @click="fetchTasks">{{ t('admin.actions.loadTasks') }}</el-button>
          <el-button type="primary" size="small" :disabled="!projectIdFilter" @click="goCreate">
            {{ t('admin.actions.add') }}
          </el-button>
        </div>
      </template>

      <div class="meta-row" v-if="taskStats.total">
        <div class="pill">
          <span class="pill-label">Tổng task</span>
          <strong>{{ taskStats.total }}</strong>
        </div>
        <div class="pill warning">
          <span class="pill-label">Pending</span>
          <strong>{{ taskStats.pending }}</strong>
        </div>
        <div class="pill info">
          <span class="pill-label">Đang làm</span>
          <strong>{{ taskStats.inProgress }}</strong>
        </div>
        <div class="pill success">
          <span class="pill-label">Hoàn thành</span>
          <strong>{{ taskStats.done }}</strong>
        </div>
      </div>

      <el-table :data="filteredTasks" stripe :empty-text="t('admin.empty')" style="width: 100%">
        <el-table-column :label="t('admin.table.title')" min-width="240">
          <template #default="scope">
            <div class="title-col">
              <span class="title">{{ scope.row.title }}</span>
              <span class="subtitle" v-if="scope.row.description">{{ scope.row.description }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.table.status')" width="140">
          <template #default="scope">
            <el-tag :type="statusMeta(scope.row.status).type" effect="dark" size="small">
              {{ statusMeta(scope.row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.table.assignee')" min-width="140">
          <template #default="scope">
            <el-tag v-if="scope.row.assignedToUserId" type="info" size="small" effect="plain">
              {{ scope.row.assignedToUserId }}
            </el-tag>
            <span v-else class="muted">--</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.table.dueDate')" width="150">
          <template #default="scope">
            <span :class="['due', { overdue: isOverdue(scope.row.dueDate) }]">{{ formatDate(scope.row.dueDate) }}</span>
          </template>
        </el-table-column>
        <el-table-column width="200" :label="t('admin.actions.view')">
          <template #default="scope">
            <el-space wrap size="4">
              <el-button text size="small" @click="goView(scope.row.id)">{{ t('admin.actions.view') }}</el-button>
              <el-button text size="small" type="primary" @click="goEdit(scope.row.id)">{{
                t('admin.actions.edit') }}</el-button>
              <el-button text size="small" type="danger" @click="() => confirmDeleteTask(scope.row.id)">{{
                t('admin.actions.delete') }}</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </SectionCard>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiTasks } from '@/services/apiTasks'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const { t } = useI18n()
const router = useRouter()

const tasks = ref([])
const projectIdFilter = ref('')
const taskSearch = ref('')
const statusFilter = ref('all')

const statusOptions = [
  { label: 'Tất cả', value: 'all' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'In progress', value: 'IN_PROGRESS' },
  { label: 'Done', value: 'DONE' },
  { label: 'Cancelled', value: 'CANCELLED' }
]

const statusLookup = {
  PENDING: { label: 'Pending', type: 'warning' },
  IN_PROGRESS: { label: 'In progress', type: 'info' },
  DONE: { label: 'Done', type: 'success' },
  CANCELLED: { label: 'Cancelled', type: 'danger' }
}

const statusMeta = (status) => statusLookup[status] ?? { label: status || '--', type: 'info' }

const formatDate = (value) => {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toISOString().split('T')[0]
}

const isOverdue = (value) => {
  if (!value) return false
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

const filteredTasks = computed(() => {
  let list = tasks.value

  if (taskSearch.value) {
    const keyword = taskSearch.value.toLowerCase()
    list = list.filter((task) => task.title?.toLowerCase().includes(keyword))
  }

  if (statusFilter.value !== 'all') {
    list = list.filter((task) => task.status === statusFilter.value)
  }

  return list
})

const taskStats = computed(() => {
  const counts = tasks.value.reduce(
    (acc, item) => ({ ...acc, [item.status]: (acc[item.status] || 0) + 1 }),
    {}
  )
  return {
    total: tasks.value.length,
    inProgress: counts.IN_PROGRESS || 0,
    done: counts.DONE || 0,
    pending: counts.PENDING || 0
  }
})

const fetchTasks = async () => {
  const auth = useAuthStore()
  // If project filter provided, fetch tasks for that project
  if (projectIdFilter.value) {
    const data = await apiTasks.byProject(projectIdFilter.value)
    tasks.value = data
    return
  }
  // No project filter: fall back to role-based lists
  if (auth.isAdmin) {
    // Admin without filter: keep tasks empty until a project is selected
    tasks.value = []
  } else if (auth.isPM) {
    const data = await apiTasks.myProjectsTasks()
    tasks.value = data
  } else if (auth.isStaff) {
    const data = await apiTasks.myTasks()
    tasks.value = data
  } else {
    tasks.value = []
  }
}

const goCreate = () => {
  if (!projectIdFilter.value) return
  router.push({ name: 'admin-tasks-new', query: { projectId: projectIdFilter.value } })
}

const goEdit = (id) => router.push({ name: 'admin-tasks-edit', params: { id } })

const goView = (id) => router.push({ name: 'admin-tasks-edit', params: { id }, query: { mode: 'view' } })

const deleteTask = async (id) => {
  try {
    await apiTasks.remove(id)
    ElMessage.success(t('message.MSG0102', { count: 1, entity: t('admin.entities.task') }))
    fetchTasks()
  } catch (error) {
    ElMessage.error(t('message.ERR011', { entity: t('admin.entities.task') }))
  }
}

const confirmDeleteTask = async (id) => {
  try {
    await ElMessageBox.confirm(
      t('message.MSG0101', { count: 1, entity: t('admin.entities.task') }),
      t('confirm'),
      {
        confirmButtonText: t('admin.actions.delete'),
        cancelButtonText: t('admin.actions.cancel'),
        type: 'warning'
      }
    )
  } catch {
    return
  }

  await deleteTask(id)
}
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 14px;
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  justify-content: flex-end;
}

.project-input {
  max-width: 180px;
}

.search-input {
  max-width: 180px;
}

.filter-select {
  width: 140px;
}

.meta-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 8px;
  margin: 12px 0 6px;
}

.pill {
  background: #fff4f4;
  border: 1px solid #ffe0e0;
  color: #c1121f;
  border-radius: 12px;
  padding: 10px 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
}

.pill .pill-label {
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #a30f1a;
}

.pill.warning {
  background: #fff8eb;
  border-color: #ffe3b8;
  color: #b45309;
}

.pill.info {
  background: #f1f5ff;
  border-color: #dfe6ff;
  color: #1d4ed8;
}

.pill.success {
  background: #e9f9ef;
  border-color: #c6f0d5;
  color: #15803d;
}

.title-col {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.title {
  font-weight: 700;
  color: #0f172a;
}

.subtitle {
  color: #6b7280;
  font-size: 12px;
}

.muted {
  color: #9ca3af;
}

.due.overdue {
  color: #b11226;
  font-weight: 700;
}

.due {
  color: #0f172a;
}
</style>

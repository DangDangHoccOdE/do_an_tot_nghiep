<template>
  <div class="grid">
    <SectionCard :eyebrow="sectionLabel" :title="sectionLabel">
      <template #actions>
        <div class="action-row">
          <el-input v-model="projectSearch" :placeholder="t('admin.filters.search')" size="small" class="search-input"
            clearable />
          <el-select v-model="statusFilter" size="small" class="filter-select">
            <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-button type="primary" size="small" @click="goCreate">{{ t('admin.actions.add') }}</el-button>
          <el-button size="small" @click="fetchProjects()">{{ t('admin.actions.refresh') }}</el-button>
        </div>
      </template>

      <div class="meta-row" v-if="projectStats.total">
        <div class="pill">
          <span class="pill-label">Tổng dự án</span>
          <strong>{{ projectStats.total }}</strong>
        </div>
        <div class="pill warning">
          <span class="pill-label">Đang duyệt</span>
          <strong>{{ projectStats.pending }}</strong>
        </div>
        <div class="pill info">
          <span class="pill-label">Đang thực hiện</span>
          <strong>{{ projectStats.inProgress }}</strong>
        </div>
        <div class="pill success">
          <span class="pill-label">Hoàn thành</span>
          <strong>{{ projectStats.done }}</strong>
        </div>
      </div>

      <el-table :data="filteredProjects" stripe :empty-text="t('admin.empty')" style="width: 100%">
        <el-table-column :label="t('admin.table.projectName')" min-width="200">
          <template #default="scope">
            <div class="title-col">
              <span class="title">{{ scope.row.projectName }}</span>
              <span class="subtitle" v-if="scope.row.clientId">Client: {{ scope.row.clientId }}</span>
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
        <el-table-column :label="t('admin.table.startDate')" width="140">
          <template #default="scope">{{ formatDate(scope.row.startDate) }}</template>
        </el-table-column>
        <el-table-column :label="t('admin.table.endDate')" width="140">
          <template #default="scope">{{ formatDate(scope.row.endDate) }}</template>
        </el-table-column>
        <el-table-column :label="t('admin.form.teamId')" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.teamId" size="small" type="info" effect="plain">{{ scope.row.teamId }}</el-tag>
            <span v-else class="muted">--</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.form.budgetEstimated')" width="140">
          <template #default="scope">{{ formatCurrency(scope.row.budgetEstimated) }}</template>
        </el-table-column>
        <el-table-column width="200" :label="t('admin.actions.view')">
          <template #default="scope">
            <el-space wrap size="4">
              <el-button text size="small" @click="goView(scope.row.id)">
                {{ t('admin.actions.view') }}
              </el-button>
              <el-button text size="small" type="primary" @click="goEdit(scope.row.id)">
                {{ t('admin.actions.edit') }}
              </el-button>
              <el-popconfirm :title="t('admin.confirm.deleteMessage')" @confirm="deleteProject(scope.row.id)">
                <template #reference>
                  <el-button text size="small" type="danger">{{ t('admin.actions.delete') }}</el-button>
                </template>
              </el-popconfirm>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination background layout="prev, pager, next" :current-page="projectPage.page"
          :page-size="projectPage.size" :total="projectPage.total" @current-change="handleProjectPage" />
      </div>
    </SectionCard>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiProjects } from '@/services/apiProjects'

const props = defineProps({
  status: { type: String, default: 'current' }
})

const { t } = useI18n()
const router = useRouter()

const projectPage = reactive({ data: [], total: 0, page: 1, size: 10, status: props.status })
const projectSearch = ref('')
const statusFilter = ref('all')

const statusOptions = [
  { label: 'Tất cả', value: 'all' },
  { label: 'Pending', value: 'PENDING' },
  { label: 'Approved', value: 'APPROVED' },
  { label: 'In progress', value: 'IN_PROGRESS' },
  { label: 'Done', value: 'DONE' },
  { label: 'Cancelled', value: 'CANCELLED' }
]

const statusLookup = {
  PENDING: { label: 'Pending', type: 'warning' },
  APPROVED: { label: 'Approved', type: 'info' },
  IN_PROGRESS: { label: 'In progress', type: 'info' },
  DONE: { label: 'Done', type: 'success' },
  CANCELLED: { label: 'Cancelled', type: 'danger' }
}

const sectionLabel = computed(() =>
  props.status === 'current' ? t('admin.menu.currentProjects') : t('admin.menu.futureProjects')
)

const filteredProjects = computed(() => {
  let list = projectPage.data

  if (projectSearch.value) {
    const keyword = projectSearch.value.toLowerCase()
    list = list.filter((p) => p.projectName?.toLowerCase().includes(keyword))
  }

  if (statusFilter.value !== 'all') {
    list = list.filter((p) => p.status === statusFilter.value)
  }

  return list
})

const statusMeta = (status) => statusLookup[status] ?? { label: status || '--', type: 'info' }

const projectStats = computed(() => {
  const counts = projectPage.data.reduce(
    (acc, item) => ({ ...acc, [item.status]: (acc[item.status] || 0) + 1 }),
    {}
  )
  return {
    total: projectPage.data.length,
    inProgress: counts.IN_PROGRESS || 0,
    done: counts.DONE || 0,
    pending: counts.PENDING || 0
  }
})

const fetchProjects = async () => {
  const data = await apiProjects.list({
    status: projectPage.status,
    page: projectPage.page - 1,
    size: projectPage.size
  })
  projectPage.data = data.content
  projectPage.total = data.totalElements
}

const handleProjectPage = (page) => {
  projectPage.page = page
  fetchProjects()
}

const goCreate = () => {
  router.push({ name: 'admin-projects-new', query: { bucket: props.status } })
}

const goEdit = (id) => {
  router.push({ name: 'admin-projects-edit', params: { id }, query: { from: props.status } })
}

const goView = (id) => {
  router.push({ name: 'admin-projects-edit', params: { id }, query: { mode: 'view', from: props.status } })
}

const formatDate = (value) => {
  if (!value) return '--'
  const date = typeof value === 'string' ? value : value.toISOString()
  return date.split('T')[0]
}

const formatCurrency = (value) => {
  if (value === null || value === undefined || value === '') return '--'
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0
  }).format(value)
}

const deleteProject = async (id) => {
  await apiProjects.remove(id)
  fetchProjects()
}

watch(
  () => props.status,
  (nextStatus) => {
    projectPage.status = nextStatus
    projectPage.page = 1
    fetchProjects()
  },
  { immediate: true }
)
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

.search-input {
  max-width: 240px;
}

.filter-select {
  width: 150px;
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>

<template>
  <div class="page">
    <SectionCard>
      <div class="page-header">
        <div class="title-group">
          <h2 class="page-title">{{ sectionLabel }}</h2>
        </div>
        <div class="header-actions">
          <el-button @click="fetchProjects()">{{ t('admin.actions.refresh') }}</el-button>
          <el-button type="primary" @click="goCreate">{{ t('admin.actions.add') }}</el-button>
        </div>
      </div>

      <div class="search-section">
        <el-input v-model="projectSearch" :placeholder="t('admin.filters.search')" class="search-input" clearable
          size="large">
          <template #prefix>
            <el-icon>
              <Search />
            </el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" size="large" class="filter-select" clearable>
          <el-option v-for="opt in statusOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </div>

      <div class="meta-row" v-if="projectStats.total">
        <div class="pill" :class="{ 'future-pill': props.status === 'future' }">
          <span class="pill-label">Tổng dự án</span>
          <strong>{{ projectStats.total }}</strong>
        </div>
        <div class="pill warning" :class="{ 'future-pill': props.status === 'future' }">
          <span class="pill-label">Đang duyệt</span>
          <strong>{{ projectStats.pending }}</strong>
        </div>
        <div class="pill info" v-if="props.status !== 'future'" :class="{ 'future-pill': props.status === 'future' }">
          <span class="pill-label">Đang thực hiện</span>
          <strong>{{ projectStats.inProgress }}</strong>
        </div>
        <div class="pill success" v-if="props.status !== 'future'"
          :class="{ 'future-pill': props.status === 'future' }">
          <span class="pill-label">Hoàn thành</span>
          <strong>{{ projectStats.done }}</strong>
        </div>
      </div>

      <el-table :data="filteredProjects" stripe :empty-text="t('admin.empty')" style="width: 100%">
        <el-table-column :label="t('admin.table.projectName')" min-width="260">
          <template #default="scope">
            <div class="title-col">
              <span class="title">{{ scope.row.projectName }}</span>
              <span class="subtitle" v-if="scope.row.clientName">{{ scope.row.clientName }}</span>
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
        <el-table-column :label="t('admin.table.startDate')" width="130">
          <template #default="scope">{{ formatDate(scope.row.startDate) }}</template>
        </el-table-column>
        <el-table-column :label="t('admin.table.endDate')" width="130">
          <template #default="scope">{{ formatDate(scope.row.endDate) }}</template>
        </el-table-column>
        <el-table-column :label="t('admin.form.budgetEstimated')" width="180">
          <template #default="scope">
            {{ formatCurrency(scope.row.budgetEstimated, scope.row.currencyUnit) }}
          </template>
        </el-table-column>
        <el-table-column width="240" align="center" :label="t('admin.actions.view')">
          <template #default="scope">
            <div class="action-buttons">
              <el-button @click="goView(scope.row.id)" plain size="small">
                {{ t('admin.actions.view') }}
              </el-button>
              <el-button type="primary" @click="goEdit(scope.row.id)" plain size="small">
                {{ t('admin.actions.edit') }}
              </el-button>
              <el-popconfirm :title="t('admin.confirm.deleteMessage')" @confirm="deleteProject(scope.row.id)">
                <template #reference>
                  <el-button type="danger" plain size="small">{{ t('admin.actions.delete') }}</el-button>
                </template>
              </el-popconfirm>
            </div>
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
import { Search } from '@element-plus/icons-vue'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiProjects } from '@/services/apiProjects'
import { apiUsers } from '@/services/apiUsers'

const props = defineProps({
  status: { type: String, default: 'current' }
})

const { t } = useI18n()
const router = useRouter()

const projectPage = reactive({ data: [], total: 0, page: 1, size: 10, status: props.status })
const projectSearch = ref('')
const statusFilter = ref('all')
const customers = ref([])

const statusOptions = computed(() => {
  // Nếu là dự án tương lai, chỉ hiển thị PENDING
  if (props.status === 'future') {
    return [
      { label: 'Tất cả', value: 'all' },
      { label: t('admin.projectStatus.PENDING'), value: 'PENDING' }
    ]
  }

  // Dự án hiện tại - hiển thị tất cả trạng thái
  return [
    { label: 'Tất cả', value: 'all' },
    { label: t('admin.projectStatus.PENDING'), value: 'PENDING' },
    { label: t('admin.projectStatus.APPROVED'), value: 'APPROVED' },
    { label: t('admin.projectStatus.IN_PROGRESS'), value: 'IN_PROGRESS' },
    { label: t('admin.projectStatus.DONE'), value: 'DONE' },
    { label: t('admin.projectStatus.CANCELLED'), value: 'CANCELLED' }
  ]
})

const statusLookup = computed(() => ({
  PENDING: { label: t('admin.projectStatus.PENDING'), type: 'warning' },
  APPROVED: { label: t('admin.projectStatus.APPROVED'), type: 'info' },
  IN_PROGRESS: { label: t('admin.projectStatus.IN_PROGRESS'), type: 'info' },
  DONE: { label: t('admin.projectStatus.DONE'), type: 'success' },
  CANCELLED: { label: t('admin.projectStatus.CANCELLED'), type: 'danger' }
}))

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

const statusMeta = (status) => statusLookup.value[status] ?? { label: status || '--', type: 'info' }

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

  // Map tên khách hàng vào dự án
  projectPage.data = data.content.map(project => {
    const customer = customers.value.find(c => c.id === project.clientId)
    return {
      ...project,
      clientName: customer ? `${customer.firstName || ''} ${customer.lastName || ''}`.trim() || customer.email : null
    }
  })
  projectPage.total = data.totalElements
}

const fetchCustomers = async () => {
  try {
    const data = await apiUsers.listCustomers({ page: 0, size: 1000 })
    customers.value = data.content || []
  } catch (error) {
    console.error('Failed to load customers:', error)
  }
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
  const date = new Date(value)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}/${month}/${year}`
}


const formatCurrency = (value, currencyUnit = 'VND') => {
  if (value === null || value === undefined || value === '') return '--'
  const locale = currencyUnit === 'JPY' ? 'ja-JP' : currencyUnit === 'USD' ? 'en-US' : 'vi-VN'
  const currency = currencyUnit || 'VND'
  const options = {
    style: 'currency',
    currency: currency,
    maximumFractionDigits: currency === 'JPY' ? 0 : 2
  }
  return new Intl.NumberFormat(locale, options).format(value)
}

const deleteProject = async (id) => {
  await apiProjects.remove(id)
  fetchProjects()
}

watch(
  () => props.status,
  async (nextStatus) => {
    projectPage.status = nextStatus
    projectPage.page = 1
    await fetchCustomers()
    await fetchProjects()
  },
  { immediate: true }
)
</script>

<style scoped>
.page {
  display: grid;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.title-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.search-section {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 240px;
  max-width: 400px;
}

.filter-select {
  width: 180px;
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

.pill.future-pill .pill-label {
  font-size: 11px;
  letter-spacing: 0.02em;
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

.project-col {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.project-col .title-col {
  flex: 1;
  min-width: 180px;
}

.project-col .status-tag {
  flex-shrink: 0;
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

.action-buttons {
  display: flex;
  gap: 6px;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
}

.action-buttons .el-button {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 6px;
}

.budget-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.budget-amount {
  font-weight: 500;
  color: #10b981;
}

.table-wrapper {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow-x: auto;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}
</style>

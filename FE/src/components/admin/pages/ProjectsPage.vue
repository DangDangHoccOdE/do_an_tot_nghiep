<template>
  <div class="grid">
    <SectionCard :eyebrow="sectionLabel" :title="sectionLabel">
      <template #actions>
        <div class="flex">
          <el-input
            v-model="projectSearch"
            :placeholder="t('admin.filters.search')"
            size="small"
            class="search-input"
            clearable
          />
          <el-button type="primary" size="small" @click="openProjectDrawer('create')">{{ t('admin.actions.add') }}</el-button>
          <el-button size="small" @click="fetchProjects()">{{ t('admin.actions.refresh') }}</el-button>
        </div>
      </template>
      <el-table :data="filteredProjects" stripe :empty-text="t('admin.empty')" style="width: 100%">
        <el-table-column prop="projectName" :label="t('admin.table.projectName')" />
        <el-table-column prop="status" :label="t('admin.table.status')" />
        <el-table-column prop="startDate" :label="t('admin.table.startDate')" />
        <el-table-column prop="endDate" :label="t('admin.table.endDate')" />
        <el-table-column width="160" :label="t('admin.actions.view')">
          <template #default="scope">
            <el-button text size="small" @click="openProjectDrawer('view', scope.row.id)">{{ t('admin.actions.view') }}</el-button>
            <el-button text size="small" type="primary" @click="openProjectDrawer('edit', scope.row.id)">{{ t('admin.actions.edit') }}</el-button>
            <el-popconfirm :title="t('admin.confirm.deleteMessage')" @confirm="deleteProject(scope.row.id)">
              <template #reference>
                <el-button text size="small" type="danger">{{ t('admin.actions.delete') }}</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          background
          layout="prev, pager, next"
          :current-page="projectPage.page"
          :page-size="projectPage.size"
          :total="projectPage.total"
          @current-change="handleProjectPage"
        />
      </div>
    </SectionCard>

    <el-drawer v-model="projectDrawer.visible" :title="projectDrawerTitle" size="50%">
      <el-form label-position="top" :disabled="projectDrawer.mode === 'view'">
        <el-form-item :label="t('admin.form.projectName')">
          <el-input v-model="projectForm.projectName" />
        </el-form-item>
        <el-form-item :label="t('admin.form.description')">
          <el-input v-model="projectForm.description" type="textarea" />
        </el-form-item>
        <div class="two-col">
          <el-form-item :label="t('admin.form.status')">
            <el-select v-model="projectForm.status" class="full">
              <el-option label="PENDING" value="PENDING" />
              <el-option label="APPROVED" value="APPROVED" />
              <el-option label="IN_PROGRESS" value="IN_PROGRESS" />
              <el-option label="DONE" value="DONE" />
              <el-option label="CANCELLED" value="CANCELLED" />
            </el-select>
          </el-form-item>
          <el-form-item :label="t('admin.form.teamId')">
            <el-input v-model="projectForm.teamId" />
          </el-form-item>
        </div>
        <div class="two-col">
          <el-form-item :label="t('admin.form.startDate')">
            <el-date-picker v-model="projectForm.startDate" type="date" class="full" />
          </el-form-item>
          <el-form-item :label="t('admin.form.endDate')">
            <el-date-picker v-model="projectForm.endDate" type="date" class="full" />
          </el-form-item>
        </div>
        <div class="two-col">
          <el-form-item :label="t('admin.form.clientId')">
            <el-input v-model="projectForm.clientId" />
          </el-form-item>
          <el-form-item :label="t('admin.form.budgetEstimated')">
            <el-input v-model.number="projectForm.budgetEstimated" type="number" />
          </el-form-item>
        </div>
        <el-form-item :label="t('admin.form.timelineEstimated')">
          <el-input v-model.number="projectForm.timelineEstimated" type="number" />
        </el-form-item>

        <div class="drawer-actions" v-if="projectDrawer.mode !== 'view'">
          <el-button @click="closeProjectDrawer">{{ t('admin.actions.cancel') }}</el-button>
          <el-button type="primary" @click="saveProject">{{ t('admin.actions.save') }}</el-button>
        </div>
        <div class="drawer-actions" v-else>
          <el-button type="primary" @click="closeProjectDrawer">{{ t('admin.actions.close') }}</el-button>
        </div>
      </el-form>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiProjects } from '@/services/apiProjects'

const props = defineProps({
  status: { type: String, default: 'current' }
})

const { t } = useI18n()

const projectPage = reactive({ data: [], total: 0, page: 1, size: 10, status: props.status })
const projectSearch = ref('')

const projectForm = reactive({
  projectName: '',
  description: '',
  status: 'PENDING',
  startDate: '',
  endDate: '',
  teamId: '',
  clientId: '',
  budgetEstimated: null,
  timelineEstimated: null
})

const projectDrawer = reactive({ visible: false, mode: 'create', id: null })

const sectionLabel = computed(() =>
  props.status === 'current' ? t('admin.menu.currentProjects') : t('admin.menu.futureProjects')
)

const filteredProjects = computed(() => {
  if (!projectSearch.value) return projectPage.data
  return projectPage.data.filter((p) => p.projectName?.toLowerCase().includes(projectSearch.value.toLowerCase()))
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

const openProjectDrawer = async (mode, id = null) => {
  projectDrawer.mode = mode
  projectDrawer.id = id
  if (id) {
    const detail = await apiProjects.detail(id)
    Object.assign(projectForm, detail)
  } else {
    resetProjectForm()
  }
  projectDrawer.visible = true
}

const normalizeDate = (value) => {
  if (!value) return ''
  if (typeof value === 'string') return value
  return value.toISOString().split('T')[0]
}

const saveProject = async () => {
  const payload = {
    ...projectForm,
    startDate: normalizeDate(projectForm.startDate),
    endDate: normalizeDate(projectForm.endDate)
  }
  if (projectDrawer.mode === 'edit' && projectDrawer.id) {
    await apiProjects.update(projectDrawer.id, payload)
  } else {
    await apiProjects.create(payload)
  }
  closeProjectDrawer()
  fetchProjects()
}

const deleteProject = async (id) => {
  await apiProjects.remove(id)
  fetchProjects()
}

const closeProjectDrawer = () => {
  projectDrawer.visible = false
}

const resetProjectForm = () => {
  Object.assign(projectForm, {
    projectName: '',
    description: '',
    status: 'PENDING',
    startDate: '',
    endDate: '',
    teamId: '',
    clientId: '',
    budgetEstimated: null,
    timelineEstimated: null
  })
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

const projectDrawerTitle = computed(() => {
  if (projectDrawer.mode === 'view') return `${t('admin.actions.view')} ${t('admin.table.projectName')}`
  if (projectDrawer.mode === 'edit') return `${t('admin.actions.edit')} ${t('admin.table.projectName')}`
  return `${t('admin.actions.add')} ${t('admin.table.projectName')}`
})
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 14px;
}

.flex {
  display: flex;
  gap: 8px;
  align-items: center;
}

.search-input {
  max-width: 260px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.two-col {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
}

.full {
  width: 100%;
}
</style>

<template>
  <div class="admin-shell">
    <AdminSidebar :items="navItems" :active="activeSection" @select="handleSelect" />

    <div class="admin-main">
      <div class="hero">
        <div>
          <p class="eyebrow">{{ t('admin.title') }}</p>
          <h1>{{ t('admin.subtitle') }}</h1>
        </div>
        <div class="badges">
          <span class="badge">JWT</span>
          <span class="badge">RBAC</span>
          <span class="badge">Element+</span>
        </div>
      </div>

      <div class="grid">
        <SectionCard
          v-if="activeSection === 'current' || activeSection === 'future'"
          :eyebrow="sectionLabel"
          :title="sectionLabel"
        >
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

        <SectionCard
          v-if="activeSection === 'teams'"
          :eyebrow="t('admin.menu.teams')"
          :title="t('admin.menu.teams')"
        >
          <template #actions>
            <div class="flex">
              <el-input
                v-model="teamSearch"
                :placeholder="t('admin.filters.search')"
                size="small"
                class="search-input"
                clearable
              />
              <el-button type="primary" size="small" @click="openTeamDrawer('create')">{{ t('admin.actions.add') }}</el-button>
              <el-button size="small" @click="fetchTeams()">{{ t('admin.actions.refresh') }}</el-button>
            </div>
          </template>
          <el-table :data="filteredTeams" stripe :empty-text="t('admin.empty')" style="width: 100%">
            <el-table-column prop="name" :label="t('admin.table.teamName')" />
            <el-table-column prop="description" :label="t('admin.table.description')" />
            <el-table-column :label="t('admin.table.members')">
              <template #default="scope">
                <div class="members">
                  <span v-for="member in scope.row.members" :key="member.userId" class="member-pill">
                    {{ member.userId?.slice(0, 6) }} - {{ member.roleInTeam }}
                  </span>
                </div>
              </template>
            </el-table-column>
            <el-table-column width="160" :label="t('admin.actions.view')">
              <template #default="scope">
                <el-button text size="small" @click="openTeamDrawer('view', scope.row.id)">{{ t('admin.actions.view') }}</el-button>
                <el-button text size="small" type="primary" @click="openTeamDrawer('edit', scope.row.id)">{{ t('admin.actions.edit') }}</el-button>
                <el-popconfirm :title="t('admin.confirm.deleteMessage')" @confirm="deleteTeam(scope.row.id)">
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
              :current-page="teamPage.page"
              :page-size="teamPage.size"
              :total="teamPage.total"
              @current-change="handleTeamPage"
            />
          </div>
        </SectionCard>

        <SectionCard
          v-if="activeSection === 'tasks'"
          full
          :eyebrow="t('admin.menu.tasks')"
          :title="t('admin.menu.tasks')"
        >
          <template #actions>
            <div class="flex">
              <el-input
                v-model="projectIdFilter"
                :placeholder="t('admin.filters.projectId')"
                size="small"
                class="project-input"
                clearable
              />
              <el-button type="primary" size="small" @click="fetchTasks">{{ t('admin.actions.loadTasks') }}</el-button>
            </div>
          </template>
          <el-table :data="tasks" stripe :empty-text="t('admin.empty')" style="width: 100%">
            <el-table-column prop="title" :label="t('admin.table.title')" />
            <el-table-column prop="status" :label="t('admin.table.status')" />
            <el-table-column prop="assignedToUserId" :label="t('admin.table.assignee')" />
            <el-table-column prop="dueDate" :label="t('admin.table.dueDate')" />
          </el-table>
        </SectionCard>
      </div>
    </div>

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

    <el-drawer v-model="teamDrawer.visible" :title="teamDrawerTitle" size="35%">
      <el-form label-position="top" :disabled="teamDrawer.mode === 'view'">
        <el-form-item :label="t('admin.form.teamName')">
          <el-input v-model="teamForm.name" />
        </el-form-item>
        <el-form-item :label="t('admin.form.teamDescription')">
          <el-input v-model="teamForm.description" type="textarea" />
        </el-form-item>
        <div class="drawer-actions" v-if="teamDrawer.mode !== 'view'">
          <el-button @click="closeTeamDrawer">{{ t('admin.actions.cancel') }}</el-button>
          <el-button type="primary" @click="saveTeam">{{ t('admin.actions.save') }}</el-button>
        </div>
        <div class="drawer-actions" v-else>
          <el-button type="primary" @click="closeTeamDrawer">{{ t('admin.actions.close') }}</el-button>
        </div>
      </el-form>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { get, post, put, del } from '@/utils/http'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import SectionCard from '@/components/admin/SectionCard.vue'

const { t } = useI18n()

const activeSection = ref('current')
const projectPage = reactive({ data: [], total: 0, page: 1, size: 10, status: 'current' })
const teamPage = reactive({ data: [], total: 0, page: 1, size: 10 })
const tasks = ref([])
const projectIdFilter = ref('')
const projectSearch = ref('')
const teamSearch = ref('')

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

const teamForm = reactive({
  name: '',
  description: ''
})

const projectDrawer = reactive({ visible: false, mode: 'create', id: null })
const teamDrawer = reactive({ visible: false, mode: 'create', id: null })

const navItems = computed(() => [
  { key: 'current', label: t('admin.menu.currentProjects'), icon: '📈' },
  { key: 'future', label: t('admin.menu.futureProjects'), icon: '🗓️' },
  { key: 'teams', label: t('admin.menu.teams'), icon: '👥' },
  { key: 'tasks', label: t('admin.menu.tasks'), icon: '✅' }
])

const sectionLabel = computed(() =>
  activeSection.value === 'current' ? t('admin.menu.currentProjects') : t('admin.menu.futureProjects')
)

const filteredProjects = computed(() => {
  if (!projectSearch.value) return projectPage.data
  return projectPage.data.filter((p) => p.projectName?.toLowerCase().includes(projectSearch.value.toLowerCase()))
})

const filteredTeams = computed(() => {
  if (!teamSearch.value) return teamPage.data
  return teamPage.data.filter((tItem) => tItem.name?.toLowerCase().includes(teamSearch.value.toLowerCase()))
})

const handleSelect = (key) => {
  activeSection.value = key
  if (key === 'current' || key === 'future') {
    projectPage.page = 1
    projectPage.status = key === 'current' ? 'current' : 'future'
    fetchProjects()
  }
  if (key === 'teams') {
    fetchTeams()
  }
}

const fetchProjects = async () => {
  const data = await get('/api/v1/projects', {
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
    const detail = await get(`/api/v1/projects/${id}`)
    Object.assign(projectForm, detail)
  } else {
    resetProjectForm()
  }
  projectDrawer.visible = true
}

const saveProject = async () => {
  const payload = { ...projectForm }
  if (projectDrawer.mode === 'edit' && projectDrawer.id) {
    await put(`/api/v1/projects/${projectDrawer.id}`, payload)
  } else {
    await post('/api/v1/projects', payload)
  }
  closeProjectDrawer()
  fetchProjects()
}

const deleteProject = async (id) => {
  await del(`/api/v1/projects/${id}`)
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

const fetchTeams = async () => {
  const data = await get('/api/v1/teams', {
    page: teamPage.page - 1,
    size: teamPage.size
  })
  teamPage.data = data.content
  teamPage.total = data.totalElements
}

const handleTeamPage = (page) => {
  teamPage.page = page
  fetchTeams()
}

const openTeamDrawer = async (mode, id = null) => {
  teamDrawer.mode = mode
  teamDrawer.id = id
  if (id) {
    const detail = await get(`/api/v1/teams/${id}`)
    Object.assign(teamForm, detail)
  } else {
    resetTeamForm()
  }
  teamDrawer.visible = true
}

const saveTeam = async () => {
  const payload = { ...teamForm }
  if (teamDrawer.mode === 'edit' && teamDrawer.id) {
    await put(`/api/v1/teams/${teamDrawer.id}`, payload)
  } else {
    await post('/api/v1/teams', payload)
  }
  closeTeamDrawer()
  fetchTeams()
}

const deleteTeam = async (id) => {
  await del(`/api/v1/teams/${id}`)
  fetchTeams()
}

const closeTeamDrawer = () => {
  teamDrawer.visible = false
}

const resetTeamForm = () => {
  Object.assign(teamForm, { name: '', description: '' })
}

const fetchTasks = async () => {
  if (!projectIdFilter.value) return
  const data = await get(`/api/v1/projects/${projectIdFilter.value}/tasks`)
  tasks.value = data
}

onMounted(() => {
  fetchProjects()
  fetchTeams()
})

const projectDrawerTitle = computed(() => {
  if (projectDrawer.mode === 'view') return `${t('admin.actions.view')} ${t('admin.table.projectName')}`
  if (projectDrawer.mode === 'edit') return `${t('admin.actions.edit')} ${t('admin.table.projectName')}`
  return `${t('admin.actions.add')} ${t('admin.table.projectName')}`
})

const teamDrawerTitle = computed(() => {
  if (teamDrawer.mode === 'view') return `${t('admin.actions.view')} ${t('admin.table.teamName')}`
  if (teamDrawer.mode === 'edit') return `${t('admin.actions.edit')} ${t('admin.table.teamName')}`
  return `${t('admin.actions.add')} ${t('admin.table.teamName')}`
})
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
  padding: 22px;
  background: radial-gradient(circle at 20% 20%, rgba(37, 99, 235, 0.08), transparent 25%),
    radial-gradient(circle at 80% 0%, rgba(16, 185, 129, 0.08), transparent 30%),
    #f4f6fb;
}

.admin-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  background: linear-gradient(120deg, #111827 0%, #1f2937 55%, #111827 100%);
  color: #f9fafb;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.24);
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
  color: #9ca3af;
}

h1 {
  margin: 6px 0 0;
  font-size: 22px;
}

.badges {
  display: flex;
  gap: 8px;
}

.badge {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #e5e7eb;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 14px;
}

.members {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.member-pill {
  background: #eef2ff;
  color: #4338ca;
  border-radius: 12px;
  padding: 4px 8px;
  font-size: 12px;
  border: 1px solid #e0e7ff;
}

.flex {
  display: flex;
  gap: 8px;
  align-items: center;
}

.project-input,
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

@media (max-width: 960px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>

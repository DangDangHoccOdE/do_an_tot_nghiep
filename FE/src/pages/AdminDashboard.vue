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
          v-if="activeSection === 'current'"
          :eyebrow="t('admin.menu.currentProjects')"
          :title="t('admin.menu.currentProjects')"
          :description="t('admin.subtitle')"
        >
          <template #actions>
            <el-button type="primary" size="small" @click="fetchCurrentProjects">{{ t('admin.actions.refresh') }}</el-button>
          </template>
          <el-table :data="currentProjects" stripe :empty-text="t('admin.empty')" style="width: 100%">
            <el-table-column prop="projectName" :label="t('admin.table.projectName')" />
            <el-table-column prop="status" :label="t('admin.table.status')" />
            <el-table-column prop="startDate" :label="t('admin.table.startDate')" />
            <el-table-column prop="endDate" :label="t('admin.table.endDate')" />
          </el-table>
        </SectionCard>

        <SectionCard
          v-if="activeSection === 'future'"
          :eyebrow="t('admin.menu.futureProjects')"
          :title="t('admin.menu.futureProjects')"
          :description="t('admin.subtitle')"
        >
          <template #actions>
            <el-button type="primary" size="small" @click="fetchFutureProjects">{{ t('admin.actions.refresh') }}</el-button>
          </template>
          <el-table :data="futureProjects" stripe :empty-text="t('admin.empty')" style="width: 100%">
            <el-table-column prop="projectName" :label="t('admin.table.projectName')" />
            <el-table-column prop="status" :label="t('admin.table.status')" />
            <el-table-column prop="startDate" :label="t('admin.table.startDate')" />
            <el-table-column prop="endDate" :label="t('admin.table.endDate')" />
          </el-table>
        </SectionCard>

        <SectionCard
          v-if="activeSection === 'teams'"
          :eyebrow="t('admin.menu.teams')"
          :title="t('admin.menu.teams')"
          description=""
        >
          <template #actions>
            <el-button type="primary" size="small" @click="fetchTeams">{{ t('admin.actions.refresh') }}</el-button>
          </template>
          <el-table :data="teams" stripe :empty-text="t('admin.empty')" style="width: 100%">
            <el-table-column prop="name" :label="t('admin.table.teamName')" />
            <el-table-column prop="description" :label="t('admin.table.description')" />
            <el-table-column :label="t('admin.table.members')">
              <template #default="scope">
                <div class="members">
                  <span v-for="member in scope.row.members" :key="member.userId" class="member-pill">
                    {{ member.userId.slice(0, 6) }} - {{ member.roleInTeam }}
                  </span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </SectionCard>

        <SectionCard
          v-if="activeSection === 'tasks'"
          full
          :eyebrow="t('admin.menu.tasks')"
          :title="t('admin.menu.tasks')"
          :description="t('admin.subtitle')"
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { get } from '@/utils/http'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
import SectionCard from '@/components/admin/SectionCard.vue'

const { t } = useI18n()

const activeSection = ref('current')
const currentProjects = ref([])
const futureProjects = ref([])
const teams = ref([])
const tasks = ref([])
const projectIdFilter = ref('')

const navItems = computed(() => [
  { key: 'current', label: t('admin.menu.currentProjects'), icon: '📈' },
  { key: 'future', label: t('admin.menu.futureProjects'), icon: '🗓️' },
  { key: 'teams', label: t('admin.menu.teams'), icon: '👥' },
  { key: 'tasks', label: t('admin.menu.tasks'), icon: '✅' }
])

const handleSelect = (key) => {
  activeSection.value = key
}

const fetchCurrentProjects = async () => {
  const data = await get('/api/v1/projects/current')
  currentProjects.value = data
}

const fetchFutureProjects = async () => {
  const data = await get('/api/v1/projects/future')
  futureProjects.value = data
}

const fetchTeams = async () => {
  const data = await get('/api/v1/teams')
  teams.value = data
}

const fetchTasks = async () => {
  if (!projectIdFilter.value) return
  const data = await get(`/api/v1/projects/${projectIdFilter.value}/tasks`)
  tasks.value = data
}

onMounted(() => {
  fetchCurrentProjects()
  fetchFutureProjects()
  fetchTeams()
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

.project-input {
  max-width: 260px;
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

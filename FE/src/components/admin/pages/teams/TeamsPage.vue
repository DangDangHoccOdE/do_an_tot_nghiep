<template>
  <div class="page">
    <SectionCard>
      <div class="page-header">
        <div class="title-group">
          <h2 class="page-title">{{ t('admin.menu.teams') }}</h2>
        </div>
        <div class="header-actions">
          <UiButton variant="refresh" @click="fetchTeams()" />
          <UiButton variant="add" @click="goCreate" />
        </div>
      </div>

      <div class="search-section">
        <div class="search-controls">
          <el-input v-model="teamSearch" :placeholder="t('admin.filters.search')" class="search-input" clearable
            size="large">
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
        </div>
        <div class="search-actions">
          <UiButton variant="delete" :label="t('admin.actions.deleteSelected')" :disabled="!selectedTeamIds.length"
            @click="() => handleBulkDelete(selectedTeamIds)" />
        </div>
      </div>

      <div class="meta-row" v-if="teamStats.total">
        <div class="pill">
          <span class="pill-label">{{ t('admin.stats.teamsTotal') }}</span>
          <strong>{{ teamStats.total }}</strong>
        </div>
        <div class="pill info">
          <span class="pill-label">{{ t('admin.stats.membersTotal') }}</span>
          <strong>{{ teamStats.members }}</strong>
        </div>
      </div>

      <TableListView :data="filteredTeams" :columns="tableColumns" :total="teamPage.total" :current-page="teamPage.page"
        :page-size="teamPage.size" :loading="loading" :selectable="true"
        @update:current-page="(page) => teamPage.page = page" @update:page-size="(size) => teamPage.size = size"
        @page-change="handleTeamPage" @bulk-delete="handleBulkDelete" @selection-change="onTeamSelectionChange"
        @row-click="handleRowClick">
        <template #teamName="{ row }">
          <div class="title-col">
            <span class="title">{{ row.name }}</span>
            <span class="subtitle" v-if="row.description">{{ row.description }}</span>
          </div>
        </template>

        <template #projectName="{ row }">
          <el-tag v-if="row.projectName" type="primary" size="small" effect="plain">
            {{ row.projectName }}
          </el-tag>
          <span v-else class="muted">{{ t('admin.empty') }}</span>
        </template>

        <template #members="{ row }">
          <div class="members">
            <el-tag v-for="member in row.members" :key="member.userId" size="small" effect="plain"
              :type="memberRoleType(member.roleInTeam)">
              {{ member.userId?.slice(0, 6) }} • {{ member.roleInTeam || t('admin.table.members') }}
            </el-tag>
            <span v-if="!row.members?.length" class="muted">{{ t('admin.empty') }}</span>
          </div>
        </template>

        <template #memberCount="{ row }">
          <div class="count-pill">{{ row.members?.length || 0 }}</div>
        </template>

        <template #actions="{ row }">
          <el-space wrap size="4">
            <UiButton variant="edit" size="small" @click.stop="goEdit(row.id)" />
            <UiButton variant="delete" size="small" @click.stop="() => confirmDeleteTeam(row.id)" />
          </el-space>
        </template>
      </TableListView>
    </SectionCard>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox, ElTag, ElButton, ElSpace } from 'element-plus'
import UiButton from '@/components/common/UiButton.vue'
import { Search } from '@element-plus/icons-vue'
import SectionCard from '@/components/admin/SectionCard.vue'
import TableListView from '@/components/common/TableListView.vue'
import { apiTeams } from '@/services/apiTeams'

const { t } = useI18n()
const router = useRouter()

const teamPage = reactive({ data: [], total: 0, page: 1, size: 10 })
const teamSearch = ref('')
const loading = ref(false)
const selectedTeamIds = ref([])

const filteredTeams = computed(() => {
  if (!teamSearch.value) return teamPage.data
  return teamPage.data.filter((tItem) => tItem.name?.toLowerCase().includes(teamSearch.value.toLowerCase()))
})

const teamStats = computed(() => {
  const memberTotal = teamPage.data.reduce((acc, item) => acc + (item.members?.length || 0), 0)
  return { total: teamPage.data.length, members: memberTotal }
})

const tableColumns = computed(() => [
  {
    prop: 'name',
    label: t('admin.table.teamName'),
    width: 200,
    slot: 'teamName'
  },
  {
    prop: 'projectName',
    label: t('admin.table.projectName'),
    width: 180,
    slot: 'projectName'
  },
  {
    prop: 'members',
    label: t('admin.table.members'),
    width: 220,
    slot: 'members'
  },
  {
    prop: 'memberCount',
    label: t('admin.table.memberCount'),
    width: 120,
    slot: 'memberCount'
  },
  {
    prop: 'actions',
    label: t('admin.actions.view'),
    width: 200,
    slot: 'actions'
  }
])

const memberRoleType = (role) => {
  if (!role) return 'info'
  if (role.toLowerCase().includes('lead')) return 'success'
  if (role.toLowerCase().includes('qa')) return 'warning'
  return 'info'
}

const fetchTeams = async () => {
  loading.value = true
  try {
    const data = await apiTeams.list({
      page: teamPage.page - 1,
      size: teamPage.size
    })
    teamPage.data = data.content
    teamPage.total = data.totalElements
  } finally {
    loading.value = false
  }
}

const handleTeamPage = (page) => {
  teamPage.page = page
  fetchTeams()
}

const goCreate = () => router.push({ name: 'admin-teams-new' })

const goEdit = (id) => router.push({ name: 'admin-teams-edit', params: { id } })

const goView = (id) => router.push({ name: 'admin-teams-edit', params: { id }, query: { mode: 'view' } })

const deleteTeam = async (id) => {
  try {
    await apiTeams.remove(id)
    ElMessage.success(t('message.MSG0102', { count: 1, entity: t('admin.entities.team') }))
    await fetchTeams()
  } catch (error) {
    ElMessage.error(t('message.ERR011', { entity: t('admin.entities.team') }))
  }
}

const confirmDeleteTeam = async (id) => {
  try {
    await ElMessageBox.confirm(
      t('message.MSG0101', { count: 1, entity: t('admin.entities.team') }),
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

  await deleteTeam(id)
}

const handleBulkDelete = async (selectedIds) => {
  if (!selectedIds?.length) return
  try {
    await ElMessageBox.confirm(
      t('message.MSG0101', { count: selectedIds.length, entity: t('admin.entities.team') }),
      t('confirm'),
      {
        confirmButtonText: t('admin.actions.delete'),
        cancelButtonText: t('admin.actions.cancel'),
        type: 'warning'
      }
    )

    loading.value = true
    await apiTeams.removeBulk(selectedIds)
    ElMessage.success(t('message.MSG0102', { count: selectedIds.length, entity: t('admin.entities.team') }))
    await fetchTeams()
    selectedTeamIds.value = []
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('message.ERR011', { entity: t('admin.entities.team') }))
    }
  } finally {
    loading.value = false
  }
}

const onTeamSelectionChange = (ids) => {
  selectedTeamIds.value = ids
}

const handleRowClick = (row) => {
  goView(row.id)
}

onMounted(() => {
  fetchTeams()
})
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

.search-controls {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  flex: 1;
  min-width: 240px;
}

.search-input {
  flex: 1;
  min-width: 240px;
  max-width: 400px;
}

.search-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
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

.pill.info {
  background: #f1f5ff;
  border-color: #dfe6ff;
  color: #1d4ed8;
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

.members {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.muted {
  color: #9ca3af;
}

.count-pill {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 6px 10px;
  display: inline-flex;
  font-weight: 700;
  color: #0f172a;
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

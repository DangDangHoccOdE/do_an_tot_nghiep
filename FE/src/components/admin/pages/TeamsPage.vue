<template>
  <div class="grid">
    <SectionCard :eyebrow="t('admin.menu.teams')" :title="t('admin.menu.teams')">
      <template #actions>
        <div class="action-row">
          <el-input v-model="teamSearch" :placeholder="t('admin.filters.search')" size="small" class="search-input"
            clearable />
          <el-button type="primary" size="small" @click="goCreate">{{ t('admin.actions.add') }}</el-button>
          <el-button size="small" @click="fetchTeams()">{{ t('admin.actions.refresh') }}</el-button>
        </div>
      </template>

      <div class="meta-row" v-if="teamStats.total">
        <div class="pill">
          <span class="pill-label">Tổng đội</span>
          <strong>{{ teamStats.total }}</strong>
        </div>
        <div class="pill info">
          <span class="pill-label">Tổng thành viên</span>
          <strong>{{ teamStats.members }}</strong>
        </div>
      </div>

      <el-table :data="filteredTeams" stripe :empty-text="t('admin.empty')" style="width: 100%">
        <el-table-column :label="t('admin.table.teamName')" min-width="200">
          <template #default="scope">
            <div class="title-col">
              <span class="title">{{ scope.row.name }}</span>
              <span class="subtitle" v-if="scope.row.description">{{ scope.row.description }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.table.members')" min-width="220">
          <template #default="scope">
            <div class="members">
              <el-tag v-for="member in scope.row.members" :key="member.userId" size="small" effect="plain"
                :type="memberRoleType(member.roleInTeam)">
                {{ member.userId?.slice(0, 6) }} • {{ member.roleInTeam || 'Member' }}
              </el-tag>
              <span v-if="!scope.row.members?.length" class="muted">--</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="t('admin.table.members')" width="120">
          <template #default="scope">
            <div class="count-pill">{{ scope.row.members?.length || 0 }} người</div>
          </template>
        </el-table-column>
        <el-table-column width="200" :label="t('admin.actions.view')">
          <template #default="scope">
            <el-space wrap size="4">
              <el-button text size="small" @click="goView(scope.row.id)">{{ t('admin.actions.view')
              }}</el-button>
              <el-button text size="small" type="primary" @click="goEdit(scope.row.id)">{{
                t('admin.actions.edit') }}</el-button>
              <el-popconfirm :title="t('admin.confirm.deleteMessage')" @confirm="deleteTeam(scope.row.id)">
                <template #reference>
                  <el-button text size="small" type="danger">{{ t('admin.actions.delete') }}</el-button>
                </template>
              </el-popconfirm>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination background layout="prev, pager, next" :current-page="teamPage.page" :page-size="teamPage.size"
          :total="teamPage.total" @current-change="handleTeamPage" />
      </div>
    </SectionCard>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiTeams } from '@/services/apiTeams'

const { t } = useI18n()
const router = useRouter()

const teamPage = reactive({ data: [], total: 0, page: 1, size: 10 })
const teamSearch = ref('')

const filteredTeams = computed(() => {
  if (!teamSearch.value) return teamPage.data
  return teamPage.data.filter((tItem) => tItem.name?.toLowerCase().includes(teamSearch.value.toLowerCase()))
})

const teamStats = computed(() => {
  const memberTotal = teamPage.data.reduce((acc, item) => acc + (item.members?.length || 0), 0)
  return { total: teamPage.data.length, members: memberTotal }
})

const memberRoleType = (role) => {
  if (!role) return 'info'
  if (role.toLowerCase().includes('lead')) return 'success'
  if (role.toLowerCase().includes('qa')) return 'warning'
  return 'info'
}

const fetchTeams = async () => {
  const data = await apiTeams.list({
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

const goCreate = () => router.push({ name: 'admin-teams-new' })

const goEdit = (id) => router.push({ name: 'admin-teams-edit', params: { id } })

const goView = (id) => router.push({ name: 'admin-teams-edit', params: { id }, query: { mode: 'view' } })

const deleteTeam = async (id) => {
  await apiTeams.remove(id)
  fetchTeams()
}

onMounted(() => {
  fetchTeams()
})
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

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>

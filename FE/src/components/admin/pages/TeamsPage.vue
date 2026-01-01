<template>
  <div class="grid">
    <SectionCard :eyebrow="t('admin.menu.teams')" :title="t('admin.menu.teams')">
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
import { computed, reactive, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiTeams } from '@/services/apiTeams'

const { t } = useI18n()

const teamPage = reactive({ data: [], total: 0, page: 1, size: 10 })
const teamSearch = ref('')

const teamForm = reactive({
  name: '',
  description: ''
})

const teamDrawer = reactive({ visible: false, mode: 'create', id: null })

const filteredTeams = computed(() => {
  if (!teamSearch.value) return teamPage.data
  return teamPage.data.filter((tItem) => tItem.name?.toLowerCase().includes(teamSearch.value.toLowerCase()))
})

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

const openTeamDrawer = async (mode, id = null) => {
  teamDrawer.mode = mode
  teamDrawer.id = id
  if (id) {
    const detail = await apiTeams.detail(id)
    Object.assign(teamForm, detail)
  } else {
    resetTeamForm()
  }
  teamDrawer.visible = true
}

const saveTeam = async () => {
  const payload = { ...teamForm }
  if (teamDrawer.mode === 'edit' && teamDrawer.id) {
    await apiTeams.update(teamDrawer.id, payload)
  } else {
    await apiTeams.create(payload)
  }
  closeTeamDrawer()
  fetchTeams()
}

const deleteTeam = async (id) => {
  await apiTeams.remove(id)
  fetchTeams()
}

const closeTeamDrawer = () => {
  teamDrawer.visible = false
}

const resetTeamForm = () => {
  Object.assign(teamForm, { name: '', description: '' })
}

onMounted(() => {
  fetchTeams()
})

const teamDrawerTitle = computed(() => {
  if (teamDrawer.mode === 'view') return `${t('admin.actions.view')} ${t('admin.table.teamName')}`
  if (teamDrawer.mode === 'edit') return `${t('admin.actions.edit')} ${t('admin.table.teamName')}`
  return `${t('admin.actions.add')} ${t('admin.table.teamName')}`
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
</style>

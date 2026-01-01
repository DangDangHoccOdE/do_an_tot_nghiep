<template>
  <div class="grid">
    <SectionCard full :eyebrow="t('admin.menu.tasks')" :title="t('admin.menu.tasks')">
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
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiTasks } from '@/services/apiTasks'

const { t } = useI18n()

const tasks = ref([])
const projectIdFilter = ref('')

const fetchTasks = async () => {
  if (!projectIdFilter.value) return
  const data = await apiTasks.byProject(projectIdFilter.value)
  tasks.value = data
}
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

.project-input {
  max-width: 260px;
}
</style>

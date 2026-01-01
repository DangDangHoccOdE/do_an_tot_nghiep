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

      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const navItems = computed(() => [
  { key: 'current', label: t('admin.menu.currentProjects'), icon: '📈', routeName: 'admin-projects-current' },
  { key: 'future', label: t('admin.menu.futureProjects'), icon: '🗓️', routeName: 'admin-projects-future' },
  { key: 'teams', label: t('admin.menu.teams'), icon: '👥', routeName: 'admin-teams' },
  { key: 'tasks', label: t('admin.menu.tasks'), icon: '✅', routeName: 'admin-tasks' }
])

const activeSection = computed(() => route.meta.sectionKey || 'current')

const handleSelect = (key) => {
  const target = navItems.value.find((item) => item.key === key)
  if (target?.routeName) {
    router.push({ name: target.routeName })
  }
}
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

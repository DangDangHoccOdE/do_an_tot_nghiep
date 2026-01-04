<template>
  <div class="admin-shell" :class="{ collapsed: isSidebarCollapsed }">
    <AdminSidebar :items="navItems" :active="activeSection" :collapsed="isSidebarCollapsed"
      :user-initials="userInitials" :user-name="auth.user?.fullName || auth.user?.email" :user-email="auth.user?.email"
      @select="handleSelect" @logout="handleLogout" @home="goHome" @toggle="toggleSidebar" />

    <div class="admin-main">
      <div class="content-card">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import AdminSidebar from '@/components/admin/AdminSidebar.vue'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const isSidebarCollapsed = ref(false)

const navItems = computed(() => [
  { key: 'current', label: t('admin.menu.currentProjects'), icon: '📈', routeName: 'admin-projects-current' },
  { key: 'future', label: t('admin.menu.futureProjects'), icon: '🗓️', routeName: 'admin-projects-future' },
  { key: 'teams', label: t('admin.menu.teams'), icon: '👥', routeName: 'admin-teams' },
  { key: 'tasks', label: t('admin.menu.tasks'), icon: '✅', routeName: 'admin-tasks' },
  { key: 'customers', label: 'Khách hàng', icon: '👤', routeName: 'admin-customers' },
  { key: 'staff', label: 'Nhân viên', icon: '👨‍💼', routeName: 'admin-staff' },
  { key: 'users', label: 'Người dùng', icon: '🔧', routeName: 'admin-users' }
])

const activeSection = computed(() => route.meta.sectionKey || 'current')

const handleSelect = (key) => {
  const target = navItems.value.find((item) => item.key === key)
  if (target?.routeName) {
    router.push({ name: target.routeName })
  }
}

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

const handleLogout = () => {
  auth.logout()
  router.push('/')
}

const goHome = () => {
  router.push('/')
}

const userInitials = computed(() => {
  if (auth.user?.fullName) {
    const parts = auth.user.fullName.trim().split(' ')
    if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    return auth.user.fullName.slice(0, 2).toUpperCase()
  }
  if (auth.user?.email) return auth.user.email.slice(0, 2).toUpperCase()
  return 'A'
})
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 18px;
  padding: 16px 18px 22px;
  background: radial-gradient(circle at 12% 18%, rgba(206, 24, 30, 0.08), transparent 22%),
    radial-gradient(circle at 70% 0%, rgba(0, 122, 204, 0.08), transparent 26%),
    #f7f8fb;
  transition: grid-template-columns 0.2s ease;
}

.admin-shell.collapsed {
  grid-template-columns: 88px 1fr;
}

.admin-main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.content-card {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  min-height: 95vh;
}

@media (max-width: 960px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }
}
</style>

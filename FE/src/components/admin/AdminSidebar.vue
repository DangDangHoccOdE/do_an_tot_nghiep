<template>
  <aside :class="['sidebar', { collapsed }]" @mouseleave="hideDropdown">
    <div class="brand">
      <div class="brand-main">
        <LuvinaLogo class="brand-logo" />
      </div>
    </div>

    <div class="language-box" v-if="!collapsed">
      <LanguageSwitcher />
    </div>

    <nav>
      <button v-for="item in items" :key="item.key" :class="['nav-item', { active: item.key === active, collapsed }]"
        type="button" @click="$emit('select', item.key)">
        <span class="icon">{{ item.icon }}</span>
        <span v-if="!collapsed">{{ item.label }}</span>
      </button>
    </nav>

    <div class="sidebar-footer" v-if="userInitials || userName">
      <button class="avatar-btn" type="button" :aria-label="showDropdown ? 'Ẩn menu' : 'Mở menu'"
        @click="toggleDropdown">
        <div class="avatar" aria-hidden="true">{{ userInitials }}</div>
        <div class="user-info" v-if="!collapsed">
          <p class="user-name">{{ userName }}</p>
          <p class="user-email">{{ userEmail }}</p>
        </div>
        <span class="caret" aria-hidden="true">
          <component :is="showDropdown ? ChevronUpIcon : ChevronDownIcon" />
        </span>
      </button>

      <transition name="fade">
        <div v-if="showDropdown" class="avatar-dropdown">
          <button type="button" @click="handleHome">{{ $t('admin.sidebar.home') }}</button>
          <button type="button" class="danger" @click="handleLogout">{{ $t('admin.sidebar.logout') }}</button>
        </div>
      </transition>
    </div>

    <button class="collapse-handle" type="button"
      :aria-label="collapsed ? $t('admin.sidebar.expand') : $t('admin.sidebar.collapse')" @click="$emit('toggle')">
      <component :is="collapsed ? MenuUnfoldIcon : MenuFoldIcon" class="collapse-icon" />
    </button>
  </aside>
</template>

<script setup>
import { defineEmits, defineProps, ref } from 'vue'
import LuvinaLogo from '@/components/icons/LuvinaLogo.vue'
import MenuFoldIcon from '@/components/icons/MenuFoldIcon.vue'
import MenuUnfoldIcon from '@/components/icons/MenuUnfoldIcon.vue'
import ChevronDownIcon from '@/components/icons/ChevronDownIcon.vue'
import ChevronUpIcon from '@/components/icons/ChevronUpIcon.vue'
import LanguageSwitcher from '@/components/common/LanguageSwitcher.vue'

defineProps({
  items: { type: Array, required: true },
  active: { type: String, required: true },
  collapsed: { type: Boolean, default: false },
  userInitials: { type: String, default: '' },
  userName: { type: String, default: '' },
  userEmail: { type: String, default: '' }
})

const emit = defineEmits(['select', 'logout', 'home', 'toggle'])
const showDropdown = ref(false)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const hideDropdown = () => {
  showDropdown.value = false
}

const handleLogout = () => {
  emit('logout')
  hideDropdown()
}

const handleHome = () => {
  emit('home')
  hideDropdown()
}
</script>

<style scoped>
.sidebar {
  position: relative;
  width: 240px;
  background: linear-gradient(180deg, #a1121f 0%, #5a0d15 100%);
  color: #fff7f5;
  border-radius: 18px;
  padding: 20px 14px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.28);
  transition: width 0.2s ease, padding 0.2s ease;
}

.sidebar.collapsed {
  width: 88px;
  padding: 18px 10px;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  color: inherit;
  width: 100%;
  box-sizing: border-box;
  justify-content: center;
  background-color: #fff;
}

.brand-logo {
  width: 150px;
  height: 50px;
}

.brand-main {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  justify-content: center;
}

nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.nav-item {
  border: none;
  background: transparent;
  color: #ffe9e6;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  width: 100%;
  box-sizing: border-box;
}

.nav-item.collapsed {
  justify-content: center;
  gap: 0;
  padding: 12px 10px;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.nav-item.active {
  background: linear-gradient(90deg, #ff6b6b 0%, #c1121f 100%);
  color: #fff;
  box-shadow: 0 10px 24px rgba(193, 18, 31, 0.3);
}

.icon {
  font-size: 18px;
}

.sidebar-footer {
  position: relative;
  margin-top: auto;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 12px;
  padding: 8px 10px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.sidebar.collapsed .sidebar-footer {
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 10px;
}

.avatar-btn {
  border: none;
  background: transparent;
  color: inherit;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  width: 100%;
  text-align: left;
  padding: 0;
  justify-content: space-between;
}

.sidebar.collapsed .avatar-btn {
  justify-content: center;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #fffaf7;
  color: #b11226;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.user-name {
  margin: 0;
  font-weight: 700;
  font-size: 14px;
  color: #fffaf7;
}

.user-email {
  margin: 0;
  font-size: 12px;
  color: #f3e0de;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.caret {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fffaf7;
}

.avatar-dropdown {
  position: absolute;
  right: 0;
  bottom: calc(100% + 8px);
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.2);
  padding: 8px;
  display: grid;
  gap: 6px;
  min-width: 200px;
  z-index: 5;
}

.language-box {
  width: 100%;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  justify-content: center;
  background-color: #fff;
}

.sidebar.collapsed .language-box {
  padding: 8px 0;
}

.avatar-dropdown button {
  width: 100%;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #111827;
  border-radius: 8px;
  padding: 8px 10px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
  font-weight: 600;
}

.avatar-dropdown button:hover {
  background: #fff5f5;
  border-color: #b70f17;
}

.avatar-dropdown .danger {
  background: #ffe0e0;
  border-color: #f5a3a3;
}

.avatar-dropdown .danger:hover {
  background: #fcb5b5;
  border-color: #f26a6a;
}

.collapse-handle {
  position: absolute;
  top: 50%;
  right: -12px;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #f08c8c;
  background: #fff6f6;
  display: grid;
  place-items: center;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.18);
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.sidebar:hover .collapse-handle {
  opacity: 1;
}

.collapse-handle:hover {
  transform: translateY(-50%) scale(1.05);
}

.collapse-icon {
  width: 14px;
  height: 14px;
  color: #b11226;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

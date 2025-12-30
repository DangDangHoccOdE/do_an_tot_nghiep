<template>
  <aside class="sidebar">
    <div class="brand">Admin</div>
    <nav>
      <button
        v-for="item in items"
        :key="item.key"
        :class="['nav-item', { active: item.key === active } ]"
        @click="$emit('select', item.key)"
      >
        <span class="icon">{{ item.icon }}</span>
        <span>{{ item.label }}</span>
      </button>
    </nav>
  </aside>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  items: { type: Array, required: true },
  active: { type: String, required: true }
})
</script>

<style scoped>
.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #111827 0%, #1f2937 100%);
  color: #e5e7eb;
  border-radius: 16px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.28);
}

.brand {
  font-weight: 700;
  letter-spacing: 0.5px;
  font-size: 18px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.06);
  color: #f9fafb;
}

nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  border: none;
  background: transparent;
  color: inherit;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.nav-item.active {
  background: linear-gradient(90deg, #2563eb 0%, #60a5fa 100%);
  color: #fff;
  box-shadow: 0 8px 22px rgba(37, 99, 235, 0.35);
}

.icon {
  font-size: 18px;
}
</style>

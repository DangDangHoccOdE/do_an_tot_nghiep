<template>
  <section class="card" :class="{ 'full': full }">
    <header class="card-header">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h2>{{ title }}</h2>
        <p class="desc" v-if="description">{{ description }}</p>
      </div>
      <div class="actions">
        <slot name="actions" />
      </div>
    </header>
    <div class="body">
      <slot />
    </div>
  </section>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
  title: { type: String, required: true },
  description: { type: String, default: '' },
  eyebrow: { type: String, default: '' },
  full: { type: Boolean, default: false }
})
</script>

<style scoped>
.card {
  background: #ffffff;
  border-radius: 16px;
  padding: 18px;
  box-shadow: 0 14px 35px rgba(0, 0, 0, 0.06);
  border: 1px solid #eef2f7;
}

.card.full {
  grid-column: 1 / -1;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.eyebrow {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #6b7280;
  margin: 0 0 4px;
}

h2 {
  margin: 0;
  font-size: 20px;
  color: #0f172a;
}

.desc {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 14px;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.body {
  margin-top: 6px;
}
</style>

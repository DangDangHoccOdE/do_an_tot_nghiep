<template>
  <div class="min-h-screen flex flex-col">
    <Header v-if="!hideLayout" />

    <main class="flex-1">
      <router-view />
    </main>

    <Footer v-if="!hideLayout" />

    <!-- Chat Widget - chỉ hiện khi đã đăng nhập -->
    <ChatWidget v-if="isLoggedIn" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth/useAuthStore'
import Header from './layouts/Header.vue'
import Footer from './layouts/Footer.vue'
import ChatWidget from '@/components/common/ChatWidget.vue'

const route = useRoute()
const authStore = useAuthStore()

const hideLayout = computed(() => route.meta.hideLayout)
const isLoggedIn = computed(() => authStore.isLoggedIn)
</script>

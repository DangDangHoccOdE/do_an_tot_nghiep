<template>
  <div class="min-h-screen flex flex-col">
    <Header v-if="!hideLayout" />

    <main class="flex-1">
      <router-view />
    </main>

    <Footer v-if="!hideLayout" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './layouts/Header.vue';
import Footer from './layouts/Footer.vue';

const route = useRoute()
const hideLayout = computed(() => route.meta.hideLayout)
</script>

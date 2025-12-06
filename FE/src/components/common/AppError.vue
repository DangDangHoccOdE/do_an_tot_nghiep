<script setup>
import { ref, computed } from 'vue'

// Nhận props dạng JS
const props = defineProps({
  title: String,
  message: String
})

const { title, message } = props

const isChunkError = ref(false)

const handleReload = () => {
  if (isChunkError.value) {
    // Clear cache khi bị lỗi chunk
    if ('caches' in window) {
      caches
        .keys()
        .then((names) => {
          names.forEach((name) => caches.delete(name))
        })
        .finally(() => {
          window.location.reload()
        })
    } else {
      window.location.reload()
    }
  } else {
    window.location.reload()
  }
}

const classBtn = computed(() => {
  return {
    'px-6 py-2 rounded text-white font-medium': true,
    'bg-blue-600 hover:bg-blue-700': isChunkError.value,
    'bg-black hover:bg-gray-800': !isChunkError.value
  }
})
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center gap-4 p-6 text-center">

    <div v-if="isChunkError" class="mb-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
      <svg
        class="w-6 h-6 text-blue-600 mx-auto mb-2"
        fill="none"
        stroke="currentColor"
        viewBox="0 0 24 24"
      >
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
        />
      </svg>
      <p class="text-sm text-blue-800">New app version detected</p>
    </div>

    <h1 class="text-2xl font-bold">
      {{ title }}
    </h1>
    <p class="text-gray-600 max-w-xl">
      {{ message }}
    </p>

    <div class="flex gap-3">
      <button :class="classBtn" @click="handleReload">
        <template v-if="isChunkError"> Update App </template>
        <template v-else> Reload </template>
      </button>

      <a
        v-if="!isChunkError"
        href="/"
        class="px-4 py-2 rounded border border-gray-300 hover:bg-gray-50"
      >
        Go Home
      </a>
    </div>
  </div>
</template>

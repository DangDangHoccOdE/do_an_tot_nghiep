<template>
    <label class="required-label">
      {{ label }}<span class="mr-1">:</span><span v-if="required" class="text-red-500">*</span>
    </label>
  </template>
  
  <script setup>
  defineProps({
    label: { type: String, required: true },
    required: { type: Boolean, default: false },
  });
  </script>
  
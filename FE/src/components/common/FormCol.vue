<template>
    <el-col v-bind="colProps" class="custom-col">
      <slot />
    </el-col>
  </template>
  
  <script setup>
  defineProps({
    colProps: {
      type: Object,
      default: () => ({
        xs: 24,
        sm: 24,
        md: 24,
        lg: 12,
        xl: 8,
      }),
    },
  });
  </script>
  
  <style scoped>
  .custom-col {
    padding-left: 24px;
    padding-right: 24px;
  }
  </style>
  
<template>
    <svg 
      width="16" 
      height="16" 
      viewBox="0 0 16 16" 
      fill="none" 
      xmlns="http://www.w3.org/2000/svg"
      :class="className"
    >
      <path d="M4 6L8 10L12 6" stroke="#4A4B4C" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
    </svg>
  </template>
  
  <script setup>
  defineProps({
    className: {
      type: String,
      default: ''
    }
  })
  
  defineOptions({
    name: 'ArrowDownIcon'
  })
  </script>
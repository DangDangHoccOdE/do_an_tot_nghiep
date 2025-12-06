<template>
  <svg 
    class="footer-svg-panel" 
    width="17" 
    height="10" 
    viewBox="0 0 17 10" 
    fill="none" 
    xmlns="http://www.w3.org/2000/svg"
    :class="{ 'rotate-180': isDown }"
  >
    <g clip-path="url(#clip0_5_8389)">
      <path d="M0.70874 9.29902L8.56488 1.41113L16.3012 9.16889" stroke="#4A4B4C" stroke-width="2" stroke-miterlimit="10"></path>
    </g>
    <defs>
      <clipPath id="clip0_5_8389">
        <rect width="17" height="10" fill="white"></rect>
      </clipPath>
    </defs>
  </svg>
</template>

<script setup>
defineProps({
  isDown: {
    type: Boolean,
    default: false
  }
})

defineOptions({
  name: 'ChevronUpIcon'
})
</script>

<style scoped>
.rotate-180 {
  transform: rotate(180deg);
}
</style>

<template>
  <el-date-picker v-model="innerValue" type="date" format="dd/MM/yyyy" value-format="dd/MM/yyyy" clearable editable
    style="width: 100%" :disabled="extraDisabled" :class="classStyle" @blur="handleManualInput" />
</template>

<script setup>
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import { ref, watch } from 'vue';

dayjs.extend(customParseFormat);

const props = defineProps({
  modelValue: {
    type: String,
    default: null,
  },
  isRequire: {
    type: Boolean,
    default: true,
  },
  emitChange: {
    type: Boolean,
    default: false,
  },
  extraDisabled: {
    type: Boolean,
    default: false,
  },
  classStyle: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['update:modelValue', 'change']);

const innerValue = ref(props.modelValue);

watch(
  () => props.modelValue,
  (val) => {
    innerValue.value = val;
  }
);

watch(innerValue, (val) => {
  emit('update:modelValue', val);
  if (props.emitChange) {
    emit('change', val);
  }
});

const handleManualInput = (event) => {
  const inputValue = event.target.value.trim();
  if (dayjs(inputValue, 'DD/MM/YYYY', true).isValid()) {
    innerValue.value = dayjs(inputValue, 'DD/MM/YYYY').format('YYYY-MM-DD');
  } else {
    innerValue.value = '';
  }
  if (props.emitChange) {
    emit('change', innerValue.value);
  }
};
</script>
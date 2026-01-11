<template>
    <div v-if="isArrayProp">
        <template v-for="(key, idx) in column.prop" :key="idx">
            <template v-if="shouldShow(key)">
                <el-link v-if="row[key]" class="text-dodger-blue" type="primary" underline="always"
                    @click="(event) => handleClick(event, key)">
                    {{ row[key] }}
                </el-link>

                <span v-else>{{ column.emptyValue ?? '-' }}</span>

                <span v-if="idx < column.prop.length - 1" class="separator">
                    {{ column.separator ?? ', ' }}
                </span>
            </template>
        </template>
    </div>

    <div v-else>
        <el-link v-if="row[propKey]" type="primary" class="text-dodger-blue" underline="always"
            @click="(event) => handleClick(event, propKey)">
            {{ row[propKey] }}
        </el-link>

        <span v-else>{{ column.emptyValue ?? '-' }}</span>
    </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
    column: {
        type: Object,
        required: true,
    },
    row: {
        type: Object,
        required: true,
    },
})

const emits = defineEmits(['linkClick'])

const isArrayProp = computed(() => Array.isArray(props.column.prop))

const propKey = computed(() => {
    if (isArrayProp.value) {
        return props.column.prop[0]
    }
    return props.column.prop
})

const shouldShow = (key) => {
    if (typeof props.column.showIf === 'function') {
        return props.column.showIf(props.row, key)
    }
    return true
}

const handleClick = (event, key) => {
    event.stopPropagation()

    if (typeof props.column.onClick === 'function') {
        props.column.onClick(props.row, key)
    }

    if (typeof props.column.handler === 'function') {
        props.column.handler(props.row, key)
    }

    emits('linkClick', props.row, key)
}
</script>

<style scoped>
.separator {
    margin: 0 4px;
    color: #606266;
}
</style>

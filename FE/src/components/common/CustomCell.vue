<template>
    <div>
        <!-- Custom Render with HTML -->
        <span v-if="hasCustomRender" v-html="renderedValue" />

        <!-- Formatter: Object format (currency) -->
        <div v-else-if="hasFormatter && isFormatterObject" class="currency-cell">
            <span>{{ formatterResult?.key || '' }}</span>
            <span>{{ formatterResult?.value || '' }}</span>
        </div>

        <!-- Formatter: String format -->
        <span v-else-if="hasFormatter">{{ formatterResult || '' }}</span>

        <!-- Default value -->
        <span v-else>{{ defaultValue }}</span>
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

const hasCustomRender = computed(
    () => typeof props.column.customRender === 'function'
)

const hasFormatter = computed(
    () => typeof props.column.formatter === 'function'
)

const renderedValue = computed(() => {
    if (hasCustomRender.value) {
        return props.column.customRender({ row: props.row })
    }
    return ''
})

const formatterResult = computed(() => {
    if (hasFormatter.value && props.column.formatter) {
        // Element Plus formatter expects 4 params, but only row is used
        return props.column.formatter(props.row)
    }
    return null
})

const isFormatterObject = computed(() => {
    const result = formatterResult.value
    return (
        result !== null &&
        typeof result === 'object' &&
        !Array.isArray(result) &&
        'key' in result &&
        'value' in result
    )
})

const defaultValue = computed(() => {
    const { column, row } = props

    if (Array.isArray(column.prop)) {
        const key = column.prop.find((k) => row[k] != null)
        return key ? row[key] : column.emptyValue ?? '-'
    }

    return row[column.prop] ?? column.emptyValue ?? '-'
})
</script>

<style scoped>
.currency-cell {
    display: flex;
    gap: 15px;
    justify-content: space-between;
    white-space: nowrap;
}

.currency-cell span:first-child {
    flex: 1;
    text-align: left;
}

.currency-cell span:last-child {
    flex: 1;
    text-align: right;
}
</style>

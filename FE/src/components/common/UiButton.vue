<template>
    <el-button :type="elType" :size="size" :plain="plain" :text="text" :disabled="disabled"
        :class="['ui-btn', variantClass, { 'is-text': text }]" @click="$emit('click', $event)">
        <component v-if="iconComponent" :is="iconComponent" class="btn-icon" />
        <span class="btn-label">{{ resolvedLabel }}</span>
    </el-button>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import AddIcon from '@/components/icons/AddIcon.vue'
import EditIcon from '@/components/icons/EditIcon.vue'
import DeleteIcon from '@/components/icons/DeleteIcon.vue'
import ExportIcon from '@/components/icons/ExportIcon.vue'
import RefreshIcon from '@/components/icons/RefreshIcon.vue'
import SearchIcon from '@/components/icons/SearchIcon.vue'
import ViewIcon from '@/components/icons/ViewIcon.vue'
import SaveIcon from '@/components/icons/SaveIcon.vue'
import CancelIcon from '@/components/icons/CancelIcon.vue'

const { t } = useI18n()

const props = defineProps({
    variant: { type: String, default: 'default' },
    label: { type: String, default: '' },
    labelKey: { type: String, default: '' },
    icon: { type: Object, default: null },
    size: { type: String, default: 'small' },
    plain: { type: Boolean, default: false },
    text: { type: Boolean, default: false },
    disabled: { type: Boolean, default: false },
})

const emits = defineEmits(['click'])

const iconMap = {
    add: AddIcon,
    edit: EditIcon,
    delete: DeleteIcon,
    export: ExportIcon,
    refresh: RefreshIcon,
    search: SearchIcon,
    view: ViewIcon,
    save: SaveIcon,
    cancel: CancelIcon,
}

const labelKeyMap = {
    add: 'admin.actions.add',
    edit: 'admin.actions.edit',
    delete: 'admin.actions.delete',
    export: 'admin.actions.export',
    refresh: 'admin.actions.refresh',
    search: 'admin.filters.search',
    view: 'admin.actions.view',
    save: 'admin.actions.save',
    cancel: 'admin.actions.cancel',
}

const typeMap = {
    add: 'primary',
    edit: 'primary',
    delete: 'danger',
    export: 'info',
    refresh: '',
    search: '',
    view: '',
    save: 'success',
    cancel: '',
    default: '',
}

const iconComponent = computed(() => props.icon || iconMap[props.variant] || null)
const resolvedLabel = computed(() => {
    if (props.label) return props.label
    const key = props.labelKey || labelKeyMap[props.variant]
    return key ? t(key) : ''
})
const elType = computed(() => typeMap[props.variant] ?? '')
const variantClass = computed(() => `ui-btn--${props.variant || 'default'}`)
</script>

<style scoped>
.btn-icon {
    margin-right: 6px;
    display: inline-flex;
    align-items: center;
}

.btn-label {
    vertical-align: middle;
}

.ui-btn {
    border: 1px solid #cbd5e1;
    border-radius: 6px;
    padding: 6px 12px;
    transition: all 0.2s ease;
    background-color: #f8fafc;
    color: #0f172a;
    box-shadow: 0 1px 1px rgba(0, 0, 0, 0.06);
}

.ui-btn:hover {
    background-color: #e2e8f0;
    border-color: #94a3b8;
}

.ui-btn.el-button--primary {
    background-color: #dbeafe;
    border-color: #93c5fd;
    color: #1d4ed8;
}

.ui-btn.el-button--primary:hover {
    background-color: #d0e2ff;
    border-color: #7fb4fb;
}

.ui-btn.el-button--danger {
    background-color: #fee2e2;
    border-color: #fca5a5;
    color: #b91c1c;
}

.ui-btn.el-button--danger:hover {
    background-color: #fcd4d4;
    border-color: #f87171;
}

.ui-btn.el-button--success {
    background-color: #dcfce7;
    border-color: #bbf7d0;
    color: #15803d;
}

.ui-btn.el-button--success:hover {
    background-color: #c7f9d9;
    border-color: #86efac;
}

.ui-btn.is-text {
    background-color: transparent;
    border-color: transparent;
    box-shadow: none;
}
</style>

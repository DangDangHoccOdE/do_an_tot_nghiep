<template>
  <div class="table-wrapper">
    <div class="table-scroll-container">
      <el-table ref="tableRef" :data="tableData" :span-method="spanMethod" border stripe :height="height"
        v-loading="loading" :header-cell-style="headerStyle" :style="{ width: '100%' }" :default-sort="currentSort"
        :empty-text="tableData.length === 0 && !loading ? emptyText : ''" @sort-change="handleSortChange"
        @row-click="handleRowClick" @select="onRowSelect" @select-all="onSelectAll">
        <!-- Selection Column -->
        <el-table-column v-if="selectable" type="selection" width="55" fixed="left" align="center" />

        <!-- Dynamic Columns -->
        <el-table-column v-for="col in columns" :key="Array.isArray(col.prop) ? col.prop.join('-') : col.prop"
          :prop="Array.isArray(col.prop) ? undefined : col.prop" :label="col.label" :formatter="col.formatter || null"
          :width="col.width" :min-width="col.minWidth" :sortable="col.sortable || false"
          :sort-orders="col.sortOrders || ['ascending', 'descending', null]" :align="col.align || 'left'"
          :class-name="col.className || ''" show-overflow-tooltip>
          <template v-if="col.tooltip" #header>
            <el-tooltip :content="col.tooltip" placement="top">
              {{ col.label }}
            </el-tooltip>
          </template>
          <template #default="{ row }">
            <LinkCell v-if="col.type === 'link'" :column="col" :row="row"
              @link-click="(clickedRow) => col.handler?.(clickedRow)" />

            <template v-else-if="col.slot">
              <slot :name="col.slot" :row="row" />
            </template>

            <CustomCell v-else-if="col.customRender || col.formatter" :column="col" :row="row" />

            <span v-else>{{ Array.isArray(col.prop) ? row[col.prop[0]] : row[col.prop] }}</span>
          </template>
        </el-table-column>

        <!-- Action Column -->
        <el-table-column v-if="actions?.buttons?.length" :prop="actions.prop" :label="t('admin.actions.view')"
          align="center" :width="actions.buttons.length > 1 ? 180 : 80" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button v-for="(action, index) in actions.buttons" :key="index" :type="action.type || 'primary'"
                :disabled="action.disabled ? action.disabled(row) : false" size="small" class="table-action-btn" @click="
                  (event) => {
                    event.stopPropagation();
                    action.handler(row);
                  }
                ">
                <el-icon v-if="action.icon">
                  <component :is="action.icon" />
                </el-icon>
                {{ action.label }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Pagination -->
    <div v-if="total > 0" class="pagination-container">
      <div class="pagination-left">
        <el-pagination background :current-page="currentPage" :page-size="pageSizeProxy" :total="total" :pager-count="5"
          layout="prev, pager, next" @current-change="handlePageChange" />
      </div>
      <div class="pagination-right">
        <el-select v-model="pageSizeProxy" size="small" class="page-size-select">
          <el-option v-for="item in pageSizes" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
        <span class="total-text ml-2">{{ t('totalPage') }}: <strong>{{ total }}</strong></span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, watch, ref, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { Delete } from '@element-plus/icons-vue'
import LinkCell from './LinkCell.vue'
import CustomCell from './CustomCell.vue'
import UiButton from '@/components/common/UiButton.vue'


const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] },
  columns: { type: Array, default: () => [] },
  actions: { type: Object, default: undefined },
  selectedId: { type: Array, default: () => [] },
  formSearch: { type: Object, default: () => ({}) },
  allRecordKeys: { type: Array, default: () => [] },
  selectable: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  height: { type: String, default: '' },
  currentPage: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  loading: { type: Boolean, default: false },
  onSortChange: { type: Function, default: () => { } },
  onPageChange: { type: Function, default: () => { } },
  defaultSort: { type: Object, default: () => ({ prop: '', order: null }) },
  sortState: { type: Object, default: () => ({ prop: '', order: null }) },
  spanMethod: { type: Function, default: undefined },
  isApiCalled: { type: Boolean, default: false },
  emptyText: { type: String, default: '' },
  fetchAllRecordKeys: { type: Object, default: undefined },
  disableDefaultSort: { type: Boolean, default: false },
  onBulkDelete: { type: Function, default: undefined },
})

const emits = defineEmits([
  'update:currentPage',
  'update:pageSize',
  'selectionChange',
  'rowClick',
  'selectAll',
  'bulkDelete',
])

/* ================= Pagination ================= */

const pageSizes = [
  { label: t('pageNumber', { number: 10 }), value: 10 },
  { label: t('pageNumber', { number: 20 }), value: 20 },
  { label: t('pageNumber', { number: 50 }), value: 50 },
  { label: t('pageNumber', { number: 100 }), value: 100 },
]

const emptyText = computed(() => {
  if (props.loading) return ''
  if (props.emptyText) return props.emptyText
  return t('noData')
})

const pageSizeProxy = computed({
  get: () => props.pageSize,
  set: (val) => {
    emits('update:pageSize', val)
    emits('update:currentPage', 1)
    props.onPageChange?.(1, val)
  },
})

/* ================= Table ================= */

const tableRef = ref()
const tableData = computed(() => props.data)
const currentSort = ref({ ...props.defaultSort })
const isInternalSortTrigger = ref(false)

/* ================= Selection ================= */

const selectedRowsMap = ref(new Map())

function updateHeaderCheckboxState() {
  nextTick(() => {
    const table = tableRef.value
    if (!table?.$el) return

    const checkbox = table.$el.querySelector(
      'thead th.el-table-column--selection .el-checkbox__input'
    )
    if (!checkbox) return

    checkbox.classList.remove('is-checked', 'is-indeterminate')

    const totalSelected = selectedRowsMap.value.size
    const totalRecords = props.total

    if (totalSelected === totalRecords && totalRecords > 0) {
      checkbox.classList.add('is-checked')
    } else if (totalSelected > 0) {
      checkbox.classList.add('is-indeterminate')
    }
  })
}

/* ================= Sort ================= */

watch(
  () => props.sortState,
  (newVal) => {
    currentSort.value = { ...newVal }
    nextTick(() => {
      isInternalSortTrigger.value = true
      tableRef.value?.sort(currentSort.value.prop, currentSort.value.order)
      updateHeaderCheckboxState()
    })
  }
)

function handleSortChange({ prop, order }) {
  if (isInternalSortTrigger.value) {
    isInternalSortTrigger.value = false
    return
  }

  if (!order) {
    if (props.disableDefaultSort) {
      currentSort.value = { prop: '', order: null }

      nextTick(() => {
        isInternalSortTrigger.value = true
        tableRef.value?.clearSort()
        updateHeaderCheckboxState()
      })

      props.onSortChange?.({ prop: '', order: null })
      return
    }

    currentSort.value = { ...props.defaultSort }
    props.onSortChange?.(currentSort.value)

    nextTick(() => {
      isInternalSortTrigger.value = true
      tableRef.value?.sort(currentSort.value.prop, currentSort.value.order)
      updateHeaderCheckboxState()
    })
    return
  }

  currentSort.value = { prop, order }
  props.onSortChange?.({ prop, order })
  updateHeaderCheckboxState()
}

/* ================= Pagination ================= */

function handlePageChange(page) {
  emits('update:currentPage', page)
  props.onPageChange?.(page, props.pageSize)
  updateHeaderCheckboxState()
}

/* ================= Row Selection ================= */

function onRowSelect(selection, row) {
  if (selection.find((r) => r.id === row.id)) {
    selectedRowsMap.value.set(row.id, row)
  } else {
    selectedRowsMap.value.delete(row.id)
  }

  emits('selectionChange', Array.from(selectedRowsMap.value.keys()))
  updateHeaderCheckboxState()
}

async function onSelectAll(selection) {
  if (selection.length === 0) {
    selectedRowsMap.value = new Map()
    emits('selectionChange', [])
    emits('selectAll', false)
    updateHeaderCheckboxState()
    return
  }

  try {
    if (props.fetchAllRecordKeys && Object.values(props.fetchAllRecordKeys).length) {
      const fetchAllClone = { ...props.fetchAllRecordKeys }
      fetchAllClone.params = { search: JSON.stringify(props.formSearch) }

      await fetchAllClone.fetch()

      const newMap = new Map()
      props.allRecordKeys?.forEach((item) => {
        newMap.set(item.id, item)
      })

      selectedRowsMap.value = newMap
      emits('selectionChange', Array.from(newMap.keys()))
    } else {
      emits('selectAll', true)
    }
  } catch {
    emits('selectAll', false)
  } finally {
    updateHeaderCheckboxState()
  }
}

/* ================= Row Click ================= */

function handleRowClick(row, _, event) {
  const target = event.target
  if (
    target.tagName === 'INPUT' ||
    target.classList.contains('el-checkbox__inner') ||
    target.closest('.el-table-column--selection') ||
    target.classList.contains('el-link__inner') ||
    target.closest('.action-buttons') ||
    target.closest('.table-action-btn') ||
    target.closest('.ui-btn') ||
    target.closest('button')
  ) {
    return
  }
  emits('rowClick', row)
}

/* ================= Bulk Delete ================= */

function handleBulkDelete() {
  const selectedIds = Array.from(selectedRowsMap.value.keys())
  if (selectedIds.length === 0) return

  if (props.onBulkDelete) {
    props.onBulkDelete(selectedIds)
  } else {
    emits('bulkDelete', selectedIds)
  }
}

function clearSelection() {
  tableRef.value?.clearSelection()
  selectedRowsMap.value = new Map()
  emits('selectionChange', [])
  updateHeaderCheckboxState()
}

/* ================= Watch data ================= */

watch(
  () => props.data,
  async () => {
    await nextTick()
    const table = tableRef.value
    if (!table) return

    props.data.forEach((row) => {
      table.toggleRowSelection(row, selectedRowsMap.value.has(row.id))
    })
    updateHeaderCheckboxState()
  },
  { immediate: true }
)

watch(
  () => props.selectedId,
  (newVal) => {
    const table = tableRef.value
    if (selectedRowsMap.value.size === 0 && newVal.length > 0) {
      newVal.forEach((item) => selectedRowsMap.value.set(item, item))
      props.data.forEach((row) => {
        table.toggleRowSelection(row, selectedRowsMap.value.has(row.id))
      })
      updateHeaderCheckboxState()
    }
  },
  { immediate: true }
)

/* ================= Expose ================= */

defineExpose({
  clearSelection,
  resetSortToDefault: () => {
    const { prop, order } = props.defaultSort
    currentSort.value = { prop, order }
    nextTick(() => {
      isInternalSortTrigger.value = true
      tableRef.value?.sort(prop, order)
    })
  },
  updateHeaderCheckboxState,
  toggleAllSelection: () => tableRef.value?.toggleAllSelection(),
  clearSort: () => tableRef.value?.clearSort(),
})

/* ================= Styles ================= */

const headerStyle = {
  background: '#E2EFFF',
  color: '#495057',
  fontWeight: '600',
  textAlign: 'center',
}
</script>

<style scoped>
.table-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bulk-actions-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  gap: 12px;
}

.selected-count {
  color: #0369a1;
  font-size: 14px;
}

.selected-count strong {
  font-weight: 700;
  font-size: 16px;
}

.bulk-actions {
  display: flex;
  gap: 8px;
}

.table-scroll-container {
  overflow-x: auto;
}

.pagination-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f9fafb;
  border-top: 1px solid #e5e7eb;
  border-radius: 0 0 8px 8px;
  margin-top: -1px;
}

.pagination-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-size-label {
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
}

.page-size-select {
  width: 100px;
}

.pagination-right {
  display: flex;
  align-items: center;
}

.total-text {
  color: #4b5563;
  font-size: 13px;
  font-weight: 500;
}

.total-text strong {
  color: #111827;
  font-weight: 700;
}

.action-buttons {
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.table-action-btn {
  border: 1px solid #cbd5e1;
  background-color: #f8fafc;
  color: #0f172a;
  padding: 6px 10px;
  border-radius: 6px;
}

.table-action-btn.el-button--primary {
  background-color: #dbeafe;
  border-color: #93c5fd;
  color: #1d4ed8;
}

.table-action-btn.el-button--danger {
  background-color: #fee2e2;
  border-color: #fca5a5;
  color: #b91c1c;
}

.table-action-btn.el-button--success {
  background-color: #dcfce7;
  border-color: #bbf7d0;
  color: #15803d;
}

.table-action-btn:hover {
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
}
</style>

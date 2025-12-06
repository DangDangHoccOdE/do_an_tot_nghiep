<script setup lang="ts">
import { computed, watch, ref, nextTick } from "vue";
import { useI18n } from "vue-i18n";
import LinkCell from "./LinkCell.vue";
import CustomCell from "./CustomCell.vue";

const { t } = useI18n();

const props = withDefaults(
  defineProps({
    data: { default: () => [] },
    columns: { default: () => [] },
    actions: { default: undefined },
    selectedId: { default: () => [] },
    fetchAllRecordKeys: { default: undefined },
    formSearch: { default: () => ({}) },
    allRecordKeys: { default: () => [] },
    selectable: { default: false },
    total: { default: 0 },
    currentPage: { default: 1 },
    pageSize: { default: 10 },
    loading: { default: false },
    onSortChange: { default: () => {} },
    onPageChange: { default: () => {} },
    defaultSort: { default: () => ({ prop: "", order: null }) },
    sortState: { default: () => ({ prop: "", order: null }) },
    spanMethod: { default: undefined },
    isApiCalled: { default: false },
  }),
  {}
);

const emits = defineEmits([
  "update:currentPage",
  "update:pageSize",
  "selectionChange",
  "rowClick",
]);

const pageSizes = [
  { label: t("pageNumber", { number: 10 }), value: 10 },
  { label: t("pageNumber", { number: 20 }), value: 20 },
  { label: t("pageNumber", { number: 50 }), value: 50 },
  { label: t("pageNumber", { number: 100 }), value: 100 },
];

const emptyText = computed(() => {
  if (!props.isApiCalled || props.loading) return " ";
  return t("message.MSG006");
});

const tableRef = ref();
const currentSort = ref({ ...props.defaultSort });
const tableData = computed(() => props.data);

const selectedRowsMap = ref(new Map());
const isInternalSortTrigger = ref(false);

const pageSizeProxy = computed({
  get: () => props.pageSize,
  set: (val) => {
    emits("update:pageSize", val);
    emits("update:currentPage", 1);
    props.onPageChange(1, val);
  },
});

watch(
  () => props.sortState,
  (newVal) => {
    currentSort.value = { ...newVal };
    nextTick(() => {
      isInternalSortTrigger.value = true;
      tableRef.value?.sort(currentSort.value.prop, currentSort.value.order);
      updateHeaderCheckboxState();
    });
  }
);

function handleSortChange({ prop, order }) {
  if (isInternalSortTrigger.value) {
    isInternalSortTrigger.value = false;
    return;
  }

  if (!order) {
    currentSort.value = { ...props.defaultSort };
    props.onSortChange(currentSort.value);
    nextTick(() => {
      isInternalSortTrigger.value = true;
      tableRef.value?.sort(currentSort.value.prop, currentSort.value.order);
      updateHeaderCheckboxState();
    });
    return;
  }

  currentSort.value = { prop, order };
  props.onSortChange({ prop, order });
  updateHeaderCheckboxState();
}

function handlePageChange(page) {
  emits("update:currentPage", page);
  props.onPageChange(page, props.pageSize);
  updateHeaderCheckboxState();
}

function onRowSelect(selection, row) {
  if (selection.some((r) => r.id === row.id)) {
    selectedRowsMap.value.set(row.id, row);
  } else {
    selectedRowsMap.value.delete(row.id);
  }

  emits("selectionChange", [...selectedRowsMap.value.keys()]);
  updateHeaderCheckboxState();
}

async function onSelectAll(selection) {
  if (selection.length === 0) {
    selectedRowsMap.value = new Map();
    emits("selectionChange", []);
    updateHeaderCheckboxState();
    return;
  }

  try {
    const clone = { ...props.fetchAllRecordKeys };
    clone.params = {
      search: JSON.stringify(props.formSearch),
    };

    await clone.fetch();

    const newMap = new Map();
    props.allRecordKeys.forEach((item) => newMap.set(item.id, item));

    selectedRowsMap.value = newMap;
    emits("selectionChange", [...newMap.keys()]);
  } catch (error) {
    console.error("Failed to fetch all records:", error);
  } finally {
    updateHeaderCheckboxState();
  }
}

function updateHeaderCheckboxState() {
  nextTick(() => {
    const table = tableRef.value;
    if (!table || !table.$el) return;

    const checkbox = table.$el.querySelector(
      "thead th.el-table-column--selection .el-checkbox__input"
    );

    if (!checkbox) return;

    checkbox.classList.remove("is-checked", "is-indeterminate");

    const totalSelected = selectedRowsMap.value.size;
    const totalRecords = props.total;

    if (totalSelected === totalRecords && totalRecords > 0) {
      checkbox.classList.add("is-checked");
    } else if (totalSelected > 0) {
      checkbox.classList.add("is-indeterminate");
    }
  });
}

function handleRowClick(row, column, event) {
  const target = event.target;
  if (
    target.tagName === "INPUT" ||
    target.classList.contains("el-checkbox__inner") ||
    target.closest(".el-table-column--selection") ||
    target.classList.contains("el-link__inner")
  ) {
    return;
  }
  emits("rowClick", row);
}

function resetSortToDefault() {
  const { prop, order } = props.defaultSort;
  currentSort.value = { prop, order };
  nextTick(() => {
    isInternalSortTrigger.value = true;
    tableRef.value?.sort(prop, order);
  });
}

watch(
  () => props.data,
  async () => {
    await nextTick();
    const table = tableRef.value;
    if (!table) return;

    props.data.forEach((row) => {
      table.toggleRowSelection(row, selectedRowsMap.value.has(row.id));
    });

    updateHeaderCheckboxState();
  },
  { immediate: true }
);

watch(
  () => props.selectedId,
  (newVal) => {
    const table = tableRef.value;

    if (selectedRowsMap.value.size === 0 && newVal.length > 0) {
      newVal.forEach((id) => selectedRowsMap.value.set(id, id));

      props.data.forEach((row) => {
        table.toggleRowSelection(row, selectedRowsMap.value.has(row.id));
      });

      updateHeaderCheckboxState();
    }
  },
  { immediate: true }
);

defineExpose({
  clearSelection: () => {
    tableRef.value?.clearSelection();
    selectedRowsMap.value = new Map();
    emits("selectionChange", []);
    updateHeaderCheckboxState();
  },
  resetSortToDefault,
  updateHeaderCheckboxState,
});
</script>

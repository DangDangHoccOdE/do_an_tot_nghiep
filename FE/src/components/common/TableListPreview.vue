<template>
    <BaseDialog :model-value="isVisible" :before-close="handleClose" class="custom-table-dialog"
        :close-on-click-modal="false" :title="t('student.unassignedStudentTitle')"
        :size="width < 1025 ? 'full' : 'x-large'" :loading="loading">
        <template #header>
            <el-row align="middle" class="justify-between">
                <div class="header-title">
                    <ListIcon />
                    <span class="text-base">{{ t('upload.titlePreview') }}</span>
                </div>
            </el-row>
        </template>

        <el-row class="table-wrapper">
            <el-table ref="tableRef" :data="processedData" border stripe v-loading="loading"
                :header-cell-style="headerStyle" max-height="500" style="width: 100%">
                <el-table-column :label="t('upload.lineNumber')" width="70" align="center" fixed>
                    <template #default="{ row, $index }">
                        <span :style="{ color: getRowColor(row.type) }">
                            {{ (currentPage - 1) * pageSize + $index + 1 }}
                        </span>
                    </template>
                </el-table-column>

                <el-table-column v-for="col in columns" :key="col.prop" :prop="col.prop" :label="col.label"
                    :formatter="col.formatter" :width="col.width" :align="col.align || 'left'">
                    <template #default="{ row }">
                        <template v-if="col.type === 'link'">
                            <el-link class="custom-link" @click.stop="col.handler(row)"
                                :style="{ color: getRowColor(row.type) }">
                                {{ row[col.prop] }}
                            </el-link>
                        </template>
                        <template v-else-if="col.slot">
                            <slot :name="col.slot" :row="row" />
                        </template>
                        <template v-else>
                            <span :style="{ color: getRowColor(row.type) }">
                                {{ col.formatter ? col.formatter(row) : row[col.prop] }}
                            </span>
                        </template>
                    </template>
                </el-table-column>

                <el-table-column v-if="totalError > 0 || totalDuplicate > 0" :label="t('upload.errorHeader')"
                    prop="error" width="250" align="left" fixed="right">
                    <template #default="{ row }">
                        <div v-for="(err, index) in (row.error || '').split(';').filter((e) => e.trim())" :key="index"
                            :style="{ color: getRowColor(row.type) }">
                            {{ err.trim() }}
                        </div>
                    </template>
                </el-table-column>
            </el-table>

            <!-- Pagination -->
            <div v-if="tableData.length > 0" class="pagination-container">
                <el-pagination background :current-page="currentPage" :page-size="pageSize" :total="tableData.length"
                    :pager-count="4" layout="prev, pager, next, ->, slot" @current-change="handlePageChange">
                    <template #default>
                        <div class="pagination-slot">
                            <el-select v-model="pageSize" @change="handlePageSizeChange">
                                <el-option v-for="item in pageSizes" :key="item.value" :label="item.label"
                                    :value="item.value" />
                            </el-select>
                            {{ t('totalPage') }}:
                            <span> {{ tableData.length }} </span>
                        </div>
                    </template>
                </el-pagination>
            </div>
        </el-row>

        <el-divider />

        <!-- Footer thống kê -->
        <div class="footer-container text-base">
            <el-row justify="start" class="stat-row">
                <FormCol :col-props="{ lg: 6, xl: 6 }">
                    <span>{{ t('upload.totalValidRecords') }}: </span>
                    <span :style="{ color: getRowColor(ROW_TYPE.VALID) }">{{ totalValid }}</span>
                </FormCol>
                <FormCol :col-props="{ lg: 6, xl: 6 }">
                    <span>{{ t('upload.totalDuplicateRecords') }}: </span>
                    <span :style="{ color: getRowColor(ROW_TYPE.DUPLICATE) }">{{ totalDuplicate }}</span>
                </FormCol>
                <FormCol :col-props="{ lg: 12, xl: 12 }">
                    <span>{{ t('upload.totalErrorRecords') }}: </span>
                    <span :style="{ color: getRowColor(ROW_TYPE.ERROR) }">{{ totalError }}</span>
                </FormCol>
            </el-row>
            <el-row align="middle" class="mt-5">
                <FormCol :col-props="{ lg: 24, xl: 24 }">
                    <div class="download-row">
                        <span class="mr-10">{{ t('upload.messageErrorDescription1') }}</span>
                        <template v-if="totalError > 0">
                            <DownloadIcon class="custom-download-icon" />
                            <el-link class="custom-link" @click="handleDownloadErrorFile">
                                {{ t('upload.downloadErrorFile') }}
                            </el-link>
                        </template>
                    </div>
                </FormCol>
            </el-row>
            <el-row v-if="totalDuplicate > 0" align="middle" class="mt-5">
                <FormCol :col-props="{ lg: 24, xl: 24 }">
                    <span class="mr-10">{{ t('upload.messageErrorDescription2') }}</span>
                    <el-checkbox v-model="shouldUpdateDuplicate" class="large-checkbox" />
                </FormCol>
            </el-row>
        </div>

        <!-- Footer button -->
        <template #footer>
            <el-row class="mb-4">
                <el-col class="text-right">
                    <el-button class="text-base" size="small" color="#005B94" @click.once="handleImport"
                        :disabled="isImportButtonDisabled">
                        {{ $t('upload.import') }}
                    </el-button>
                    <el-button class="text-base" size="small" color="#CE181E" @click="handleCancel">
                        {{ $t('upload.cancel') }}
                    </el-button>
                </el-col>
            </el-row>
        </template>
    </BaseDialog>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import ListIcon from '@/components/Icons/ListIcon.vue';
import FormCol from '@/components/common/FormCol.vue';
import DownloadIcon from '@/components/Icons/DownloadIcon.vue';
import { ROW_TYPE, ROW_COLOR } from '@/constants/constants';
import BaseDialog from '@/components/ui/BaseDialog.vue'
import { useWindowResize } from '@/composables/useWindowResize';

const { width } = useWindowResize();
const { t } = useI18n();

const props = defineProps({
    isVisible: Boolean,
    data: {
        type: Object,
        default: () => ({ validRows: [], errors: [], duplicates: [] }),
    },
    columns: { type: Array, default: () => [] },
    loading: {
        type: Boolean,
        default: false,
    },
    isApiCalled: {
        type: Boolean,
        default: false,
    },
});

const emit = defineEmits(['update:isVisible', 'downloadError', 'cancel', 'import']);

const shouldUpdateDuplicate = ref(false);
const currentPage = ref(1);
const pageSize = ref(20);

const pageSizes = [
    { label: t('pageNumber', { number: 10 }), value: 10 },
    { label: t('pageNumber', { number: 20 }), value: 20 },
    { label: t('pageNumber', { number: 50 }), value: 50 },
    { label: t('pageNumber', { number: 100 }), value: 100 },
];

const tableData = computed(() => [
    ...(props.data?.duplicates || []).map((item) => ({
        ...item.rowData,
        duplicateId: item.duplicateId,
        type: 'duplicate',
        error: t('upload.ExistingRecord'),
    })),
    ...(props.data?.errors || []).map((item) => ({
        ...item.rowData,
        error: item.message,
        type: 'error',
    })),
    ...(props.data?.validRows || []).map((item) => ({
        ...item,
        type: 'valid',
        error: null,
    })),
]);

const processedData = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    const end = start + pageSize.value;
    return tableData.value.slice(start, end);
});

const totalError = computed(() => props.data?.errors?.length || 0);
const totalDuplicate = computed(() => props.data?.duplicates?.length || 0);
const totalValid = computed(() => props.data?.validRows?.length || 0);

function handlePageChange(page) {
    currentPage.value = page;
}

function handlePageSizeChange(size) {
    pageSize.value = size;
    currentPage.value = 1;
}

function handleClose() {
    emit('cancel');
}

const headerStyle = {
    background: '#E2EFFF',
    color: '#495057',
    fontWeight: '600',
    textAlign: 'center',
};

function getRowColor(type) {
    return ROW_COLOR[type] || 'inherit';
}

function handleImport() {
    const payload = {
        validRows: props.data?.validRows || [],
        shouldUpdateDuplicate: shouldUpdateDuplicate.value,
    };

    if (shouldUpdateDuplicate.value) {
        payload.duplicates = props.data?.duplicates || [];
    }
    emit('import', payload);
}

function handleCancel() {
    shouldUpdateDuplicate.value = false;
    emit('cancel');
}

function handleDownloadErrorFile() {
    const payload = {
        invalidRows: props.data?.errors || [],
        duplicates: props.data?.duplicates || [],
        schoolYearId: props.data?.errors?.[0]?.rowData?.schoolYearId ?? null,
    };
    emit('downloadError', payload);
}

const isImportButtonDisabled = computed(() => {
    if (props.loading || !props.isApiCalled) {
        return true;
    }

    const validCount = totalValid.value;
    const duplicateCount = totalDuplicate.value;

    if (validCount === 0 && duplicateCount === 0) {
        return true;
    }

    if (duplicateCount > 0 && validCount === 0 && !shouldUpdateDuplicate.value) {
        return true;
    }

    return false;
});
</script>

<style scoped>
.table-wrapper {
    flex-direction: column;
    width: 100%;
}

.pagination-container {
    margin-top: 15px;
    text-align: right;
}

.pagination-slot {
    display: flex;
    align-items: center;
    gap: 8px;
}

.pagination-slot .el-select {
    width: 120px;
}

:deep(.el-table__body-wrapper) {
    overflow-x: auto !important;
    overflow-y: auto !important;
}

.footer-container {
    margin-top: 15px;
}

.stat-row {
    font-weight: bold;
}

.label-text {
    font-size: 18px;
    font-weight: bold;
}

.download-row {
    display: flex;
    align-items: center;
}

.custom-download-icon {
    color: #005b94;
    width: 16px;
    height: 16px;
    margin-right: 8px;
}


:deep(.large-checkbox .el-checkbox__inner) {
    width: 18px;
    height: 18px;
}

:deep(.el-pager li.number) {
    background-color: transparent;
}

:deep(.el-pager .is-active) {
    background-color: #005b94 !important;
    color: white !important;
}

:deep(.custom-table-dialog) {
    height: 700px;
}

:deep(.custom-table-dialog .el-dialog__body) {
    max-height: 500px;
    overflow-y: auto;
}

:deep(.custom-col) {
    padding-left: 0 !important;
    padding-right: 0 !important;
}

.custom-link {
    color: #1b84ff;
    text-decoration: underline;
    text-decoration-color: #1b84ff;
    text-decoration-thickness: 0.5px;
    text-underline-offset: 2px;
}

.custom-link:hover {
    text-decoration: none;
}
</style>

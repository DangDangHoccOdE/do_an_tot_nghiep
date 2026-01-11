<template>
    <div class="metrics-page">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <span>{{ $t('admin.metrics.title') }}</span>
                </div>
            </template>

            <!-- Filters -->
            <div class="filters">
                <el-select v-model="selectedProject" :placeholder="$t('admin.metrics.selectProject')"
                    @change="loadMetrics" clearable size="large" class="filter-project">
                    <el-option v-for="project in projects" :key="project.id" :label="project.projectName"
                        :value="project.id" />
                </el-select>

                <el-date-picker v-model="dateRange" type="daterange" range-separator="to" format="DD/MM/YYYY"
                    value-format="YYYY-MM-DD" :start-placeholder="$t('admin.metrics.startDate')"
                    :end-placeholder="$t('admin.metrics.endDate')" @change="loadMetrics" size="large"
                    class="filter-date" />

                <UiButton v-if="canCalculate" variant="refresh" size="large" :label="$t('admin.metrics.calculate')"
                    @click="calculateMetrics" class="calculate-btn" />
            </div>

            <!-- Metrics Stats -->
            <div class="metrics-stats">
                <div v-for="metric in currentMetrics" :key="metric.id" class="stat-card">
                    <div class="stat-item">
                        <span class="stat-label">{{ $t('admin.metrics.totalTasks') }}:</span>
                        <span class="stat-value">{{ metric.totalTasks }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">{{ $t('admin.metrics.completed') }}:</span>
                        <span class="stat-value success">{{ metric.completedTasks }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">{{ $t('admin.metrics.inProgress') }}:</span>
                        <span class="stat-value warning">{{ metric.inProgressTasks }}</span>
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">{{ $t('admin.metrics.completionRate') }}:</span>
                        <el-progress :percentage="metric.completionRate || 0"
                            :color="getProgressColor(metric.completionRate)" />
                    </div>
                    <div class="stat-item">
                        <span class="stat-label">{{ $t('admin.metrics.teamMembers') }}:</span>
                        <span class="stat-value">{{ metric.teamMembersAssigned }}</span>
                    </div>
                </div>
            </div>

            <!-- Metrics Table -->
            <TableListView :data="metricsData" :columns="metricsColumns" :total="metricsData.length" :current-page="1"
                :page-size="50" :loading="false" :selectable="false">
                <template #projectName="{ row }">
                    <span>{{ row.projectName || '--' }}</span>
                </template>

                <template #reportDate="{ row }">
                    <span>{{ row.reportDate || '--' }}</span>
                </template>

                <template #completionRate="{ row }">
                    <el-progress :percentage="row.completionRate || 0" :color="getProgressColor(row.completionRate)"
                        :show-text="false" />
                </template>
            </TableListView>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import UiButton from '@/components/common/UiButton.vue'
import TableListView from '@/components/common/TableListView.vue'
import { apiProjectMetrics } from '@/services/apiProjectMetrics'
import { apiProjects } from '@/services/apiProjects'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const { t } = useI18n()
const authStore = useAuthStore()

const projects = ref([])
const metricsData = ref([])
const selectedProject = ref(null)
const dateRange = ref([])

const canCalculate = computed(() => {
    return authStore.isAdmin
})

const emptyText = computed(() => {
    return t('noData')
})

const metricsColumns = computed(() => [
    {
        prop: 'projectName',
        label: t('admin.metrics.project'),
        minWidth: 200,
        slot: 'projectName'
    },
    {
        prop: 'reportDate',
        label: t('admin.metrics.date'),
        minWidth: 120,
        slot: 'reportDate'
    },
    {
        prop: 'totalTasks',
        label: t('admin.metrics.totalTasks'),
        minWidth: 100
    },
    {
        prop: 'completedTasks',
        label: t('admin.metrics.completed'),
        minWidth: 100
    },
    {
        prop: 'inProgressTasks',
        label: t('admin.metrics.inProgress'),
        minWidth: 120
    },
    {
        prop: 'blockedTasks',
        label: t('admin.metrics.blocked'),
        minWidth: 100
    },
    {
        prop: 'completionRate',
        label: t('admin.metrics.completionRate'),
        minWidth: 120,
        slot: 'completionRate'
    },
    {
        prop: 'teamMembersAssigned',
        label: t('admin.metrics.teamMembers'),
        minWidth: 120
    }
])

const currentMetrics = computed(() => {
    if (selectedProject.value && metricsData.value.length > 0) {
        return metricsData.value.filter(m => m.projectId === selectedProject.value)
    }
    return metricsData.value.slice(0, 1)
})

onMounted(async () => {
    await loadProjects()
    await loadMetrics()
})

const loadProjects = async () => {
    try {
        const response = await apiProjects.list()
        projects.value = response.content || []
        if (projects.value.length > 0) {
            selectedProject.value = projects.value[0].id
        }
    } catch (error) {
        console.error('Failed to load projects', error)
    }
}

const loadMetrics = async () => {
    try {
        if (selectedProject.value) {
            if (dateRange.value && dateRange.value.length === 2) {
                const startDate = dateRange.value[0] instanceof Date
                    ? dateRange.value[0].toISOString().split('T')[0]
                    : dateRange.value[0]
                const endDate = dateRange.value[1] instanceof Date
                    ? dateRange.value[1].toISOString().split('T')[0]
                    : dateRange.value[1]
                const response = await apiProjectMetrics.getByProjectDateRange(
                    selectedProject.value,
                    startDate,
                    endDate
                )
                metricsData.value = response.data || []
            } else {
                const today = new Date().toISOString().split('T')[0]
                try {
                    const response = await apiProjectMetrics.getByProjectAndDate(selectedProject.value, today)
                    metricsData.value = response ? [response] : []
                } catch (err) {
                    // If metrics not found, initialize empty array
                    metricsData.value = []
                    ElMessage.warning(t('admin.metrics.noMetricsFound'))
                }
            }
        } else {
            metricsData.value = []
        }
    } catch (error) {
        console.error('Failed to load metrics', error)
        metricsData.value = []
        const errorMessage = error?.response?.data?.message || error?.message || t('common.loadFailed')
        ElMessage.error(errorMessage)
    }
}

const calculateMetrics = async () => {
    try {
        if (!selectedProject.value) {
            ElMessage.warning(t('admin.metrics.selectProjectFirst'))
            return
        }
        const today = new Date().toISOString().split('T')[0]

        // Avoid duplicate insert: check existing metrics for today
        try {
            const existing = await apiProjectMetrics.getByProjectAndDate(selectedProject.value, today)
            if (existing) {
                metricsData.value = [existing]
                ElMessage.info(t('admin.metrics.alreadyExists'))
                return
            }
        } catch (checkErr) {
            // if not found, continue to calculate
        }

        await apiProjectMetrics.calculateAndSave(selectedProject.value, today)
        ElMessage.success(t('admin.metrics.calculateSuccess'))
        await loadMetrics()
    } catch (error) {
        console.error('Calculate error', error)
        // Display backend error message if available
        const errorMessage = error?.response?.data?.message || error?.message || t('admin.metrics.calculateFailed')
        ElMessage.error(errorMessage)
    }
}

const getProgressColor = (percentage) => {
    if (percentage >= 80) return '#67C23A'
    if (percentage >= 60) return '#409EFF'
    if (percentage >= 40) return '#E6A23C'
    return '#F56C6C'
}
</script>

<style scoped>
.metrics-page {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.filters {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
    flex-wrap: wrap;
    align-items: center;
}

.filters>* {
    flex: 1;
    min-width: 150px;
}

.filter-project,
.filter-date {
    max-width: 300px;
}

.calculate-btn {
    flex: 0 0 auto;
    width: auto !important;
    min-width: 120px !important;
    max-width: 150px !important;
}

.metrics-stats {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 15px;
    margin-bottom: 20px;
}

.stat-card {
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 15px;
    background-color: #fafafa;
}

.stat-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    padding: 8px 0;
}

.stat-label {
    font-weight: bold;
    color: #333;
}

.stat-value {
    font-size: 18px;
    color: #409EFF;
    font-weight: bold;
}

.stat-value.success {
    color: #67C23A;
}

.stat-value.warning {
    color: #E6A23C;
}

.stat-item:last-child {
    margin-bottom: 0;
}
</style>

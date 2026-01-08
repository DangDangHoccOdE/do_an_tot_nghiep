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
                    @change="loadMetrics" clearable>
                    <el-option v-for="project in projects" :key="project.id" :label="project.projectName"
                        :value="project.id" />
                </el-select>

                <el-date-picker v-model="dateRange" type="daterange" range-separator="to"
                    :start-placeholder="$t('admin.metrics.startDate')" :end-placeholder="$t('admin.metrics.endDate')"
                    @change="loadMetrics" />

                <el-button type="primary" @click="calculateMetrics" v-if="canCalculate">
                    {{ $t('admin.metrics.calculate') }}
                </el-button>
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
            <el-table :data="metricsData" stripe style="width: 100%; margin-top: 20px">
                <el-table-column prop="projectName" :label="$t('admin.metrics.project')" width="200" />
                <el-table-column prop="reportDate" :label="$t('admin.metrics.date')" width="120" />
                <el-table-column prop="totalTasks" :label="$t('admin.metrics.totalTasks')" width="100" />
                <el-table-column prop="completedTasks" :label="$t('admin.metrics.completed')" width="100" />
                <el-table-column prop="inProgressTasks" :label="$t('admin.metrics.inProgress')" width="120" />
                <el-table-column prop="blockedTasks" :label="$t('admin.metrics.blocked')" width="100" />
                <el-table-column prop="completionRate" :label="$t('admin.metrics.completionRate')" width="120">
                    <template #default="{ row }">
                        <el-progress :percentage="row.completionRate || 0" :color="getProgressColor(row.completionRate)"
                            :show-text="false" />
                    </template>
                </el-table-column>
                <el-table-column prop="teamMembersAssigned" :label="$t('admin.metrics.teamMembers')" width="120" />
            </el-table>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import * as apiProjectMetrics from '@/services/apiProjectMetrics'
import * as apiProjects from '@/services/apiProjects'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const { t } = useI18n()
const authStore = useAuthStore()

const projects = ref([])
const metricsData = ref([])
const selectedProject = ref(null)
const dateRange = ref([])

const canCalculate = computed(() => {
    return authStore.hasRole('ROLE_ADMIN')
})

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
        const response = await apiProjects.getAllProjects()
        projects.value = response.data || []
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
                const startDate = dateRange.value[0].toISOString().split('T')[0]
                const endDate = dateRange.value[1].toISOString().split('T')[0]
                const response = await apiProjectMetrics.getMetricsForProjectDateRange(
                    selectedProject.value,
                    startDate,
                    endDate
                )
                metricsData.value = response.data || []
            } else {
                const today = new Date().toISOString().split('T')[0]
                const response = await apiProjectMetrics.getMetricsForProject(selectedProject.value, today)
                metricsData.value = [response.data]
            }
        }
    } catch (error) {
        console.error('Failed to load metrics', error)
        ElMessage.error(t('common.loadFailed'))
    }
}

const calculateMetrics = async () => {
    try {
        if (!selectedProject.value) {
            ElMessage.warning(t('admin.metrics.selectProjectFirst'))
            return
        }
        const today = new Date().toISOString().split('T')[0]
        await apiProjectMetrics.calculateAndSaveMetrics(selectedProject.value, today)
        ElMessage.success(t('admin.metrics.calculateSuccess'))
        await loadMetrics()
    } catch (error) {
        console.error('Calculate error', error)
        ElMessage.error(t('admin.metrics.calculateFailed'))
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
}

.filters>* {
    flex: 1;
    min-width: 150px;
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

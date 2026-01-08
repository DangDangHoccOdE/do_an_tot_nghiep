<template>
    <div class="page">
        <SectionCard>
            <div class="page-header">
                <div class="title-group">
                    <h2 class="page-title">{{ t('admin.menu.revenueManagement') }}</h2>
                </div>
                <div class="header-actions">
                    <el-button @click="fetchData">{{ t('admin.actions.refresh') }}</el-button>
                    <el-button type="primary" @click="exportData" :loading="exporting">
                        {{ t('admin.actions.export') }}
                    </el-button>
                </div>
            </div>

            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-label">{{ t('admin.revenue.totalRevenue') }}</div>
                    <div class="stat-value">{{ formatCurrency(stats.totalRevenue, 'VND') }}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">{{ t('admin.revenue.thisMonth') }}</div>
                    <div class="stat-value">{{ formatCurrency(stats.thisMonthRevenue, 'VND') }}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">{{ t('admin.revenue.completedProjects') }}</div>
                    <div class="stat-value">{{ stats.completedProjects }}</div>
                </div>
                <div class="stat-card">
                    <div class="stat-label">{{ t('admin.revenue.avgProjectValue') }}</div>
                    <div class="stat-value">{{ formatCurrency(stats.avgProjectValue, 'VND') }}</div>
                </div>
            </div>

            <!-- Year Selector -->
            <div class="year-selector">
                <el-select v-model="selectedYear" size="large" @change="fetchData">
                    <el-option v-for="year in yearOptions" :key="year" :label="year" :value="year" />
                </el-select>
            </div>

            <!-- Monthly Revenue Chart -->
            <div class="chart-container">
                <div class="chart-header">
                    <h3 class="chart-title">{{ t('admin.revenue.monthlyChart') }}</h3>
                </div>
                <div class="chart" ref="chartContainer"></div>
            </div>

            <!-- Top Projects by Revenue -->
            <div class="top-projects-container">
                <div class="section-title">{{ t('admin.revenue.topByRevenue') }}</div>
                <el-table :data="topProjectsByRevenue" stripe style="width: 100%">
                    <el-table-column :label="t('admin.table.projectName')" min-width="200">
                        <template #default="scope">{{ scope.row.projectName }}</template>
                    </el-table-column>
                    <el-table-column :label="t('admin.form.clientId')" min-width="150">
                        <template #default="scope">{{ scope.row.clientName }}</template>
                    </el-table-column>
                    <el-table-column :label="t('admin.revenue.revenue')" width="160">
                        <template #default="scope">
                            <span class="revenue-value">{{ formatCurrency(scope.row.budgetEstimated,
                                scope.row.currencyUnit) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="t('admin.table.status')" width="130">
                        <template #default="scope">
                            <el-tag :type="statusMeta(scope.row.status).type" effect="dark" size="small">
                                {{ statusMeta(scope.row.status).label }}
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column :label="t('admin.table.endDate')" width="130">
                        <template #default="scope">{{ formatDate(scope.row.endDate) }}</template>
                    </el-table-column>
                </el-table>
            </div>

            <!-- Top Projects by Completion Date -->
            <div class="top-projects-container">
                <div class="section-title">{{ t('admin.revenue.topCompleted') }}</div>
                <el-table :data="topProjectsByCompletion" stripe style="width: 100%">
                    <el-table-column :label="t('admin.table.projectName')" min-width="200">
                        <template #default="scope">{{ scope.row.projectName }}</template>
                    </el-table-column>
                    <el-table-column :label="t('admin.form.clientId')" min-width="150">
                        <template #default="scope">{{ scope.row.clientName }}</template>
                    </el-table-column>
                    <el-table-column :label="t('admin.table.endDate')" width="160">
                        <template #default="scope">{{ formatDate(scope.row.endDate) }}</template>
                    </el-table-column>
                    <el-table-column :label="t('admin.revenue.revenue')" width="160">
                        <template #default="scope">
                            <span class="revenue-value">{{ formatCurrency(scope.row.budgetEstimated,
                                scope.row.currencyUnit) }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="t('admin.table.status')" width="130">
                        <template #default="scope">
                            <el-tag :type="statusMeta(scope.row.status).type" effect="dark" size="small">
                                {{ statusMeta(scope.row.status).label }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </SectionCard>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiProjects } from '@/services/apiProjects'
import { apiRevenue } from '@/services/apiRevenue'
import * as echarts from 'echarts'

const { t } = useI18n()

const selectedYear = ref(new Date().getFullYear())
const yearOptions = computed(() => {
    const years = []
    const currentYear = new Date().getFullYear()
    for (let i = currentYear; i >= currentYear - 5; i--) {
        years.push(i)
    }
    return years
})

const stats = reactive({
    totalRevenue: 0,
    thisMonthRevenue: 0,
    completedProjects: 0,
    avgProjectValue: 0
})

const topProjectsByRevenue = ref([])
const topProjectsByCompletion = ref([])
const chartContainer = ref(null)
let chartInstance = null
const exporting = ref(false)
const loading = ref(false)

const statusLookup = computed(() => ({
    PENDING: { label: t('admin.projectStatus.PENDING'), type: 'warning' },
    APPROVED: { label: t('admin.projectStatus.APPROVED'), type: 'info' },
    IN_PROGRESS: { label: t('admin.projectStatus.IN_PROGRESS'), type: 'info' },
    DONE: { label: t('admin.projectStatus.DONE'), type: 'success' },
    CANCELLED: { label: t('admin.projectStatus.CANCELLED'), type: 'danger' }
}))

const statusMeta = (status) => statusLookup.value[status] ?? { label: status || '--', type: 'info' }

const formatDate = (value) => {
    if (!value) return '--'
    const date = new Date(value)
    const day = String(date.getDate()).padStart(2, '0')
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const year = date.getFullYear()
    return `${day}/${month}/${year}`
}

const formatCurrency = (value, currencyUnit = 'VND') => {
    if (value === null || value === undefined || value === '') return '--'
    const locale = currencyUnit === 'JPY' ? 'ja-JP' : currencyUnit === 'USD' ? 'en-US' : 'vi-VN'
    const currency = currencyUnit || 'VND'
    const options = {
        style: 'currency',
        currency: currency,
        maximumFractionDigits: currency === 'JPY' ? 0 : 0
    }
    return new Intl.NumberFormat(locale, options).format(value)
}

const fetchData = async () => {
    loading.value = true
    try {
        // Fetch all projects for current year
        const allProjects = await apiProjects.list({ page: 0, size: 10000 })
        const projects = allProjects.content || []

        // Filter by completed and calculate stats
        const completedProjects = projects.filter(p => p.status === 'DONE')
        const thisYear = new Date().getFullYear()
        const thisMonth = new Date().getMonth() + 1
        const thisMonthProjects = projects.filter(p => {
            if (!p.endDate) return false
            const endDate = new Date(p.endDate)
            return endDate.getFullYear() === thisYear && endDate.getMonth() + 1 === thisMonth
        })

        // Calculate stats
        stats.totalRevenue = projects.reduce((sum, p) => sum + (p.budgetEstimated || 0), 0)
        stats.thisMonthRevenue = thisMonthProjects.reduce((sum, p) => sum + (p.budgetEstimated || 0), 0)
        stats.completedProjects = completedProjects.length
        stats.avgProjectValue = projects.length > 0 ? stats.totalRevenue / projects.length : 0

        // Top by revenue - sort and get top 5
        topProjectsByRevenue.value = projects
            .sort((a, b) => (b.budgetEstimated || 0) - (a.budgetEstimated || 0))
            .slice(0, 5)

        // Top by completion - get completed projects sorted by endDate ascending
        topProjectsByCompletion.value = completedProjects
            .sort((a, b) => {
                const dateA = a.endDate ? new Date(a.endDate).getTime() : 0
                const dateB = b.endDate ? new Date(b.endDate).getTime() : 0
                return dateA - dateB
            })
            .slice(0, 5)

        // Draw monthly chart
        await nextTick()
        drawMonthlyChart(projects, selectedYear.value)
    } catch (error) {
        console.error('Failed to fetch revenue data:', error)
        ElMessage.error(t('admin.messages.loadFail'))
    } finally {
        loading.value = false
    }
}

const drawMonthlyChart = (projects, year) => {
    if (!chartContainer.value) return

    // Initialize data for 12 months
    const monthlyData = Array(12).fill(0)

    // Aggregate revenue by month
    projects.forEach(p => {
        if (p.endDate && p.status === 'DONE') {
            const endDate = new Date(p.endDate)
            if (endDate.getFullYear() === year) {
                const month = endDate.getMonth()
                monthlyData[month] += p.budgetEstimated || 0
            }
        }
    })

    // Destroy previous chart
    if (chartInstance) {
        chartInstance.dispose()
    }

    // Create chart
    chartInstance = echarts.init(chartContainer.value)

    const option = {
        title: {
            text: `${t('admin.revenue.monthlyRevenue')} - ${year}`,
            textStyle: {
                fontSize: 14,
                fontWeight: 'bold'
            }
        },
        tooltip: {
            trigger: 'axis',
            formatter: (params) => {
                if (params.length === 0) return ''
                const value = params[0].value
                return `${params[0].name}: ${formatCurrency(value, 'VND')}`
            }
        },
        grid: {
            left: 50,
            right: 30,
            bottom: 30,
            top: 60,
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
            axisLine: {
                lineStyle: {
                    color: '#ccc'
                }
            }
        },
        yAxis: {
            type: 'value',
            axisLabel: {
                formatter: (value) => {
                    if (value >= 1000000000) return (value / 1000000000).toFixed(0) + 'B'
                    if (value >= 1000000) return (value / 1000000).toFixed(0) + 'M'
                    if (value >= 1000) return (value / 1000).toFixed(0) + 'K'
                    return value
                }
            }
        },
        series: [
            {
                data: monthlyData,
                type: 'bar',
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#83bff6' },
                        { offset: 0.5, color: '#188df0' },
                        { offset: 1, color: '#188df0' }
                    ])
                },
                emphasis: {
                    itemStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: '#2378f7' },
                            { offset: 0.7, color: '#0e6de8' },
                            { offset: 1, color: '#0095d8' }
                        ])
                    }
                },
                smooth: true
            }
        ]
    }

    chartInstance.setOption(option)

    // Responsive
    window.addEventListener('resize', () => {
        chartInstance?.resize()
    })
}

const exportData = async () => {
    exporting.value = true
    try {
        const response = await apiRevenue.exportRevenue(selectedYear.value)
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `revenue_${selectedYear.value}.xlsx`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        ElMessage.success(t('admin.messages.exportSuccess'))
    } catch (error) {
        console.error('Failed to export:', error)
        ElMessage.error(t('admin.messages.exportFail'))
    } finally {
        exporting.value = false
    }
}

onMounted(() => {
    fetchData()
})
</script>

<style scoped>
.page {
    display: grid;
    gap: 16px;
}

.page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
}

.title-group {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.page-title {
    margin: 0;
    font-size: 22px;
    font-weight: 700;
    color: #0f172a;
}

.header-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 24px;
}

.stat-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 20px;
    color: white;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-card:nth-child(2) {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card:nth-child(3) {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card:nth-child(4) {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-label {
    font-size: 14px;
    font-weight: 600;
    opacity: 0.9;
    margin-bottom: 8px;
}

.stat-value {
    font-size: 24px;
    font-weight: 700;
}

.year-selector {
    margin-bottom: 24px;
    display: flex;
    gap: 12px;
    align-items: center;
}

.year-selector :deep(.el-select) {
    width: 120px;
}

.chart-container {
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 16px;
    margin-bottom: 24px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.chart-header {
    margin-bottom: 16px;
}

.chart-title {
    margin: 0;
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
}

.chart {
    width: 100%;
    height: 400px;
}

.top-projects-container {
    margin-bottom: 32px;
}

.section-title {
    font-size: 16px;
    font-weight: 700;
    color: #0f172a;
    margin-bottom: 16px;
}

.revenue-value {
    color: #10b981;
    font-weight: 600;
}
</style>

<template>
    <div class="feedback-dashboard">
        <el-card class="box-card">
            <template #header>
                <div class="card-header">
                    <span>{{ t('admin.feedbackDashboard') }}</span>
                    <el-button :icon="Refresh" circle @click="loadStatistics" :loading="isLoading" />
                </div>
            </template>

            <!-- Statistics Cards -->
            <div class="statistics-grid">
                <!-- Average Rating -->
                <div class="stat-card">
                    <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                        <el-icon>
                            <Star />
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <p class="stat-label">{{ t('admin.averageRating') }}</p>
                        <p class="stat-value">{{ statistics?.averageRating?.toFixed(1) || '0.0' }}</p>
                        <p class="stat-unit">/ 5.0</p>
                    </div>
                </div>

                <!-- Total Feedbacks -->
                <div class="stat-card">
                    <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                        <el-icon>
                            <ChatDotSquare />
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <p class="stat-label">{{ t('admin.totalFeedbacks') }}</p>
                        <p class="stat-value">{{ statistics?.totalFeedbacks || '0' }}</p>
                        <p class="stat-unit">{{ t('admin.responses') }}</p>
                    </div>
                </div>

                <!-- Helpful Count -->
                <div class="stat-card">
                    <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                        <el-icon>
                            <SuccessFilled />
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <p class="stat-label">{{ t('admin.helpful') }}</p>
                        <p class="stat-value">{{ statistics?.helpfulCount || '0' }}</p>
                        <p class="stat-unit">
                            {{
                                statistics?.totalFeedbacks
                                    ? `(${((statistics.helpfulCount / statistics.totalFeedbacks) * 100).toFixed(0)}%)`
                            : '(0%)'
                            }}
                        </p>
                    </div>
                </div>

                <!-- Not Helpful Count -->
                <div class="stat-card">
                    <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
                        <el-icon>
                            <CircleCloseFilled />
                        </el-icon>
                    </div>
                    <div class="stat-content">
                        <p class="stat-label">{{ t('admin.notHelpful') }}</p>
                        <p class="stat-value">{{ statistics?.notHelpfulCount || '0' }}</p>
                        <p class="stat-unit">
                            {{
                                statistics?.totalFeedbacks
                                    ? `(${((statistics.notHelpfulCount / statistics.totalFeedbacks) * 100).toFixed(0)}%)`
                            : '(0%)'
                            }}
                        </p>
                    </div>
                </div>
            </div>

            <!-- Top Issues Section -->
            <div v-if="statistics?.topIssues?.length > 0" class="issues-section">
                <h4 class="section-title">
                    <el-icon>
                        <Warning />
                    </el-icon>
                    {{ t('admin.topIssues') }}
                </h4>
                <div class="issues-list">
                    <div v-for="(issue, index) in statistics.topIssues" :key="index" class="issue-item">
                        <div class="issue-bar-container">
                            <span class="issue-type">{{ formatIssueType(issue.issueType) }}</span>
                            <div class="issue-bar">
                                <div class="issue-bar-fill" :style="{
                                    width: `${(issue.count / (statistics.topIssues[0]?.count || 1)) * 100}%`,
                                    backgroundColor: getIssueColor(index)
                                }" />
                            </div>
                            <span class="issue-count">{{ issue.count }}</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Low Rated Feedbacks Section -->
            <div class="low-rated-section">
                <h4 class="section-title">
                    <el-icon>
                        <DocumentCopy />
                    </el-icon>
                    {{ t('admin.lowRatedFeedbacks') }}
                </h4>
                <el-table :data="lowRatedFeedbacks" stripe style="width: 100%; font-size: 12px"
                    :default-sort="{ prop: 'rating', order: 'ascending' }" v-loading="isLoadingLowRated">
                    <el-table-column prop="rating" label="Rating" width="80" sortable>
                        <template #default="{ row }">
                            <el-rate :model-value="row.rating" disabled size="small" />
                        </template>
                    </el-table-column>
                    <el-table-column prop="issueType" :label="t('admin.issueType')" width="120" />
                    <el-table-column prop="feedbackText" :label="t('admin.feedback')" show-overflow-tooltip />
                    <el-table-column prop="createdAt" :label="t('admin.date')" width="150" sortable>
                        <template #default="{ row }">
                            {{ formatDate(row.createdAt) }}
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
    Star,
    ChatDotSquare,
    SuccessFilled,
    CircleCloseFilled,
    Warning,
    DocumentCopy,
    Refresh
} from '@element-plus/icons-vue'
import { apiChat } from '@/services/apiChat'

const { t } = useI18n()

const statistics = ref(null)
const lowRatedFeedbacks = ref([])
const isLoading = ref(false)
const isLoadingLowRated = ref(false)

const loadStatistics = async () => {
    isLoading.value = true
    try {
        const [statsRes, lowRatedRes] = await Promise.all([
            apiChat.getFeedbackStatistics(),
            apiChat.getLowRatedFeedbacks()
        ])

        statistics.value = statsRes
        lowRatedFeedbacks.value = lowRatedRes || []
    } catch (error) {
        console.error('Load statistics error:', error)
        ElMessage.error(t('admin.loadStatisticsError'))
    } finally {
        isLoading.value = false
        isLoadingLowRated.value = false
    }
}

const formatIssueType = (issueType) => {
    const types = {
        'INACCURATE': t('chatFeedback.inaccurate'),
        'IRRELEVANT': t('chatFeedback.irrelevant'),
        'INCOMPLETE': t('chatFeedback.incomplete'),
        'RUDE': t('chatFeedback.rude'),
        'OTHER': t('common.other')
    }
    return types[issueType] || issueType
}

const getIssueColor = (index) => {
    const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
    return colors[index % colors.length]
}

const formatDate = (date) => {
    if (!date) return '-'
    return new Date(date).toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    })
}

onMounted(() => {
    loadStatistics()
})
</script>

<style scoped>
.feedback-dashboard {
    padding: 20px;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    font-size: 18px;
    font-weight: 600;
}

.statistics-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 20px;
    margin-bottom: 30px;
}

.stat-card {
    display: flex;
    gap: 16px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
    border-left: 4px solid transparent;
    transition: all 0.3s ease;
}

.stat-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 28px;
    flex-shrink: 0;
}

.stat-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    min-width: 0;
}

.stat-label {
    font-size: 12px;
    color: #909399;
    margin: 0 0 4px 0;
    font-weight: 500;
}

.stat-value {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    margin: 0;
}

.stat-unit {
    font-size: 11px;
    color: #909399;
    margin: 4px 0 0 0;
}

.issues-section,
.low-rated-section {
    margin-bottom: 30px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
}

.section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    margin: 0 0 16px 0;
    color: #303133;
}

.issues-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.issue-item {
    padding: 12px;
    background: #f5f7fa;
    border-radius: 6px;
}

.issue-bar-container {
    display: flex;
    align-items: center;
    gap: 12px;
}

.issue-type {
    min-width: 100px;
    font-weight: 500;
    font-size: 13px;
    color: #606266;
}

.issue-bar {
    flex: 1;
    height: 24px;
    background: #ebeef5;
    border-radius: 4px;
    overflow: hidden;
}

.issue-bar-fill {
    height: 100%;
    transition: width 0.3s ease;
}

.issue-count {
    min-width: 40px;
    text-align: right;
    font-weight: 600;
    color: #303133;
    font-size: 13px;
}

:deep(.el-table) {
    font-size: 12px;
}

:deep(.el-table__header) {
    background-color: #f5f7fa;
}
</style>

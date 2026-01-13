<template>
    <div class="intent-display">
        <!-- Intent Badge -->
        <div class="intent-badge" :class="`intent-${intentData?.detectedIntent?.toLowerCase() || 'general'}`">
            <el-icon>
                <DataAnalysis />
            </el-icon>
            <span class="intent-text">{{ getIntentLabel(intentData?.detectedIntent) }}</span>
            <el-popover placement="top" :content="`Confidence: ${(intentData?.confidenceScore * 100).toFixed(0)}%`"
                trigger="hover">
                <template #reference>
                    <span class="confidence-badge">{{ (intentData?.confidenceScore * 100).toFixed(0) }}%</span>
                </template>
            </el-popover>
        </div>

        <!-- Extracted Entities -->
        <el-collapse-transition>
            <div v-if="showDetails && intentData?.extractedEntities && Object.keys(intentData.extractedEntities).length > 0"
                class="entities-section">
                <div class="entities-header">
                    <el-icon>
                        <CollectionTag />
                    </el-icon>
                    <span>{{ t('chatIntent.extractedEntities') }}</span>
                </div>
                <div class="entities-list">
                    <div v-for="(value, key) in intentData.extractedEntities" :key="key" class="entity-item">
                        <span class="entity-key">{{ formatEntityKey(key) }}:</span>
                        <span class="entity-value">
                            <span v-if="Array.isArray(value)">{{ value.join(', ') }}</span>
                            <span v-else>{{ value }}</span>
                        </span>
                    </div>
                </div>
            </div>
        </el-collapse-transition>

        <!-- Toggle Details Button -->
        <button v-if="intentData?.extractedEntities && Object.keys(intentData.extractedEntities).length > 0"
            class="toggle-details-btn" @click="showDetails = !showDetails">
            {{ showDetails ? t('common.hideDetails') : t('common.showDetails') }}
        </button>
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { DataAnalysis, CollectionTag } from '@element-plus/icons-vue'

const props = defineProps({
    intentData: {
        type: Object,
        default: null
    }
})

const { t } = useI18n()

const showDetails = ref(false)

const getIntentLabel = (intent) => {
    const labels = {
        'PRICING_INQUIRY': t('chatIntent.pricingInquiry'),
        'TECH_RECOMMENDATION': t('chatIntent.techRecommendation'),
        'PROJECT_TIMELINE': t('chatIntent.projectTimeline'),
        'FEATURE_REQUEST': t('chatIntent.featureRequest'),
        'GENERAL_INFO': t('chatIntent.generalInfo')
    }
    return labels[intent] || intent || t('chatIntent.unknown')
}

const formatEntityKey = (key) => {
    const keys = {
        'budget': t('chatIntent.budget'),
        'timeline': t('chatIntent.timeline'),
        'project_type': t('chatIntent.projectType'),
        'mentioned_technologies': t('chatIntent.technologies')
    }
    return keys[key] || key
}
</script>

<style scoped>
.intent-display {
    background: #f0f9ff;
    border-left: 3px solid #409eff;
    border-radius: 6px;
    padding: 10px 12px;
    margin: 8px 0;
    font-size: 12px;
}

.intent-badge {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 6px 10px;
    border-radius: 4px;
    background: white;
    border: 1px solid #d9d9d9;
    width: fit-content;
}

.intent-badge.intent-pricing_inquiry {
    border-color: #67c23a;
    color: #67c23a;
}

.intent-badge.intent-tech_recommendation {
    border-color: #409eff;
    color: #409eff;
}

.intent-badge.intent-project_timeline {
    border-color: #e6a23c;
    color: #e6a23c;
}

.intent-badge.intent-feature_request {
    border-color: #ea4335;
    color: #ea4335;
}

.intent-badge.intent-general_info {
    border-color: #909399;
    color: #909399;
}

.intent-badge.intent-general {
    border-color: #d9d9d9;
    color: #606266;
}

.intent-text {
    font-weight: 500;
}

.confidence-badge {
    display: inline-block;
    background: rgba(255, 255, 255, 0.6);
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 11px;
    font-weight: 600;
}

.entities-section {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid #e1f5fe;
}

.entities-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-weight: 500;
    color: #409eff;
    margin-bottom: 8px;
}

.entities-list {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.entity-item {
    display: flex;
    gap: 8px;
    padding: 6px;
    background: white;
    border-radius: 4px;
    border: 1px solid #e1f5fe;
}

.entity-key {
    font-weight: 500;
    color: #606266;
    min-width: 100px;
}

.entity-value {
    color: #303133;
    word-break: break-word;
}

.toggle-details-btn {
    margin-top: 8px;
    background: none;
    border: none;
    color: #409eff;
    cursor: pointer;
    padding: 0;
    font-size: 12px;
    text-decoration: underline;
    transition: color 0.3s;
}

.toggle-details-btn:hover {
    color: #66b1ff;
}
</style>

<template>
    <div class="chat-feedback">
        <!-- Rating Stars Only (compact) -->
        <div class="rating-section">
            <span class="rating-label">{{ t('chatFeedback.rating') }}</span>
            <el-rate v-model="feedback.rating" :colors="['#f7ba16', '#f7ba16', '#409eff']" size="small" allow-half
                @change="onRatingChange" />
        </div>

        <!-- Expandable Feedback Form - shows when user interacts -->
        <el-collapse-transition>
            <div v-if="isExpanded" class="feedback-form">
                <!-- Helpful/Not Helpful Section -->
                <div class="feedback-section">
                    <span class="section-label">{{ t('chatFeedback.helpful') }}?</span>
                    <div class="button-group">
                        <el-button size="small" plain :type="feedback.isHelpful === true ? 'success' : ''"
                            :active="feedback.isHelpful === true" @click="setHelpful(true)">
                            <el-icon>
                                <ThumbsUpIcon />
                            </el-icon>
                            {{ t('chatFeedback.yes') }}
                        </el-button>
                        <el-button size="small" plain :type="feedback.isHelpful === false ? 'danger' : ''"
                            :active="feedback.isHelpful === false" @click="setHelpful(false)">
                            <el-icon>
                                <ThumbsDownIcon />
                            </el-icon>
                            {{ t('chatFeedback.no') }}
                        </el-button>
                    </div>
                </div>

                <!-- Issue Type Selection -->
                <div class="form-group">
                    <label>{{ t('chatFeedback.issueType') }}</label>
                    <el-select v-model="feedback.issueType" :placeholder="t('chatFeedback.selectIssue')" clearable
                        size="small">
                        <el-option :label="t('chatFeedback.inaccurate')" value="INACCURATE" />
                        <el-option :label="t('chatFeedback.irrelevant')" value="IRRELEVANT" />
                        <el-option :label="t('chatFeedback.incomplete')" value="INCOMPLETE" />
                        <el-option :label="t('chatFeedback.rude')" value="RUDE" />
                        <el-option :label="t('chatFeedback.other')" value="OTHER" />
                    </el-select>
                </div>

                <!-- Feedback Text -->
                <div class="form-group">
                    <label>{{ t('chatFeedback.feedbackText') }}</label>
                    <el-input v-model="feedback.feedbackText" type="textarea"
                        :placeholder="t('chatFeedback.feedbackPlaceholder')" :rows="2" size="small" maxlength="500"
                        show-word-limit />
                </div>

                <!-- Submit Button -->
                <div class="form-actions">
                    <el-button type="primary" size="small" :loading="isSubmitting" @click="submitFeedback">
                        {{ t('chatFeedback.submit') }}
                    </el-button>
                    <el-button size="small" @click="resetFeedback">
                        {{ t('common.cancel') }}
                    </el-button>
                </div>
            </div>
        </el-collapse-transition>

        <!-- Success Message -->
        <el-alert v-if="showSuccess" type="success" :title="t('chatFeedback.successMessage')" :closable="true"
            @close="showSuccess = false" style="margin-top: 8px" />
    </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'
import ThumbsUpIcon from '@/assets/icons/ThumbsUpIcon.vue'
import ThumbsDownIcon from '@/assets/icons/ThumbsDownIcon.vue'
import { apiChat } from '@/services/apiChat'

const props = defineProps({
    conversationId: {
        type: String,
        required: true
    },
    messageId: {
        type: String,
        required: true
    }
})

const emit = defineEmits(['feedback-submitted'])

const { t } = useI18n()

const feedback = ref({
    conversationId: props.conversationId,
    messageId: props.messageId,
    isHelpful: null,
    rating: null,
    issueType: null,
    feedbackText: ''
})

const isExpanded = computed(() => {
    return feedback.value.isHelpful !== null || feedback.value.rating !== null
})

const onRatingChange = () => {
    // Automatically expand form when user selects a rating
    if (feedback.value.rating !== null) {
        // Form will expand automatically due to isExpanded computed
    }
}

const isSubmitting = ref(false)
const showSuccess = ref(false)

const setHelpful = (value) => {
    feedback.value.isHelpful = feedback.value.isHelpful === value ? null : value
}

const submitFeedback = async () => {
    // Validate at least one feedback is given
    if (!feedback.value.isHelpful && !feedback.value.rating && !feedback.value.feedbackText) {
        ElMessage.warning(t('chatFeedback.selectAtLeast'))
        return
    }

    isSubmitting.value = true
    try {
        const payload = {
            conversationId: feedback.value.conversationId,
            messageId: feedback.value.messageId,
            isHelpful: feedback.value.isHelpful,
            rating: feedback.value.rating,
            issueType: feedback.value.issueType,
            feedbackText: feedback.value.feedbackText
        }

        await apiChat.submitFeedback(payload)

        showSuccess.value = true
        emit('feedback-submitted', payload)

        // Reset after 3 seconds
        setTimeout(() => {
            resetFeedback()
            showSuccess.value = false
        }, 3000)
    } catch (error) {
        console.error('Submit feedback error:', error)
        ElMessage.error(t('chatFeedback.submitError'))
    } finally {
        isSubmitting.value = false
    }
}

const resetFeedback = () => {
    feedback.value = {
        conversationId: props.conversationId,
        messageId: props.messageId,
        isHelpful: null,
        rating: null,
        issueType: null,
        feedbackText: ''
    }
}
</script>

<style scoped>
.chat-feedback {
    background: #f5f7fa;
    border-radius: 8px;
    padding: 12px;
    margin-top: 12px;
    font-size: 13px;
}

.rating-section {
    display: flex;
    align-items: center;
    gap: 8px;
    justify-content: center;
}

.rating-label {
    font-weight: 500;
    color: #606266;
    font-size: 12px;
    white-space: nowrap;
}

.feedback-form {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #dcdfe6;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.feedback-section {
    display: flex;
    align-items: center;
    gap: 8px;
}

.section-label {
    font-weight: 500;
    color: #606266;
    font-size: 12px;
    min-width: 80px;
}

.button-group {
    display: flex;
    gap: 6px;
}

.button-group :deep(.el-button) {
    padding: 6px 12px;
    font-size: 12px;
    border-radius: 4px;
}

.button-group :deep(.el-button.is-active) {
    border-color: currentColor;
}

.form-group {
    margin-bottom: 0;
}

.form-group label {
    display: block;
    font-weight: 500;
    color: #606266;
    margin-bottom: 6px;
    font-size: 12px;
}

.form-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
    margin-top: 8px;
}
</style>

<template>
    <div class="chat-history">
        <div class="panel list">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">{{ t('admin.chat.subtitle') }}</p>
                    <h3>{{ t('admin.chat.title') }}</h3>
                </div>
                <div class="panel-actions">
                    <el-button circle :icon="Refresh" size="small" @click="loadConversations"
                        :loading="loadingConversations" />
                    <el-button size="small" @click="syncDomain" :loading="syncing">
                        {{ t('admin.chat.sync') }}
                    </el-button>
                </div>
            </div>
            <el-table :data="conversations" size="small" @row-click="handleRowClick" height="520">
                <el-table-column prop="title" :label="t('admin.chat.columns.title')" min-width="180" />
                <el-table-column prop="locale" :label="t('admin.chat.columns.locale')" width="80" />
                <el-table-column prop="messageCount" :label="t('admin.chat.columns.messages')" width="110" />
                <el-table-column :label="t('admin.chat.columns.updated')" width="160">
                    <template #default="scope">
                        <span>{{ formatTime(scope.row.lastMessageAt) }}</span>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <div class="panel conversation">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">{{ selectedConversation ? t('admin.chat.thread') : t('admin.chat.pick') }}</p>
                    <h3>{{ selectedConversation?.title || t('admin.chat.noConversation') }}</h3>
                </div>
                <el-button size="small" type="primary" @click="loadMessages" :loading="loadingMessages"
                    :disabled="!selectedConversation">
                    {{ t('admin.chat.refreshMessages') }}
                </el-button>
            </div>

            <div v-if="loadingMessages" class="loader">{{ t('admin.chat.loading') }}</div>
            <div v-else-if="!selectedConversation" class="empty">{{ t('admin.chat.noConversation') }}</div>
            <div v-else class="message-list" ref="messagesContainer">
                <div v-for="item in messages" :key="item.id"
                    :class="['message', item.role === 'assistant' ? 'assistant' : 'user']">
                    <div class="meta">
                        <span class="role">{{ item.role }}</span>
                        <span class="time">{{ formatTime(item.createdAt) }}</span>
                    </div>
                    <p v-html="formatMessage(item.content)"></p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { apiChat } from '@/services/apiChat'

const { t } = useI18n()

const conversations = ref([])
const messages = ref([])
const selectedConversation = ref(null)
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const syncing = ref(false)
const messagesContainer = ref(null)

const loadConversations = async () => {
    loadingConversations.value = true
    try {
        const data = await apiChat.getConversations({ page: 0, size: 30 })
        conversations.value = data || []
        if (!selectedConversation.value && conversations.value.length) {
            selectedConversation.value = conversations.value[0]
            await loadMessages()
        }
    } catch (error) {
        console.error('Load conversations failed', error)
        ElMessage.error(t('admin.chat.error'))
    } finally {
        loadingConversations.value = false
    }
}

const loadMessages = async () => {
    if (!selectedConversation.value) return
    loadingMessages.value = true
    try {
        const data = await apiChat.getMessages(selectedConversation.value.id)
        messages.value = data || []
        await nextTick()
        if (messagesContainer.value) {
            messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
    } catch (error) {
        console.error('Load messages failed', error)
        ElMessage.error(t('admin.chat.error'))
    } finally {
        loadingMessages.value = false
    }
}

const handleRowClick = async (row) => {
    selectedConversation.value = row
    await loadMessages()
}

const syncDomain = async () => {
    syncing.value = true
    try {
        await apiChat.syncDomain()
        ElMessage.success(t('admin.chat.synced'))
        await loadConversations()
    } catch (error) {
        console.error('Sync domain failed', error)
        ElMessage.error(t('admin.chat.error'))
    } finally {
        syncing.value = false
    }
}

const formatTime = (value) => {
    if (!value) return '--'
    return new Date(value).toLocaleString()
}

const formatMessage = (text) => {
    return (text || '').replace(/\n/g, '<br>')
}

onMounted(() => {
    loadConversations()
})
</script>

<style scoped>
.chat-history {
    display: grid;
    grid-template-columns: 1fr 1.2fr;
    gap: 16px;
}

.panel {
    background: #fff;
    border: 1px solid #e5e7eb;
    border-radius: 12px;
    padding: 14px;
    display: flex;
    flex-direction: column;
    height: 100%;
}

.panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
}

.panel-actions {
    display: flex;
    gap: 8px;
}

.eyebrow {
    margin: 0;
    color: #6b7280;
    font-size: 12px;
}

h3 {
    margin: 2px 0 0;
}

.conversation {
    min-height: 540px;
}

.message-list {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding-right: 6px;
}

.message {
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    padding: 10px 12px;
    background: #f9fafb;
}

.message.assistant {
    border-color: #c7d2fe;
    background: #eef2ff;
}

.meta {
    display: flex;
    justify-content: space-between;
    color: #6b7280;
    font-size: 12px;
    margin-bottom: 6px;
}

.empty,
.loader {
    color: #6b7280;
    text-align: center;
    padding: 24px 12px;
}

@media (max-width: 960px) {
    .chat-history {
        grid-template-columns: 1fr;
    }
}
</style>

<template>
    <div class="chat-widget">
        <!-- Chat Button -->
        <transition name="bounce">
            <button v-if="!isOpen" @click="toggleChat" class="chat-button" :class="{ 'has-unread': hasUnread }">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                </svg>
                <span v-if="hasUnread" class="unread-badge">{{ unreadCount }}</span>
            </button>
        </transition>

        <!-- Chat Window -->
        <transition name="slide-up">
            <div v-if="isOpen" class="chat-window">
                <!-- Header -->
                <div class="chat-header">
                    <div class="header-info">
                        <div class="avatar">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                                fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round">
                                <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8z">
                                </path>
                                <circle cx="12" cy="12" r="3"></circle>
                            </svg>
                        </div>
                        <div class="header-text">
                            <h3>{{ t('chatWidget.title') }}</h3>
                            <span class="status" :class="{ 'online': isOnline }">
                                <span class="status-dot"></span>
                                {{ isOnline ? t('chatWidget.online') : t('chatWidget.offline') }}
                            </span>
                        </div>
                    </div>
                    <button @click="toggleChat" class="close-button">
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                            stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <line x1="18" y1="6" x2="6" y2="18"></line>
                            <line x1="6" y1="6" x2="18" y2="18"></line>
                        </svg>
                    </button>
                </div>

                <!-- Messages -->
                <div class="chat-messages" ref="messagesContainer">
                    <!-- Welcome Message -->
                    <div v-if="messages.length === 0" class="welcome-message">
                        <div class="welcome-icon">👋</div>
                        <h4>{{ t('chatWidget.welcome') }}</h4>
                        <p>{{ t('chatWidget.welcomeDesc') }}</p>
                        <div class="quick-questions">
                            <button v-for="(question, index) in quickQuestions" :key="index"
                                @click="sendQuickQuestion(question)" class="quick-question-btn">
                                {{ question }}
                            </button>
                        </div>
                    </div>

                    <!-- Chat Messages -->
                    <div v-for="(msg, index) in messages" :key="index" class="message"
                        :class="{ 'user-message': msg.role === 'user', 'bot-message': msg.role === 'assistant' }">
                        <div class="message-avatar" v-if="msg.role === 'assistant'">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                                fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"></circle>
                                <circle cx="12" cy="12" r="3"></circle>
                            </svg>
                        </div>
                        <div class="message-content">
                            <div class="message-text" v-html="formatMessage(msg.content)"></div>
                            <span class="message-time">{{ formatTime(msg.timestamp) }}</span>
                        </div>
                    </div>

                    <!-- Typing Indicator -->
                    <div v-if="isTyping" class="message bot-message typing-indicator">
                        <div class="message-avatar">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                                fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                stroke-linejoin="round">
                                <circle cx="12" cy="12" r="10"></circle>
                                <circle cx="12" cy="12" r="3"></circle>
                            </svg>
                        </div>
                        <div class="message-content">
                            <div class="typing-dots">
                                <span></span>
                                <span></span>
                                <span></span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Input -->
                <div class="chat-input">
                    <el-input v-model="userInput" :placeholder="t('chatWidget.inputPlaceholder')"
                        @keyup.enter="sendMessage" :disabled="isTyping" size="large">
                        <template #suffix>
                            <el-button :icon="Promotion" circle @click="sendMessage"
                                :disabled="!userInput.trim() || isTyping" :loading="isTyping" />
                        </template>
                    </el-input>
                </div>

                <!-- Footer -->
                <div class="chat-footer">
                    <span>{{ t('chatWidget.poweredBy') }} <strong>{{ aiProvider }}</strong></span>
                </div>
            </div>
        </transition>
    </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'

const { t } = useI18n()

const isOpen = ref(false)
const isTyping = ref(false)
const isOnline = ref(true)
const userInput = ref('')
const messages = ref([])
const messagesContainer = ref(null)
const hasUnread = ref(false)
const unreadCount = ref(0)

// AI Provider - Change this to 'openai' or 'gemini'
const AI_PROVIDER = 'gemini' // 'gemini' or 'openai'
const aiProvider = computed(() => AI_PROVIDER === 'gemini' ? 'Google Gemini' : 'OpenAI GPT')

// API Keys - Người dùng cần thay thế bằng key của họ
const GEMINI_API_KEY = 'YOUR_GEMINI_API_KEY_HERE' // Get from: https://makersuite.google.com/app/apikey
const OPENAI_API_KEY = 'YOUR_OPENAI_API_KEY_HERE' // Get from: https://platform.openai.com/api-keys

const quickQuestions = computed(() => [
    t('chatWidget.quickQ1'),
    t('chatWidget.quickQ2'),
    t('chatWidget.quickQ3'),
    t('chatWidget.quickQ4')
])

const systemPrompt = `Bạn là trợ lý AI tư vấn dự án IT của công ty phần mềm. Nhiệm vụ của bạn:

1. Tư vấn về các dự án phát triển phần mềm (Web, Mobile, Desktop)
2. Giải thích quy trình thực hiện dự án (Planning → Design → Development → Testing → Deployment)
3. Ước tính thời gian và chi phí dự án dựa trên yêu cầu
4. Tư vấn công nghệ phù hợp (Frontend, Backend, Database, DevOps)
5. Trả lời câu hỏi về dịch vụ IT outsourcing

Khi khách hàng hỏi về giá:
- Dự án nhỏ (1-3 tháng): 50-150 triệu VNĐ
- Dự án trung bình (3-6 tháng): 150-500 triệu VNĐ
- Dự án lớn (6-12 tháng): 500 triệu - 2 tỷ VNĐ

Luôn thân thiện, chuyên nghiệp, và khuyến khích khách hàng để lại thông tin liên hệ để được tư vấn chi tiết hơn.`

const toggleChat = () => {
    isOpen.value = !isOpen.value
    if (isOpen.value) {
        hasUnread.value = false
        unreadCount.value = 0
        nextTick(() => {
            scrollToBottom()
        })
    }
}

const sendQuickQuestion = (question) => {
    userInput.value = question
    sendMessage()
}

const sendMessage = async () => {
    const text = userInput.value.trim()
    if (!text) return

    // Add user message
    messages.value.push({
        role: 'user',
        content: text,
        timestamp: new Date()
    })

    userInput.value = ''
    isTyping.value = true
    await nextTick()
    scrollToBottom()

    try {
        let response
        if (AI_PROVIDER === 'gemini') {
            response = await sendToGemini(text)
        } else {
            response = await sendToOpenAI(text)
        }

        // Add bot response
        messages.value.push({
            role: 'assistant',
            content: response,
            timestamp: new Date()
        })

        if (!isOpen.value) {
            hasUnread.value = true
            unreadCount.value++
        }
    } catch (error) {
        console.error('Chat error:', error)
        messages.value.push({
            role: 'assistant',
            content: t('chatWidget.errorMessage'),
            timestamp: new Date()
        })
        ElMessage.error(t('chatWidget.errorMessage'))
    } finally {
        isTyping.value = false
        await nextTick()
        scrollToBottom()
    }
}

const sendToGemini = async (message) => {
    if (GEMINI_API_KEY === 'YOUR_GEMINI_API_KEY_HERE') {
        return `⚠️ **Cấu hình API Key:**\n\nVui lòng thay thế \`GEMINI_API_KEY\` trong file \`ChatWidget.vue\`.\n\n📌 Lấy miễn phí tại: https://makersuite.google.com/app/apikey`
    }

    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=${GEMINI_API_KEY}`

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            contents: [{
                parts: [{
                    text: `${systemPrompt}\n\nKhách hàng: ${message}`
                }]
            }],
            generationConfig: {
                temperature: 0.7,
                topK: 40,
                topP: 0.95,
                maxOutputTokens: 1024
            }
        })
    })

    const data = await response.json()

    if (!response.ok) {
        throw new Error(data.error?.message || 'API Error')
    }

    return data.candidates[0]?.content?.parts[0]?.text || 'Xin lỗi, tôi không thể trả lời câu hỏi này.'
}

const sendToOpenAI = async (message) => {
    if (OPENAI_API_KEY === 'YOUR_OPENAI_API_KEY_HERE') {
        return `⚠️ **Cấu hình API Key:**\n\nVui lòng thay thế \`OPENAI_API_KEY\` trong file \`ChatWidget.vue\`.\n\n📌 Lấy tại: https://platform.openai.com/api-keys`
    }

    const response = await fetch('https://api.openai.com/v1/chat/completions', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${OPENAI_API_KEY}`
        },
        body: JSON.stringify({
            model: 'gpt-3.5-turbo',
            messages: [
                { role: 'system', content: systemPrompt },
                { role: 'user', content: message }
            ],
            temperature: 0.7,
            max_tokens: 1000
        })
    })

    const data = await response.json()

    if (!response.ok) {
        throw new Error(data.error?.message || 'API Error')
    }

    return data.choices[0]?.message?.content || 'Xin lỗi, tôi không thể trả lời câu hỏi này.'
}

const formatMessage = (text) => {
    return text
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>')
}

const formatTime = (timestamp) => {
    const date = new Date(timestamp)
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = () => {
    if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
}

watch(messages, () => {
    nextTick(() => {
        scrollToBottom()
    })
})
</script>

<style scoped>
.chat-widget {
    position: fixed;
    bottom: 24px;
    right: 24px;
    z-index: 9999;
}

.chat-button {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
    color: white;
    cursor: pointer;
    box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    position: relative;
}

.chat-button:hover {
    transform: scale(1.1);
    box-shadow: 0 6px 24px rgba(102, 126, 234, 0.6);
}

.chat-button.has-unread {
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0% {
        box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
    }

    50% {
        box-shadow: 0 6px 28px rgba(102, 126, 234, 0.7);
    }

    100% {
        box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
    }
}

.unread-badge {
    position: absolute;
    top: -4px;
    right: -4px;
    background: #ef4444;
    color: white;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 700;
    border: 2px solid white;
}

.chat-window {
    width: 380px;
    height: 600px;
    background: white;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.chat-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-info {
    display: flex;
    gap: 12px;
    align-items: center;
}

.avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
}

.header-text h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
}

.status {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    opacity: 0.9;
}

.status-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #10b981;
    animation: blink 2s infinite;
}

@keyframes blink {

    0%,
    100% {
        opacity: 1;
    }

    50% {
        opacity: 0.3;
    }
}

.close-button {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: background 0.2s;
}

.close-button:hover {
    background: rgba(255, 255, 255, 0.3);
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    background: #f9fafb;
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.welcome-message {
    text-align: center;
    padding: 40px 20px;
}

.welcome-icon {
    font-size: 48px;
    margin-bottom: 16px;
}

.welcome-message h4 {
    margin: 0 0 8px;
    color: #111827;
    font-size: 18px;
}

.welcome-message p {
    margin: 0 0 20px;
    color: #6b7280;
    font-size: 14px;
}

.quick-questions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 16px;
}

.quick-question-btn {
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 12px;
    cursor: pointer;
    font-size: 13px;
    color: #374151;
    transition: all 0.2s;
    text-align: left;
}

.quick-question-btn:hover {
    background: #f3f4f6;
    border-color: #667eea;
    color: #667eea;
}

.message {
    display: flex;
    gap: 8px;
    align-items: flex-start;
}

.user-message {
    flex-direction: row-reverse;
}

.message-avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
}

.message-content {
    max-width: 70%;
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.message-text {
    padding: 12px 16px;
    border-radius: 12px;
    font-size: 14px;
    line-height: 1.5;
    word-wrap: break-word;
}

.bot-message .message-text {
    background: white;
    color: #374151;
    border-bottom-left-radius: 4px;
}

.user-message .message-text {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-bottom-right-radius: 4px;
}

.message-time {
    font-size: 11px;
    color: #9ca3af;
    padding: 0 4px;
}

.user-message .message-time {
    text-align: right;
}

.typing-indicator .message-content {
    background: white;
    padding: 12px 16px;
    border-radius: 12px;
    border-bottom-left-radius: 4px;
}

.typing-dots {
    display: flex;
    gap: 4px;
}

.typing-dots span {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #9ca3af;
    animation: typing 1.4s infinite;
}

.typing-dots span:nth-child(2) {
    animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes typing {

    0%,
    60%,
    100% {
        transform: translateY(0);
        opacity: 0.5;
    }

    30% {
        transform: translateY(-10px);
        opacity: 1;
    }
}

.chat-input {
    padding: 16px;
    background: white;
    border-top: 1px solid #e5e7eb;
}

.chat-footer {
    padding: 12px 16px;
    background: #f9fafb;
    border-top: 1px solid #e5e7eb;
    text-align: center;
    font-size: 12px;
    color: #9ca3af;
}

.chat-footer strong {
    color: #667eea;
}

/* Transitions */
.bounce-enter-active {
    animation: bounce-in 0.5s;
}

.bounce-leave-active {
    animation: bounce-out 0.3s;
}

@keyframes bounce-in {
    0% {
        transform: scale(0);
    }

    50% {
        transform: scale(1.1);
    }

    100% {
        transform: scale(1);
    }
}

@keyframes bounce-out {
    0% {
        transform: scale(1);
    }

    100% {
        transform: scale(0);
    }
}

.slide-up-enter-active {
    animation: slide-up 0.4s ease-out;
}

.slide-up-leave-active {
    animation: slide-down 0.3s ease-in;
}

@keyframes slide-up {
    0% {
        transform: translateY(100%);
        opacity: 0;
    }

    100% {
        transform: translateY(0);
        opacity: 1;
    }
}

@keyframes slide-down {
    0% {
        transform: translateY(0);
        opacity: 1;
    }

    100% {
        transform: translateY(100%);
        opacity: 0;
    }
}

/* Scrollbar */
.chat-messages::-webkit-scrollbar {
    width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
    background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
    background: #d1d5db;
    border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
    background: #9ca3af;
}

/* Mobile Responsive */
@media (max-width: 480px) {
    .chat-window {
        width: calc(100vw - 32px);
        height: calc(100vh - 100px);
        max-height: 600px;
    }

    .chat-widget {
        bottom: 16px;
        right: 16px;
    }
}
</style>

<template>
    <div class="unauthorized-container">
        <div class="error-card">
            <div class="error-icon">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                    stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
            </div>
            <h1 class="error-title">{{ t('unauthorized.title') }}</h1>
            <p class="error-message">{{ t('unauthorized.message') }}</p>
            <div class="error-details">
                <p>{{ t('unauthorized.reason') }}</p>
                <ul>
                    <li>{{ t('unauthorized.sessionExpired') }}</li>
                    <li>{{ t('unauthorized.tokenExpired') }}</li>
                    <li>{{ t('unauthorized.invalidCredentials') }}</li>
                </ul>
            </div>
            <div class="action-buttons">
                <el-button type="primary" size="large" @click="goToLogin">
                    {{ t('unauthorized.loginAgain') }}
                </el-button>
                <el-button size="large" @click="goToHome">
                    {{ t('unauthorized.goHome') }}
                </el-button>
            </div>
            <div class="help-text">
                <p>{{ t('unauthorized.needHelp') }} <a href="mailto:support@luvina.net">support@luvina.net</a></p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth/useAuthStore'

const router = useRouter()
const { t } = useI18n()
const auth = useAuthStore()

const goToLogin = () => {
    // Clear any existing auth state
    auth.logout()
    // Redirect to login
    router.push('/login')
}

const goToHome = () => {
    // Clear any existing auth state
    auth.logout()
    // Redirect to home
    router.push('/')
}
</script>

<style scoped>
.unauthorized-container {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    padding: 20px;
}

.error-card {
    background: white;
    border-radius: 16px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    padding: 48px 40px;
    max-width: 540px;
    width: 100%;
    text-align: center;
}

.error-icon {
    width: 80px;
    height: 80px;
    margin: 0 auto 24px;
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.error-icon svg {
    width: 48px;
    height: 48px;
    color: white;
}

.error-title {
    font-size: 32px;
    font-weight: 700;
    color: #1a202c;
    margin: 0 0 16px;
}

.error-message {
    font-size: 18px;
    color: #4a5568;
    margin: 0 0 32px;
    line-height: 1.6;
}

.error-details {
    background: #f7fafc;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 32px;
    text-align: left;
}

.error-details p {
    font-weight: 600;
    color: #2d3748;
    margin: 0 0 12px;
    font-size: 14px;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.error-details ul {
    list-style: none;
    padding: 0;
    margin: 0;
}

.error-details li {
    padding: 8px 0;
    color: #4a5568;
    font-size: 15px;
    position: relative;
    padding-left: 24px;
}

.error-details li::before {
    content: "•";
    position: absolute;
    left: 8px;
    color: #f5576c;
    font-weight: bold;
    font-size: 18px;
}

.action-buttons {
    display: flex;
    gap: 12px;
    justify-content: center;
    margin-bottom: 24px;
}

.action-buttons .el-button {
    min-width: 140px;
    border-radius: 8px;
    font-weight: 600;
}

.help-text {
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #e2e8f0;
}

.help-text p {
    color: #718096;
    font-size: 14px;
    margin: 0;
}

.help-text a {
    color: #667eea;
    text-decoration: none;
    font-weight: 600;
}

.help-text a:hover {
    text-decoration: underline;
}

@media (max-width: 640px) {
    .error-card {
        padding: 32px 24px;
    }

    .error-title {
        font-size: 24px;
    }

    .error-message {
        font-size: 16px;
    }

    .action-buttons {
        flex-direction: column;
    }

    .action-buttons .el-button {
        width: 100%;
    }
}
</style>

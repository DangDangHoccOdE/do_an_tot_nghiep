<template>
    <div class="activation-shell">
        <div class="activation-panel">
            <div class="panel-header">
                <p class="eyebrow">{{ $t('auth.activation.title') }}</p>
                <h1>{{ $t('auth.activation.title') }}</h1>
                <p class="subtitle">
                    {{ $t('auth.activation.subtitle') }}
                </p>
            </div>

            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
                <el-form-item :label="$t('auth.activation.email')" prop="email">
                    <el-input v-model="form.email" autocomplete="email" clearable
                        :placeholder="$t('auth.activation.emailPlaceholder')" />
                </el-form-item>

                <el-form-item :label="$t('auth.activation.code')" prop="activationCode">
                    <el-input v-model="form.activationCode" clearable
                        :placeholder="$t('auth.activation.codePlaceholder')" />
                </el-form-item>

                <div class="actions">
                    <el-button type="primary" :loading="loading" @click="handleActivate()">
                        {{ $t('auth.activation.activate') }}
                    </el-button>
                    <el-button type="default" link :loading="resendLoading" @click="handleResend()">
                        {{ $t('auth.activation.resend') }}
                    </el-button>
                </div>

                <p v-if="statusMessage" class="status">{{ statusMessage }}</p>
            </el-form>

            <div class="footer">
                <span>{{ $t('auth.activation.alreadyActivated') }}</span>
                <el-button text @click="router.push('/login')">{{ $t('auth.activation.backToLogin') }}</el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { apiAuth } from '@/services/apiAuth'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const resendLoading = ref(false)
const statusMessage = ref('')

const form = reactive({
    email: (route.query.email || '').toString(),
    activationCode: (route.query.code || '').toString()
})

const rules = {
    email: [
        { required: true, message: 'Email is required', trigger: 'blur' },
        { type: 'email', message: 'Enter a valid email', trigger: ['blur', 'change'] }
    ],
    activationCode: [
        { required: true, message: 'Activation code is required', trigger: 'blur' },
        { min: 4, message: 'Code looks too short', trigger: 'blur' }
    ]
}

const handleActivate = async (isAuto = false) => {
    if (!formRef.value) return
    statusMessage.value = ''

    try {
        await formRef.value.validate()
        loading.value = true
        const response = await apiAuth.activateAccount({
            email: form.email.trim(),
            activationCode: form.activationCode.trim()
        })

        ElMessage.success(response?.message || 'Account activated')
        statusMessage.value = 'Your account is active. You can sign in now.'
        setTimeout(() => router.push('/login'), 800)
    } catch (error) {
        if (error?.response?.data?.message) {
            statusMessage.value = error.response.data.message
        } else if (!isAuto) {
            statusMessage.value = 'Activation failed. Please check the code and try again.'
        }
    } finally {
        loading.value = false
    }
}

const handleResend = async () => {
    statusMessage.value = ''
    if (!form.email) {
        statusMessage.value = 'Enter your email to resend the activation code.'
        return
    }
    try {
        resendLoading.value = true
        const response = await apiAuth.resendActivation({ email: form.email.trim() })
        ElMessage.success(response?.message || 'Sent a new activation email')
        statusMessage.value = 'We sent a fresh activation link to your inbox.'
    } catch (error) {
        if (error?.response?.data?.message) {
            statusMessage.value = error.response.data.message
        } else {
            statusMessage.value = 'Could not resend activation email.'
        }
    } finally {
        resendLoading.value = false
    }
}

onMounted(() => {
    if (form.email && form.activationCode) {
        handleActivate(true)
    }
})
</script>

<style scoped>
.activation-shell {
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background: radial-gradient(circle at 20% 20%, #f1f5f9 0, #e9eef5 40%, #dce4f0 70%, #d2dce9 100%);
    padding: 48px 16px;
    font-family: "Poppins", "Segoe UI", sans-serif;
}

.activation-panel {
    width: min(520px, 100%);
    background: #ffffff;
    border-radius: 18px;
    box-shadow: 0 18px 60px rgba(26, 50, 81, 0.08);
    padding: 36px;
    border: 1px solid rgba(26, 50, 81, 0.05);
}

.panel-header {
    margin-bottom: 18px;
}

.eyebrow {
    text-transform: uppercase;
    letter-spacing: 0.08em;
    color: #5c6f88;
    font-size: 11px;
    font-weight: 700;
    margin-bottom: 6px;
}

.panel-header h1 {
    font-size: 26px;
    color: #1f2a3d;
    margin: 0 0 8px;
    font-weight: 700;
}

.subtitle {
    color: #4a5568;
    font-size: 14px;
    line-height: 1.5;
}

.actions {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 8px 0 4px;
}

.status {
    margin-top: 8px;
    color: #1f2a3d;
    font-size: 13px;
    background: #f5f7fb;
    border: 1px solid #e4e9f2;
    border-radius: 10px;
    padding: 10px 12px;
}

.footer {
    margin-top: 22px;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #4a5568;
    font-size: 13px;
}

:deep(.el-form-item__label) {
    color: #2f3b52;
    font-weight: 600;
    margin-bottom: 6px;
}

:deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: none;
    border: 1px solid #e2e8f0;
    padding: 4px 10px;
}

:deep(.el-input__wrapper.is-focus) {
    border-color: #2f7bfd;
    box-shadow: 0 0 0 3px rgba(47, 123, 253, 0.12);
}

:deep(.el-form-item) {
    margin-bottom: 16px;
}
</style>

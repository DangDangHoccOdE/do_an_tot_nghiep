<template>
    <div class="page">
        <SectionCard>
            <div class="page-header">
                <div class="title-group">
                    <h2 class="page-title">{{ t('admin.entities.user') }}</h2>
                </div>
                <div class="header-actions">
                    <el-button @click="confirmCancel">{{ t('admin.actions.cancel') }}</el-button>
                    <el-button type="primary" :loading="isLoading" @click="handleSubmit">
                        {{ isEdit ? t('admin.actions.update') : t('admin.actions.create') }}
                    </el-button>
                </div>
            </div>

            <div class="form-wrapper">
                <el-form ref="formRef" :model="form" :rules="rules" label-width="auto" @submit.prevent="handleSubmit">
                    <!-- Avatar Upload Section -->
                    <div class="form-section-full">
                        <div class="form-section-title">{{ t('admin.form.avatar') }}</div>
                        <div class="avatar-upload-container">
                            <div v-if="form.avatar" class="avatar-preview">
                                <img :src="form.avatar" alt="Avatar" class="preview-img" />
                                <el-button text type="danger" size="small" @click="removeAvatar">
                                    {{ t('admin.actions.delete') }}
                                </el-button>
                            </div>
                            <el-upload v-else action="#" :auto-upload="false" :on-change="handleAvatarSelect"
                                :file-list="[]" accept="image/*" drag class="avatar-upload">
                                <el-icon class="el-icon--upload">
                                    <UploadFilled />
                                </el-icon>
                                <div class="el-upload__text">
                                    {{ t('admin.form.dragOrClick') }}<em>{{ t('admin.form.selectFile') }}</em>
                                </div>
                            </el-upload>
                            <div v-if="uploadProgress > 0 && uploadProgress < 100" class="progress-bar">
                                <el-progress :percentage="uploadProgress" />
                            </div>
                        </div>
                    </div>

                    <!-- Account Information Section -->
                    <div class="form-section-full">
                        <div class="form-section-title">{{ $t('admin.form.accountInfo') || 'Thông tin tài khoản' }}
                        </div>
                        <div class="form-grid">
                            <el-form-item prop="email">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.email')" :required="true" />
                                </template>
                                <el-input v-model="form.email" :placeholder="t('admin.placeholders.email')"
                                    :disabled="isEdit" />
                            </el-form-item>

                            <el-form-item prop="password" v-if="!isEdit">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.password')" :required="true" />
                                </template>
                                <el-input v-model="form.password" type="password"
                                    :placeholder="t('admin.placeholders.password')" show-password />
                            </el-form-item>
                        </div>
                    </div>

                    <!-- Personal Information Section -->
                    <div class="form-section-full">
                        <div class="form-section-title">{{ $t('admin.form.personalInfo') || 'Thông tin cá nhân' }}</div>
                        <div class="form-grid">
                            <el-form-item prop="firstName">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.firstName')" :required="true" />
                                </template>
                                <el-input v-model="form.firstName" :placeholder="t('admin.placeholders.firstName')" />
                            </el-form-item>

                            <el-form-item prop="lastName">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.lastName')" :required="true" />
                                </template>
                                <el-input v-model="form.lastName" :placeholder="t('admin.placeholders.lastName')" />
                            </el-form-item>

                            <el-form-item prop="phone">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.phone')" :required="true" />
                                </template>
                                <el-input v-model="form.phone" :placeholder="t('admin.placeholders.phone')" />
                            </el-form-item>

                            <el-form-item prop="roleId">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.memberRole')" :required="true" />
                                </template>
                                <el-select v-model="form.roleId" :placeholder="t('admin.form.selectRole')">
                                    <el-option v-for="role in roles" :key="role.id" :label="getRoleLabel(role.name)"
                                        :value="role.id" />
                                </el-select>
                            </el-form-item>
                        </div>
                    </div>

                </el-form>
            </div>
        </SectionCard>
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import SectionCard from '@/components/admin/SectionCard.vue'
import RequiredLabel from '@/components/common/RequiredLabel.vue'
import { apiUsers } from '@/services/apiUsers'
import { apiRoles } from '@/services/apiRoles'
import { createUserRules } from '@/validations/userRules'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const props = defineProps({
    mode: { type: String, default: 'create' },
    id: { type: String, default: null }
})

const formRef = ref(null)
const roles = ref([])
const isLoading = ref(false)
const uploadProgress = ref(0)

const form = reactive({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    roleId: null,
    avatar: null,
    avatarFile: null
})

const isEdit = computed(() => props.mode === 'edit')

const rules = createUserRules(t, form)

onMounted(async () => {
    await fetchRoles()
    if (isEdit.value && props.id) {
        await fetchUser()
    }
})

const fetchRoles = async () => {
    try {
        const data = await apiRoles.list()
        roles.value = data || []
    } catch (error) {
        console.error('Failed to fetch roles:', error)
        ElMessage.error(t('admin.messages.loadFail'))
    }
}

const fetchUser = async () => {
    try {
        const data = await apiUsers.detail(props.id)
        form.email = data.email
        form.firstName = data.firstName
        form.lastName = data.lastName
        form.phone = data.phone
        form.roleId = data.roleId
        form.avatar = data.avatar
    } catch (error) {
        console.error('Failed to fetch user:', error)
        ElMessage.error(t('admin.messages.loadFail'))
    }
}

const handleAvatarSelect = (file) => {
    // Validate file type
    if (!file.raw.type.startsWith('image/')) {
        ElMessage.error(t('admin.messages.imageTypeError'))
        return
    }

    // Validate file size (5MB)
    const maxSize = 5 * 1024 * 1024
    if (file.raw.size > maxSize) {
        ElMessage.error(t('admin.messages.imageSizeError'))
        return
    }

    // Lưu file và tạo preview URL
    form.avatarFile = file.raw
    form.avatar = URL.createObjectURL(file.raw)
    ElMessage.success(t('admin.messages.imageSelected'))
}

const removeAvatar = () => {
    // Revoke object URL để tránh memory leak
    if (form.avatar && form.avatar.startsWith('blob:')) {
        URL.revokeObjectURL(form.avatar)
    }
    form.avatar = null
    form.avatarFile = null
}

const getRoleLabel = (roleName) => {
    const labelMap = {
        'ROLE_ADMIN': t('admin.form.roleAdmin'),
        'ROLE_PM': t('admin.form.rolePM'),
        'ROLE_STAFF': t('admin.form.roleStaff'),
        'ROLE_USER': t('admin.form.roleUser')
    }
    return labelMap[roleName] || roleName
}

const handleSubmit = async () => {
    if (!formRef.value) return

    try {
        await formRef.value.validate()
        isLoading.value = true

        const formData = new FormData()
        formData.append('email', form.email)
        formData.append('firstName', form.firstName || '')
        formData.append('lastName', form.lastName || '')
        formData.append('phone', form.phone || '')
        formData.append('roleId', form.roleId)

        if (!isEdit.value && form.password) {
            formData.append('password', form.password)
        }

        if (form.avatarFile) {
            formData.append('avatar', form.avatarFile)
        }

        if (!isEdit.value) {
            await apiUsers.create(formData)
            ElMessage.success(t('admin.messages.userCreated'))
        } else {
            await apiUsers.update(props.id, formData)
            ElMessage.success(t('admin.messages.userUpdated'))
        }

        router.push({ name: 'admin-users' })
    } catch (error) {
        console.error('Form validation or submission error:', error)
        if (error.response?.data?.message) {
            ElMessage.error(error.response.data.message)
        } else {
            ElMessage.error(isEdit.value ? t('admin.messages.updateFailed') : t('admin.messages.createFailed'))
        }
    } finally {
        isLoading.value = false
    }
}

const goBack = () => {
    router.push({ name: 'admin-users' })
}

const confirmCancel = async () => {
    try {
        await ElMessageBox.confirm(
            t('admin.messages.confirmCancel') || 'Bạn có chắc muốn hủy? Mọi thay đổi sẽ không được lưu.',
            t('admin.actions.cancel'),
            {
                confirmButtonText: t('admin.actions.cancel'),
                cancelButtonText: t('admin.actions.close'),
                type: 'warning'
            }
        )
        goBack()
    } catch (error) {
        // user closed dialog; no action
    }
}
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

.eyebrow {
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.6px;
    color: #6b7280;
    margin: 0;
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

.form-wrapper {
    background: linear-gradient(180deg, #f8fafc 0%, #fdfefe 100%);
    border: 1px solid #e2e8f0;
    border-radius: 14px;
    padding: 16px;
    width: 100%;
    box-shadow: 0 12px 30px rgba(15, 23, 42, 0.05);
}

:deep(.el-form) {
    width: 100%;
}

.form-section-full {
    padding: 20px;
    margin-bottom: 16px;
    border: 1px solid #dce3ed;
    border-radius: 14px;
    background: #fff;
    box-shadow: inset 0 1px 0 #f1f5f9, 0 10px 24px rgba(15, 23, 42, 0.05);
}

.form-section-full:last-of-type {
    margin-bottom: 0;
}

.form-section-title {
    font-size: 14px;
    font-weight: 800;
    margin-bottom: 14px;
    color: #0b132b;
    letter-spacing: 0.3px;
}

.form-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 16px;
    max-width: 100%;
}

:deep(.el-form-item) {
    margin-bottom: 0;
    display: flex;
    flex-direction: column;
    width: 100%;
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 12px;
    padding: 12px 14px 10px;
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.04);
}

:deep(.el-form-item__label) {
    font-weight: 700;
    color: #0b132b;
    font-size: 13px;
    margin-bottom: 6px;
    line-height: 1.2;
}

:deep(.el-form-item__content) {
    width: 100%;
}

:deep(.el-input),
:deep(.el-select) {
    width: 100%;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper),
:deep(.el-input-number .el-input__wrapper) {
    padding: 6px 12px;
    border-radius: 8px;
    min-height: 40px;
}

:deep(.el-input input) {
    font-size: 13px;
}

:deep(.el-select__wrapper) {
    border-radius: 6px;
}

:deep(.el-button--primary) {
    background-color: #0066cc;
    border-color: #0066cc;
}

:deep(.el-button--primary:hover) {
    background-color: #0052a3;
    border-color: #0052a3;
}

.avatar-upload-container {
    width: 100%;
}

.avatar-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 20px;
    background: #f9f9fb;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
}

.preview-img {
    width: 120px;
    height: 120px;
    border-radius: 8px;
    object-fit: cover;
    border: 2px solid #ddd;
    background: #fff;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.avatar-upload {
    width: 100%;
}

:deep(.avatar-upload .el-upload-dragger) {
    border: 2px dashed #b3d8ff;
    border-radius: 8px;
    padding: 40px 20px;
    min-height: 160px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #f0f7ff;
    transition: all 0.3s ease;
}

:deep(.avatar-upload .el-upload-dragger:hover) {
    border-color: #0066cc;
    background-color: #f5f9ff;
}

:deep(.avatar-upload .el-icon--upload) {
    font-size: 48px;
    color: #0066cc;
    margin-bottom: 12px;
}

.el-upload__text {
    color: #606266;
    font-size: 14px;
    text-align: center;
}

.el-upload__text em {
    color: #0066cc;
    font-style: normal;
    display: block;
    margin-top: 8px;
    font-weight: 600;
}

.progress-bar {
    margin-top: 16px;
}

@media (max-width: 768px) {
    .form-grid {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .form-section-full {
        padding: 16px;
    }

    .preview-img {
        width: 100px;
        height: 100px;
    }

}

@media (max-width: 480px) {
    .form-section-full {
        padding: 12px;
    }

    .form-section-title {
        font-size: 13px;
        margin-bottom: 12px;
    }

    .preview-img {
        width: 80px;
        height: 80px;
    }

    :deep(.avatar-upload .el-upload-dragger) {
        padding: 30px 15px;
        min-height: 140px;
    }

    :deep(.el-form-item__label) {
        font-size: 12px;
    }
}

:deep(.el-form-item__label-wrap) {
    margin-left: 0 !important;
}
</style>

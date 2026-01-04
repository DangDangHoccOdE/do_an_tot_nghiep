<template>
    <div class="grid">
        <SectionCard :eyebrow="isEdit ? t('admin.actions.edit') : t('admin.actions.create')"
            :title="t('admin.entities.user')">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="140px" @submit.prevent="handleSubmit">
                <!-- Avatar Upload -->
                <el-form-item :label="t('admin.form.avatar')" prop="avatar">
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
                </el-form-item>

                <el-form-item prop="email">
                    <template #label>
                        <RequiredLabel :label="t('admin.form.email')" :required="true" />
                    </template>
                    <el-input v-model="form.email" :placeholder="t('admin.placeholders.email')" :disabled="isEdit" />
                </el-form-item>

                <el-form-item prop="password" v-if="!isEdit">
                    <template #label>
                        <RequiredLabel :label="t('admin.form.password')" :required="true" />
                    </template>
                    <el-input v-model="form.password" type="password" :placeholder="t('admin.placeholders.password')"
                        show-password />
                </el-form-item>

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

                <el-form-item>
                    <el-button type="primary" @click="handleSubmit" :loading="isLoading">
                        {{ isEdit ? t('admin.actions.update') : t('admin.actions.create') }}
                    </el-button>
                    <el-button @click="goBack">{{ t('admin.actions.back') }}</el-button>
                </el-form-item>
            </el-form>
        </SectionCard>
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
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
</script>

<style scoped>
.grid {
    display: grid;
    gap: 16px;
}

:deep(.el-form-item) {
    margin-bottom: 22px;
}

:deep(.el-input),
:deep(.el-select) {
    width: 100%;
}

.avatar-upload-container {
    width: 100%;
}

.avatar-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}

.preview-img {
    width: 150px;
    height: 150px;
    border-radius: 8px;
    object-fit: cover;
    border: 2px solid #ddd;
}

.remove-btn {
    font-size: 12px;
}

.avatar-upload {
    width: 100%;
}

:deep(.avatar-upload .el-upload-dragger) {
    border: 2px dashed #ddd;
    border-radius: 6px;
    padding: 40px 0;
}

:deep(.avatar-upload .el-upload-dragger:hover) {
    border-color: #409eff;
    background-color: #f5f7fa;
}

:deep(.avatar-upload .el-icon--upload) {
    font-size: 67px;
    color: #409eff;
    margin-bottom: 16px;
}

.el-upload__text {
    color: #606266;
    font-size: 14px;
}

.el-upload__text em {
    color: #409eff;
    font-style: normal;
    display: block;
    margin-top: 8px;
}

.progress-bar {
    margin-top: 16px;
}
</style>

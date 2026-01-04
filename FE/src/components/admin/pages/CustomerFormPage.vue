<template>
    <div class="customer-form-container">
        <SectionCard eyebrow="Quản lý khách hàng"
            :title="isEdit ? 'Chỉnh sửa thông tin khách hàng' : 'Thêm khách hàng mới'">
            <el-form ref="formRef" :model="form" :rules="customerRules" label-width="140px" class="customer-form">

                <!-- Avatar Section -->
                <div class="avatar-section">
                    <div class="avatar-preview" v-if="previewUrl || form.avatar">
                        <img :src="previewUrl || form.avatar" alt="Avatar" class="avatar-image" />
                        <el-button v-if="avatarFile" text type="danger" @click="removeAvatar" class="remove-btn">
                            <el-icon>
                                <Delete />
                            </el-icon>
                        </el-button>
                    </div>
                    <div class="avatar-upload-area" v-else>
                        <el-icon class="upload-icon">
                            <Picture />
                        </el-icon>
                        <p class="upload-text">Không có ảnh</p>
                    </div>
                </div>

                <!-- Avatar Upload -->
                <el-form-item label="Ảnh đại diện" class="avatar-form-item">
                    <el-upload ref="uploadRef" action="#" :auto-upload="false" :on-change="handleAvatarChange"
                        accept="image/*" :limit="1" class="avatar-uploader">
                        <template #trigger>
                            <el-button type="primary" :icon="Upload">
                                {{ avatarFile ? 'Chọn ảnh khác' : 'Chọn ảnh' }}
                            </el-button>
                        </template>
                        <template #tip>
                            <div class="el-upload__tip">
                                Chỉ chấp nhận ảnh (JPG, PNG). Dung lượng tối đa 5MB
                            </div>
                        </template>
                    </el-upload>
                </el-form-item>

                <!-- Form Fields -->
                <el-divider />

                <el-form-item label="Email" prop="email">
                    <el-input v-model="form.email" :disabled="isEdit" placeholder="example@email.com" clearable />
                </el-form-item>

                <el-form-item label="Mật khẩu" :prop="isEdit ? '' : 'password'">
                    <el-input v-model="form.password" type="password"
                        :placeholder="isEdit ? 'Để trống nếu không đổi mật khẩu' : 'Nhập mật khẩu'" show-password />
                </el-form-item>


                <el-row :gutter="20">
                    <el-col :xs="24" :sm="12">
                        <el-form-item label="Tên" prop="firstName">
                            <el-input v-model="form.firstName" placeholder="Tên của khách hàng" clearable />
                        </el-form-item>
                    </el-col>
                    <el-col :xs="24" :sm="12">
                        <el-form-item label="Họ" prop="lastName">
                            <el-input v-model="form.lastName" placeholder="Họ của khách hàng" clearable />
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-form-item label="Số điện thoại" prop="phone">
                    <el-input v-model="form.phone" placeholder="0912345678" clearable />
                </el-form-item>

                <!-- Action Buttons -->
                <el-divider />

                <el-form-item>
                    <div class="button-group">
                        <el-button type="primary" @click="submit" :loading="isLoading" size="large">
                            <el-icon>
                                <Check />
                            </el-icon>
                            {{ isEdit ? 'Cập nhật' : 'Thêm khách hàng' }}
                        </el-button>
                        <el-button @click="$router.back()" size="large">
                            <el-icon>
                                <Close />
                            </el-icon>
                            Hủy
                        </el-button>
                    </div>
                </el-form-item>
            </el-form>
        </SectionCard>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Upload, Delete, Picture, Check, Close } from '@element-plus/icons-vue'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiUsers } from '@/services/apiUsers'
import { customerRules } from '@/validations/customerRules'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const uploadRef = ref()

const isEdit = ref(false)
const isLoading = ref(false)
const previewUrl = ref('')
const avatarFile = ref(null)

const form = reactive({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    avatar: ''
})

onMounted(async () => {
    if (route.params.id) {
        isEdit.value = true
        try {
            const customer = await apiUsers.detail(route.params.id)
            form.email = customer.data?.email || ''
            form.firstName = customer.data?.firstName || ''
            form.lastName = customer.data?.lastName || ''
            form.phone = customer.data?.phone || ''
            form.avatar = customer.data?.avatar || ''
        } catch (error) {
            console.error('Failed to load customer:', error)
            ElMessage.error('Lỗi khi tải dữ liệu khách hàng')
        }
    }
})

const handleAvatarChange = (file) => {
    // Chỉ chọn được 1 ảnh
    if (file.raw.size > 5 * 1024 * 1024) {
        ElMessage.error('Dung lượng ảnh không được vượt quá 5MB')
        return
    }

    avatarFile.value = file.raw
    const reader = new FileReader()
    reader.onload = (e) => {
        previewUrl.value = e.target.result
    }
    reader.readAsDataURL(file.raw)
}

const removeAvatar = () => {
    avatarFile.value = null
    previewUrl.value = ''
    if (uploadRef.value) {
        uploadRef.value.clearFiles()
    }
}

const submit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (!valid) return

        isLoading.value = true
        try {
            const formData = new FormData()
            formData.append('email', form.email)
            if (form.password) {
                formData.append('password', form.password)
            }
            formData.append('firstName', form.firstName)
            formData.append('lastName', form.lastName)
            formData.append('phone', form.phone)

            if (avatarFile.value) {
                formData.append('avatar', avatarFile.value)
            }

            if (isEdit.value) {
                await apiUsers.updateCustomer(route.params.id, formData)
                ElMessage.success('Cập nhật khách hàng thành công')
            } else {
                await apiUsers.createCustomer(formData)
                ElMessage.success('Thêm khách hàng thành công')
            }

            router.push({ name: 'admin-customers' })
        } catch (error) {
            console.error('Failed to submit:', error)
            ElMessage.error(error.response?.data?.message || 'Lỗi khi lưu dữ liệu')
        } finally {
            isLoading.value = false
        }
    })
}
</script>

<style scoped>
.customer-form-container {
    max-width: 900px;
    width: 100%;
    margin: 0 auto;
    padding: 20px;
}

.customer-form {
    padding: 0;
}

/* Avatar Section */
.avatar-section {
    text-align: center;
    margin-bottom: 35px;
    padding: 40px 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
    position: relative;
    overflow: hidden;
}

.avatar-section::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 350px;
    height: 350px;
    background: rgba(255, 255, 255, 0.12);
    border-radius: 50%;
    pointer-events: none;
}

.avatar-section::after {
    content: '';
    position: absolute;
    bottom: -100%;
    left: -50%;
    width: 400px;
    height: 400px;
    background: rgba(255, 255, 255, 0.08);
    border-radius: 50%;
    pointer-events: none;
}

.avatar-preview {
    position: relative;
    display: inline-block;
    margin-bottom: 20px;
    z-index: 1;
}

.avatar-image {
    width: 140px;
    height: 140px;
    border-radius: 50%;
    object-fit: cover;
    border: 6px solid white;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.avatar-image:hover {
    transform: scale(1.08) translateY(-2px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
}

.avatar-upload-area {
    padding: 40px 30px;
    background: white;
    border-radius: 12px;
    border: 2.5px dashed #667eea;
    display: inline-block;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    z-index: 1;
    position: relative;
}

.avatar-upload-area:hover {
    border-color: #764ba2;
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.2);
    background: linear-gradient(135deg, #fafbff 0%, #f5f0ff 100%);
}

.upload-icon {
    font-size: 56px;
    color: #667eea;
    margin-bottom: 14px;
    transition: all 0.3s ease;
}

.avatar-upload-area:hover .upload-icon {
    color: #764ba2;
    transform: scale(1.1);
}

.upload-text {
    margin: 0;
    color: #606266;
    font-size: 15px;
    font-weight: 500;
}

.remove-btn {
    position: absolute;
    top: -12px;
    right: -12px;
    background: #f56c6c !important;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    border: 2px solid white;
}

.remove-btn:hover {
    transform: scale(1.15) rotate(8deg);
    box-shadow: 0 6px 16px rgba(245, 108, 108, 0.5);
}

/* Avatar Form Item */
.avatar-form-item {
    text-align: center;
    margin-bottom: 30px;
}

.avatar-uploader {
    display: flex;
    justify-content: center;
    margin-top: 25px;
    flex-wrap: wrap;
    gap: 10px;
}

:deep(.avatar-uploader .el-button) {
    background: white;
    color: #667eea;
    border: 2px solid #667eea;
    font-weight: 600;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    padding: 8px 24px;
}

:deep(.avatar-uploader .el-button:hover) {
    background: #667eea;
    color: white;
    box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35);
    transform: translateY(-2px);
}

:deep(.el-upload__tip) {
    margin-top: 14px;
    color: #909399;
    font-size: 13px;
}

/* Form sections */
:deep(.el-form-item__label) {
    color: #303133;
    font-weight: 600;
    font-size: 14px;
}

/* Button Group */
.button-group {
    display: flex;
    gap: 12px;
    justify-content: flex-start;
    margin-top: 15px;
    flex-wrap: wrap;
}

:deep(.button-group .el-button) {
    min-width: 160px;
    font-weight: 600;
    border-radius: 6px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.button-group .el-button:first-child) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-color: transparent;
}

:deep(.button-group .el-button:first-child:hover) {
    box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
    transform: translateY(-2px);
}

:deep(.button-group .el-button.is-loading) {
    opacity: 0.8;
}

/* Form Spacing */
:deep(.el-form-item) {
    margin-bottom: 24px;
}

:deep(.el-divider) {
    margin: 32px 0;
    background-color: #e4e7ed;
}

/* Input Styling */
:deep(.el-input__wrapper) {
    border-radius: 8px;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    background-color: #fafafa;
    border: 1.5px solid #e8e8e8;
}

:deep(.el-input__wrapper:hover) {
    background-color: white;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.08);
}

:deep(.el-input__wrapper.is-focus) {
    background-color: white;
    border-color: #667eea;
    box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.15);
}

:deep(.el-input-password__wrapper) {
    border-radius: 8px;
}

/* Desktop Layout */
@media (min-width: 1200px) {
    .customer-form-container {
        padding: 30px 40px;
    }

    .avatar-section {
        padding: 45px 35px;
        margin-bottom: 40px;
    }
}

/* Tablet Layout */
@media (max-width: 1199px) and (min-width: 769px) {
    .customer-form-container {
        max-width: 850px;
        padding: 25px 30px;
    }

    .avatar-section {
        padding: 35px 28px;
    }

    .avatar-image {
        width: 130px;
        height: 130px;
    }
}

/* Mobile Layout */
@media (max-width: 768px) {
    .customer-form-container {
        max-width: 100%;
        padding: 16px 16px;
    }

    .customer-form {
        padding: 0;
    }

    .avatar-section {
        padding: 28px 20px;
        margin-bottom: 28px;
        border-radius: 12px;
    }

    .avatar-image {
        width: 110px;
        height: 110px;
        border-width: 5px;
    }

    .avatar-upload-area {
        padding: 28px 20px;
        border-radius: 10px;
    }

    .upload-icon {
        font-size: 48px;
        margin-bottom: 10px;
    }

    .upload-text {
        font-size: 14px;
    }

    .button-group {
        flex-direction: column;
        gap: 10px;
        margin-top: 12px;
    }

    :deep(.button-group .el-button) {
        width: 100%;
        min-width: unset;
    }

    :deep(.el-form-item__label) {
        font-size: 13px;
    }

    :deep(.el-form-item) {
        margin-bottom: 20px;
    }

    :deep(.el-divider) {
        margin: 24px 0;
    }

    .avatar-form-item {
        margin-bottom: 22px;
    }

    .avatar-uploader {
        margin-top: 18px;
    }

    :deep(.el-input__wrapper) {
        border-radius: 6px;
    }

    :deep(.el-input-password__wrapper) {
        border-radius: 6px;
    }
}

/* Small Mobile */
@media (max-width: 480px) {
    .customer-form-container {
        padding: 12px 12px;
    }

    .customer-form {
        padding: 0;
    }

    .avatar-section {
        padding: 22px 16px;
        margin-bottom: 20px;
    }

    .avatar-preview {
        margin-bottom: 12px;
    }

    .avatar-image {
        width: 95px;
        height: 95px;
        border-width: 4px;
    }

    .avatar-upload-area {
        padding: 22px 16px;
    }

    .upload-icon {
        font-size: 40px;
        margin-bottom: 8px;
    }

    :deep(.el-form-item) {
        margin-bottom: 16px;
    }

    :deep(.el-divider) {
        margin: 20px 0;
    }

    :deep(.el-form-item__label) {
        font-size: 12px;
    }

    :deep(.button-group .el-button) {
        font-size: 14px;
    }

    .remove-btn {
        width: 36px;
        height: 36px;
        top: -8px;
        right: -8px;
    }
}
</style>

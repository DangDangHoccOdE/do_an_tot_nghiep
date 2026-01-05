<template>
    <div class="page">
        <SectionCard>
            <div class="page-header">
                <div class="title-group">
                    <h2 class="page-title">{{ t('admin.entities.staff') }}</h2>
                </div>
                <div class="header-actions">
                    <el-button @click="confirmCancel">{{ t('admin.actions.cancel') }}</el-button>
                    <el-button type="primary" @click="submit">
                        {{ isEdit ? t('admin.actions.update') : t('admin.actions.create') }}
                    </el-button>
                </div>
            </div>
            <div class="form-wrapper">
                <el-form ref="formRef" :model="form" :rules="staffRules" label-width="auto">
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
                            <el-upload v-else action="#" :auto-upload="false" :on-change="handleAvatarChange"
                                :file-list="[]" accept="image/*" drag class="avatar-upload">
                                <el-icon class="el-icon--upload">
                                    <UploadFilled />
                                </el-icon>
                                <div class="el-upload__text">
                                    {{ t('admin.form.dragOrClick') }}<em>{{ t('admin.form.selectFile') }}</em>
                                </div>
                            </el-upload>
                        </div>
                    </div>

                    <!-- Account Information -->
                    <div class="form-section-full">
                        <div class="form-section-title">{{ t('admin.form.accountInfo') }}</div>
                        <div class="form-grid">
                            <el-form-item prop="email">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.email')" :required="true" />
                                </template>
                                <el-input v-model="form.email" :disabled="isEdit" clearable
                                    :placeholder="t('admin.placeholders.email')" />
                            </el-form-item>

                            <el-form-item prop="password" v-if="!isEdit">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.password')" :required="true" />
                                </template>
                                <el-input v-model="form.password" type="password" clearable
                                    :placeholder="t('admin.placeholders.password')" show-password />
                            </el-form-item>
                        </div>
                    </div>

                    <!-- Personal Information -->
                    <div class="form-section-full">
                        <div class="form-section-title">
                            {{ t('admin.form.personalInfo') }}
                        </div>
                        <div class="form-grid">
                            <el-form-item prop="firstName">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.firstName')" :required="true" />
                                </template>
                                <el-input v-model="form.firstName" clearable />
                            </el-form-item>

                            <el-form-item prop="lastName">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.lastName')" :required="true" />
                                </template>
                                <el-input v-model="form.lastName" clearable />
                            </el-form-item>

                            <el-form-item prop="phone">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.phone')" :required="true" />
                                </template>
                                <el-input v-model="form.phone" clearable />
                            </el-form-item>

                            <el-form-item prop="cv">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.cv')" :required="false" />
                                </template>
                                <el-input v-model="form.cv" clearable />
                            </el-form-item>
                        </div>
                    </div>

                    <!-- IT Role Section -->
                    <div class="form-section-full">
                        <div class="form-section-title">{{ `${t('admin.form.itRole')} & ${t('admin.form.skills')}` }}
                        </div>
                        <div class="form-grid">
                            <el-form-item prop="itRole" style="max-width: 100%;">
                                <template #label>
                                    <RequiredLabel :label="t('admin.form.itRole')" :required="true" />
                                </template>
                                <el-select v-model="form.itRole" :placeholder="t('admin.form.selectItRole')"
                                    style="width: 100%;" clearable>
                                    <el-option label="Frontend Developer" value="FRONTEND_DEVELOPER" />
                                    <el-option label="Backend Developer" value="BACKEND_DEVELOPER" />
                                    <el-option label="Fullstack Developer" value="FULLSTACK_DEVELOPER" />
                                    <el-option label="Mobile Developer" value="MOBILE_DEVELOPER" />
                                    <el-option label="DevOps Engineer" value="DEVOPS_ENGINEER" />
                                    <el-option label="QA Manual Tester" value="QA_MANUAL" />
                                    <el-option label="QA Automation" value="QA_AUTOMATION" />
                                    <el-option label="Business Analyst" value="BUSINESS_ANALYST" />
                                    <el-option label="Product Owner" value="PRODUCT_OWNER" />
                                    <el-option label="Project Manager" value="PROJECT_MANAGER" />
                                    <el-option label="Scrum Master" value="SCRUM_MASTER" />
                                    <el-option label="Tech Lead" value="TECH_LEAD" />
                                    <el-option label="Solution Architect" value="SOLUTION_ARCHITECT" />
                                    <el-option label="UI/UX Designer" value="UI_UX_DESIGNER" />
                                    <el-option label="Data Engineer" value="DATA_ENGINEER" />
                                    <el-option label="Data Analyst" value="DATA_ANALYST" />
                                    <el-option label="Security Engineer" value="SECURITY_ENGINEER" />
                                    <el-option label="Database Administrator" value="DATABASE_ADMINISTRATOR" />
                                    <el-option label="System Administrator" value="SYSTEM_ADMINISTRATOR" />
                                </el-select>
                            </el-form-item>
                        </div>

                        <!-- Skills Section -->
                        <el-form-item :label="t('admin.form.skills')" style="max-width: 100%; margin-top: 16px;">
                            <div class="skills-section">
                                <!-- Add new skill -->
                                <div class="skill-input-group">
                                    <el-select v-model="newSkill.skillId" :placeholder="t('admin.form.selectSkill')"
                                        style="width: 180px; flex: 1; margin-right: 8px;" clearable>
                                        <el-option v-for="skill in availableSkills" :key="skill.id" :label="skill.name"
                                            :value="skill.id" />
                                    </el-select>
                                    <el-select v-model="newSkill.level"
                                        :placeholder="t('admin.placeholders.skillLevel')"
                                        style="width: 180px; margin-right: 8px;" clearable>
                                        <el-option label="Intern" value="INTERN" />
                                        <el-option label="Fresher" value="FRESHER" />
                                        <el-option label="Junior" value="JUNIOR" />
                                        <el-option label="Middle" value="MIDDLE" />
                                        <el-option label="Senior" value="SENIOR" />
                                        <el-option label="Lead" value="LEAD" />
                                        <el-option label="Principal" value="PRINCIPAL" />
                                        <el-option label="Architect" value="ARCHITECT" />
                                        <el-option label="Expert" value="EXPERT" />
                                    </el-select>
                                    <el-input-number v-model="newSkill.yearsOfExperience" :min="0" :max="50"
                                        :placeholder="t('admin.placeholders.yearsOfExperience')"
                                        style="width: 160px; margin-right: 8px;" />
                                    <el-button type="primary" @click="addSkill" :disabled="!newSkill.skillId">
                                        {{ t('admin.actions.add') }}
                                    </el-button>
                                </div>

                                <!-- Skills list -->
                                <div class="skills-list" v-if="form.skills && form.skills.length > 0">
                                    <div class="skill-item" v-for="(skill, index) in form.skills" :key="index">
                                        <span class="skill-text">
                                            {{ getSkillName(skill.skillId) }} - {{ formatSkillLevel(skill.level) }} ({{
                                                skill.yearsOfExperience }} {{ t('admin.form.yearsLabel') }})
                                        </span>
                                        <el-button type="danger" size="small" @click="removeSkill(index)">
                                            {{ t('admin.actions.delete') }}
                                        </el-button>
                                    </div>
                                </div>
                                <div class="help-text" v-if="!form.skills || form.skills.length === 0">
                                    {{ t('admin.form.addSkill') }}
                                </div>
                            </div>
                        </el-form-item>
                    </div>
                </el-form>
            </div>
        </SectionCard>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import SectionCard from '@/components/admin/SectionCard.vue'
import RequiredLabel from '@/components/common/RequiredLabel.vue'
import { apiUsers } from '@/services/apiUsers'
import { apiSkills } from '@/services/apiSkills'
import { staffRules } from '@/validations/staffRules'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const formRef = ref()

const isEdit = ref(false)
const avatarFile = ref(null)
const availableSkills = ref([])

const newSkill = reactive({
    skillId: '',
    level: '',
    yearsOfExperience: 0
})

const form = reactive({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    itRole: '',
    skills: [],
    cv: '',
    avatar: ''
})

onMounted(async () => {
    // Load available skills
    try {
        const skillsData = await apiSkills.list()
        availableSkills.value = skillsData || []
    } catch (error) {
        console.error('Failed to load skills:', error)
        ElMessage.error(t('admin.messages.loadSkillsError'))
    }

    if (route.params.id) {
        isEdit.value = true
        try {
            const staffMember = await apiUsers.detail(route.params.id)
            form.email = staffMember.email || ''
            form.firstName = staffMember.firstName || ''
            form.lastName = staffMember.lastName || ''
            form.phone = staffMember.phone || ''
            form.itRole = staffMember.itRole || ''
            form.skills = staffMember.skills || []
            form.cv = staffMember.cv || ''
            form.avatar = staffMember.avatar || ''
        } catch (error) {
            console.error('Failed to load staff:', error)
            ElMessage.error(t('admin.messages.loadStaffError'))
        }
    }
})

const getSkillName = (skillId) => {
    const skill = availableSkills.value.find(s => s.id === skillId)
    return skill ? skill.name : 'Unknown'
}

const formatSkillLevel = (level) => {
    const levelMap = {
        'INTERN': 'Intern',
        'FRESHER': 'Fresher',
        'JUNIOR': 'Junior',
        'MIDDLE': 'Middle',
        'SENIOR': 'Senior',
        'LEAD': 'Lead',
        'PRINCIPAL': 'Principal',
        'ARCHITECT': 'Architect',
        'EXPERT': 'Expert'
    }
    return levelMap[level] || level
}

const addSkill = () => {
    if (!newSkill.skillId) {
        ElMessage.warning(t('admin.messages.selectSkillWarning'))
        return
    }
    if (!newSkill.level) {
        ElMessage.warning(t('admin.messages.selectLevelWarning'))
        return
    }

    // Check if skill already exists
    if (form.skills.find(s => s.skillId === newSkill.skillId)) {
        ElMessage.warning(t('admin.messages.skillExistsWarning'))
        return
    }

    form.skills.push({
        skillId: newSkill.skillId,
        level: newSkill.level,
        yearsOfExperience: newSkill.yearsOfExperience || 0
    })

    // Reset input
    newSkill.skillId = ''
    newSkill.level = ''
    newSkill.yearsOfExperience = 0
}

const removeSkill = (index) => {
    form.skills.splice(index, 1)
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
        router.back()
    } catch (error) {
        // user cancelled
    }
}

const handleAvatarChange = (file) => {
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

    // Save file and create preview URL
    avatarFile.value = file.raw
    form.avatar = URL.createObjectURL(file.raw)
    ElMessage.success(t('admin.messages.imageSelected'))
}

const removeAvatar = () => {
    form.avatar = ''
    avatarFile.value = null
    ElMessage.info(t('admin.messages.imageRemoved'))
}

const submit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (!valid) return

        if (isEdit.value) {
            try {
                await ElMessageBox.confirm(
                    t('admin.messages.confirmEdit'),
                    t('admin.actions.update'),
                    {
                        confirmButtonText: t('admin.actions.update'),
                        cancelButtonText: t('admin.actions.close'),
                        type: 'warning'
                    }
                )
            } catch (error) {
                return
            }
        }

        try {
            const formData = new FormData()
            formData.append('email', form.email)
            if (form.password) {
                formData.append('password', form.password)
            }
            formData.append('firstName', form.firstName)
            formData.append('lastName', form.lastName)
            formData.append('phone', form.phone)
            formData.append('cv', form.cv)

            // Add IT Role
            if (form.itRole) {
                formData.append('itRole', form.itRole)
            }

            // Add skills as JSON
            if (form.skills && form.skills.length > 0) {
                formData.append('skills', JSON.stringify(form.skills))
            }

            if (avatarFile.value) {
                formData.append('avatar', avatarFile.value)
            } else if (form.avatar) {
                formData.append('avatar', form.avatar)
            }

            if (isEdit.value) {
                await apiUsers.updateStaff(route.params.id, formData)
                ElMessage.success(t('admin.messages.staffUpdated'))
            } else {
                await apiUsers.createStaff(formData)
                ElMessage.success(t('admin.messages.staffCreated'))
            }

            router.push({ name: 'admin-staff' })
        } catch (error) {
            console.error('Failed to submit:', error)
            ElMessage.error(t('admin.messages.saveError'))
        }
    })
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

:deep(.el-form-item__label-wrap) {
    margin-left: 0 !important;
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
:deep(.el-select),
:deep(.el-input-number) {
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

.skills-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 16px;
    background: #f9f9fb;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
}

.skill-input-group {
    display: flex;
    gap: 8px;
    align-items: center;
    flex-wrap: wrap;
}

.skills-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 8px;
    padding: 8px;
    background-color: #f5f7fa;
    border-radius: 4px;
}

.skill-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    background-color: #fff;
    border-left: 3px solid #409eff;
    border-radius: 2px;
}

.skill-text {
    font-size: 14px;
    color: #333;
}

.avatar-upload-container {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 20px;
}

.avatar-preview {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
}

.avatar-preview .preview-img {
    width: 150px;
    height: 150px;
    object-fit: cover;
    border-radius: 8px;
    border: 2px solid #e5e7eb;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-upload {
    width: 100%;
    max-width: 400px;
}

:deep(.el-upload) {
    width: 100%;
}

:deep(.el-upload-dragger) {
    border: 2px dashed #b3d8ff;
    border-radius: 8px;
    padding: 30px 20px;
    background-color: #f0f7ff;
    transition: all 0.3s ease;
}

:deep(.el-upload-dragger:hover) {
    border-color: #0066cc;
    background-color: #f5f9ff;
}

:deep(.el-icon--upload) {
    font-size: 36px;
    color: #0066cc;
    margin-bottom: 8px;
}

.help-text {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
    padding: 8px;
    background-color: #fff;
    border-radius: 4px;
    border-left: 3px solid #faad14;
}

@media (max-width: 768px) {
    .form-grid {
        grid-template-columns: 1fr;
        gap: 16px;
    }

    .form-section-full {
        padding: 16px;
    }

    .skill-input-group {
        flex-direction: column;
    }

    .skill-input-group>* {
        width: 100%;
    }

    .avatar-upload {
        flex-direction: column;
        padding: 12px;
    }

    .avatar-preview {
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

    .avatar-preview {
        width: 80px;
        height: 80px;
    }

    :deep(.el-form-item__label) {
        font-size: 12px;
    }
}
</style>

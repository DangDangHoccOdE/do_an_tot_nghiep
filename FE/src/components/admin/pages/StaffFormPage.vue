<template>
    <div class="grid">
        <SectionCard eyebrow="Quản lý nhân viên" :title="isEdit ? 'Sửa nhân viên' : 'Thêm nhân viên'">
            <el-form ref="formRef" :model="form" :rules="staffRules" label-width="150px">
                <el-form-item label="Email" prop="email">
                    <el-input v-model="form.email" :disabled="isEdit" />
                </el-form-item>

                <el-form-item label="Mật khẩu" prop="password" v-if="!isEdit">
                    <el-input v-model="form.password" type="password" />
                </el-form-item>

                <el-form-item label="Tên" prop="firstName">
                    <el-input v-model="form.firstName" />
                </el-form-item>

                <el-form-item label="Họ" prop="lastName">
                    <el-input v-model="form.lastName" />
                </el-form-item>

                <el-form-item label="Số điện thoại" prop="phone">
                    <el-input v-model="form.phone" />
                </el-form-item>

                <!-- IT Role Section -->
                <el-form-item label="Vai trò IT" prop="itRole">
                    <el-select v-model="form.itRole" placeholder="Chọn vai trò" style="width: 100%;">
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

                <!-- Skills Section -->
                <el-form-item label="Kỹ năng">
                    <div class="skills-section">
                        <!-- Add new skill -->
                        <div class="skill-input-group">
                            <el-select v-model="newSkill.skillId" placeholder="Chọn kỹ năng"
                                style="flex: 1; margin-right: 8px;">
                                <el-option v-for="skill in availableSkills" :key="skill.id" :label="skill.name"
                                    :value="skill.id" />
                            </el-select>
                            <el-select v-model="newSkill.level" placeholder="Cấp độ"
                                style="width: 150px; margin-right: 8px;">
                                <el-option label="Intern (Thực tập)" value="INTERN" />
                                <el-option label="Fresher (0-1 năm)" value="FRESHER" />
                                <el-option label="Junior (1-2 năm)" value="JUNIOR" />
                                <el-option label="Middle (2-4 năm)" value="MIDDLE" />
                                <el-option label="Senior (4-7 năm)" value="SENIOR" />
                                <el-option label="Lead (7-10 năm)" value="LEAD" />
                                <el-option label="Principal (10+ năm)" value="PRINCIPAL" />
                                <el-option label="Architect (10+ năm)" value="ARCHITECT" />
                                <el-option label="Expert (15+ năm)" value="EXPERT" />
                            </el-select>
                            <el-input-number v-model="newSkill.yearsOfExperience" :min="0" :max="50"
                                placeholder="Năm kinh nghiệm" style="width: 120px; margin-right: 8px;" />
                            <el-button type="primary" @click="addSkill" :disabled="!newSkill.skillId">
                                Thêm
                            </el-button>
                        </div>

                        <!-- Skills list -->
                        <div class="skills-list" v-if="form.skills && form.skills.length > 0">
                            <div class="skill-item" v-for="(skill, index) in form.skills" :key="index">
                                <span class="skill-text">
                                    {{ getSkillName(skill.skillId) }} - {{ skill.level }} ({{ skill.yearsOfExperience }}
                                    năm)
                                </span>
                                <el-button type="danger" size="small" @click="removeSkill(index)">
                                    Xóa
                                </el-button>
                            </div>
                        </div>
                        <div class="help-text" v-if="!form.skills || form.skills.length === 0">
                            Chưa có kỹ năng nào. Thêm kỹ năng để tiếp tục.
                        </div>
                    </div>
                </el-form-item>

                <el-form-item label="CV" prop="cv">
                    <el-input v-model="form.cv" placeholder="Đường dẫn tới CV hoặc file URL" />
                </el-form-item>

                <el-form-item label="Avatar">
                    <div class="avatar-upload">
                        <div class="avatar-preview" v-if="form.avatar || previewUrl">
                            <img :src="previewUrl || form.avatar" alt="preview" />
                        </div>
                        <el-upload :on-change="handleAvatarChange" :auto-upload="false" accept="image/*" drag
                            action="#">
                            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                            <div class="el-upload__text">
                                Kéo file vào đây hoặc <em>click để chọn</em>
                            </div>
                        </el-upload>
                    </div>
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="submit">{{ isEdit ? 'Cập nhật' : 'Thêm' }}</el-button>
                    <el-button @click="$router.back()">Hủy</el-button>
                </el-form-item>
            </el-form>
        </SectionCard>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import SectionCard from '@/components/admin/SectionCard.vue'
import { apiUsers } from '@/services/apiUsers'
import { apiSkills } from '@/services/apiSkills'
import { staffRules } from '@/validations/staffRules'

const router = useRouter()
const route = useRoute()
const formRef = ref()

const isEdit = ref(false)
const previewUrl = ref('')
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
        ElMessage.error('Lỗi khi tải danh sách kỹ năng')
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
            ElMessage.error('Lỗi khi tải dữ liệu nhân viên')
        }
    }
})

const getSkillName = (skillId) => {
    const skill = availableSkills.value.find(s => s.id === skillId)
    return skill ? skill.name : 'Unknown'
}

const addSkill = () => {
    if (!newSkill.skillId) {
        ElMessage.warning('Vui lòng chọn kỹ năng')
        return
    }
    if (!newSkill.level) {
        ElMessage.warning('Vui lòng chọn cấp độ')
        return
    }

    // Check if skill already exists
    if (form.skills.find(s => s.skillId === newSkill.skillId)) {
        ElMessage.warning('Kỹ năng này đã được thêm')
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

const handleAvatarChange = (file) => {
    avatarFile.value = file.raw
    const reader = new FileReader()
    reader.onload = (e) => {
        previewUrl.value = e.target.result
    }
    reader.readAsDataURL(file.raw)
}

const submit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
        if (!valid) return

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
                ElMessage.success('Cập nhật nhân viên thành công')
            } else {
                await apiUsers.createStaff(formData)
                ElMessage.success('Thêm nhân viên thành công')
            }

            router.push({ name: 'admin-staff' })
        } catch (error) {
            console.error('Failed to submit:', error)
            ElMessage.error('Lỗi khi lưu dữ liệu')
        }
    })
}
</script>

<style scoped>
.grid {
    display: grid;
    gap: 16px;
}

.skills-section {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.skill-input-group {
    display: flex;
    gap: 8px;
    align-items: center;
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

.avatar-upload {
    display: flex;
    gap: 16px;
    align-items: flex-start;
}

.avatar-preview {
    width: 100px;
    height: 100px;
}

.avatar-preview img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 4px;
    border: 1px solid #ddd;
}

.help-text {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
    padding: 8px;
    background-color: #f5f7fa;
    border-radius: 4px;
}
</style>

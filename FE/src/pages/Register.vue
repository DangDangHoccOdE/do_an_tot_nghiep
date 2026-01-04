<template>
  <div class="auth-container">
    <div class="container">
      <!-- Form Panel -->
      <div class="form-panel">
        <h2>{{ $t("auth.register.title") }}</h2>

        <el-form :model="registerForm" :rules="rules" ref="formRef" label-position="top"
          @submit.prevent="handleRegister" v-loading="loading">
          <!-- First Name -->
          <el-form-item prop="firstName">
            <template #label>
              <RequiredLabel required :label="$t('firstName')" />
            </template>
            <el-input v-model="registerForm.firstName" clearable :placeholder="$t('placeholder.firstName')" />
          </el-form-item>

          <!-- Last Name -->
          <el-form-item prop="lastName">
            <template #label>
              <RequiredLabel required :label="$t('lastName')" />
            </template>
            <el-input v-model="registerForm.lastName" clearable :placeholder="$t('placeholder.lastName')" />
          </el-form-item>

          <!-- Email -->
          <el-form-item prop="email">
            <template #label>
              <RequiredLabel required :label="$t('email')" />
            </template>
            <el-input v-model="registerForm.email" clearable type="email" :placeholder="$t('placeholder.email')" />
          </el-form-item>

          <!-- Password -->
          <el-form-item prop="password">
            <template #label>
              <RequiredLabel required :label="$t('password')" />
            </template>
            <el-input v-model="registerForm.password" show-password type="password"
              :placeholder="$t('placeholder.password')" />
          </el-form-item>

          <el-button type="primary" class="submit-btn" @click="handleRegister" :loading="loading">
            {{ $t("register") }}
          </el-button>
        </el-form>
      </div>

      <!-- Side Panel -->
      <div class="side-panel">
        <h2>{{ $t("auth.register.welcomeBack") }}</h2>
        <p>{{ $t("auth.register.haveAccount") }}</p>

        <el-button type="success" @click="goToLogin" class="switch-btn">
          {{ $t("auth.register.loginNow") }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { ElMessage } from "element-plus";
import { apiRegister } from "@/services/apiRegister";
import { createRegisterRules } from "@/validations/registerRules";
import RequiredLabel from "@/components/common/RequiredLabel.vue";

const { t } = useI18n();
const router = useRouter();
const formRef = ref();
const loading = ref(false);

// Form data
const registerForm = reactive({
  firstName: "",
  lastName: "",
  email: "",
  password: "",
});

// Validation rules
const rules = createRegisterRules(t, apiRegister);

// Handle registration
const handleRegister = async () => {
  if (!formRef.value) return;

  try {
    // Validate form
    await formRef.value.validate();

    loading.value = true;

    // Prepare data for API
    const registerData = {
      firstName: registerForm.firstName.trim(),
      lastName: registerForm.lastName.trim(),
      email: registerForm.email.trim().toLowerCase(),
      password: registerForm.password,
      authProvider: "local",
    };

    // Call register API
    const response = await apiRegister.register(registerData);

    // Show success message
    ElMessage.success({
      message: response?.message || t("registrationSuccess"),
      duration: 3000,
    });

    // Reset form
    formRef.value.resetFields();

    router.push({ path: "/activate", query: { email: registerData.email } });
  } catch (error) {
    if (error.errors) {
      // Validation errors
      console.log("Validation failed:", error);
    } else {
      // API errors
      ElMessage.error({
        message: error.message || t("registrationFailed"),
        duration: 3000,
      });
    }
  } finally {
    loading.value = false;
  }
};

const goToLogin = () => {
  router.push("/login");
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.auth-container {
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #efeff0 0%, #f8f8f8 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.container {
  display: flex;
  max-width: 900px;
  width: 100%;
  background: white;
  border-radius: 30px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(206, 24, 30, 0.1);
  position: relative;
}

/* Form Panel */
.form-panel {
  flex: 1;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  animation: slideIn 0.5s ease-out;
}

.form-panel h2 {
  font-size: 28px;
  color: #4a4b4c;
  margin-bottom: 40px;
  text-align: center;
  font-weight: 600;
}

/* Element Plus Form Overrides */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #4a4b4c;
  margin-bottom: 8px;
}

:deep(.el-input__wrapper) {
  background-color: #efeff0;
  border-radius: 10px;
  padding: 12px 15px;
  box-shadow: none;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  background-color: #e5e5e5;
}

:deep(.el-input__wrapper.is-focus) {
  background-color: #e5e5e5;
  box-shadow: 0 0 0 2px rgba(206, 24, 30, 0.1) !important;
}

:deep(.el-input__inner) {
  color: #4a4b4c;
}

:deep(.el-input__inner::placeholder) {
  color: #70706f;
}

:deep(.el-form-item) {
  margin-bottom: 25px;
}

:deep(.el-form-item__error) {
  font-size: 12px;
  padding-top: 4px;
}

.submit-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #ce181e 0%, #f93535 100%);
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 10px;
  height: auto;
}

.submit-btn:hover:not(.is-loading) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(206, 24, 30, 0.4);
  background: linear-gradient(135deg, #ce181e 0%, #f93535 100%);
}

/* Side Panel */
.side-panel {
  flex: 1;
  background: linear-gradient(135deg, #ce181e 0%, #f93535 100%);
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.side-panel::before {
  content: "";
  position: absolute;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  top: -100px;
  right: -100px;
}

.side-panel h2 {
  font-size: 32px;
  margin-bottom: 15px;
  z-index: 1;
  font-weight: 600;
}

.side-panel p {
  font-size: 14px;
  margin-bottom: 30px;
  opacity: 0.9;
  z-index: 1;
}

.switch-btn {
  background: transparent;
  border: 2px solid white;
  color: white;
  padding: 12px 40px;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
  z-index: 1;
}

.switch-btn:hover {
  background: white;
  color: #ce181e;
  transform: translateY(-2px);
}

/* Animation */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(30px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .container {
    flex-direction: column-reverse;
  }

  .side-panel {
    padding: 40px 30px;
  }

  .form-panel {
    padding: 40px 30px;
  }

  .form-panel h2 {
    font-size: 24px;
    margin-bottom: 30px;
  }

  .side-panel h2 {
    font-size: 26px;
  }
}
</style>

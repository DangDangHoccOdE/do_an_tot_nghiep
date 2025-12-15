<template>
  <div class="auth-container">
    <div class="container">
      <!-- Form Panel -->
      <div class="form-panel">
        <h2>{{ $t("titleRegister") }}</h2>

        <el-form
          :model="registerForm"
          :rules="rules"
          :validate-on-rule-change="false"
          ref="form_ref"
          label-position="top"
          label-width="160px"
          v-loading="loading"
        >
          <!-- First Name -->
          <el-form-item prop="firstName">
            <template #label>
              <RequiredLabel :label="$t('firstName')" required />
            </template>
            <el-input v-model="registerForm.firstName" clearable />
          </el-form-item>

          <!-- Last Name -->
          <el-form-item prop="lastName">
            <template #label>
              <RequiredLabel :label="$t('lastName')" required />
            </template>
            <el-input v-model="registerForm.lastName" />
          </el-form-item>

          <!-- Email -->
          <el-form-item prop="email">
            <template #label>
              <RequiredLabel :label="$t('email')" required />
            </template>
            <el-input v-model="registerForm.email" />
          </el-form-item>

          <!-- Password -->
          <el-form-item prop="password">
            <template #label>
              <RequiredLabel :label="$t('password')" required />
            </template>
            <el-input v-model="registerForm.password" show-password />
          </el-form-item>

          <el-button type="primary" class="submit-btn" @click="handleRegister">
            {{ $t("register") }}
          </el-button>
        </el-form>
      </div>

      <!-- Side Panel -->
      <div class="side-panel">
        <h2>{{ $t("welcomeBack") }}</h2>
        <p>{{ $t("haveAccount") }}</p>

        <el-button type="success" @click="goToLogin" class="switch-btn">
          {{ $t("login") }}
        </el-button>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";
import { apiRegister } from "@/services/apiRegister";
import { handleError, handleSuccess } from "@/utils/handleMessage";
import { useLoading } from "@/utils/useLoading";
import RequiredLabel from "@/components/common/RequiredLabel.vue";
import { createRegisterRules } from "@/validations/registerRules";

const { t } = useI18n();
const router = useRouter();

const { loading, withLoading } = useLoading(t);

const registerForm = ref({
  firstName: "",
  lastName: "",
  email: "",
  password: "",
});

const rules = createRegisterRules(t, apiRegister);
const form_ref = ref();
const handleRegister = async () => {
  await withLoading(async () => {
    // Prepare data for API
    const registerData = {
      firstName: registerForm.value.firstName.trim(),
      lastName: registerForm.value.lastName.trim(),
      email: registerForm.value.email.trim(),
      password: registerForm.value.password,
      authProvider: "local", // Set default auth provider
    };

    // Call register API
    const response = await apiRegister.register(registerData);

    // Show success message
    const successMessage = response?.message || t("registrationSuccess");
    handleSuccess(successMessage);

    // Redirect to login page after 1.5 seconds
    setTimeout(() => {
      router.push("/login");
    }, 1500);
  }, "message.MSG0010");
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
}

.input-group {
  position: relative;
  margin-bottom: 25px;
}

.input-group input {
  width: 100%;
  padding: 15px 45px 15px 15px;
  border: none;
  background: #efeff0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s;
  color: #4a4b4c;
}

.input-group input:focus {
  outline: none;
  background: #e5e5e5;
  box-shadow: 0 0 0 2px rgba(206, 24, 30, 0.1);
}

.input-group input::placeholder {
  color: #70706f;
}

.input-group .select-input {
  width: 100%;
  padding: 15px;
  border: none;
  background: #efeff0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s;
  color: #4a4b4c;
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2370706f' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 15px center;
  padding-right: 45px;
}

.input-group .select-input:focus {
  outline: none;
  background-color: #e5e5e5;
  box-shadow: 0 0 0 2px rgba(206, 24, 30, 0.1);
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%2370706f' d='M6 9L1 4h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 15px center;
}

.input-group .select-input option {
  background: white;
  color: #4a4b4c;
}

.input-group .icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #70706f;
  font-size: 16px;
}

.submit-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #ce181e 0%, #f93535 100%);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(206, 24, 30, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  cursor: pointer;
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
}
</style>

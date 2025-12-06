<template>
  <Header />
  <div class="register-page">
    <div class="register-form">
      <h2 class="font-bold text-lg">
        {{ $t('titleRegister') }}
      </h2>
      <el-form
        ref="registerForm"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            :placeholder="$t('placeholder.username')"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            type="email"
            :placeholder="$t('placeholder.email')"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="$t('placeholder.password')"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            :placeholder="$t('placeholder.confirmPassword')"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="w-full"
            @click="handleRegister"
          >
            {{ t('register') }}
          </el-button>
        </el-form-item>
        <p class="mt-4 text-center">
          {{ $t('haveAccount') }} <a
            href="#"
            class="font-semibold hover:underline link-decoration"
            @click.prevent="goToLogin"
          >{{ $t('loginNow') }}</a>
        </p>
      </el-form>
    </div>
  </div> 
  <Footer />
</template>

<script setup>
defineOptions({ name: 'RegisterPage' });
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/layouts/Header.vue'
import Footer from '@/layouts/Footer.vue' 
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const router = useRouter()

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.value.password) {
    callback(new Error('Mật khẩu xác nhận không khớp'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' }],
  email: [
    { required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
    { type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }
  ],
  password: [{ required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: 'Vui lòng xác nhận mật khẩu', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = () => {
  // TODO: Implement registration logic
  router.push('/login')
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 40vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  space-x: 4
}

.register-form {
  padding: 30px 40px;
  border-radius: 10px;
  width: 400px;
  text-align: left !important;
}

h2 {
  text-align: center;
  margin-bottom: 25px;
}
</style>

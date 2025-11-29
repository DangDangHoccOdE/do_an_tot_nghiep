<template>
  <Header />
  <div class="login-page">
    <div class="login-form">
      <h2 class="font-bold text-lg">
        {{ $t('titleLogin') }}
      </h2>
      <el-form
        ref="loginForm"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            :placeholder="$t('placeholder.username')"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="$t('placeholder.password')"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <div class="mb-4 text-gray-600">
          <a
            href="#"
            class="hover:underline link-decoration"
          >{{ $t('fogotPassword') }}</a>
        </div>
        <el-form-item>
          <el-button
            type="primary"
            class="w-full"
            @click="handleLogin"
          >
            {{ t('login') }}
          </el-button>
        </el-form-item>
        <p class="mt-4 text-center">
          {{ $t('haveNotAccount') }} <a
            href="#"
            class="font-semibold hover:underline link-decoration"
          >{{ $t('registerNow') }}</a>
        </p>
      </el-form>
    </div>
  </div> 
  <Footer />
</template>

<script setup>
defineOptions({ name: 'LoginPage' });
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/layouts/Header.vue'
import Footer from '@/layouts/Footer.vue' 
import { useI18n } from 'vue-i18n';
import { loginSchema } from '@/validations/login.schema';

const { t } = useI18n();
const router = useRouter()
// function handleLogin() {
//   auth.login()
//   router.push('/')
// }

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' }],
  password: [{ required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' }]
}

const handleLogin = () => {
  // auth.login()
  router.push('/')
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 40vh;
  /* background-color: rgba(242, 230, 230, 0.95); */
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-form {
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

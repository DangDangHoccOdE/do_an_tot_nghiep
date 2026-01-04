<template>
  <div class="auth-container">
    <div class="container">
      <!-- Side Panel -->
      <div class="side-panel">
        <h2>{{ $t('auth.login.welcome') }}</h2>
        <p>{{ $t('auth.login.noAccount') }}</p>
        <button @click="goToRegister" class="switch-btn">{{ $t('auth.login.registerNow') }}</button>
      </div>

      <!-- Form Panel -->
      <div class="form-panel">
        <h2>{{ $t('auth.login.title') }}</h2>
        <form @submit.prevent="handleLogin">
          <div class="input-group">
            <input v-model="loginForm.username" type="text" :placeholder="$t('auth.login.username')"
              :class="{ 'input-error': errors.username }">
            <span class="icon">👤</span>
            <span v-if="errors.username" class="error-msg">{{ errors.username }}</span>
          </div>

          <div class="input-group">
            <input v-model="loginForm.password" type="password" :placeholder="$t('auth.login.password')"
              :class="{ 'input-error': errors.password }">
            <span class="icon">🔒</span>
            <span v-if="errors.password" class="error-msg">{{ errors.password }}</span>
          </div>

          <div v-if="errors.general" class="error-banner">
            {{ errors.general }}
          </div>

          <div class="forgot-password">
            <a href="#" @click.prevent="handleForgotPassword">{{ $t('auth.login.forgotPassword') }}</a>
          </div>

          <button type="submit" class="submit-btn" :disabled="isLoading">
            {{ isLoading ? $t('auth.login.loading') : $t('auth.login.submit') }}
          </button>

          <div class="divider">{{ $t('auth.login.socialLogin') }}</div>

          <div class="social-login">
            <div class="social-btn google" @click="handleGoogleLogin">G</div>
            <div class="social-btn facebook" @click="handleFacebookLogin">f</div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth/useAuthStore';
import { apiAuth } from '@/services/apiAuth';

export default {
  name: 'Login',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const auth = useAuthStore();

    return {
      router,
      route,
      auth
    };
  },
  data() {
    return {
      isLoading: false,
      loginForm: {
        username: '',
        password: ''
      },
      errors: {
        username: '',
        password: '',
        general: ''
      }
    };
  },
  methods: {
    clearErrors() {
      this.errors = { username: '', password: '', general: '' };
    },
    async handleLogin() {
      this.clearErrors();
      this.isLoading = true;

      try {
        const data = await apiAuth.login(this.loginForm);
        this.auth.login(data);
        const redirect = this.route.query.redirect || '/admin';
        this.router.push(redirect);
      } catch (error) {
        console.error('Login error:', error);

        // Get message từ response data - backend trả về dạng { code, message, data, success }
        let errorMsg = this.$t('auth.errors.loginFailed');
        const status = error?.response?.status;

        if (error?.response?.data) {
          // Nếu backend trả về ApiResponse format
          errorMsg = error.response.data.message || error.response.data.error || errorMsg;
        } else if (error?.message) {
          errorMsg = error.message;
        }

        // Hiển thị message lỗi ở banner (vì không thể phân biệt username hay password từ backend)
        this.errors.general = errorMsg;

        const msgLower = (errorMsg || '').toLowerCase();
        if (status === 403 || msgLower.includes('chua duoc kich hoat')) {
          setTimeout(() => {
            this.router.push({
              path: '/activate',
              query: { email: this.loginForm.username }
            });
          }, 2000); // 2 giây
        }
      } finally {
        this.isLoading = false;
      }
    },
    handleGoogleLogin() {
      window.location.href = `http://localhost:8080/oauth2/authorize/google?redirect_uri=http://localhost:5173/oauth2/redirect`;
    },
    handleFacebookLogin() {
      window.location.href = `http://localhost:8080/oauth2/authorize/facebook?redirect_uri=http://localhost:5173/oauth2/redirect`;
    },
    handleForgotPassword() {
      alert('Forgot password - Implement password reset');
      // this.router.push('/forgot-password');
    },
    goToRegister() {
      this.router.push('/register');
    }
  }
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.auth-container {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background: linear-gradient(135deg, #EFEFF0 0%, #f8f8f8 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
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

/* Side Panel */
.side-panel {
  flex: 1;
  background: linear-gradient(135deg, #CE181E 0%, #f93535 100%);
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
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  top: -100px;
  left: -100px;
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
  color: #CE181E;
  transform: translateY(-2px);
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
  color: #4A4B4C;
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
  background: #EFEFF0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.3s;
  color: #4A4B4C;
}

.input-group input:focus {
  outline: none;
  background: #e5e5e5;
  box-shadow: 0 0 0 2px rgba(206, 24, 30, 0.1);
}

.input-group input::placeholder {
  color: #70706F;
}

.input-group input.input-error {
  border: 2px solid #CE181E;
  background: #fff5f5;
  box-shadow: 0 0 0 2px rgba(206, 24, 30, 0.1);
}

.error-msg {
  display: block;
  color: #CE181E;
  font-size: 12px;
  margin-top: 6px;
  font-weight: 500;
}

.input-group .icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #70706F;
  font-size: 16px;
}

.forgot-password {
  text-align: right;
  margin-bottom: 25px;
}

.forgot-password a {
  color: #CE181E;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.3s;
}

.forgot-password a:hover {
  color: #b0151a;
}

.error-banner {
  background: #fff5f5;
  border: 1px solid #f5b4b4;
  color: #CE181E;
  padding: 12px 14px;
  border-radius: 8px;
  font-size: 13px;
  margin-bottom: 20px;
  text-align: center;
  font-weight: 500;
}

.submit-btn {
  width: 100%;
  padding: 15px;
  background: linear-gradient(135deg, #CE181E 0%, #f93535 100%);
  border: none;
  border-radius: 10px;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 25px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(206, 24, 30, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.divider {
  text-align: center;
  margin: 25px 0;
  color: #70706F;
  font-size: 13px;
  position: relative;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 40%;
  height: 1px;
  background: #ddd;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.social-btn {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: 1px solid #ddd;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 18px;
  font-weight: bold;
}

.social-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(206, 24, 30, 0.1);
}

.social-btn.google {
  color: #DB4437;
}

.social-btn.facebook {
  color: #4267B2;
}

.social-btn.github {
  color: #333;
}

.social-btn.linkedin {
  color: #0077B5;
}

/* Animation */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-30px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Responsive */
@media (max-width: 768px) {
  .container {
    flex-direction: column;
  }

  .side-panel {
    padding: 40px 30px;
  }

  .form-panel {
    padding: 40px 30px;
  }
}
</style>
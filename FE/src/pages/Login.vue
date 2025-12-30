<template>
  <div class="auth-container">
    <div class="container">
      <!-- Side Panel -->
      <div class="side-panel">
        <h2>Hello, Welcome!</h2>
        <p>Don't have an account?</p>
        <button @click="goToRegister" class="switch-btn">Register</button>
      </div>

      <!-- Form Panel -->
      <div class="form-panel">
        <h2>Login</h2>
        <form @submit.prevent="handleLogin">
          <div class="input-group">
            <input 
              v-model="loginForm.username" 
              type="text" 
              placeholder="Username" 
              required
            >
            <span class="icon">👤</span>
          </div>

          <div class="input-group">
            <input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="Password" 
              required
            >
            <span class="icon">🔒</span>
          </div>

          <div class="forgot-password">
            <a href="#" @click.prevent="handleForgotPassword">Forgot password?</a>
          </div>

          <button type="submit" class="submit-btn" :disabled="isLoading">
            {{ isLoading ? 'Loading...' : 'Login' }}
          </button>

          <div class="divider">or login with social platforms</div>

          <div class="social-login">
            <div class="social-btn google" @click="handleGoogleLogin">G</div>
            <div class="social-btn facebook" @click="handleFacebookLogin">f</div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { post } from '@/utils/http';
import { useAuthStore } from '@/stores/auth/useAuthStore';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();
const isLoading = ref(false);

const loginForm = ref({
  username: '',
  password: ''
});

const handleLogin = async () => {
  isLoading.value = true;
  
  try {
    const data = await post('/api/v1/auth/login', loginForm.value);
    auth.login(data);
    const redirect = route.query.redirect || '/admin';
    router.push(redirect);
  } catch (error) {
    console.error('Login error:', error);
    alert('Login failed. Please try again.');
  } finally {
    isLoading.value = false;
  }
};

const handleGoogleLogin = () => {
  window.location.href = `http://localhost:8080/oauth2/authorize/google?redirect_uri=http://localhost:5173/oauth2/redirect`;
};

const handleFacebookLogin = () => {
  window.location.href = `http://localhost:8080/oauth2/authorize/facebook?redirect_uri=http://localhost:5173/oauth2/redirect`;
};

const handleForgotPassword = () => {
  alert('Forgot password - Implement password reset');
  // router.push('/forgot-password');
};

const goToRegister = () => {
  router.push('/register');
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

.social-btn.google { color: #DB4437; }
.social-btn.facebook { color: #4267B2; }
.social-btn.github { color: #333; }
.social-btn.linkedin { color: #0077B5; }

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
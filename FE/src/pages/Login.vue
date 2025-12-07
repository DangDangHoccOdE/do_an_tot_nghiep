<!-- <template>
  <section class="bg-gray-100 flex items-center justify-center h-full py-4">
    <div class="w-full max-w-5xl bg-white shadow-md rounded-lg overflow-hidden">
      <div class="grid grid-cols-1 lg:grid-cols-2">
        <div class="p-8 md:p-12">
          <div class="text-center mb-8">
            <img
              src="/Luvina.jpg"
              class="w-40 mx-auto"
              alt="Luvina logo"
            />
            <h4 class="mt-4 text-xl font-semibold">Luvina Software</h4>
          </div>

          <form @submit.prevent="handleLogin">
            <p class="mb-4 text-gray-600">{{ $t('pleaseLogin') }}</p>

            <div class="mb-4">
              <label class="block text-sm font-medium mb-1">{{ $t('emailOrPhone') }}</label>
              <input
                type="text"
                v-model="email"
                class="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring focus:ring-blue-200 focus:border-blue-400 outline-none"
                :placeholder="$t('enterEmailOrPhone')"
              />
            </div>

            <div class="mb-4">
              <label class="block text-sm font-medium mb-1">{{ $t('form.password') }}</label>
              <input
                type="password"
                v-model="password"
                class="w-full border border-gray-300 rounded-md px-3 py-2 focus:ring focus:ring-blue-200 focus:border-blue-400 outline-none"
                :placeholder="$t('enterPassword')"
              />
            </div>

            <div class="text-center mb-6">
              <button
                class="w-full bg-[#CE181E] hover:bg-[#b0151a] text-white font-medium px-4 py-2 rounded-md transition"
                type="submit"
              >
                {{ $t('login') }}
              </button>

              <a href="#" class="text-gray-500 text-sm inline-block mt-3 hover:underline">
                {{ $t('forgotPassword') }}
              </a>
            </div>

            <div class="flex items-center justify-center gap-2">
              <p class="text-sm">{{ $t('dontHaveAccount') }}</p>
              <button
                type="button"
                @click="goToRegister"
                class="text-[#CE181E] font-medium hover:underline"
              >
                {{ $t('createNew') }}
              </button>
            </div>
          </form>
        </div>

        <div class="hidden lg:flex items-center justify-center bg-gradient-to-br from-[#CE181E] to-[#f93535] p-10">
          <img 
            src="/Luvina.jpg"
            alt="Luvina Software" 
            class="w-full h-full object-cover rounded-lg shadow-lg"
            style="max-height: 600px;"
          />
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useI18n } from "vue-i18n";

const router = useRouter();
const { t } = useI18n();

const email = ref("");
const password = ref("");

const handleLogin = () => {
  console.log("Email:", email.value);
  console.log("Password:", password.value);

  router.push("/");
};

const goToRegister = () => {
  router.push("/register");
};
</script>

<style scoped>
</style> -->


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
            <div class="social-btn github" @click="handleGithubLogin">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
              </svg>
            </div>
            <div class="social-btn linkedin" @click="handleLinkedinLogin">in</div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const isLoading = ref(false);

const loginForm = ref({
  username: '',
  password: ''
});

const handleLogin = async () => {
  isLoading.value = true;
  
  try {
    // TODO: Call your login API here
    console.log('Login data:', loginForm.value);
    
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // Example API call:
    // const response = await fetch('/api/login', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify(loginForm.value)
    // });
    // const data = await response.json();
    
    alert('Login successful!');
    // router.push('/dashboard');
  } catch (error) {
    console.error('Login error:', error);
    alert('Login failed. Please try again.');
  } finally {
    isLoading.value = false;
  }
};

const handleGoogleLogin = () => {
  console.log('Google login initiated');
  // TODO: Implement Google OAuth
  // window.location.href = 'YOUR_GOOGLE_OAUTH_URL';
  alert('Google login - Connect to OAuth');
};

const handleFacebookLogin = () => {
  console.log('Facebook login initiated');
  // TODO: Implement Facebook OAuth
  alert('Facebook login - Connect to OAuth');
};

const handleGithubLogin = () => {
  console.log('GitHub login initiated');
  // TODO: Implement GitHub OAuth
  alert('GitHub login - Connect to OAuth');
};

const handleLinkedinLogin = () => {
  console.log('LinkedIn login initiated');
  // TODO: Implement LinkedIn OAuth
  alert('LinkedIn login - Connect to OAuth');
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
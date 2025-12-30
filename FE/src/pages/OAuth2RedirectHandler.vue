<template>
  <div class="oauth-redirect-container">
    <el-icon class="loading-icon" :size="50">
      <Loading />
    </el-icon>
    <h2>{{ message }}</h2>
    <p v-if="error" class="error-text">{{ error }}</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Loading } from "@element-plus/icons-vue";

const router = useRouter();
const route = useRoute();
const message = ref("Đang xử lý đăng nhập...");
const error = ref("");

onMounted(() => {
  handleOAuth2Redirect();
});

const handleOAuth2Redirect = () => {
  // Lấy tokens từ URL query parameters
  const accessToken = route.query.accessToken;
  const refreshToken = route.query.refreshToken;
  const errorParam = route.query.error;

  if (errorParam) {
    // Xử lý lỗi
    error.value = decodeURIComponent(errorParam);
    message.value = "Đăng nhập thất bại!";

    ElMessage.error({
      message: error.value,
      duration: 5000,
    });

    // Redirect về trang login sau 3 giây
    setTimeout(() => {
      router.push("/login");
    }, 3000);
    return;
  }

  if (accessToken && refreshToken) {
    // Lưu tokens vào localStorage
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);

    message.value = "Đăng nhập thành công!";

    ElMessage.success({
      message: "Đăng nhập thành công!",
      duration: 2000,
    });

    // Redirect đến dashboard
    setTimeout(() => {
      router.push("/dashboard");
    }, 1000);
  } else {
    // Không có tokens
    error.value = "Không nhận được thông tin xác thực";
    message.value = "Đăng nhập thất bại!";

    ElMessage.error({
      message: error.value,
      duration: 3000,
    });

    setTimeout(() => {
      router.push("/login");
    }, 2000);
  }
};
</script>

<style scoped>
.oauth-redirect-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #efeff0 0%, #f8f8f8 100%);
  padding: 20px;
}

.loading-icon {
  color: #ce181e;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

h2 {
  font-size: 24px;
  color: #4a4b4c;
  margin-bottom: 10px;
  font-weight: 600;
}

.error-text {
  color: #f56c6c;
  font-size: 14px;
  text-align: center;
  max-width: 500px;
}
</style>

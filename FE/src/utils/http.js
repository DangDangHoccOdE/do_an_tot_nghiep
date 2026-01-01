import axios from 'axios';
import { useAuthStore } from "@/stores/auth/useAuthStore";
import router from "@/router";

const baseURL = import.meta.env.VITE_BASE_API || "http://localhost:8080";

const apiService = import.meta.env.VITE_API_PREFIX || "";

axios.defaults.timeout = 50000;
axios.defaults.baseURL = `${baseURL}/${apiService}`;

// Request interceptor
axios.interceptors.request.use(
    (config) => {
        try {
            // Chỉ gọi store khi Pinia đã được khởi tạo
            const auth = useAuthStore();
            // Nếu user đã login và có token (có thể lưu trong user object hoặc state riêng)
            if(auth.isLoggedIn && auth.accessToken) {
                config.headers["Authorization"] = `Bearer ${auth.accessToken}`
            }
        } catch (error) {
            // Pinia chưa được khởi tạo, bỏ qua việc thêm token
            // Điều này xảy ra khi gọi API từ validator hoặc bên ngoài component
            // Không cần xử lý, chỉ cần bỏ qua
        }
        return config;
    },
    (error) => Promise.reject(error)
)

// Response interceptor
axios.interceptors.response.use(
    (res) => {
        // Trả về response như là, để component tự xử lý logic
        return res;
    },
    (error) => {
        const auth = useAuthStore();
        
        // Nếu là lỗi 401/403 và không phải login page, đăng xuất và redirect
        if (error?.response?.status === 401 || error?.response?.status === 403) {
            const path = router.currentRoute.value.fullPath;
            // Chỉ logout nếu đã qua trang login (có token lưu nhưng hết hạn)
            if(!path.includes('login') && auth.accessToken) {
                auth.logout();
                router.push(`/login?redirect=${path}`);
            }
        }
        
        return Promise.reject(error);
    }
);

// API methods
export function get(url, params = {}) {
  return axios.get(url, { params }).then((res) => res.data);
}

export function post(url, data = {}) {
  return axios.post(url, data).then((res) => res.data);
}

export function put(url, data = {}) {
  return axios.put(url, data).then((res) => res.data);
}

export function del(url, data = {}) {
  return axios.delete(url, { data }).then((res) => res.data);
}

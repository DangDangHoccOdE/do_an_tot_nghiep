import axios from 'axios';
import { useAuthStore } from "@/stores/auth/useAuthStore";
import router from "@/router";

const baseURL = import.meta.env.VITE_BASE_API || "http://localhost:8080";

const apiService = import.meta.env.VITE_API_PREFIX || "";

axios.defaults.timeout = 50000;
axios.defaults.baseURL = `${baseURL}/${apiService}`;

// Flag để tránh gọi refresh token nhiều lần cùng lúc
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

// Request interceptor
axios.interceptors.request.use(
    (config) => {
        try {
            // Chỉ gọi store khi Pinia đã được khởi tạo
            const auth = useAuthStore();
            const locale = localStorage.getItem('locale') || 'en';
            config.headers["Accept-Language"] = locale;
            // Không đính kèm access token cho endpoint refresh
            if (config?.url && config.url.includes('/auth/refresh')) {
                return config;
            }
            // Nếu user đã login và có token (có thể lưu trong user object hoặc state riêng)
            if(auth.isLoggedIn && auth.accessToken && auth.isTokenValid) {
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
    async (error) => {
        const originalRequest = error.config;
        const auth = useAuthStore();
        
        // Nếu là lỗi 401 và chưa retry và có refresh token
        if (error?.response?.status === 401 && !originalRequest._retry && auth.refreshToken) {
            // Nếu đang refresh thì đưa request vào queue
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(token => {
                    originalRequest.headers['Authorization'] = `Bearer ${token}`;
                    return axios(originalRequest);
                }).catch(err => {
                    return Promise.reject(err);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                // Gọi API refresh token
                const response = await axios.post('/auth/refresh', {
                    refreshToken: auth.refreshToken
                }, {
                    headers: {
                        'X-Refresh-Token': auth.refreshToken
                    }
                });

                const newAccessToken = response.data.accessToken || response.data.accessTokenJwt;
                const newRefreshToken = response.data.refreshToken || response.data.refreshTokenJwt;

                // Cập nhật token mới vào store
                auth.updateTokens(newAccessToken, newRefreshToken);

                // Xử lý các request đang chờ
                processQueue(null, newAccessToken);

                // Retry request ban đầu với token mới
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                return axios(originalRequest);
            } catch (refreshError) {
                // Refresh token cũng hết hạn, logout user
                processQueue(refreshError, null);
                console.warn('Refresh token failed, logging out user');
                auth.logout(); // Đảm bảo xóa auth khỏi localStorage
                const path = router.currentRoute.value.fullPath;
                if (!path.includes('login')) {
                    router.push(`/login?redirect=${path}`);
                }
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }
        
        // Nếu là lỗi 403 hoặc không có refresh token
        if (error?.response?.status === 403) {
            const path = router.currentRoute.value.fullPath;
            if(!path.includes('login') && auth.accessToken) {
                console.warn('403 error, logging out user');
                auth.logout(); // Đảm bảo xóa auth khỏi localStorage
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

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

// Helper function to decode JWT and check expiration
function isTokenExpired(token) {
    if (!token) return true;
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        const decoded = JSON.parse(jsonPayload);
        if (!decoded || !decoded.exp) return true;
        // exp is in seconds, Date.now() is in milliseconds
        // Add 10 second buffer to avoid edge cases
        const currentTime = (Date.now() / 1000) + 10;
        return decoded.exp < currentTime;
    } catch (error) {
        console.error('Error checking token expiration:', error);
        return true;
    }
}

// Response interceptor
axios.interceptors.response.use(
    (res) => {
        // Trả về response như là, để component tự xử lý logic
        return res;
    },
    async (error) => {
        const originalRequest = error.config;
        const auth = useAuthStore();
        
        // Nếu là lỗi 401 và chưa retry
        if (error?.response?.status === 401 && !originalRequest._retry) {
            // Kiểm tra xem có phải lỗi từ API refresh không - nếu có thì redirect đến trang 401
            if (originalRequest.url && originalRequest.url.includes('/auth/refresh')) {
                console.warn('Refresh token expired or invalid');
                auth.logout();
                router.push('/unauthorized');
                return Promise.reject(error);
            }

            // Kiểm tra có refresh token không
            if (!auth.refreshToken) {
                console.warn('No refresh token available');
                auth.logout();
                const path = router.currentRoute.value.fullPath;
                if (!path.includes('login')) {
                    router.push(`/login?redirect=${encodeURIComponent(path)}`);
                }
                return Promise.reject(error);
            }

            // Kiểm tra refresh token có hết hạn không
            if (isTokenExpired(auth.refreshToken)) {
                console.warn('Refresh token is expired');
                auth.logout();
                router.push('/unauthorized');
                return Promise.reject(error);
            }

            // Kiểm tra access token có thực sự hết hạn không
            // Chỉ gọi refresh nếu access token thực sự hết hạn
            if (!isTokenExpired(auth.accessToken)) {
                // Access token chưa hết hạn, lỗi 401 do lý do khác (không có quyền truy cập)
                console.warn('401 error but access token is still valid - insufficient permissions');
                return Promise.reject(error);
            }

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
                console.log('Access token expired, attempting to refresh...');
                
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

                if (!newAccessToken) {
                    throw new Error('No access token received from refresh');
                }

                console.log('Token refreshed successfully');

                // Cập nhật token mới vào store
                auth.updateTokens(newAccessToken, newRefreshToken);

                // Xử lý các request đang chờ
                processQueue(null, newAccessToken);

                // Retry request ban đầu với token mới
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                return axios(originalRequest);
            } catch (refreshError) {
                // Refresh token cũng hết hạn hoặc invalid, redirect đến trang 401
                processQueue(refreshError, null);
                console.warn('Refresh token failed');
                auth.logout();
                router.push('/unauthorized');
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }
        
        // Nếu là lỗi 403 (Forbidden) 
        if (error?.response?.status === 403) {
            const path = router.currentRoute.value.fullPath;
            if(!path.includes('login') && auth.accessToken) {
                console.warn('403 Forbidden error, logging out user');
                auth.logout();
                router.push(`/login?redirect=${encodeURIComponent(path)}`);
            }
        }

        // Nếu là lỗi 404 (Not Found) - endpoint không tồn tại
        if (error?.response?.status === 404) {
            console.warn('404 Not Found:', error.config?.url);
            // Không logout, chỉ hiển thị thông báo lỗi
        }
        
        return Promise.reject(error);
    }
);

// API methods
export function get(url, params = {}, config = {}) {
  return axios.get(url, { params, ...config }).then((res) => res.data);
}

export function post(url, data = {}, config = {}) {
  // Nếu responseType là blob, trả về toàn bộ response để xử lý download
  if (config.responseType === 'blob') {
    return axios.post(url, data, config).then((res) => res);
  }
  return axios.post(url, data, config).then((res) => res.data);
}

export function put(url, data = {}, config = {}) {
  return axios.put(url, data, config).then((res) => res.data);
}

export function del(url, data = {}, config = {}) {
  return axios.delete(url, { data, ...config }).then((res) => res.data);
}

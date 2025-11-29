import axios from 'axios';
import { useAuthStore } from "@/stores/auth";
import router from "@/router";

const baseURL = import.meta.env.VITE_BASE_API || "https://687f0ea4efe65e52008840fa.mockapi.io";

const apiService = import.meta.env.VITE_API_PREFIX || "";

axios.defaults.timeout = 50000;
axios.defaults.baseURL = baseURL + apiService;

// Request interceptor
axios.interceptors.request.use(
    (config) => {
        const auth = useAuthStore();
        if(auth.isLoggedIn && auth.token) {
            config.headers["Access-Token"] = auth.token
        }
        return config;
    },
    (error) => Promise.reject(error)
)

// Response interceptor
axios.interceptors.response.use(
    (res) => {
        if (res.data) {
            return res;
        }
        if (res || res.data.success) {
            return res;
        }
        if (res.data.code === "402" || res.data.code === "406") {
            const path = router.currentRoute.value.fullPath;
            if(!path.includes('login')) {
                router.push(`/login?redirect=${path}`)
        }
    }
    return Promise.reject(res.data.msg || new Error(res.msg || "Error"));
    },
    (error) => Promise.reject(error)
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

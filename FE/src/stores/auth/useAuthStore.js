import { defineStore } from "pinia";

// Helper function to decode JWT token
function decodeToken(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('Error decoding token:', error);
        return null;
    }
}

// Helper function to check if token is expired
function isTokenExpired(token) { 
    if (!token) return true;
    
    const decoded = decodeToken(token);
    if (!decoded || !decoded.exp) return true;
    
    // exp is in seconds, Date.now() is in milliseconds
    const currentTime = Date.now() / 1000;
    return decoded.exp < currentTime;
}

export const useAuthStore = defineStore('auth', {
    state: () => {
        const cached = typeof localStorage !== 'undefined' ? localStorage.getItem('auth') : null;
        const parsed = cached ? JSON.parse(cached) : null;
        const cachedAccessToken = parsed?.accessToken || parsed?.accessTokenJwt;
        return {
            isLoggedIn: !!cachedAccessToken,
            user: parsed?.user || null,
            accessToken: cachedAccessToken || null,
            refreshToken: parsed?.refreshToken || parsed?.refreshTokenJwt || null,
            role: parsed?.role || null
        };
    },
    getters: {
        // Kiểm tra có thể authenticate không (có token hoặc có refresh token còn hạn)
        canAuthenticate: (state) => {
            // Nếu accessToken còn hạn -> OK
            if (state.accessToken && !isTokenExpired(state.accessToken)) {
                return true;
            }
            // Nếu accessToken hết hạn nhưng còn refreshToken -> vẫn OK (sẽ tự động refresh)
            if (state.refreshToken && !isTokenExpired(state.refreshToken)) {
                return true;
            }
            // Cả 2 đều hết hạn hoặc không có -> không OK
            return false;
        },
        // Kiểm tra accessToken có hợp lệ không (tồn tại và chưa hết hạn)
        isTokenValid: (state) => {
            return !!state.accessToken && !isTokenExpired(state.accessToken);
        },
        // Kiểm tra người dùng có role cụ thể
        hasRole: (state) => (roleName) => {
            return state.role === roleName;
        },
        // Kiểm tra có phải Admin không
        isAdmin: (state) => {
            return state.role === 'ROLE_ADMIN';
        },
        // Kiểm tra có phải Project Manager không
        isPM: (state) => {
            return state.role === 'ROLE_PM';
        },
        // Kiểm tra có phải Staff không
        isStaff: (state) => {
            return state.role === 'ROLE_STAFF';
        },
        // Kiểm tra có phải User (khách hàng) không
        isUser: (state) => {
            return state.role === 'ROLE_USER';
        },
        // Kiểm tra có quyền quản lý (Admin hoặc PM)
        canManage: (state) => {
            return state.role === 'ROLE_ADMIN' || state.role === 'ROLE_PM';
        },
        // Kiểm tra có quyền truy cập admin panel (Admin, PM, hoặc Staff)
        canAccessAdmin: (state) => {
            return ['ROLE_ADMIN', 'ROLE_PM', 'ROLE_STAFF'].includes(state.role);
        }
    },
    actions: {
        login(payload) {
            payload = payload.data
            this.isLoggedIn = true;
            this.user = {
                id: payload.userId,
                email: payload.email,
                fullName: payload.fullName
            };
            // backend may return either accessToken or accessTokenJwt
            this.accessToken = payload.accessToken || payload.accessTokenJwt;
            this.refreshToken = payload.refreshToken || payload.refreshTokenJwt;
            this.role = payload.role;
            localStorage.setItem('auth', JSON.stringify({
                user: this.user,
                accessToken: this.accessToken,
                refreshToken: this.refreshToken,
                role: this.role
            }));
        },
        updateTokens(accessToken, refreshToken) {
            this.accessToken = accessToken;
            if (refreshToken) {
                this.refreshToken = refreshToken;
            }
            localStorage.setItem('auth', JSON.stringify({
                user: this.user,
                accessToken: this.accessToken,
                refreshToken: this.refreshToken,
                role: this.role
            }));
        },
        logout() {
            this.isLoggedIn = false;
            this.user = null;
            this.accessToken = null;
            this.refreshToken = null;
            this.role = null;
            localStorage.removeItem('auth');
        },
        hydrate() {
            const cached = localStorage.getItem('auth');
            if (cached) {
                const data = JSON.parse(cached);
                this.user = data.user;
                this.accessToken = data.accessToken || data.accessTokenJwt;
                this.refreshToken = data.refreshToken || data.refreshTokenJwt;
                this.role = data.role;
                this.isLoggedIn = !!this.accessToken;
                
                // Chỉ xóa khi cả accessToken và refreshToken đều hết hạn
                const accessExpired = !this.accessToken || isTokenExpired(this.accessToken);
                const refreshExpired = !this.refreshToken || isTokenExpired(this.refreshToken);
                
                if (accessExpired && refreshExpired) {
                    console.warn('Both access and refresh tokens expired, clearing auth');
                    this.logout();
                } else if (accessExpired && !refreshExpired) {
                    console.log('Access token expired but refresh token is valid, will refresh on next API call');
                }
            }
        },
        // Kiểm tra và xóa token nếu cả 2 đều hết hạn
        validateAndCleanup() {
            const accessExpired = !this.accessToken || isTokenExpired(this.accessToken);
            const refreshExpired = !this.refreshToken || isTokenExpired(this.refreshToken);
            
            // Chỉ logout khi cả 2 đều hết hạn
            if (accessExpired && refreshExpired) {
                console.warn('Both tokens expired, logging out');
                this.logout();
                return false;
            }
            
            // Nếu accessToken hết hạn nhưng refreshToken còn -> vẫn OK
            if (accessExpired && !refreshExpired) {
                console.log('Access token expired, will auto-refresh on API call');
                return true;
            }
            
            return true;
        }
    }
}
)
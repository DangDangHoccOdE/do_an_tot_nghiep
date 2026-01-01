import { defineStore } from "pinia";

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
            }
        }
    }
}
)
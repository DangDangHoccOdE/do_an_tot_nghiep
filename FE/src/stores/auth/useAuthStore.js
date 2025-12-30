import { defineStore } from "pinia";

export const useAuthStore = defineStore('auth', {
    state: () => {
        const cached = typeof localStorage !== 'undefined' ? localStorage.getItem('auth') : null;
        const parsed = cached ? JSON.parse(cached) : null;
        return {
            isLoggedIn: !!parsed?.accessToken,
            user: parsed?.user || null,
            accessToken: parsed?.accessToken || null,
            refreshToken: parsed?.refreshToken || null,
            role: parsed?.role || null
        };
    },
    actions: {
        login(payload) {
            this.isLoggedIn = true;
            this.user = {
                id: payload.userId,
                email: payload.email,
                fullName: payload.fullName
            };
            this.accessToken = payload.accessTokenJwt;
            this.refreshToken = payload.refreshTokenJwt;
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
                this.accessToken = data.accessToken;
                this.refreshToken = data.refreshToken;
                this.role = data.role;
                this.isLoggedIn = !!data.accessToken;
            }
        }
    }
}
)
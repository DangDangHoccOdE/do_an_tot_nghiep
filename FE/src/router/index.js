// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import MainLayout from "@/layouts/MainLayout.vue";
import Login from "@/pages/Login.vue";
import Register from "@/pages/Register.vue";
import OAuth2RedirectHandler from "@/pages/OAuth2RedirectHandler.vue";
import AdminDashboard from "@/pages/AdminDashboard.vue";
import { useAuthStore } from "@/stores/auth/useAuthStore";

const routes = [
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/", component: Login},
  { path: "/admin", component: AdminDashboard, meta: { requiresAuth: true, roles: ["ROLE_ADMIN", "ROLE_PM", "ROLE_STAFF"], hideLayout: true } },
  {
    path: "/oauth2/redirect",
    component: OAuth2RedirectHandler
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  if (!auth.isLoggedIn) {
    auth.hydrate();
  }

  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return next(`/login?redirect=${to.fullPath}`);
  }

  if (to.meta.roles && auth.role && !to.meta.roles.includes(auth.role)) {
    return next('/');
  }

  next();
});

export default router;

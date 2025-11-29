// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import MainLayout from "@/layouts/MainLayout.vue";
import Login from "@/pages/Login.vue";

const routes = [
  { path: "/login", component: Login },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// router.beforeEach((to, from, next) => {
//   const auth = useAuthStore()
//   if (to.path !== '/login' && !auth.isLoggedIn) {
//     next('/login')
//   } else {
//     next()
//   }
// })

export default router;

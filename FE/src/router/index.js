// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import Login from "@/pages/Login.vue";
import Register from "@/pages/Register.vue";
import OAuth2RedirectHandler from "@/pages/OAuth2RedirectHandler.vue";
import AdminDashboard from "@/pages/AdminDashboard.vue";
import { useAuthStore } from "@/stores/auth/useAuthStore";
import ProjectsPage from "@/components/admin/pages/ProjectsPage.vue";
import TeamsPage from "@/components/admin/pages/TeamsPage.vue";
import TasksPage from "@/components/admin/pages/TasksPage.vue";
import MainLayout from "@/layouts/MainLayout.vue";

const routes = [
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/", component: MainLayout},
  {
    path: "/admin",
    component: AdminDashboard,
    meta: { requiresAuth: true, roles: ["ROLE_ADMIN", "ROLE_PM", "ROLE_STAFF"], hideLayout: true },
    children: [
      { path: "", redirect: { name: "admin-projects-current" } },
      {
        path: "projects/current",
        name: "admin-projects-current",
        component: ProjectsPage,
        props: { status: "current" },
        meta: { sectionKey: "current" }
      },
      {
        path: "projects/future",
        name: "admin-projects-future",
        component: ProjectsPage,
        props: { status: "future" },
        meta: { sectionKey: "future" }
      },
      {
        path: "teams",
        name: "admin-teams",
        component: TeamsPage,
        meta: { sectionKey: "teams" }
      },
      {
        path: "tasks",
        name: "admin-tasks",
        component: TasksPage,
        meta: { sectionKey: "tasks" }
      }
    ]
  },
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

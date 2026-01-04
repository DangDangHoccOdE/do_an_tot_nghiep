// src/router/index.js
import { createRouter, createWebHistory } from "vue-router";
import Login from "@/pages/Login.vue";
import Register from "@/pages/Register.vue";
import ActivateAccount from "@/pages/ActivateAccount.vue";
import OAuth2RedirectHandler from "@/pages/OAuth2RedirectHandler.vue";
import AdminDashboard from "@/pages/AdminDashboard.vue";
import MyProjects from "@/pages/MyProjects.vue";
import { useAuthStore } from "@/stores/auth/useAuthStore";
import ProjectsPage from "@/components/admin/pages/ProjectsPage.vue";
import TeamsPage from "@/components/admin/pages/TeamsPage.vue";
import TasksPage from "@/components/admin/pages/TasksPage.vue";
import ProjectFormPage from "@/components/admin/pages/ProjectFormPage.vue";
import TeamFormPage from "@/components/admin/pages/TeamFormPage.vue";
import TaskFormPage from "@/components/admin/pages/TaskFormPage.vue";
import UsersPage from "@/components/admin/pages/UsersPage.vue";
import UserFormPage from "@/components/admin/pages/UserFormPage.vue";
import CustomersPage from "@/components/admin/pages/CustomersPage.vue";
import CustomerFormPage from "@/components/admin/pages/CustomerFormPage.vue";
import StaffPage from "@/components/admin/pages/StaffPage.vue";
import StaffFormPage from "@/components/admin/pages/StaffFormPage.vue";
import MainLayout from "@/layouts/MainLayout.vue";

const routes = [
  { path: "/login", component: Login },
  { path: "/register", component: Register },
  { path: "/activate", component: ActivateAccount },
  { path: "/", component: MainLayout},
  {
    path: "/my-projects",
    component: MyProjects,
    meta: { requiresAuth: true }
  },
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
        path: "projects/new",
        name: "admin-projects-new",
        component: ProjectFormPage,
        props: { mode: "create" },
        meta: { sectionKey: "current" }
      },
      {
        path: "projects/:id/edit",
        name: "admin-projects-edit",
        component: ProjectFormPage,
        props: (route) => ({ id: route.params.id, mode: route.query.mode || "edit" }),
        meta: { sectionKey: "current" }
      },
      {
        path: "teams",
        name: "admin-teams",
        component: TeamsPage,
        meta: { sectionKey: "teams" }
      },
      {
        path: "teams/new",
        name: "admin-teams-new",
        component: TeamFormPage,
        props: { mode: "create" },
        meta: { sectionKey: "teams" }
      },
      {
        path: "teams/:id/edit",
        name: "admin-teams-edit",
        component: TeamFormPage,
        props: (route) => ({ id: route.params.id, mode: route.query.mode || "edit" }),
        meta: { sectionKey: "teams" }
      },
      {
        path: "tasks",
        name: "admin-tasks",
        component: TasksPage,
        meta: { sectionKey: "tasks" }
      },
      {
        path: "tasks/new",
        name: "admin-tasks-new",
        component: TaskFormPage,
        props: (route) => ({ projectId: route.query.projectId || null, mode: "create" }),
        meta: { sectionKey: "tasks" }
      },
      {
        path: "tasks/:id/edit",
        name: "admin-tasks-edit",
        component: TaskFormPage,
        props: (route) => ({ id: route.params.id, mode: route.query.mode || "edit" }),
        meta: { sectionKey: "tasks" }
      },
      {
        path: "users",
        name: "admin-users",
        component: UsersPage,
        meta: { sectionKey: "users" }
      },
      {
        path: "users/new",
        name: "admin-users-new",
        component: UserFormPage,
        props: { mode: "create" },
        meta: { sectionKey: "users" }
      },
      {
        path: "users/:id/edit",
        name: "admin-users-edit",
        component: UserFormPage,
        props: (route) => ({ id: route.params.id, mode: "edit" }),
        meta: { sectionKey: "users" }
      },
      {
        path: "customers",
        name: "admin-customers",
        component: CustomersPage,
        meta: { sectionKey: "customers" }
      },
      {
        path: "customers/new",
        name: "admin-customers-new",
        component: CustomerFormPage,
        props: { mode: "create" },
        meta: { sectionKey: "customers" }
      },
      {
        path: "customers/:id/edit",
        name: "admin-customers-edit",
        component: CustomerFormPage,
        props: (route) => ({ id: route.params.id, mode: "edit" }),
        meta: { sectionKey: "customers" }
      },
      {
        path: "staff",
        name: "admin-staff",
        component: StaffPage,
        meta: { sectionKey: "staff" }
      },
      {
        path: "staff/new",
        name: "admin-staff-new",
        component: StaffFormPage,
        props: { mode: "create" },
        meta: { sectionKey: "staff" }
      },
      {
        path: "staff/:id/edit",
        name: "admin-staff-edit",
        component: StaffFormPage,
        props: (route) => ({ id: route.params.id, mode: "edit" }),
        meta: { sectionKey: "staff" }
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
  
  // Hydrate store nếu chưa có thông tin
  if (!auth.isLoggedIn) {
    auth.hydrate();
  }

  // Kiểm tra và xóa token nếu cả 2 đều hết hạn
  auth.validateAndCleanup();

  const isAuthPage = ['/login', '/register', '/activate'].includes(to.path);
  
  // Nếu đã có khả năng authenticate (token còn hạn hoặc có refresh token) và đang cố vào trang login/register -> redirect về home
  if (isAuthPage && auth.canAuthenticate) {
    return next('/admin');
  }

  // Nếu trang yêu cầu auth nhưng không có khả năng authenticate (cả 2 token đều hết hạn)
  if (to.meta.requiresAuth && !auth.canAuthenticate) {
    return next(`/login?redirect=${to.fullPath}`);
  }

  // Kiểm tra phân quyền
  if (to.meta.roles && auth.role && !to.meta.roles.includes(auth.role)) {
    return next('/');
  }

  next();
});

export default router;

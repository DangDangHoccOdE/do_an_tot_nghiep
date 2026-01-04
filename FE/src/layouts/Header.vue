<template>
  <header class="bg-white sticky top-0 z-50 shadow-sm">
    <div
      class="flex justify-between items-center max-w-[1355px] w-[90vw] header-width m-auto text-base leading-7 max-md:px-5 responsive-desktop-f">
      <!-- Logo -->
      <router-link to="/" class="flex-shrink-0">
        <LuvinaLogo />
      </router-link>

      <!-- Desktop Menu -->
      <div class="hidden md:flex 2xl:gap-8 md:gap-4 items-center self-stretch my-auto min-w-[240px]">
        <div v-for="(menu, index) in menus" :key="index" class="relative group menu-item">
          <component :is="'a'" :href="menu.external ? menu.url : undefined" :to="menu.external ? undefined : menu.url"
            :target="menu.external ? '_blank' : undefined"
            class="title-menu flex gap-1 items-center min-h-[70px] text-[#4A4B4C] cursor-pointer no-underline"
            @mouseenter="activeMenu = index" @mouseleave="activeMenu = null">
            <div class="w-fit text-base transition-colors duration-200"
              :class="{ 'text-[#CE181E]': activeMenu === index }">
              {{ $t(menu.labelKey) }}
            </div>
            <template v-if="menu.submenus">
              <ArrowDownIcon class="w-4 h-4 transition-all duration-200" :class="activeMenu === index
                ? 'opacity-0 pointer-events-none'
                : 'opacity-100'
                " />

              <ArrowDownRedIcon class="w-4 h-4 hidden transition-all duration-200"
                :class="{ block: activeMenu === index }" />
            </template>
          </component>

          <!-- Dropdown -->
          <div v-if="menu.submenus"
            class="absolute top-full left-0 bg-white shadow-lg border border-gray-300 rounded-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-300 z-10 min-w-[250px] mt-1">
            <div class="flex flex-col p-4 space-y-3">
              <component v-for="(sub, i) in menu.submenus" :key="i" :is="'a'" :href="sub.external ? sub.url : undefined"
                :to="sub.external ? undefined : sub.url" :target="sub.external ? '_blank' : undefined"
                class="text-[#4A4B4C] font-bold text-base hover:text-[#CE181E] transition-colors duration-200 no-underline py-1">
                {{ $t(sub.labelKey) }}
              </component>
            </div>
          </div>
        </div>

        <!-- User menu / Login toggle -->
        <div v-if="auth.isLoggedIn" class="relative group user-menu ml-4">
          <button
            class="flex items-center gap-2 px-3 py-2 text-[#4A4B4C] hover:text-[#CE181E] transition-colors duration-200"
            @mouseenter="isUserMenuOpen = true" @mouseleave="isUserMenuOpen = false">
            <span
              class="w-8 h-8 bg-[#CE181E] text-white rounded-full flex items-center justify-center text-sm font-semibold">
              {{ userInitials }}
            </span>
            <span class="text-sm font-medium max-w-[120px] truncate">{{ auth.user?.fullName || auth.user?.email
            }}</span>
            <ChevronDownIcon class="w-4 h-4" />
          </button>

          <div v-show="isUserMenuOpen" @mouseenter="isUserMenuOpen = true" @mouseleave="isUserMenuOpen = false"
            class="absolute top-full left-0 bg-white shadow-lg border border-gray-200 rounded-lg min-w-[200px] mt-1 z-20">
            <div class="p-3 border-b border-gray-100">
              <p class="text-sm font-semibold text-[#4A4B4C] truncate">{{ auth.user?.fullName }}</p>
              <p class="text-xs text-gray-500 truncate">{{ auth.user?.email }}</p>
            </div>
            <div class="py-2">
              <router-link to="/my-projects"
                class="flex items-center gap-3 px-4 py-2 text-sm text-[#4A4B4C] hover:bg-gray-50 hover:text-[#CE181E] transition-colors no-underline"
                @click="isUserMenuOpen = false">
                <MyProjectsIcon />
                {{ $t("userMenu.myProjects") }}
              </router-link>
              <router-link v-if="hasAdminAccess" to="/admin"
                class="flex items-center gap-3 px-4 py-2 text-sm text-[#4A4B4C] hover:bg-gray-50 hover:text-[#CE181E] transition-colors no-underline"
                @click="isUserMenuOpen = false">
                <AdminGearIcon />
                {{ $t("userMenu.admin") }}
              </router-link>
              <button @click="handleLogout"
                class="flex items-center gap-3 w-full px-4 py-2 text-sm text-[#CE181E] hover:bg-red-50 transition-colors">
                <LogoutIcon />
                {{ $t("userMenu.logout") }}
              </button>
            </div>
          </div>
        </div>
        <router-link v-else to="/login"
          class="overflow-hidden gap-2.5 flex justify-center items-center self-stretch py-1 my-auto text-lg text-white bg-[#CE181E] hover:bg-[#f93535] rounded-lg h-[36px] w-[134px] cursor-pointer transition-colors duration-200 no-underline">
          {{ $t("auth.login.title") }}
        </router-link>
        <LanguageSwitcher />
      </div>

      <!-- Mobile Menu Button -->
      <button @click="toggleMobileMenu" class="md:hidden p-2 text-[#4A4B4C] focus:outline-none"
        :aria-label="$t('toggleMenu')">
        <MenuIcon v-if="!isMobileMenuOpen" class="w-6 h-6" />
        <CloseIcon v-else class="w-6 h-6" />
      </button>
    </div>

    <!-- Mobile Menu -->
    <div v-if="isMobileMenuOpen" class="md:hidden bg-white border-t border-gray-200 shadow-lg">
      <div class="flex flex-col">
        <!-- Mobile User Info (when logged in) -->
        <div v-if="auth.isLoggedIn" class="p-4 bg-gray-50 border-b border-gray-200">
          <div class="flex items-center gap-3">
            <span
              class="w-10 h-10 bg-[#CE181E] text-white rounded-full flex items-center justify-center text-sm font-semibold">
              {{ userInitials }}
            </span>
            <div>
              <p class="font-semibold text-[#4A4B4C]">{{ auth.user?.fullName }}</p>
              <p class="text-sm text-gray-500">{{ auth.user?.email }}</p>
            </div>
          </div>
          <div class="mt-3 flex gap-2">
            <router-link to="/my-projects"
              class="flex-1 text-center py-2 px-3 text-sm bg-white border border-gray-300 rounded-lg text-[#4A4B4C] hover:bg-gray-50 no-underline"
              @click="closeMobileMenu">
              {{ $t("userMenu.myProjects") }}
            </router-link>
            <button @click="handleLogout"
              class="flex-1 py-2 px-3 text-sm bg-[#CE181E] text-white rounded-lg hover:bg-[#f93535]">
              {{ $t("userMenu.logout") }}
            </button>
          </div>
        </div>

        <div v-for="(menu, index) in menus" :key="index" class="border-b border-gray-200">
          <button v-if="menu.submenus" @click="toggleSubmenu(index)"
            class="w-full flex justify-between items-center p-4 text-left text-[#4A4B4C] font-medium hover:bg-gray-50 transition-colors">
            <span>{{ $t(menu.labelKey) }}</span>
            <ChevronDownIcon class="w-5 h-5 transition-transform duration-200"
              :class="{ 'rotate-180': openSubmenu === index }" />
          </button>
          <component v-else :is="'a'" :href="menu.external ? menu.url : undefined"
            :to="menu.external ? undefined : menu.url" :target="menu.external ? '_blank' : undefined"
            class="block w-full p-4 text-left text-[#4A4B4C] font-medium hover:bg-gray-50 transition-colors no-underline"
            @click="closeMobileMenu">
            {{ $t(menu.labelKey) }}
          </component>

          <!-- Mobile Submenu -->
          <div v-if="menu.submenus && openSubmenu === index" class="bg-gray-50">
            <component v-for="(sub, i) in menu.submenus" :key="i" :is="'a'" :href="sub.external ? sub.url : undefined"
              :to="sub.external ? undefined : sub.url" :target="sub.external ? '_blank' : undefined"
              class="block px-8 py-3 text-[#4A4B4C] hover:text-[#CE181E] hover:bg-white transition-colors no-underline"
              @click="closeMobileMenu">
              {{ $t(sub.labelKey) }}
            </component>
          </div>
        </div>

        <!-- Mobile Login Button -->
        <router-link v-if="!auth.isLoggedIn" to="/login"
          class="mx-4 my-4 flex justify-center items-center py-2 text-lg text-white bg-[#CE181E] hover:bg-[#f93535] rounded-lg transition-colors no-underline"
          @click="closeMobileMenu">
          {{ $t("auth.login.title") }}
        </router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth/useAuthStore";
import LuvinaLogo from "@/components/icons/LuvinaLogo.vue";
import ArrowDownIcon from "@/components/icons/ArrowDownIcon.vue";
import ArrowDownRedIcon from "@/components/icons/ArrowDownRedIcon.vue";
import MenuIcon from "@/components/icons/MenuIcon.vue";
import CloseIcon from "@/components/icons/CloseIcon.vue";
import ChevronDownIcon from "@/components/icons/ChevronDownIcon.vue";
import MyProjectsIcon from "@/components/icons/MyProjectsIcon.vue";
import AdminGearIcon from "@/components/icons/AdminGearIcon.vue";
import LogoutIcon from "@/components/icons/LogoutIcon.vue";
import LanguageSwitcher from "@/components/common/LanguageSwitcher.vue";

const { t } = useI18n();
const router = useRouter();
const auth = useAuthStore();

const activeMenu = ref(null);
const isMobileMenuOpen = ref(false);
const openSubmenu = ref(null);
const isUserMenuOpen = ref(false);

const userInitials = computed(() => {
  if (auth.user?.fullName) {
    const names = auth.user.fullName.split(' ');
    if (names.length >= 2) {
      return (names[0][0] + names[names.length - 1][0]).toUpperCase();
    }
    return auth.user.fullName.substring(0, 2).toUpperCase();
  }
  if (auth.user?.email) {
    return auth.user.email.substring(0, 2).toUpperCase();
  }
  return 'U';
});

const hasAdminAccess = computed(() => {
  return ['ROLE_ADMIN', 'ROLE_PM', 'ROLE_STAFF'].includes(auth.role);
});

const handleLogout = () => {
  auth.logout();
  isUserMenuOpen.value = false;
  closeMobileMenu();
  router.push('/');
};

const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
  if (!isMobileMenuOpen.value) {
    openSubmenu.value = null;
  }
};

const closeMobileMenu = () => {
  isMobileMenuOpen.value = false;
  openSubmenu.value = null;
};

const toggleSubmenu = (index) => {
  openSubmenu.value = openSubmenu.value === index ? null : index;
};

const menus = computed(() => [
  {
    labelKey: "menus.overview.label",
    url: "",
    external: false,
    submenus: [
      {
        labelKey: "menus.overview.submenus.companyProfile",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.overview.submenus.whyVietnam",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.overview.submenus.whyLuvina",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.overview.submenus.aboutUs",
        url: "",
        external: false,
      },
    ],
  },
  {
    labelKey: "menus.services.label",
    url: "/",
    external: false,
    submenus: [
      {
        labelKey: "menus.services.submenus.strategyConsulting",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.services.submenus.softwareDevelopment",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.services.submenus.softwareMigration",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.services.submenus.cloudTransformation",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.services.submenus.itManagement",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.services.submenus.qaTesting",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.services.submenus.cyberSecurity",
        url: "",
        external: false,
      },
    ],
  },
  {
    labelKey: "menus.solutions.label",
    url: "",
    external: false,
    submenus: [
      {
        labelKey: "menus.solutions.submenus.erp",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.solutions.submenus.crm",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.solutions.submenus.cms",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.solutions.submenus.ecommerce",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.solutions.submenus.pos",
        url: "",
        external: false,
      },
    ],
  },
  {
    labelKey: "menus.business.label",
    url: "",
    external: false,
    submenus: [
      {
        labelKey: "menus.business.submenus.fintech",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.business.submenus.healthcare",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.business.submenus.ecommerce",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.business.submenus.education",
        url: "",
        external: false,
      },
    ],
  },
  {
    labelKey: "menus.news.label",
    url: "",
    external: false,
  },
  {
    labelKey: "menus.resources.label",
    url: "",
    external: false,
    submenus: [
      {
        labelKey: "menus.resources.submenus.blog",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.resources.submenus.caseStudy",
        url: "",
        external: false,
      },
      {
        labelKey: "menus.resources.submenus.whitepaper",
        url: "",
        external: false,
      },
    ],
  },
]);
</script>

<style scoped>
.menu-item {
  transition: all 0.2s ease;
}

.title-menu:hover {
  color: #ce181e;
}

/* Smooth dropdown animation */
.group:hover .absolute {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

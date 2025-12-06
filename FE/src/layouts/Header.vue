<template>
  <header class="bg-white sticky top-0 z-50 shadow-sm">
    <div
      class="flex justify-between items-center max-w-[1355px] w-[90vw] header-width m-auto text-base leading-7 max-md:px-5 responsive-desktop-f"
    >
      <!-- Logo -->
      <router-link to="/" class="flex-shrink-0">
        <LuvinaLogo />
      </router-link>

      <!-- Desktop Menu -->
      <div
        class="hidden md:flex 2xl:gap-8 md:gap-4 items-center self-stretch my-auto min-w-[240px]"
      >
        <div
          v-for="(menu, index) in menus"
          :key="index"
          class="relative group menu-item"
        >
          <component
            :is="menu.external ? 'a' : 'router-link'"
            :href="menu.external ? menu.url : undefined"
            :to="menu.external ? undefined : menu.url"
            :target="menu.external ? '_blank' : undefined"
            class="title-menu flex gap-1 items-center min-h-[70px] text-[#4A4B4C] cursor-pointer no-underline"
            @mouseenter="activeMenu = index"
            @mouseleave="activeMenu = null"
          >
            <div
              class="w-fit text-base transition-colors duration-200"
              :class="{ 'text-[#CE181E]': activeMenu === index }"
            >
              {{ $t(menu.labelKey) }}
            </div>
            <template v-if="menu.submenus">
              <ArrowDownIcon
                class="w-4 h-4 transition-all duration-200"
                :class="
                  activeMenu === index
                    ? 'opacity-0 pointer-events-none'
                    : 'opacity-100'
                "
              />

              <ArrowDownRedIcon
                class="w-4 h-4 hidden transition-all duration-200"
                :class="{ block: activeMenu === index }"
              />
            </template>
          </component>

          <!-- Dropdown -->
          <div
            v-if="menu.submenus"
            class="absolute top-full left-0 bg-white shadow-lg border border-gray-300 rounded-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-300 z-10 min-w-[250px] mt-1"
          >
            <div class="flex flex-col p-4 space-y-3">
              <component
                v-for="(sub, i) in menu.submenus"
                :key="i"
                :is="sub.external ? 'a' : 'router-link'"
                :href="sub.external ? sub.url : undefined"
                :to="sub.external ? undefined : sub.url"
                :target="sub.external ? '_blank' : undefined"
                class="text-[#4A4B4C] font-bold text-base hover:text-[#CE181E] transition-colors duration-200 no-underline py-1"
              >
                {{ $t(sub.labelKey) }}
              </component>
            </div>
          </div>
        </div>

        <!-- Contact Button -->
        <router-link
          to="/contact"
          class="overflow-hidden gap-2.5 flex justify-center items-center self-stretch py-1 my-auto text-lg text-white bg-[#CE181E] hover:bg-[#f93535] rounded-lg h-[36px] w-[134px] cursor-pointer transition-colors duration-200 no-underline"
        >
          {{ $t("header.contact") }}
        </router-link>
      </div>

      <!-- Mobile Menu Button -->
      <button
        @click="toggleMobileMenu"
        class="md:hidden p-2 text-[#4A4B4C] focus:outline-none"
        :aria-label="$t('header.toggleMenu')"
      >
        <MenuIcon v-if="!isMobileMenuOpen" class="w-6 h-6" />
        <CloseIcon v-else class="w-6 h-6" />
      </button>
    </div>

    <!-- Mobile Menu -->
    <div
      v-if="isMobileMenuOpen"
      class="md:hidden bg-white border-t border-gray-200 shadow-lg"
    >
      <div class="flex flex-col">
        <div
          v-for="(menu, index) in menus"
          :key="index"
          class="border-b border-gray-200"
        >
          <button
            v-if="menu.submenus"
            @click="toggleSubmenu(index)"
            class="w-full flex justify-between items-center p-4 text-left text-[#4A4B4C] font-medium hover:bg-gray-50 transition-colors"
          >
            <span>{{ $t(menu.labelKey) }}</span>
            <ChevronDownIcon
              class="w-5 h-5 transition-transform duration-200"
              :class="{ 'rotate-180': openSubmenu === index }"
            />
          </button>
          <component
            v-else
            :is="menu.external ? 'a' : 'router-link'"
            :href="menu.external ? menu.url : undefined"
            :to="menu.external ? undefined : menu.url"
            :target="menu.external ? '_blank' : undefined"
            class="block w-full p-4 text-left text-[#4A4B4C] font-medium hover:bg-gray-50 transition-colors no-underline"
            @click="closeMobileMenu"
          >
            {{ $t(menu.labelKey) }}
          </component>

          <!-- Mobile Submenu -->
          <div v-if="menu.submenus && openSubmenu === index" class="bg-gray-50">
            <component
              v-for="(sub, i) in menu.submenus"
              :key="i"
              :is="sub.external ? 'a' : 'router-link'"
              :href="sub.external ? sub.url : undefined"
              :to="sub.external ? undefined : sub.url"
              :target="sub.external ? '_blank' : undefined"
              class="block px-8 py-3 text-[#4A4B4C] hover:text-[#CE181E] hover:bg-white transition-colors no-underline"
              @click="closeMobileMenu"
            >
              {{ $t(sub.labelKey) }}
            </component>
          </div>
        </div>

        <!-- Mobile Contact Button -->
        <router-link
          to="/contact"
          class="mx-4 my-4 flex justify-center items-center py-2 text-lg text-white bg-[#CE181E] hover:bg-[#f93535] rounded-lg transition-colors no-underline"
          @click="closeMobileMenu"
        >
          {{ $t("header.contact") }}
        </router-link>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import LuvinaLogo from "@/components/icons/LuvinaLogo.vue";
import ArrowDownIcon from "@/components/icons/ArrowDownIcon.vue";
import ArrowDownRedIcon from "@/components/icons/ArrowDownRedIcon.vue";
import MenuIcon from "@/components/icons/MenuIcon.vue";
import CloseIcon from "@/components/icons/CloseIcon.vue";
import ChevronDownIcon from "@/components/icons/ChevronDownIcon.vue";

const { t } = useI18n();

const activeMenu = ref(null);
const isMobileMenuOpen = ref(false);
const openSubmenu = ref(null);

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
    labelKey: "header.menus.overview.label",
    url: "/overview",
    external: false,
    submenus: [
      {
        labelKey: "header.menus.overview.submenus.companyProfile",
        url: "/company-profile",
        external: false,
      },
      {
        labelKey: "header.menus.overview.submenus.whyVietnam",
        url: "/why-viet-nam",
        external: false,
      },
      {
        labelKey: "header.menus.overview.submenus.whyLuvina",
        url: "/why-luvina",
        external: false,
      },
      {
        labelKey: "header.menus.overview.submenus.aboutUs",
        url: "/about-us",
        external: false,
      },
    ],
  },
  {
    labelKey: "header.menus.services.label",
    url: "/services",
    external: false,
    submenus: [
      {
        labelKey: "header.menus.services.submenus.strategyConsulting",
        url: "/services/it-strategy-consulting",
        external: false,
      },
      {
        labelKey: "header.menus.services.submenus.softwareDevelopment",
        url: "/services/software-development",
        external: false,
      },
      {
        labelKey: "header.menus.services.submenus.softwareMigration",
        url: "/services/software-migration-modernization",
        external: false,
      },
      {
        labelKey: "header.menus.services.submenus.cloudTransformation",
        url: "/services/cloud-transformation",
        external: false,
      },
      {
        labelKey: "header.menus.services.submenus.itManagement",
        url: "/services/it-management-services",
        external: false,
      },
      {
        labelKey: "header.menus.services.submenus.qaTesting",
        url: "/services/qa-software-testing-service",
        external: false,
      },
      {
        labelKey: "header.menus.services.submenus.cyberSecurity",
        url: "/services/cyber-security-services",
        external: false,
      },
    ],
  },
  {
    labelKey: "header.menus.solutions.label",
    url: "/solutions",
    external: false,
    submenus: [
      {
        labelKey: "header.menus.solutions.submenus.erp",
        url: "/solutions/erp",
        external: false,
      },
      {
        labelKey: "header.menus.solutions.submenus.crm",
        url: "/solutions/crm",
        external: false,
      },
      {
        labelKey: "header.menus.solutions.submenus.cms",
        url: "/solutions/cms",
        external: false,
      },
      {
        labelKey: "header.menus.solutions.submenus.ecommerce",
        url: "/solutions/e-commerce",
        external: false,
      },
      {
        labelKey: "header.menus.solutions.submenus.pos",
        url: "/solutions/pos",
        external: false,
      },
    ],
  },
  {
    labelKey: "header.menus.business.label",
    url: "/business",
    external: false,
    submenus: [
      {
        labelKey: "header.menus.business.submenus.fintech",
        url: "/business/fintech",
        external: false,
      },
      {
        labelKey: "header.menus.business.submenus.healthcare",
        url: "/business/healthcare",
        external: false,
      },
      {
        labelKey: "header.menus.business.submenus.ecommerce",
        url: "/business/e-commerce",
        external: false,
      },
      {
        labelKey: "header.menus.business.submenus.education",
        url: "/business/education",
        external: false,
      },
    ],
  },
  {
    labelKey: "header.menus.news.label",
    url: "/news",
    external: false,
  },
  {
    labelKey: "header.menus.resources.label",
    url: "/resources",
    external: false,
    submenus: [
      {
        labelKey: "header.menus.resources.submenus.blog",
        url: "/resources/blog",
        external: false,
      },
      {
        labelKey: "header.menus.resources.submenus.caseStudy",
        url: "/resources/case-study",
        external: false,
      },
      {
        labelKey: "header.menus.resources.submenus.whitepaper",
        url: "/resources/whitepaper",
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

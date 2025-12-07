<template>
  <footer id="footer" class="footer-wrapper bg-[#EFEFF0]">
    <div class="lg:px-0 px-4 py-16 mx-auto max-w-[1354px] custom-footer">
      <div class="grid grid-cols-1 md:gap-8 gap-2 lg:grid-cols-2 footer-top custom-footer-cols">
        <!-- Left Column -->
        <div class="lg:grid lg:grid-cols-2">
          <div class="lg:max-w-[320px]">
            <div class="flex flex-col justify-between h-full">
              <div>
                <div>
                  <img
                    src="https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/logo_luvina_footer.png"
                    alt="Luvina logo"
                    class="md:w-[225px] md:h-[109px] w-[112px] h-[55px] customer-footer-logo"
                  />
                </div>
                <div class="text-[#70706F] my-8 font-normal md:text-base text-[12px] leading-[20.4px] footer-overview-offices">
                  {{ $t('overview') }}
                </div>
              </div>

              <div class="flex responsive-desktop-f space-x-2 text-gray-600 container-linked items-center">
                <a
                  v-for="link in socialLinks"
                  :key="link.name"
                  :href="link.url"
                  target="_blank"
                  rel="noreferrer"
                  class="hover:opacity-75"
                >
                  <span class="sr-only">{{ link.name }}</span>
                  <component :is="link.icon" />
                </a>
              </div>
            </div>
          </div>

          <!-- Navigation Links -->
          <div>
            <nav class="grid grid-cols-3 gap-4 lg:flex lg:flex-col lg:space-y-2 md:text-lg text-[14px] leading-[21px] text-[#6D6E71] font-bold lg:ml-16 footer-link-page">
              <router-link
                v-for="nav in navLinks"
                :key="nav.key"
                :to="nav.url"
                class="hover:opacity-75"
              >
                {{ $t(nav.labelKey) }}
              </router-link>
            </nav>
          </div>
        </div>

        <!-- Right Column (Offices) -->
        <div class="lg:grid lg:grid-cols-2">
          <div class="lg:max-w-[312px]">
            <div class="md:mt-0 mt-4">
              <div class="expansion-panels">
                <p class="font-bold md:mb-4 mb-[30px] md:text-xl text-base text-black footer-title-office">
                  {{ $t('offices') }}
                </p>

                <!-- Vietnam Office -->
                <div
                  v-for="(office, index) in offices"
                  :key="index"
                  class="expansion-panel mb-0"
                >
                  <div
                    @click="togglePanel(index)"
                    class="flex items-center gap-4 justify-between border-expansion-panels h-[54px] cursor-pointer"
                  >
                    <div class="flex items-center">
                      <component :is="office.flagIcon" />
                      <span class="ml-4 text-black font-bold md:text-xl text-base footer-sub-title-office">
                        {{ $t(office.nameKey) }}
                      </span>
                    </div>
                    <div class="ml-20 mt-2 justify-items-center text-center toggle-panel">
                      <ChevronUpIcon :is-down="activePanel !== index" />
                    </div>
                  </div>

                  <div
                    v-show="activePanel === index"
                    class="expansion-panel-content px-0"
                    :class="{ 'rotate-icon active': activePanel === index }"
                  >
                    <!-- Render office details -->
                    <template v-for="(branch, branchKey) in office.branches" :key="branchKey">
                      <p class="font-bold md:text-[18px] md:leading-[27px] text-[16px] leading-[24px] text-[#CE181E] pl-4 pt-[12px] pb-2">
                        {{ $t(branch.titleKey) }}
                      </p>
                      <p class="flex items-center">
                        <span class="mr-2">
                          <LocationPinIcon />
                        </span>
                        <span class="text-xs text-[#4A4B4C]">
                          {{ $t(branch.addressKey) }}
                        </span>
                      </p>

                      <!-- Specific Contact -->
                      <template v-if="branch.specificContact">
                        <div class="pl-4 pt-4">
                          <p class="text-[#4A4B4C] lg:text-[14px] text-base leading-[21px] font-bold">
                            {{ $t(branch.specificContact.titleKey) }}
                          </p>
                          <p v-if="branch.specificContact.contactPerson" class="text-[#4A4B4C] text-[12px] leading-[20.4px]">
                            {{ $t(branch.specificContact.contactPersonKey) }}
                            {{ $t(branch.specificContact.contactPersonNameKey) }}
                          </p>
                          <p v-if="branch.specificContact.email" class="text-[#4A4B4C] text-[12px] leading-[20.4px]">
                            {{ $t(branch.specificContact.emailKey) }} {{ branch.specificContact.emailValue }}
                          </p>
                          <p v-if="branch.specificContact.tel" class="text-[#4A4B4C] text-[12px] leading-[20.4px]">
                            {{ $t(branch.specificContact.telKey) }} {{ branch.specificContact.telValue }}
                          </p>
                        </div>
                      </template>

                      <!-- General Contact -->
                      <template v-if="branch.generalContact">
                        <div class="pl-4 pt-4">
                          <p class="text-[#4A4B4C] lg:text-[14px] text-base leading-[21px] font-bold">
                            {{ $t(branch.generalContact.titleKey) }}
                          </p>
                          <p v-if="branch.generalContact.email" class="text-[#4A4B4C] text-[12px] leading-[20.4px]">
                            {{ $t(branch.generalContact.emailKey) }} {{ branch.generalContact.emailValue }}
                          </p>
                          <p v-if="branch.generalContact.tel" class="text-[#4A4B4C] text-[12px] leading-[20.4px]">
                            {{ $t(branch.generalContact.telKey) }} {{ branch.generalContact.telValue }}
                          </p>
                          <p v-if="branch.generalContact.fax" class="text-[#4A4B4C] text-[12px] leading-[20.4px]">
                            {{ $t(branch.generalContact.faxKey) }} {{ branch.generalContact.faxValue }}
                          </p>
                        </div>
                      </template>

                      <!-- Simple Contact -->
                      <template v-if="branch.contact">
                        <div class="pl-4 pt-4">
                          <p class="text-[#4A4B4C] lg:text-[14px] text-base leading-[21px] font-bold">
                            {{ $t(branch.contact.titleKey) }}
                          </p>
                          <p v-if="branch.contact.contactPerson" class="text-[#4A4B4C] text-[12px] leading-[20.4px]">
                            {{ $t(branch.contact.contactPersonKey) }} {{ $t(branch.contact.contactPersonNameKey) }}
                          </p>
                          <p v-if="branch.contact.tel" class="text-[#4A4B4C] text-[12px] leading-[20.4px]">
                            {{ $t(branch.contact.telKey) }} {{ branch.contact.telValue }}
                          </p>
                        </div>
                      </template>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Contact Section -->
          <div class="lg:pt-0 pt-[37px]">
            <div class="md:ml-20 relative footer-pc-display customer-contact-footer">
              <p class="font-bold text-xl footer-contact-title">
                {{ $t('contact') }}
              </p>
              <nav class="flex flex-col mt-4 space-y-2 text-sm text-black font-normal">
                <a class="hover:opacity-75">
                  {{ $t('infoLuvina') }}
                </a>
                <p class="text-[#4A4B4C]">{{ $t('contactForCareer') }}</p>
                <a class="hover:opacity-75">
                  {{ $t('phoneLuvina') }}
                </a>
              </nav>
            </div>

            <div class="lg:ml-20 relative block lg:hidden">
              <p class="font-bold text-base md:text-xl">
                {{ $t('contactMobile') }}
              </p>
              <nav class="flex flex-col mt-4 space-y-2 text-sm text-[#70706F] font-normal">
                <a class="hover:opacity-75" href="">
                  {{ $t('infoLuvina') }}
                </a>
                <a class="hover:opacity-75" href="">
                  {{ $t('careerLuvina') }}
                </a>
              </nav>
            </div>

            <!-- Mobile Social Links -->
            <div class="responsive-mobile-f hidden-screen-1024 mt-[37px] space-x-2 text-gray-600">
              <!-- <a
                v-for="link in socialLinks"
                :key="link.name"
                :href="link.url"
                target="_blank"
                rel="noreferrer"
                class="hover:opacity-75"
              >
                <span class="sr-only">{{ link.name }}</span>
                <component :is="link.icon" />
              </a> -->
            </div>
          </div>
        </div>
      </div>

      <el-divider class="mb-[2px]" />

      <!-- Copyright & Privacy -->
      <div class="text-[#000000] font-normal lg:flex lg:justify-between custom-border-footer">
        <div class="flex justify-center text-[10px] leading-[13.56px] h-[14px] lg:justify-start lg:text-[16px] lg:leading-[27.2px] lg:h-[17px]">
          {{ $t('copyright') }}
        </div>
        <router-link
          to="/"
          class="flex justify-center text-[10px] leading-[13.56px] h-[14px] lg:justify-start lg:text-[16px] lg:leading-[27.2px] lg:h-[17px] hover:text-[#4A4B4C]"
        >
          {{ $t('privacyPolicy') }}
        </router-link>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import FacebookIcon from '@/components/icons/FacebookIcon.vue';
import LinkedInIcon from '@/components/icons/LinkedInIcon.vue';
import YouTubeIcon from '@/components/icons/YouTubeIcon.vue';
import LocationPinIcon from '@/components/icons/LocationPinIcon.vue';
import ChevronUpIcon from '@/components/icons/ChevronUpIcon.vue';
import VietnamFlagIcon from '@/components/icons/VietnamFlagIcon.vue';
import JapanFlagIcon from '@/components/icons/JapanFlagIcon.vue';

const { t } = useI18n();

const activePanel = ref(0); // Default open first panel

const togglePanel = (index) => {
  activePanel.value = activePanel.value === index ? null : index;
};

const socialLinks = [
  {
    name: 'Facebook',
    url: '',
    icon: FacebookIcon,
  },
  {
    name: 'LinkedIn',
    url: '',
    icon: LinkedInIcon,
  },
  {
    name: 'YouTube',
    url: '',
    icon: YouTubeIcon,
  },
];

const navLinks = computed(() => [
  { key: 'home', labelKey: 'navigation.home', url: '/' },
  { key: 'services', labelKey: 'navigation.services', url: '/' },
  { key: 'solutions', labelKey: 'navigation.solutions', url: '/' },
  { key: 'business', labelKey: 'navigation.business', url: '/' },
  { key: 'projects', labelKey: 'navigation.projects', url: '/' },
  { key: 'news', labelKey: 'navigation.news', url: '/' },
  { key: 'aboutUs', labelKey: 'navigation.aboutUs', url: '/' },
  { key: 'resources', labelKey: 'navigation.resources', url: '/' },
]);

const offices = computed(() => [
  {
    nameKey: 'officesData.vietnam.name',
    flagIcon: VietnamFlagIcon,
    branches: {
      hanoi: {
        titleKey: 'officesData.vietnam.hanoi.title',
        addressKey: 'officesData.vietnam.hanoi.address',
        specificContact: {
          titleKey: 'officesData.vietnam.hanoi.specificContact',
          contactPersonKey: 'officesData.vietnam.hanoi.contactPerson',
          contactPersonNameKey: 'officesData.vietnam.hanoi.contactPersonName',
          emailKey: 'officesData.vietnam.hanoi.email',
          emailValue: 'btbquyen@luvina.net',
          telKey: 'officesData.vietnam.hanoi.tel',
          telValue: '(84) (24) 3793 1103 (ext 113)',
        },
        generalContact: {
          titleKey: 'officesData.vietnam.hanoi.generalContact',
          emailKey: 'officesData.vietnam.hanoi.generalEmail',
          emailValue: 'info@luvina.net',
          telKey: 'officesData.vietnam.hanoi.generalTel',
          telValue: '(84) (24) 3793 1103 (ext 0)',
          faxKey: 'officesData.vietnam.hanoi.fax',
          faxValue: '(84) (24) 3793 1106',
        },
      },
      danang: {
        titleKey: 'officesData.vietnam.danang.title',
        addressKey: 'officesData.vietnam.danang.address',
        generalContact: {
          titleKey: 'officesData.vietnam.danang.generalContact',
          telKey: 'officesData.vietnam.danang.tel',
          telValue: '(84) (236) 388 8718',
          faxKey: 'officesData.vietnam.danang.fax',
          faxValue: '(84) (236) 388 8721',
        },
      },
      hcm: {
        titleKey: 'officesData.vietnam.hcm.title',
        addressKey: 'officesData.vietnam.hcm.address',
        contact: {
          titleKey: 'officesData.vietnam.hcm.contact',
          contactPersonKey: 'officesData.vietnam.hcm.contactPerson',
          contactPersonNameKey: 'officesData.vietnam.hcm.contactPersonName',
          telKey: 'officesData.vietnam.hcm.tel',
          telValue: '(84) 903 054 065',
        },
      },
    },
  },
  {
    nameKey: 'officesData.japan.name',
    flagIcon: JapanFlagIcon,
    branches: {
      tokyo: {
        titleKey: 'officesData.japan.tokyo.title',
        addressKey: 'officesData.japan.tokyo.address',
        specificContact: {
          titleKey: 'officesData.japan.tokyo.specificContact',
          contactPersonKey: 'officesData.japan.tokyo.contactPerson',
          contactPersonNameKey: 'officesData.japan.tokyo.contactPersonName',
          emailKey: 'officesData.japan.tokyo.email',
          emailValue: 'yokohama@luvina.jp',
          telKey: 'officesData.japan.tokyo.tel',
          telValue: '(81) (44) 271 7080',
        },
        generalContact: {
          titleKey: 'officesData.japan.tokyo.generalContact',
          emailKey: 'officesData.japan.tokyo.generalEmail',
          emailValue: 'info@luvina.jp',
          telKey: 'officesData.japan.tokyo.generalTel',
          telValue: '(81) (44) 271 7080',
        },
      },
    },
  },
  {
    nameKey: 'officesData.usa.name',
    flagIcon: 'img', // USA flag is an image
    branches: {
      vdx: {
        titleKey: 'officesData.usa.vdx.title',
        addressKey: 'officesData.usa.vdx.address',
        generalContact: {
          titleKey: 'officesData.usa.vdx.generalContact',
          emailKey: 'officesData.usa.vdx.email',
          emailValue: 'Info@vdx-usa.com',
          telKey: 'officesData.usa.vdx.tel',
          telValue: '+1 (408) 805-2148',
        },
      },
    },
  },
]);
</script>

<style scoped>
/* Custom styles if needed */
.expansion-panel-content {
  transition: all 0.3s ease;
}

</style>

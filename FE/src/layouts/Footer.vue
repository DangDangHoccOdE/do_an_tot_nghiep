<template>
  <footer class="lg:px-0 px-4 py-16 mx-auto max-w-[1354px] custom-footer">
    <div class="grid grid-cols-1 md:gap-8 gap-2 lg:grid-cols-2 footer-top custom-footer-cols">
      
      <!-- Left Column -->
      <div class="lg:grid lg:grid-cols-2">
        <div class="lg:max-w-[320px]">
          <div class="flex flex-col justify-between h-full">
            <!-- Logo & Overview -->
            <div>
              <img src="https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/logo_luvina_footer.png"
                   alt="Luvina logo"
                   class="md:w-[225px] md:h-[109px] w-[112px] h-[55px] customer-footer-logo" />
              <div class="text-[#70706F] my-8 font-normal md:text-base text-[12px] leading-[20.4px] footer-overview-offices">
                Top 10 công ty Gia công phần mềm Việt Nam với hơn 750 nhân tài phục vụ thị trường toàn cầu kể từ năm 2004.
              </div>
            </div>

            <!-- Social Media Links -->
            <div class="responsive-desktop-f mt-[350px] space-x-2 text-gray-600 container-linked">
              <a v-for="link in socialLinks" :key="link.name" :href="link.url" target="_blank" rel="noreferrer" class="hover:opacity-75">
                <span class="sr-only">{{ link.name }}</span>
                <component :is="link.icon" class="w-8 h-8" />
              </a>
            </div>
          </div>
        </div>

        <!-- Navigation Links -->
        <div>
          <nav class="grid grid-cols-3 gap-4 lg:flex lg:flex-col lg:space-y-2 md:text-lg text-[14px] leading-[21px] text-[#6D6E71] font-bold lg:ml-16 footer-link-page">
            <a v-for="nav in navLinks" :key="nav.name" :href="nav.url" class="hover:opacity-75">{{ nav.name }}</a>
          </nav>
        </div>
      </div>

      <!-- Right Column (Offices) -->
      <div class="lg:grid lg:grid-cols-2">
        <div class="lg:max-w-[312px]">
          <p class="font-bold md:mb-4 mb-[30px] md:text-xl text-base text-black footer-title-office">
            VĂN PHÒNG
          </p>

          <div v-for="(office, index) in offices" :key="index" class="expansion-panel mb-0">
            <div @click="togglePanel(index)"
                 class="flex items-center gap-4 justify-between border-expansion-panels h-[54px] cursor-pointer">
              <div class="flex items-center">
                <img v-if="office.flag" :src="office.flag" class="w-[40px] h-[24px]" alt="flag" />
                <svg v-else width="40" height="24" viewBox="0 0 40 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect width="40" height="24" rx="4" :fill="office.svgColor"></rect>
                  <path :d="office.svgPath" fill="#F4EB32"></path>
                </svg>
                <span class="ml-4 text-black font-bold md:text-xl text-base footer-sub-title-office">{{ office.name }}</span>
              </div>
              <div class="toggle-panel">
                <svg class="footer-svg-panel" width="17" height="10" viewBox="0 0 17 10" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M0.70874 9.29902L8.56488 1.41113L16.3012 9.16889" stroke="#4A4B4C" stroke-width="2" stroke-miterlimit="10" />
                </svg>
              </div>
            </div>

            <div v-show="activePanel === index" class="expansion-panel-content px-0">
              <p v-for="info in office.details" :key="info.title" class="pl-4 pt-4 text-[#4A4B4C] text-[12px] leading-[20.4px]">
                <span class="font-bold" v-if="info.title">{{ info.title }}</span> {{ info.content }}
              </p>
            </div>
          </div>

        </div>
      </div>

    </div>
  </footer>
</template>

<script setup>
import { ref } from 'vue'

// Social media links (you can replace <FacebookIcon />, etc. with inline SVGs or separate components)
const socialLinks = [
  { name: 'Facebook', url: 'https://www.facebook.com/LuvinaSoftware', icon: 'FacebookIcon' },
  { name: 'Linkedin', url: 'https://www.linkedin.com/company/luvina-software-outsourcing', icon: 'LinkedinIcon' },
  { name: 'Youtube', url: 'https://www.youtube.com/@luvinasoftware', icon: 'YoutubeIcon' }
]

const navLinks = [
  { name: 'Trang chủ', url: 'https://luvina.net/vi/' },
  { name: 'Dịch vụ', url: 'https://luvina.net/vi/services/' },
  { name: 'Giải pháp', url: 'https://luvina.net/vi/solutions/' },
  { name: 'Nghiệp vụ', url: 'https://luvina.net/vi/industries/' },
  { name: 'Dự án', url: 'https://luvina.net/vi/case-studies/' },
  { name: 'Tin tức', url: 'https://luvina.net/vi/news/' },
  { name: 'Về chúng tôi', url: 'https://luvina.net/vi/about-us/' },
  { name: 'Nguồn lực', url: 'https://luvina.net/vi/develop-human-capital/' }
]

// Offices
const offices = [
  {
    name: 'Việt Nam',
    flag: null,
    svgColor: '#C8202C',
    svgPath: 'M22.825 13.7L24.571 19.228 20 15.808 15.429 19.228 17.175 13.7 12.604 10.29h5.65L20 4.762l1.746 5.528h5.65l-4.571 3.41Z',
    details: [
      { title: 'Trụ sở Hà Nội', content: 'Tháp văn phòng Hòa Bình - số 106 Hoàng Quốc Việt, phường Nghĩa Đô, TP Hà Nội' },
      { title: 'Người phụ trách', content: 'Bùi Thị Bích Quyên' },
      { title: 'EMAIL', content: 'btbquyen@luvina.net' },
      { title: 'TEL', content: '(84) (24) 3793 1103 (ext 113)' }
    ]
  },
  {
    name: 'Nhật Bản',
    flag: null,
    svgColor: '#000',
    svgPath: '', // nếu muốn dùng SVG cờ thì điền path
    details: [
      { title: 'Chi nhánh', content: 'R612, Kanagawa Science Pask (KSP), 3-2-1 Sakado, Takatsu-ku, Kawasaki-shi, Kanagawa-ken 213-0012, Japan' }
    ]
  }
]

// State for active expansion panel
const activePanel = ref(null)

const togglePanel = (index) => {
  activePanel.value = activePanel.value === index ? null : index
}
</script>

<style scoped>
/* bạn có thể giữ nguyên Tailwind hoặc thêm CSS tuỳ chỉnh */
</style>

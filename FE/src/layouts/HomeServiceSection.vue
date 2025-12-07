<template>
  <div class="lg:mt-[83px] lg:mb-[151px] mt-[42px] mr-[16px] mb-[56px] ml-[17px] text-center">
    <div class="flex justify-center">
      <h2
        class="header-text lg:text-[40px] text-[20px] font-bold lg:uppercase lg:text-center text-left lg:leading-[60px] leading-[30px] lg:w-[690px] w-[360px] lg:mb-[70px] mb-[30px]"
      >
        Hãy cùng chúng tôi tham gia hành trình chuyển đổi số
      </h2>
    </div>

    <div class="flex 2xl:flex-row flex-col 2xl:items-start items-center justify-center gap-6 px-2">
      <!-- Box lớn bên trái -->
      <div
        class="relative bg-[#840000] text-white p-4 rounded-lg lg:w-[436px] lg:min-h-[317px] w-[360px] min-h-[243px] overflow-hidden transition-transform duration-300 lg:hover:scale-110"
      >
        <RouterLink to="/">
          <img
            class="absolute top-0 left-0 w-full h-full object-cover"
            src="https://luvina.net/vi/wp-content/uploads/2025/02/dich_vu_cua_chung_toi.png"
            alt="Services background"
          />

          <h3
            class="relative lg:text-[30px] text-[20px] font-bold lg:leading-[45px] leading-[30px] text-left lg:uppercase mb-2"
          >
            Dịch vụ của chúng tôi
          </h3>

          <div class="relative flex items-start gap-2 z-10">
            <p class="lg:text-[18px] text-[16px] font-normal lg:leading-[31px] leading-[27px] text-left">
              Tối ưu hành trình chuyển đổi số doanh nghiệp của bạn với các dịch vụ
              IT outsourcing toàn diện
            </p>
            <img
              class="lg:block hidden w-[24px] h-[24px]"
              src="https://luvina.net/vi/wp-content/uploads/2025/02/arrow-right-white.png"
              alt=""
            />
          </div>
        </RouterLink>
      </div>

      <!-- Danh sách dịch vụ bên phải -->
      <div class="lg:flex lg:justify-end lg:gap-6">
        <!-- Cột trái -->
        <div class="flex flex-col lg:w-[436px] lg:min-h-[317px] w-[360px] min-h-[243px] gap-6">
          <ServiceItem
            to="/"
            text="Tư vấn chiến lược"
          />
          <ServiceItem
            to="/"
            text="Phát triển phần mềm"
          />
          <ServiceItem
            to="/"
            text="Di chuyển & hiện đại hóa ứng dụng"
          />
          <ServiceItem
            to=""
            text="Chuyển đổi đám mây"
          />
        </div>

        <!-- Cột phải -->
        <div class="flex flex-col lg:w-[436px] lg:min-h-[317px] w-[360px] min-h-[243px] gap-6">
          <ServiceItem
            class="lg:mt-0 mt-6"
            to="/"
            text="Dịch vụ quản lý CNTT"
          />
          <ServiceItem
            to="/"
            text="Kiểm thử & quản lý chất lượng"
          />
          <ServiceItem
            to="/"
            text="Bảo mật công nghệ thông tin"
          />
        </div>
      </div>

      <!-- Nút xem thêm (mobile) -->
      <RouterLink
        to=""
        class="lg:hidden flex items-center justify-center rounded-lg bg-[#ce181e] text-white text-[18px] font-normal text-center leading-[23px] mt-8 px-4 py-2 transition-colors duration-300 hover:bg-red-700"
      >
        <span>Xem thêm</span>
        <img
          src="https://luvina.net/vi/wp-content/uploads/2025/02/arrow-right-white.png"
          alt="Contact Icon"
          class="ml-[3px] w-[24px] h-[24px]"
        />
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
// Component con dùng cho từng service item
import { RouterLink } from "vue-router";

const ServiceItem = defineComponent({
  props: {
    to: String,
    text: String,
  },
  template: `
    <RouterLink :to="to">
      <div
        class="service-item flex justify-between items-center p-4 lg:text-[18px] text-[16px]
               lg:font-bold font-normal leading-[27px] text-left text-[#4a4b4c] rounded-lg"
      >
        <span>{{ text }}</span>
        <img class="w-[24px] h-[24px]" src="/vi/wp-content/uploads/2025/02/arrow-right-black.png" alt="arrow right">
      </div>
    </RouterLink>
  `,
});
</script>

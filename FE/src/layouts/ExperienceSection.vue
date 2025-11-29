<template>
  <section id="business-areas" class="section">
    <div
      class="hidden md:flex flex-col items-center justify-center gap-[60px]
                 bg-gradient-to-b from-[#F9F9F9] to-white py-20"
    >
      <h2
        class="text-transparent bg-gradient-to-r from-[#840000] to-[#CE181E]
                   bg-clip-text text-center text-[20px] lg:text-[30px] font-bold lg:uppercase"
      >
        Kinh nghiệm của chúng tôi
      </h2>

      <div
        class="grid grid-cols-5 lg:gap-[72px] xl:gap-[69px] 2xl:gap-[90px] md:gap-[25px]
                   justify-center items-center"
      >
        <!-- Loop từng item -->
        <div v-for="(item, index) in industries" :key="index">
          <a
            class="pc-industries-rectangles relative flex flex-col items-center"
            href="#"
          >
            <div
              class="pc-industries-rectangle-934 
                            2xl:w-[199px] 2xl:h-[230px] 
                            xl:w-[175px] xl:h-[200px] 
                            sm:w-[130px] sm:h-[150px]
                            bg-cover bg-center bg-no-repeat"
              :style="{
                backgroundImage:
                  'url(/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png)',
              }"
            ></div>

            <div
              class="pc-industries-rectangle-935 absolute 
                            2xl:w-[199px] 2xl:h-[230px]
                            xl:w-[175px] xl:h-[200px]
                            md:w-[130px] sm:h-[150px] 
                            flex flex-col items-center justify-center gap-2
                            bg-cover bg-center bg-no-repeat"
              :style="{
                backgroundImage:
                  'url(/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png)',
              }"
            >
              <div
                class="flex items-start 
                              sm:h-[40px] xl:h-[60px] 2xl:h-[75px]"
              >
                <div
                  class="pc-industries-text-color text-[#4A4B4C]
                                xl:text-[16px] xl:leading-[27.2px]
                                sm:text-[12px] sm:leading-[16px]
                                font-noto font-normal text-center"
                  v-html="item.text"
                ></div>
              </div>

              <img
                class="2xl:w-[60px] 2xl:h-[60px] sm:w-[55px] sm:h-[55px]"
                :src="`/vi/wp-content/themes/luvina2024/assets/images/${item.img}.png`"
                alt="icon"
              />
            </div>
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref } from "vue";

const industries = ref([
  { text: "Tài chính – Bảo hiểm – <br>Ngân hàng", img: "image-1" },
  { text: "Xây dựng", img: "image-2" },
  { text: "Y tế", img: "image-3" },
  { text: "Nhân sự", img: "image-4" },
  { text: "Hậu cần & Vận tải", img: "image-5" },
  { text: "Sản xuất", img: "image-6" },
  { text: "Truyền thông <br>& Tổ chức sự kiện", img: "image-7" },
  { text: "Bất động sản", img: "image-8" },
  { text: "Bán lẻ & Thương mại<br>điện tử", img: "image-9" },
]);
</script>

<style scoped>
/* Nếu bạn có CSS riêng, thêm vào đây */
</style>

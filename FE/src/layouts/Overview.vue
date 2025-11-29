<template>
  <div
    class="lg:flex hidden flex justify-center items-center bg-[#fafafa] w-full"
  >
    <div
      class="lg:max-w-[1355px] xl:w-[90vw] w-full rounded-none h-[951px]
      bg-[url('/vi/wp-content/uploads/2025/02/backgroud_luvina_overview.png')]
      bg-cover bg-center
      2xl:bg-[length:120%] xl:bg-[length:130%] lg:bg-[length:140%]"
    >
      <div
        class="flex flex-col items-center px-20 pt-[67px] pb-10 w-full max-md:px-5 max-md:max-w-full"
      >
        <section class="flex flex-col items-center max-w-full w-[1085px]">
          <h2
            class="z-20 text-[40px] font-bold uppercase bg-clip-text
            bg-[linear-gradient(90deg,#840000_0%,#CE181E_100%)]"
          >
            Luvina overview
          </h2>

          <!-- Globe visual -->
          <div id="globeViz" class="max-w-full -mt-3">
            <div style="position: relative">
              <div>
                <div class="scene-container" style="position: relative">
                  <div class="scene-nav-info" style="display: none">
                    Left-click: rotate, Mouse-wheel/middle-click: zoom,
                    Right-click: pan
                  </div>
                  <div
                    class="float-tooltip-kap"
                    style="left: -10000px; display: none"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Statistics section -->
          <section class="self-stretch z-20 -mt-[60px]" space="132">
            <div class="flex gap-5 max-md:flex-col">
              <article
                v-for="(item, index) in counters"
                :key="index"
                class="w-3/12 max-md:ml-0 max-md:w-full text-center ml-5 first:ml-0"
              >
                <div class="flex flex-col max-md:mt-10">
                  <h3
                    class="counter self-center text-6xl font-bold leading-none uppercase text-[#353535] max-md:text-4xl"
                  >
                    {{ displayValues[index] }}+
                  </h3>
                  <p class="text-lg leading-8 text-center text-[#818181]">
                    {{ item.label }}
                  </p>
                </div>
              </article>
            </div>
          </section>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

// Dữ liệu counters
const counters = ref([
  { value: 20, label: "Năm phát triển" },
  { value: 110, label: "Khách hàng hài lòng" },
  { value: 750, label: "Nhân sự IT tài năng" },
  { value: 1000, label: "Dự án thành công" },
]);

// Giá trị hiển thị cho animation
const displayValues = ref([0, 0, 0, 0]);

// Counter animation
onMounted(() => {
  counters.value.forEach((item, index) => {
    let current = 0;
    const duration = 1500;
    const step = Math.ceil(item.value / (duration / 16)); // 60fps

    const timer = setInterval(() => {
      current += step;
      if (current >= item.value) {
        current = item.value;
        clearInterval(timer);
      }
      displayValues.value[index] = current;
    }, 16);
  });
});
</script>

<style scoped>
/* Nếu bạn muốn style riêng thì thêm ở đây */
</style>

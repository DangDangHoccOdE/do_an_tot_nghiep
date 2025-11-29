<template>
  <div class="flex flex-col rounded-none">
    <div class="flex flex-col justify-center items-center px-20 py-16 w-full bg-[#F8F8F8] lg:bg-[#F9F9F9]">
        <div class="flex flex-col items-center w-full max-w-[1355px]">
        <h2
            class="bg-gradient-to-r from-[#840000] to-[#CE181E] text-transparent bg-clip-text flex justify-center text-[40px] font-bold uppercase leading-normal"
            tabindex="0"
        >
            Tại sao nên chọn chúng tôi ?
        </h2>

        <!-- List Items -->
        <div class="self-stretch mt-28 min-h-[421px]">
            <div class="flex justify-between min-h-[421px] gap-[25px]">
            <div
                v-for="(item, i) in items"
                :key="i"
                class="flex flex-col w-[23.6%] rounded-2xl group transition duration-300 hover:shadow-xl"
            >
                <div
                class="flex flex-col grow items-center px-11 pt-14 w-full text-center bg-white rounded-2xl max-md:px-5 max-md:mt-6 justify-between"
                tabindex="0"
                >
                <h3 class="text-xl font-bold text-[#4A4B4C]">{{ item.title }}</h3>

                <div class="self-stretch mt-11 text-sm leading-6 text-black max-md:mt-10">
                    {{ item.content }}
                </div>

                <!-- ICONS -->
                <div class="flex overflow-hidden relative flex-col items-start px-2.5 pb-7 mt-11 aspect-[0.597] w-[100px] max-md:mt-10">
                    <img
                    :src="item.bg"
                    class="object-cover absolute inset-0 w-[74px] h-[124px] transition-opacity duration-300 opacity-100 group-hover:opacity-0"
                    />
                    <img
                    :src="item.icon"
                    class="object-contain absolute inset-0 z-10 mr-0 w-full transition-opacity duration-300 opacity-100 group-hover:opacity-0"
                    />
                    <img
                    :src="item.bgHover"
                    class="object-cover absolute inset-0 w-[74px] h-[124px] transition-opacity duration-300 opacity-0 group-hover:opacity-100"
                    />
                    <img
                    :src="item.iconHover"
                    class="object-contain absolute inset-0 z-10 mr-0 w-full transition-opacity duration-300 opacity-0 group-hover:opacity-100"
                    />
                </div>
                </div>
            </div>
            </div>
        </div>

        <!-- Button -->
        <a
            href="/vi/why-luvina"
            class="group flex gap-2.5 justify-center items-center w-[175px] h-[40px] mt-[62px] text-lg text-[#CE181E] rounded-lg border border-[#CE181E] hover:bg-[#CE181E] hover:text-white transition duration-300"
            aria-label="View More Details"
        >
            <p class="self-stretch my-auto">Về chúng tôi</p>

            <div class="relative w-6 h-6">
            <img
                src="https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/icon-next-red.png"
                class="absolute inset-0 object-contain transition-opacity duration-300 opacity-100 group-hover:opacity-0"
            />
            <img
                src="https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/icon-next-white.png"
                class="absolute inset-0 object-contain transition-opacity duration-300 opacity-0 group-hover:opacity-100"
            />
            </div>
        </a>
        </div>
    </div>
  </div>
</template>

<script setup>
const items = [
  {
    title: "Quản trị dự án",
    content:
      "Khả năng quản trị dự án vượt trội dựa trên việc tuân thủ các tiêu chuẩn như ISO và CMMI 3.",
    icon: "/vi/wp-content/themes/luvina2024/assets/images/gear-icon.png",
    iconHover: "/vi/wp-content/themes/luvina2024/assets/images/gear-red-icon.png",
    bg: "/vi/wp-content/themes/luvina2024/assets/images/parallelogram-icon.png",
    bgHover: "/vi/wp-content/themes/luvina2024/assets/images/parallelogram-red-icon.png",
  },
  {
    title: "Văn hóa đào tạo & phát triển",
    content:
      "Khả năng đáp ứng nghiệp vụ và kỹ thuật mới hiệu quả nhờ văn hóa và kỹ năng tự học của nhân sự.",
    icon: "/vi/wp-content/themes/luvina2024/assets/images/education-icon.png",
    iconHover: "/vi/wp-content/themes/luvina2024/assets/images/education-red-icon.png",
    bg: "/vi/wp-content/themes/luvina2024/assets/images/parallelogram-icon.png",
    bgHover: "/vi/wp-content/themes/luvina2024/assets/images/parallelogram-red-icon.png",
  },
  {
    title: "Chất lượng ổn định",
    content:
      "Đảm bảo chất lượng sản phẩm, dịch vụ cao và bền vững dù thay đổi cơ cấu nhân sự.",
    icon: "/vi/wp-content/themes/luvina2024/assets/images/quality-icon.png",
    iconHover: "/vi/wp-content/themes/luvina2024/assets/images/quality-red-icon.png",
    bg: "/vi/wp-content/themes/luvina2024/assets/images/parallelogram-icon.png",
    bgHover: "/vi/wp-content/themes/luvina2024/assets/images/parallelogram-red-icon.png",
  },
  {
    title: "Hợp tác lâu dài",
    content:
      "Chú trọng mối quan hệ hợp tác lâu dài, đồng hành cùng khách hàng vươn tới thành công.",
    icon: "/vi/wp-content/themes/luvina2024/assets/images/collaborate-icon.png",
    iconHover: "/vi/wp-content/themes/luvina2024/assets/images/collaborate-red-icon.png",
    bg: "/vi/wp-content/themes/luvina2024/assets/images/parallelogram-icon.png",
    bgHover: "/vi/wp-content/themes/luvina2024/assets/images/parallelogram-red-icon.png",
  },
];
</script>

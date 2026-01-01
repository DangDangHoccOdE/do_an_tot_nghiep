<template>
    <div class="flex flex-col items-center justify-center gap-[85px] relative py-[130px]">
        <!-- Title -->
        <h2 class="text-[20px] lg:text-[40px] font-bold lg:uppercase text-center leading-[60px]
             bg-gradient-to-r from-[#840000] to-[#CE181E]
             bg-clip-text text-transparent">
            Đối tác của chúng tôi
        </h2>

        <!-- Partner list -->
        <div class="flex items-start flex-row gap-[20px] overflow-hidden
     whitespace-nowrap p-5 max-w-[99vw]">
            <div class="ourPartnerAuto auto-scroll-x gap-[50px] lg:gap-[250px] flex">
                <div v-for="(partner, index) in partners" :key="index"
                    class="flex flex-col items-center justify-start gap-2 flex-none">
                    <div class="flex justify-center items-center gap-2">
                        <img :src="partner.src" :alt="partner.alt" :class="partner.class" decoding="async" />
                    </div>
                </div>
            </div>
        </div>


        <!-- CTA -->
        <a href="https://luvina.net/vi/contact-us/" class="flex justify-center items-center bg-[#CE181E] h-[48px]
             pl-[20px] pr-[15px] rounded-[8px]
             hover:bg-[linear-gradient(90deg,#840000_0%,#ce181e_100%)]
             gap-[10px]">
            <p class="text-[18px] leading-[170%] text-white">
                Trở thành đối tác của chúng tôi
            </p>
            <img src="https://luvina.net/vi/wp-content/uploads/2025/02/arrow-right-white.png" alt="Contact Icon"
                class="ml-[3px] w-[24px] h-[24px]" aria-hidden="true" decoding="async" loading="lazy" />
        </a>
    </div>
</template>

<script setup>
const partners = [
    {
        src: "https://luvina.net/vi/wp-content/uploads/2025/02/icon_mag_rabbit.png",
        alt: "logo-mag-rabbit",
        class: "w-[198.5px] h-[36.5px]"
    },
    {
        src: "https://luvina.net/vi/wp-content/uploads/2025/02/icon_vdx.png",
        alt: "logo-vdx",
        class: "w-[86px] h-[38px]"
    },
    {
        src: "https://luvina.net/vi/wp-content/uploads/2025/02/icon_heart_core.png",
        alt: "logo-heart-core",
        class: "w-[140px] h-[38px]"
    },
    {
        src: "https://luvina.net/vi/wp-content/uploads/2025/04/gleamcore.png",
        alt: "logo-gleamcore",
        class: "w-[214px] h-[22px]"
    },
    {
        src: "https://luvina.net/vi/wp-content/uploads/2025/04/perceptive-consulting.png",
        alt: "logo-perceptive",
        class: "w-[126px] h-[37px]"
    }
];

// nhân bản để tạo hiệu ứng scroll dài
partners.push(...partners, ...partners, ...partners);
</script>

<style scoped>
@keyframes scrollX {
    0% {
        transform: translateX(0);
    }

    100% {
        transform: translateX(-50%);
    }
}

.auto-scroll-x {
    animation: scrollX 25s linear infinite;
}
</style>

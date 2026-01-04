<template>
    <div class="bg-cover bg-no-repeat bg-center flex flex-col items-center justify-center
           gap-[85px] relative py-[130px]
           bg-[url('/vi/wp-content/themes/luvina2024/assets/images/mobile-certifications-awards-background.png')]
           lg:bg-[url('/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-background.png')]">
        <!-- Title -->
        <h2 class="text-[20px] lg:text-[40px] font-bold lg:uppercase text-center leading-[60px]
             bg-gradient-to-r from-[#840000] to-[#CE181E]
             bg-clip-text text-transparent">
            {{ $t('home.awards.title') }}
        </h2>

        <!-- List -->
        <div class="overflow-hidden max-w-[99vw] p-5">
            <div class="certificationsAwards auto-scroll-x flex gap-[50px] lg:gap-[100px]">
                <div v-for="(item, index) in awards" :key="index"
                    class="flex flex-col items-center justify-start gap-2 flex-none">
                    <div class="w-[223px] h-[94px] lg:w-[320px] lg:h-[135px]
                        flex justify-center items-center bg-no-repeat" :style="{
                            backgroundImage: 'url(https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-rectangle.png)',
                            backgroundSize: 'contain',
                            backgroundPosition: 'center'
                        }">
                        <img :src="item.src" :alt="item.title" :class="item.imgClass" decoding="async" />
                    </div>

                    <div class="text-[15px] 2xl:text-[18px] font-normal
                                     leading-[22px] lg:leading-[26.6px] 2xl:leading-[30.6px]
                                     text-center text-black whitespace-pre-line">
                        {{ $t(item.titleKey) }}
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const baseAwards = computed(() => [
    {
        src: "https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-image-7.png",
        titleKey: 'home.awards.items.iso',
        imgClass: "w-[35%]"
    },
    {
        src: "https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-image-2-jp.png",
        titleKey: 'home.awards.items.cmmiDev',
        imgClass: "w-[35%]"
    },
    {
        src: "https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-image-1-jp.png",
        titleKey: 'home.awards.items.cmmiService',
        imgClass: "w-[35%]"
    },
    {
        src: "https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-image-2.png",
        titleKey: 'home.awards.items.tokyoTech',
        imgClass: "w-[35%]"
    },
    {
        src: "https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-image-3.png",
        titleKey: 'home.awards.items.kuramae',
        imgClass: "w-[35%]"
    },
    {
        src: "https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-image-4.png",
        titleKey: 'home.awards.items.top10Ict2021',
        imgClass: "w-[35%]"
    },
    {
        src: "https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-image-5.png",
        titleKey: 'home.awards.items.top10Ict2024',
        imgClass: "w-[25%]"
    },
    {
        src: "https://luvina.net/vi/wp-content/uploads/2025/10/pc-certifications-awards-image-8.png",
        titleKey: 'home.awards.items.top10TechMap2025',
        imgClass: "w-[30%]"
    },
    {
        src: "https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/pc-certifications-awards-image-6.png",
        titleKey: 'home.awards.items.designRush',
        imgClass: "w-[25%]"
    }
])

const awards = computed(() => [...baseAwards.value, ...baseAwards.value])
</script>
<style scoped>
@keyframes scrollX {
    0% {
        transform: translateX(0);
    }

    100% {
        transform: translateX(-70%);
    }
}

.auto-scroll-x {
    animation: scrollX 12s linear infinite;
}
</style>
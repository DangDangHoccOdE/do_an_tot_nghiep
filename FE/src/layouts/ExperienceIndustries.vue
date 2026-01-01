<template>
  <section class="experience-section">
    <h2 class="experience-title">
      Kinh nghiệm của chúng tôi
    </h2>

    <div class="experience-grid">
      <article v-for="item in items" :key="item.title" class="flex justify-center">
        <a class="pc-industries-rectangles relative flex flex-col items-center" :href="item.href">
          <div class="pc-industries-rectangle-934 bg-cover bg-center bg-no-repeat"
            :style="`background-image: url(${item.bg1});`" />
          <div
            class="pc-industries-rectangle-935 absolute flex flex-col items-center justify-center gap-2 bg-cover bg-center bg-no-repeat"
            :style="`background-image: url(${item.bg2});`">
            <div class="flex items-start">
              <div class="pc-industries-text-color text-[#4A4B4C] text-center font-noto font-normal"
                v-html="item.title" />
            </div>
            <img :src="item.icon" :alt="item.alt" class="industry-icon" />
          </div>
        </a>
      </article>
    </div>

    <a href="/vi/industries/" class="view-more group">
      <p class="view-more-text">Xem thêm</p>
      <img src="https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/industry-arrow-right.png" alt=""
        class="view-more-icon group-hover:hidden" />
      <img src="https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/icon-next-white-1.png" alt=""
        class="view-more-icon hidden group-hover:block" />
    </a>
  </section>
</template>

<script setup>
const items = [
  {
    title: 'Tài chính – Bảo hiểm – <br>Ngân hàng',
    href: 'https://luvina.net/vi/industries/bfsi',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-1.png',
    alt: 'BFSI'
  },
  {
    title: 'Xây dựng',
    href: 'https://luvina.net/vi/industries/construction',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-2.png',
    alt: 'Construction'
  },
  {
    title: 'Y tế',
    href: 'https://luvina.net/vi/industries/healthcare',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-3.png',
    alt: 'Healthcare'
  },
  {
    title: 'Nhân sự',
    href: 'https://luvina.net/vi/industries/human-resources',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-4.png',
    alt: 'HR'
  },
  {
    title: 'Hậu cần &amp; Vận tải',
    href: 'https://luvina.net/vi/industries/logistics-transportation',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-5.png',
    alt: 'Logistics'
  },
  {
    title: 'Sản xuất',
    href: 'https://luvina.net/vi/industries/manufacturing',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-6.png',
    alt: 'Manufacturing'
  },
  {
    title: 'Truyền thông <br>&amp; Tổ chức sự kiện',
    href: 'https://luvina.net/vi/industries/media-event-planning',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-7.png',
    alt: 'Media'
  },
  {
    title: 'Bất động sản',
    href: 'https://luvina.net/vi/industries/real-estate',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-8.png',
    alt: 'Real Estate'
  },
  {
    title: 'Bán lẻ &amp; Thương mại<br>điện tử',
    href: 'https://luvina.net/vi/industries/retail-e-commerce',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-9.png',
    alt: 'Retail'
  },
  {
    title: 'Du lịch &amp; Hàng không',
    href: 'https://luvina.net/vi/industries/travel-aviation',
    bg1: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-934.png',
    bg2: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/rectangle-935.png',
    icon: 'https://luvina.net/vi/wp-content/themes/luvina2024/assets/images/image-10.png',
    alt: 'Travel'
  }
];
</script>

<style scoped>
.experience-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 60px;
  background: linear-gradient(to bottom, #F9F9F9, #FFFFFF);
  padding: 80px 20px;
}

.experience-title {
  background: linear-gradient(to right, #840000, #CE181E);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  text-align: center;
  font-size: 20px;
  font-weight: 700;
  text-transform: uppercase;
}

@media (min-width: 1024px) {
  .experience-title {
    font-size: 30px;
  }
}

.experience-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 25px;
  justify-items: center;
  width: 100%;
  max-width: 1200px;
}

@media (min-width: 1024px) {
  .experience-grid {
    grid-template-columns: repeat(5, minmax(150px, 1fr));
    column-gap: 72px;
  }
}

.pc-industries-rectangle-934 {
  width: 175px;
  height: 200px;
}

.pc-industries-rectangle-935 {
  width: 175px;
  height: 200px;
}

.industry-icon {
  width: 60px;
  height: 60px;
}

.view-more {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 15px 8px 20px;
  border: 1px solid #CE181E;
  border-radius: 8px;
  color: #CE181E;
  text-decoration: none;
  transition: all 0.2s ease;
}

.view-more:hover {
  background: #CE181E;
  color: #fff;
}

.view-more-text {
  font-size: 18px;
}

.view-more-icon {
  width: 24px;
  height: 24px;
}
</style>

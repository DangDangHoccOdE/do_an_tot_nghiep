<template>
  <div class="main-layout">
    <!-- Content area - nằm dọc, không cần header/footer vì đã quản lý ở App.vue -->
    <div class="content-wrapper">
      <WhyChooseUsSection />
      <ExperienceIndustries />
      <SolutionsSection />
      <LuvinaOverviewHero />
      <HomeServiceSection />
      <LuvinaAcademyOverview />
      <PartnerCarousel />
      <CertificationsAwards />
    </div>
  </div>
</template>

<script setup>
import ExperienceIndustries from './ExperienceIndustries.vue';
import SolutionsSection from './SolutionsSection.vue';
import LuvinaOverviewHero from './LuvinaOverviewHero.vue';
import HomeServiceSection from './HomeServiceSection.vue';
import LuvinaAcademyOverview from './LuvinaAcademyOverview.vue';
import WhyChooseUsSection from './WhyChooseUsSection.vue';
import PartnerCarousel from './PartnerCarousel.vue';
import CertificationsAwards from './CertificationsAwards.vue';
</script>

<style scoped>
.main-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

/* Các thành phần con nằm dọc và chiếm full width */
.content-wrapper>* {
  width: 100%;
  flex-shrink: 0;
}
</style>
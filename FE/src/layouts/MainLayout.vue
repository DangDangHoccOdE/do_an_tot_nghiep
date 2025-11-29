<template>
  <div class="flex h-screen flex-col">
    <!-- Top fixed Header -->
    <Header />

    <!-- Content area -->
    <div class="flex flex-1 overflow-hidden background-main">
      <WhyChooseUsSection/>
      <Overview/>
      <ExperienceSection/>
      <HomeServiceSection/>
      <LuvinaAcademyOverview/>
    </div>

    <Footer/>
  </div>
</template>

<script setup>
import ExperienceSection from './ExperienceSection.vue';
import Footer from './Footer.vue';
import Header from './Header.vue';
import HomeServiceSection from './HomeServiceSection.vue';
import LuvinaAcademyOverview from './LuvinaAcademyOverview.vue';
import Overview from './Overview.vue';
import WhyChooseUsSection from './WhyChooseUsSection.vue';

</script>
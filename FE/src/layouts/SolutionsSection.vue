<template>
  <section class="solutions-hero">
    <div class="solutions-copy">
      <h2 class="solutions-title">{{ $t('home.solutions.title') }}</h2>
      <div class="solutions-links">
        <a href="/vi/solutions/erp" class="solution-text" data-key="ERP">
          {{ $t('home.solutions.items.erp') }}
        </a>
        <a href="/vi/solutions/cms" class="solution-text" data-key="CMS">
          {{ $t('home.solutions.items.cms') }}
        </a>
        <a href="/vi/solutions/crm" class="solution-text" data-key="CRM">
          {{ $t('home.solutions.items.crm') }}
        </a>
        <a href="/vi/solutions/pos" class="solution-text" data-key="POS">
          {{ $t('home.solutions.items.pos') }}
        </a>
        <a href="/vi/solutions/e-commerce" class="solution-text" data-key="E-commerce">
          {{ $t('home.solutions.items.ecommerce') }}
        </a>
      </div>
      <a href="/vi/solutions" class="solutions-cta desktop-cta">
        {{ $t('home.solutions.viewMore') }}
        <img src="https://luvina.net/vi/wp-content/uploads/2025/02/arrow-right-white.png" alt="" class="cta-icon" />
      </a>
    </div>

    <div class="solutions-visual">
      <img id="solution-image" class="solutions-image"
        src="https://luvina.net/vi/wp-content/uploads/2025/02/Solution_ERP.png" alt="ERP system on a laptop screen" />
    </div>

    <a href="/vi/solutions" class="solutions-cta mobile-cta">
      {{ $t('home.solutions.viewMore') }}
      <img src="https://luvina.net/vi/wp-content/uploads/2025/02/arrow-right-white.png" alt="" class="cta-icon" />
    </a>
  </section>
</template>

<script setup>
// Static section; text localized via i18n
</script>

<style scoped>
.solutions-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 50px;
  padding: 40px 20px 60px;
  color: #fff;
  background: url('https://luvina.net/vi/wp-content/uploads/2025/02/backgroud_giai_phap_mobile.png') center/cover no-repeat;
}

@media (min-width: 1024px) {
  .solutions-hero {
    flex-direction: row;
    justify-content: center;
    padding: 0 13% 0;
    min-height: 804px;
    gap: 50px;
    background: url('https://luvina.net/vi/wp-content/uploads/2025/02/backgroud_giai_phap.png') center/cover no-repeat;
  }
}

.solutions-copy {
  max-width: 504px;
  text-align: left;
  display: flex;
  flex-direction: column;
}

.solutions-title {
  font-weight: 700;
  font-size: 20px;
  line-height: 30px;
  text-transform: none;
  margin: 39px 0 20px;
}

@media (min-width: 1024px) {
  .solutions-title {
    font-size: 40px;
    line-height: 60px;
    text-transform: uppercase;
    margin: 0 0 40px;
  }
}

.solutions-links {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin: 40px 0 40px;
}

.solution-text {
  font-size: 18px;
  line-height: 30.6px;
  color: #fff;
  text-decoration: none;
}

.solution-text:hover {
  text-decoration: underline;
}

.solutions-cta {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  border: 1px solid #fff;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 18px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
  width: fit-content;
}

.solutions-cta:hover {
  background: #fff;
  color: #ce181e;
}

.desktop-cta {
  display: none;
}

.mobile-cta {
  display: inline-flex;
  margin-top: 25px;
}

@media (min-width: 1024px) {
  .desktop-cta {
    display: inline-flex;
  }

  .mobile-cta {
    display: none;
  }
}

.solutions-visual {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
}

.solutions-image {
  width: 387px;
  max-width: 100%;
  height: auto;
}

@media (min-width: 1024px) {
  .solutions-visual {
    margin-left: 8%;
  }

  .solutions-image {
    width: 685px;
  }
}

.cta-icon {
  width: 24px;
  height: auto;
}
</style>

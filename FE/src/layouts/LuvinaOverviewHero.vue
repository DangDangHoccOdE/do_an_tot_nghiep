<template>
  <section class="overview-hero">
    <div class="overview-card">
      <div class="overview-content">
        <h2 class="overview-title">{{ $t('home.overview.title') }}</h2>
        <div class="lg:flex hidden flex justify-center mt-12 items-center bg-[#fafafa] w-full">
          <div class="lg:max-w-[1355px] xl:w-[90vw] w-full rounded-none
                    bg-[url('/vi/wp-content/uploads/2025/02/backgroud_luvina_overview.png')]
                    bg-cover bg-center
                    2xl:bg-[length:120%] xl:bg-[length:130%] lg:bg-[length:140%]">
            <div class="flex flex-col items-center px-20 pt-[67px] pb-10 w-full max-md:px-5 max-md:max-w-full">
              <section class="flex flex-col items-center max-w-full w-[1085px]">
                <section class="self-stretch z-20 -mt-[60px]">
                  <div class="flex gap-5 max-md:flex-col">
                    <article class="w-3/12 max-md:w-full text-center">
                      <div class="flex flex-col grow max-md:mt-10">
                        <h3
                          class="counter self-center text-6xl font-bold leading-none uppercase text-[#353535] max-md:text-4xl"
                          data-value="20">
                          20+
                        </h3>
                        <p class="text-lg leading-8 text-[#818181]">
                          {{ $t('home.overview.stats.years') }}
                        </p>
                      </div>
                    </article>

                    <article class="w-3/12 max-md:w-full text-center">
                      <div class="flex flex-col max-md:mt-10">
                        <h3
                          class="counter self-center text-6xl font-bold leading-none uppercase text-[#353535] max-md:text-4xl"
                          data-value="110">
                          110+
                        </h3>
                        <p class="text-lg leading-8 text-[#818181]">
                          {{ $t('home.overview.stats.clients') }}
                        </p>
                      </div>
                    </article>

                    <article class="w-3/12 max-md:w-full text-center">
                      <h3
                        class="counter text-6xl font-bold leading-none uppercase text-[#353535] max-md:mt-10 max-md:text-4xl"
                        data-value="750">
                        750+
                      </h3>
                      <p class="text-lg leading-8 text-[#818181]">
                        {{ $t('home.overview.stats.talents') }}
                      </p>
                    </article>

                    <article class="w-3/12 max-md:w-full text-center">
                      <h3
                        class="counter text-6xl font-bold leading-none uppercase text-[#353535] max-md:mt-10 max-md:text-4xl"
                        data-value="1000">
                        1000+
                      </h3>
                      <p class="text-lg leading-8 text-[#818181]">
                        {{ $t('home.overview.stats.projects') }}
                      </p>
                    </article>
                  </div>
                </section>
              </section>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted } from 'vue'

// Static layout; globe placeholder retained. Add actual globe rendering separately.
onMounted(() => {
  const counters = document.querySelectorAll('.counter')
  counters.forEach(counter => {
    const target = +counter.getAttribute('data-value')
    let current = 0
    const increment = Math.ceil(target / 50)

    const update = () => {
      if (current < target) {
        current += increment
        counter.innerText = `${current}+`
        requestAnimationFrame(update)
      } else {
        counter.innerText = `${target}+`
      }
    }
    update()
  })
})

</script>

<style scoped>
.overview-hero {
  display: none;
}

@media (min-width: 1024px) {
  .overview-hero {
    display: flex;
    justify-content: center;
    background: #fafafa;
    width: 100%;
  }

  /* .overview-card {
    width: 90vw;
    max-width: 1355px;
    min-height: 804px;
    background: url('https://luvina.net/vi/wp-content/uploads/2025/02/backgroud_luvina_overview.png') center/140% no-repeat;
    border-radius: 0;
    display: flex;
    justify-content: center;
  } */

  @media (min-width: 1280px) {
    .overview-card {
      background-size: 130%;
    }
  }

  @media (min-width: 1536px) {
    .overview-card {
      background-size: 120%;
    }
  }

  .overview-content {
    width: 100%;
    max-width: 1085px;
    padding: 67px 80px 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }

  .overview-title {
    font-size: 40px;
    font-weight: 700;
    text-transform: uppercase;
    background: linear-gradient(90deg, #840000 0%, #ce181e 100%);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    margin: 0;
  }

  .globe-holder {
    width: 700px;
    height: 700px;
    position: relative;
  }

  .stats-row {
    display: flex;
    gap: 20px;
    width: 100%;
    margin-top: -40px;
  }

  .stat {
    flex: 1;
    text-align: center;
  }

  .stat-value {
    font-size: 48px;
    font-weight: 700;
    color: #353535;
    margin: 0 0 8px;
  }

  .stat-label {
    font-size: 18px;
    color: #818181;
    margin: 0;
    line-height: 28px;
  }
}
</style>

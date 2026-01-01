<template>
  <div class="flex justify-center">
    <div class="lg:bg-[url('https://luvina.net/vi/wp-content/uploads/2025/02/background_luvina_academy.png')] 
             bg-[url('https://luvina.net/vi/wp-content/uploads/2025/02/background_luvina_academy_mobile.png')] 
             bg-cover bg-center 
             lg:pt-[114px] pt-[49px] 
             lg:pb-[67px] pb-[44px] 
             flex flex-col items-start 
             px-[16px] lg:px-[3%]">
      <!-- Tiêu đề -->
      <h2 class="lg:text-[40px] text-[20px] font-bold 
               lg:leading-[60px] leading-[30px] 
               lg:uppercase 
               bg-gradient-to-r from-[#840000] to-[#CE181E]
               bg-clip-text text-transparent">
        Luvina Academy
      </h2>

      <!-- Nội dung chính -->
      <div class=" flex lg:flex-row gap-5 flex-col mt-4">
        <!-- Bên trái: text và danh sách -->
        <article class="flex flex-col justify-between">
          <div>
            <p class="lg:mt-[14px] mt-[35px] 
                     lg:leading-[31px] leading-[27px] 
                     lg:text-[18px] text-[16px] font-normal text-[#4A4B4C] 
                     max-md:max-w-full 2xl:w-[783px] xl:w-[699px] sm:w-[590px] w-full">
              Để tăng trưởng nhân sự theo kế hoạch định trước và đảm bảo tiêu
              chuẩn chất lượng nhân sự đồng nhất, từ 2008 chúng tôi liên tục tổ
              chức các khóa đào tạo CNTT tại LUVINA ACADEMY với tần suất trung
              bình 2 khóa/năm dành cho các sinh viên năm cuối khoa CNTT và cả
              những sinh viên thuộc nhiều khoa khác như điện tử, xây dựng, cơ
              khí, tài chính, kinh tế, ngoại thương, …
            </p>

            <!-- Danh sách check -->
            <ul class="grid grid-cols-2 lg:grid-cols-1 
                     lg:mt-[43px] mt-[23px] 
                     lg:leading-[31px] leading-[15px] 
                     lg:text-[18px] text-[12px] font-normal text-black lg:space-y-[23px]">
              <div class="lg:space-y-[23px] lg:mr-0 mr-[44px]">
                <li class="flex gap-[13px]">
                  <img decoding="async" loading="lazy"
                    src="https://luvina.net/vi/wp-content/uploads/2025/02/icon_checked.png" class="w-[24px] h-[24px]"
                    alt="Icon">
                  <span>Con người là trung tâm</span>
                </li>
                <li class="flex gap-[13px] lg:mt-0 mt-[23px]">
                  <img decoding="async" loading="lazy"
                    src="https://luvina.net/vi/wp-content/uploads/2025/02/icon_checked.png" class="w-[24px] h-[24px]"
                    alt="Icon">
                  <span>Đề cao chất lượng giảng dạy</span>
                </li>
              </div>
              <div class="lg:space-y-[23px]">
                <li class="flex gap-[13px]">
                  <img decoding="async" loading="lazy"
                    src="https://luvina.net/vi/wp-content/uploads/2025/02/icon_checked.png" class="w-[24px] h-[24px]"
                    alt="Icon">
                  <span>Không ngừng học hỏi và phát triển</span>
                </li>
                <li class="flex gap-[13px] lg:mt-0 mt-[23px]">
                  <img decoding="async" loading="lazy"
                    src="https://luvina.net/vi/wp-content/uploads/2025/02/icon_checked.png" class="w-[24px] h-[24px]"
                    alt="Icon">
                  <span>Đảm bảo nguồn nhân lực ổn định và chất lượng</span>
                </li>
              </div>
            </ul>
          </div>

          <div class="hidden lg:flex justify-start items-center mt-10">
            <!-- Nút hoặc icon nếu cần -->
          </div>
        </article>

        <!-- Bên phải: ảnh -->
        <aside class="flex flex-col justify-end lg:items-end items-center">
          <div class="flex items-end">
            <img decoding="async" src="https://luvina.net/vi/wp-content/uploads/2025/02/luvinaAcademy.png"
              class="lg:w-[550px] w-[316px] lg:h-[525px] h-[345px] object-contain" alt="Luvina Academy">
          </div>
          <div class="flex lg:hidden justify-center items-center mt-[10px]">
            <!-- Nút hoặc icon mobile -->
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<script setup>
// Chưa cần JS, chỉ là component hiển thị tĩnh.
// Nếu bạn muốn thêm animation hay logic, có thể khai báo ở đây.
</script>

<style scoped>
/* Optional: thêm CSS nếu muốn override Tailwind */
</style>

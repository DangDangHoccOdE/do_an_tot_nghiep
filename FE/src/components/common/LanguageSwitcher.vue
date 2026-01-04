<template>
    <div class="language-switcher">
        <el-dropdown @command="handleCommand" trigger="click">
            <span class="el-dropdown-link">
                <component :is="currentFlagComponent" class="flag-icon" />
                <span class="lang-text">{{ currentLangText }}</span>
                <el-icon class="el-icon--right">
                    <arrow-down />
                </el-icon>
            </span>
            <template #dropdown>
                <el-dropdown-menu>
                    <el-dropdown-item command="vi" :class="{ 'is-active': currentLocale === 'vi' }">
                        <component :is="languageMap.vi.icon" class="flag-icon" />
                        <span>{{ languageMap.vi.label }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="en" :class="{ 'is-active': currentLocale === 'en' }">
                        <component :is="languageMap.en.icon" class="flag-icon" />
                        <span>{{ languageMap.en.label }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="ja" :class="{ 'is-active': currentLocale === 'ja' }">
                        <component :is="languageMap.ja.icon" class="flag-icon" />
                        <span>{{ languageMap.ja.label }}</span>
                    </el-dropdown-item>
                </el-dropdown-menu>
            </template>
        </el-dropdown>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { ArrowDown } from '@element-plus/icons-vue'
import VietnamFlagIcon from '@/components/icons/VietnamFlagIcon.vue'
import JapanFlagIcon from '@/components/icons/JapanFlagIcon.vue'
import UkFlagIcon from '@/components/icons/UkFlagIcon.vue'

const { locale } = useI18n()

const languageMap = {
    vi: { icon: VietnamFlagIcon, text: 'VI', label: 'Tiếng Việt' },
    en: { icon: UkFlagIcon, text: 'EN', label: 'English' },
    ja: { icon: JapanFlagIcon, text: 'JA', label: '日本語' }
}

const currentLocale = computed(() => locale.value)
const currentFlagComponent = computed(() => languageMap[locale.value]?.icon || UkFlagIcon)
const currentLangText = computed(() => languageMap[locale.value]?.text || 'Lang')

const handleCommand = (command) => {
    locale.value = command
    localStorage.setItem('locale', command)
    // Reload để apply ngôn ngữ mới toàn bộ
    window.location.reload()
}
</script>

<style scoped>
.language-switcher {
    display: inline-block;
    cursor: pointer;
}

.el-dropdown-link {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #4A4B4C;
    font-size: 14px;
    font-weight: 500;
    padding: 6px 12px;
    border-radius: 8px;
    transition: all 0.3s;
    cursor: pointer;
}

.el-dropdown-link:hover {
    background-color: #f5f5f5;
}

.flag-icon {
    width: 22px;
    height: 16px;
    display: inline-flex;
}

.lang-text {
    font-weight: 600;
    letter-spacing: 0.5px;
}

:deep(.el-dropdown-menu__item) {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
}

:deep(.el-dropdown-menu__item.is-active) {
    background-color: #f0f0f0;
    color: #CE181E;
    font-weight: 600;
}

:deep(.el-dropdown-menu__item .flag) {
    font-size: 18px;
}
</style>

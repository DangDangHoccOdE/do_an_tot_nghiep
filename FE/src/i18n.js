import { createI18n } from "vue-i18n";
import enCommon from "@/locales/en/en.json";
import viCommon from "@/locales/vi/vi.json";

const messages = {
    en: {
        ...enCommon
    },
    vi: {
        ...viCommon
    }
}

const i18n = createI18n({
    legacy: false,
    globalInjection: true,
    locale: 'vi',
    fallbackLocale: 'en',
    messages
});

export default i18n; 
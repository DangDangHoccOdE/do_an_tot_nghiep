import { createI18n } from "vue-i18n";
import enCommon from "@/locales/en/en.json";
import viCommon from "@/locales/vi/vi.json";
import viHeader from "@/locales/vi/header.json";
import viFooter from "@/locales/vi/footer.json";
import viAccount from "@/locales/vi/account.json";

const messages = {
    en: {
        ...enCommon
    },
    vi: {
        ...viCommon,
        ...viHeader,
        ...viFooter,
        ...viAccount
    }
}

const i18n = createI18n({
    legacy: false,
    globalInjection: true,
    locale: 'vi',
    fallbackLocale: 'en',
    messages,
});

export default i18n; 
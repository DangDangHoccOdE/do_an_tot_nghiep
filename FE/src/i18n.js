import { createI18n } from "vue-i18n";
import enCommon from "@/locales/en/common.json";
import enHeader from "@/locales/en/header.json";
import enFooter from "@/locales/en/footer.json";
import enHome from "@/locales/en/home.json";
import enAdmin from "@/locales/en/admin.json";
import enMyProjects from "@/locales/en/myProjects.json";
import enAuth from "@/locales/en/auth.json";
import enChatWidget from "@/locales/en/chatWidget.json";
import viCommon from "@/locales/vi/common.json";
import viHeader from "@/locales/vi/header.json";
import viFooter from "@/locales/vi/footer.json";
import viAccount from "@/locales/vi/account.json";
import viHome from "@/locales/vi/home.json";
import viAdmin from "@/locales/vi/admin.json";
import viMyProjects from "@/locales/vi/myProjects.json";
import viAuth from "@/locales/vi/auth.json";
import viChatWidget from "@/locales/vi/chatWidget.json";
import jaCommon from "@/locales/ja/common.json";
import jaHome from "@/locales/ja/home.json";
import jaAuth from "@/locales/ja/auth.json";
import jaHeader from "@/locales/ja/header.json";
import jaFooter from "@/locales/ja/footer.json";
import jaAdmin from "@/locales/ja/admin.json";
import jaChatWidget from "@/locales/ja/chatWidget.json";

const messages = {
    en: {
        ...enCommon,
        ...enHeader,
        ...enFooter,
        ...enHome,
        ...enAdmin,
        ...enMyProjects,
        ...enChatWidget,
        auth: enAuth
    },
    vi: {
        ...viCommon,
        ...viHeader,
        ...viFooter,
        ...viAccount,
        ...viHome,
        ...viAdmin,
        ...viMyProjects,
        ...viChatWidget,
        auth: viAuth
    },
    ja: {
        ...jaCommon,
        ...jaHome,
        ...jaHeader,
        ...jaFooter,
        ...jaAdmin,
        ...jaChatWidget,
        auth: jaAuth
    }
}

const i18n = createI18n({
    legacy: false,
    globalInjection: true,
    locale: localStorage.getItem('locale') || 'vi',
    fallbackLocale: 'en',
    messages,
});

export default i18n; 
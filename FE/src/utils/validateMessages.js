import i18n from '@/i18n';
import { pa } from 'element-plus/es/locales.mjs';

export const validateMessages = {
    required: (field) => i18n.global.t('message.ERR001', { name: field}),
    min: (field, min) => i18n.global.t('message.ERR002', { name: field, min }),
    max: (field, max) => i18n.global.t('message.ERR003', { name: field, max }),
    email: (field) => i18n.global.t('message.ERR004', { name: field }),
    phone: (field) => i18n.global.t('message.ERR005', { name: field }),
    pattern: (field) => i18n.global.t('message.ERR006', { name: field }),
    duplicate: (field) => i18n.global.t('message.ERR007', { name: field }),
    apiError: () => i18n.global.t('message.ERR008'),
}
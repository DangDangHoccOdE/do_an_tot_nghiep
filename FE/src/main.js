import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
// import Element Plus và css
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/styles/styles.css'
import './assets/styles/common.css'
// import vue-i18n
import i18n from './i18n'  

const app = createApp(App)
const pinia = createPinia()

// Tắt devtools overlay
app.config.devtools = false

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.use(i18n)
app.mount('#app')

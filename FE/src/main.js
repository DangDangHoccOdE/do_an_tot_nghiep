import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
// import Element Plus và css
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './assets/styles/styles.css'
import './assets/styles/common.css'
// import vue-i18n
import i18n from './i18n'  

const app = createApp(App)

// Tắt devtools overlay
app.config.devtools = false

app.use(router)
app.use(ElementPlus)
app.use(i18n)
app.mount('#app')

import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // 開發時把後端 API 請求轉發到 Spring Boot (port 8080)
  // 前端統一用相對路徑呼叫 (例如 /api/users/page?page=0)
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/coode_lab': 'http://localhost:8080',
      '/orders': 'http://localhost:8080',
      '/reports': 'http://localhost:8080',
      '/return-requests': 'http://localhost:8080',
      '/images/products': 'http://localhost:8080',
    },
  },
})

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    // Spring Boot가 정적 리소스로 바로 서빙할 수 있도록 백엔드 static 폴더에 직접 빌드
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    // 개발 서버(5173)에서 /api 요청을 백엔드(8080)로 프록시 - CORS 없이 세션 쿠키 그대로 전달
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})

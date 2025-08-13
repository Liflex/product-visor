import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0', // Доступен для всех сетевых интерфейсов
    port: 5173, // Порт по умолчанию
    proxy: {
      '/api/ozon': {
        target: 'http://localhost:9097',
        changeOrigin: true,
      },
      '/api/orders': {
        target: 'http://localhost:9088',
        changeOrigin: true,
      }
    }
  },
})

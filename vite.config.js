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
        target: 'http://192.168.1.59:9097',
        changeOrigin: true,
      },
      '/api/orders': {
        target: 'http://192.168.1.59:9088',
        changeOrigin: true,
      },
      '/api/companies': {
        target: 'http://192.168.1.59:9089',
        changeOrigin: true,
      },
      '/api/warehouses': {
        target: 'http://192.168.1.59:8085',
        changeOrigin: true,
      },
      '/api/auth': {
        target: 'http://192.168.1.59:9099',
        changeOrigin: true,
      }
    }
  },
})

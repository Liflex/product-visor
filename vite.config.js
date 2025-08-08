import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0', // Доступен для всех сетевых интерфейсов
    port: 5173, // Порт по умолчанию
  },
})

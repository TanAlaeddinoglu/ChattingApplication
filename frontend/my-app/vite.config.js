import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // SockJS expects a Node-like global; map it to window in the browser build
    global: 'window',
  },
})

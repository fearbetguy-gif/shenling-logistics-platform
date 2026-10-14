import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // 页面组件目前使用模板字符串定义，需要使用带编译器的 Vue 构建版本。
  resolve: {
    alias: {
      vue: 'vue/dist/vue.esm-bundler.js'
    }
  },
  base: './',
  server: { port: 5173, host: 'localhost' }
})

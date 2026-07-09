import { defineConfig } from "vite";
import uni from "@dcloudio/vite-plugin-uni";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [uni()],
  server: {
    // H5 dev 模式：/api 走代理到后端，避免浏览器跨域 (CORS)
    // target 的 localhost 指 dev server 所在机器（部署到 Linux 时即服务器本机），后端 18080 同机
    proxy: {
      '/api': {
        target: 'http://localhost:18080',
        changeOrigin: true,
      },
    },
  },
});

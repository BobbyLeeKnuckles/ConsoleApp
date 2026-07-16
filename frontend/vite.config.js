import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  // React plugin lets Vite understand JSX files.
  plugins: [react()],
  server: {
    // 5173 is the normal Vite dev server port.
    port: 5173,
    proxy: {
      // During React development, send API calls to the Spring Boot backend.
      "/api": "http://localhost:8080"
    }
  }
});

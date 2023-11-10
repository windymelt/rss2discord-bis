import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
  plugins: [scalaJSPlugin({
    projectID: 'frontendJS', // sbt-scalajs-crossproject adds JS suffix
  })],
  server: {
    proxy: {
      '^/io\.github\.windymelt\.rss2discordbis\.api\.v1\.Rss2DiscordBis/.*': 'http://localhost:8080',
    }
  },
});

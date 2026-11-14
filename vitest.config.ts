import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    include: ['src/main/web/**/*.spec.ts'],
    environment: 'jsdom',
  },
});

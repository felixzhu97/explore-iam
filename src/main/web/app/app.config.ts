import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideEchartsCore } from 'ngx-echarts';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    // Lazy-load treeshaken ECharts (same pattern as explore-ai).
    provideEchartsCore({
      echarts: () => import('./shared/charts/echarts.bundle').then((m) => m.default),
    }),
  ],
};

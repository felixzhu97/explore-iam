import { Component, input } from '@angular/core';

/** Shared split-panel chrome for password and MFA challenge pages. */
@Component({
  selector: 'app-auth-shell',
  styleUrl: '../../pages/login/login-page.component.css',
  host: { class: 'block min-h-dvh' },
  template: `
    <div class="flex min-h-dvh w-full bg-surface">
      <section
        class="relative flex min-h-dvh w-full flex-col justify-center px-6 py-10 sm:px-12 lg:w-1/2 lg:px-16 xl:px-20"
      >
        <div
          class="absolute top-6 left-6 z-10 flex items-center gap-2.5 text-foreground sm:top-8 sm:left-10 lg:left-12 xl:left-16"
        >
          <svg
            viewBox="0 0 24 24"
            class="size-5"
            aria-hidden="true"
            fill="none"
            stroke="currentColor"
            stroke-width="1.75"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M12 3l7 4v5c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V7l7-4z"
            ></path>
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              d="M9.5 12.5l1.75 1.75L14.75 10.5"
            ></path>
          </svg>
          <span class="text-base font-medium tracking-tight">Explore IAM</span>
        </div>

        <div class="mx-auto w-full max-w-md text-center">
          <ng-content />
        </div>
      </section>

      <aside
        class="relative hidden min-h-dvh overflow-hidden lg:flex lg:w-1/2 lg:flex-col lg:items-start lg:justify-center lg:p-12 xl:p-16"
        aria-hidden="true"
      >
        <div class="login-brand-panel absolute inset-0"></div>
        <div class="login-brand-beam"></div>
        <div class="login-brand-orb login-brand-orb-a"></div>
        <div class="login-brand-orb login-brand-orb-b"></div>
        <div class="login-brand-orb login-brand-orb-c"></div>
        <div class="login-brand-dots pointer-events-none absolute inset-0"></div>
        @if (mission()) {
          <div class="login-brand-mission relative z-10 max-w-md">
            <p class="text-3xl font-semibold leading-snug tracking-tight">{{ mission() }}</p>
          </div>
        }
      </aside>
    </div>
  `,
})
export class AuthShellComponent {
  /** Optional brand panel mission line (password login only). */
  readonly mission = input<string | null>(null);
}

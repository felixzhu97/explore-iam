import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ZardButtonComponent } from '../../shared/components/button/button.component';
import { ZardInputDirective } from '../../shared/components/input/input.directive';

@Component({
  selector: 'app-totp-challenge-page',
  imports: [FormsModule, ZardButtonComponent, ZardInputDirective, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './login-page.component.css',
  host: { class: 'block min-h-dvh' },
  template: `
    <div class="flex min-h-dvh w-full items-center justify-center bg-surface px-6">
      <div class="mx-auto w-full max-w-md text-center">
        <h1 class="text-2xl font-semibold tracking-tight text-foreground">验证 TOTP</h1>
        <p class="mt-2 text-sm text-muted-foreground">输入认证器中的 6 位代码，或使用备用码。</p>
        <form class="mt-8 flex flex-col gap-4 text-left" (ngSubmit)="onSubmit()">
          <label class="flex flex-col gap-1.5">
            <span class="text-sm font-medium text-foreground">验证码</span>
            <input
              z-input
              zSize="lg"
              name="code"
              autocomplete="one-time-code"
              inputmode="numeric"
              required
              [(ngModel)]="code"
            />
          </label>
          <button z-button zType="primary" zSize="lg" zFull type="submit" [zLoading]="submitting()">
            验证并继续
          </button>
        </form>
        <p class="mt-6 text-sm text-muted-foreground">
          <a routerLink="/login/ott" class="text-primary underline-offset-4 hover:underline">改用一次性令牌</a>
        </p>
      </div>
    </div>
  `,
})
export class TotpChallengePageComponent {
  code = '';
  readonly submitting = signal(false);

  onSubmit(): void {
    const code = this.code.trim();
    if (!code) {
      return;
    }
    this.submitting.set(true);
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/login/totp';
    form.style.display = 'none';
    appendHidden(form, 'code', code);
    const csrf = readCsrfToken();
    if (csrf) {
      appendHidden(form, '_csrf', csrf);
    }
    document.body.appendChild(form);
    form.submit();
  }
}

function appendHidden(form: HTMLFormElement, name: string, value: string): void {
  const input = document.createElement('input');
  input.type = 'hidden';
  input.name = name;
  input.value = value;
  form.appendChild(input);
}

function readCsrfToken(): string | null {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
  if (!match) {
    return null;
  }
  try {
    return decodeURIComponent(match[1]);
  } catch {
    return match[1];
  }
}

import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthShellComponent } from '../../shared/auth/auth-shell.component';
import { appendHidden, readCsrfToken, safeContinuePath } from '../../shared/auth/auth-form.util';
import { ZardButtonComponent } from '../../shared/components/button/button.component';
import { ZardInputDirective } from '../../shared/components/input/input.directive';

@Component({
  selector: 'app-totp-challenge-page',
  imports: [FormsModule, ZardButtonComponent, ZardInputDirective, RouterLink, AuthShellComponent],
  host: { class: 'block min-h-dvh' },
  template: `
    <app-auth-shell>
      <h1 class="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
        输入验证器中的验证码
      </h1>
      <p class="mt-2 text-sm text-muted-foreground">
        @if (useBackup()) {
          输入你保存的 8 位备用码。
        } @else {
          打开认证器 App，输入当前 6 位数字。
        }
      </p>

      @if (errorMessage()) {
        <div
          class="mt-6 rounded-lg border border-destructive/30 bg-destructive/5 px-3 py-2 text-left text-sm text-destructive"
          role="alert"
        >
          {{ errorMessage() }}
        </div>
      }

      <form class="mt-8 flex flex-col gap-4 text-left" (ngSubmit)="onSubmit()">
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-foreground">验证码</span>
          <input
            z-input
            zSize="lg"
            name="code"
            autocomplete="one-time-code"
            inputmode="numeric"
            [attr.maxlength]="useBackup() ? 8 : 6"
            required
            [(ngModel)]="code"
            [zStatus]="errorMessage() ? 'error' : undefined"
            class="tracking-[0.2em]"
          />
        </label>
        <button
          z-button
          zType="primary"
          zSize="lg"
          zFull
          type="submit"
          [zLoading]="submitting()"
          [zDisabled]="submitting()"
        >
          验证并继续
        </button>
      </form>

      <div class="mt-8 flex flex-col gap-2 text-sm text-muted-foreground">
        <button
          type="button"
          class="text-left text-primary underline-offset-4 hover:underline"
          (click)="toggleBackup()"
        >
          {{ useBackup() ? '改用验证器验证码' : '使用备用码' }}
        </button>
        <a
          [routerLink]="['/login/verify']"
          [queryParams]="continueQueryParams()"
          class="text-primary underline-offset-4 hover:underline"
        >
          换一种验证方式
        </a>
      </div>
    </app-auth-shell>
  `,
})
export class TotpChallengePageComponent implements OnInit {
  readonly #route = inject(ActivatedRoute);

  code = '';
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly useBackup = signal(false);
  readonly continuePath = signal<string | null>(null);

  ngOnInit(): void {
    const params = this.#route.snapshot.queryParamMap;
    this.continuePath.set(safeContinuePath(params.get('continue')));
    if (params.has('error')) {
      this.errorMessage.set('验证码不正确，请重试。');
    }
  }

  continueQueryParams(): Record<string, string> {
    const cont = this.continuePath();
    return cont ? { continue: cont } : {};
  }

  toggleBackup(): void {
    this.useBackup.update((v) => !v);
    this.code = '';
    this.errorMessage.set(null);
  }

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
    const cont = this.continuePath();
    if (cont) {
      appendHidden(form, 'continue', cont);
    }
    const csrf = readCsrfToken();
    if (csrf) {
      appendHidden(form, '_csrf', csrf);
    }
    document.body.appendChild(form);
    form.submit();
  }
}

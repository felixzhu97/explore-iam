import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { AuthShellComponent } from '../../shared/auth/auth-shell.component';
import { appendHidden, readCsrfToken, safeContinuePath } from '../../shared/auth/auth-form.util';
import { ZardButtonComponent } from '../../shared/components/button/button.component';
import { ZardInputDirective } from '../../shared/components/input/input.directive';

const REMEMBER_USERNAME_KEY = 'explore-iam.remember-username';

interface LoginContext {
  clientId: string | null;
  clientName: string | null;
  oauth: boolean;
}

@Component({
  selector: 'app-login-page',
  imports: [FormsModule, ZardButtonComponent, ZardInputDirective, AuthShellComponent],
  host: { class: 'block min-h-dvh' },
  template: `
    <app-auth-shell mission="连接身份与应用，让正确的人在正确的时间使用正确的技术。">
      <h1 class="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">
        登录 Explore IAM
      </h1>
      @if (oauthSubtitle()) {
        <p class="mt-2 text-sm text-muted-foreground">{{ oauthSubtitle() }}</p>
      }

      @if (errorMessage()) {
        <div
          class="mt-6 rounded-lg border border-destructive/30 bg-destructive/5 px-3 py-2 text-left text-sm text-destructive"
          role="alert"
        >
          {{ errorMessage() }}
        </div>
      }

      <form class="mt-8 flex flex-col gap-5 text-left" (ngSubmit)="onSubmit()" #loginForm="ngForm">
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-foreground">用户名</span>
          <input
            z-input
            zSize="lg"
            name="username"
            autocomplete="username"
            required
            [(ngModel)]="username"
            [zStatus]="errorMessage() ? 'error' : undefined"
            (keydown.enter)="onEnterKey($event)"
          />
        </label>

        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-foreground">密码</span>
          <div class="relative">
            <input
              z-input
              zSize="lg"
              class="pr-16"
              [type]="showPassword() ? 'text' : 'password'"
              name="password"
              autocomplete="current-password"
              required
              [(ngModel)]="password"
              [zStatus]="errorMessage() ? 'error' : undefined"
              (keydown.enter)="onEnterKey($event)"
            />
            <button
              type="button"
              class="absolute top-1/2 right-2 -translate-y-1/2 text-xs font-medium text-muted-foreground hover:text-foreground"
              (click)="showPassword.set(!showPassword())"
            >
              {{ showPassword() ? '隐藏' : '显示' }}
            </button>
          </div>
        </label>

        <label class="flex items-center gap-2 text-sm text-muted-foreground">
          <input
            type="checkbox"
            class="size-4 rounded border-input accent-primary"
            [(ngModel)]="rememberUsername"
            name="rememberUsername"
          />
          在此设备记住用户名
        </label>

        <button
          z-button
          zType="primary"
          zSize="lg"
          zFull
          type="submit"
          [zLoading]="submitting()"
          [zDisabled]="submitting()"
          class="mt-2 h-11 text-base"
        >
          登录
        </button>
      </form>
    </app-auth-shell>
  `,
})
export class LoginPageComponent implements OnInit {
  readonly #route = inject(ActivatedRoute);
  readonly #http = inject(HttpClient);

  username = '';
  password = '';
  rememberUsername = false;

  readonly showPassword = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly clientId = signal<string | null>(null);
  readonly oauthSubtitle = signal<string | null>(null);
  readonly continuePath = signal<string | null>(null);

  ngOnInit(): void {
    const stored = localStorage.getItem(REMEMBER_USERNAME_KEY);
    if (stored) {
      this.username = stored;
      this.rememberUsername = true;
    }

    const params = this.#route.snapshot.queryParamMap;
    if (params.has('error')) {
      this.errorMessage.set('用户名或密码不正确，请重试。');
    }

    const clientId = params.get('client_id')?.trim() || null;
    this.clientId.set(clientId);
    this.continuePath.set(safeContinuePath(params.get('continue')));
    this.#loadLoginContext(clientId);
  }

  onEnterKey(event: Event): void {
    event.preventDefault();
    if (!this.submitting()) {
      this.onSubmit();
    }
  }

  onSubmit(): void {
    const username = this.username.trim();
    const password = this.password;
    if (!username || !password) {
      this.errorMessage.set('请输入用户名和密码。');
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    if (this.rememberUsername) {
      localStorage.setItem(REMEMBER_USERNAME_KEY, username);
    } else {
      localStorage.removeItem(REMEMBER_USERNAME_KEY);
    }

    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/login';
    form.style.display = 'none';

    appendHidden(form, 'username', username);
    appendHidden(form, 'password', password);
    const clientId = this.clientId();
    if (clientId) {
      appendHidden(form, 'client_id', clientId);
    }
    const continuePath = this.continuePath();
    if (continuePath) {
      appendHidden(form, 'continue', continuePath);
    }
    const csrf = readCsrfToken();
    if (csrf) {
      appendHidden(form, '_csrf', csrf);
    }

    document.body.appendChild(form);
    form.submit();
  }

  #loadLoginContext(clientId: string | null): void {
    const url = clientId
      ? `/api/login/context?client_id=${encodeURIComponent(clientId)}`
      : '/api/login/context';

    this.#http.get<LoginContext>(url).subscribe({
      next: (ctx) => {
        if (ctx.oauth) {
          const name = ctx.clientName || ctx.clientId || '应用';
          this.oauthSubtitle.set(`继续访问 ${name}`);
        } else {
          this.oauthSubtitle.set(null);
        }
      },
      error: () => {
        if (clientId) {
          this.oauthSubtitle.set(`继续访问 ${clientId}`);
        }
      },
    });
  }
}

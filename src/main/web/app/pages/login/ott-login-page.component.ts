import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthShellComponent } from '../../shared/auth/auth-shell.component';
import { appendHidden, readCsrfToken, safeContinuePath } from '../../shared/auth/auth-form.util';
import { ZardButtonComponent } from '../../shared/components/button/button.component';
import { ZardInputDirective } from '../../shared/components/input/input.directive';

const REMEMBER_USERNAME_KEY = 'explore-iam.remember-username';

@Component({
  selector: 'app-ott-login-page',
  imports: [FormsModule, ZardButtonComponent, ZardInputDirective, RouterLink, AuthShellComponent],
  host: { class: 'block min-h-dvh' },
  template: `
    <app-auth-shell>
      <h1 class="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">输入验证码</h1>
      <p class="mt-2 text-sm text-muted-foreground">
        @if (hasToken()) {
          我们已准备好验证码。确认后即可完成登录。
        } @else {
          先发送验证码，然后在下方输入以完成登录。
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

      @if (!hasToken()) {
        <form class="mt-8 flex flex-col gap-4 text-left" (ngSubmit)="onGenerate()">
          <label class="flex flex-col gap-1.5">
            <span class="text-sm font-medium text-foreground">用户名</span>
            <input
              z-input
              zSize="lg"
              name="username"
              autocomplete="username"
              required
              [(ngModel)]="username"
            />
          </label>
          <button
            z-button
            zType="default"
            zSize="lg"
            zFull
            type="submit"
            [zLoading]="generating()"
            [zDisabled]="generating()"
          >
            发送验证码
          </button>
        </form>
      }

      <form class="mt-8 flex flex-col gap-4 text-left" (ngSubmit)="onSubmitToken()">
        <label class="flex flex-col gap-1.5">
          <span class="text-sm font-medium text-foreground">验证码</span>
          <input
            z-input
            zSize="lg"
            name="token"
            autocomplete="one-time-code"
            required
            [(ngModel)]="token"
            [zStatus]="errorMessage() ? 'error' : undefined"
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
        <a [routerLink]="['/login/verify']" [queryParams]="continueQueryParams()" class="text-primary underline-offset-4 hover:underline">
          换一种验证方式
        </a>
        <a routerLink="/login" class="underline-offset-4 hover:underline">返回密码登录</a>
      </div>
    </app-auth-shell>
  `,
})
export class OttLoginPageComponent implements OnInit {
  readonly #route = inject(ActivatedRoute);

  username = '';
  token = '';

  readonly generating = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly hasToken = signal(false);
  readonly continuePath = signal<string | null>(null);

  ngOnInit(): void {
    const stored = localStorage.getItem(REMEMBER_USERNAME_KEY);
    if (stored) {
      this.username = stored;
    }
    const params = this.#route.snapshot.queryParamMap;
    this.continuePath.set(safeContinuePath(params.get('continue')));
    const token = params.get('token')?.trim();
    if (token) {
      this.token = token;
      this.hasToken.set(true);
    }
    if (params.has('error')) {
      this.errorMessage.set('验证码无效或已过期，请重新发送。');
    }
  }

  continueQueryParams(): Record<string, string> {
    const cont = this.continuePath();
    return cont ? { continue: cont } : {};
  }

  onGenerate(): void {
    const username = this.username.trim();
    if (!username) {
      this.errorMessage.set('请输入用户名。');
      return;
    }
    this.generating.set(true);
    this.errorMessage.set(null);
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/ott/generate';
    form.style.display = 'none';
    appendHidden(form, 'username', username);
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

  onSubmitToken(): void {
    const token = this.token.trim();
    if (!token) {
      this.errorMessage.set('请输入验证码。');
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/login/ott';
    form.style.display = 'none';
    appendHidden(form, 'token', token);
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

import { ChangeDetectionStrategy, Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ZardButtonComponent } from '../../shared/components/button/button.component';
import { ZardInputDirective } from '../../shared/components/input/input.directive';

const REMEMBER_USERNAME_KEY = 'explore-iam.remember-username';

@Component({
  selector: 'app-ott-login-page',
  imports: [FormsModule, ZardButtonComponent, ZardInputDirective, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './login-page.component.css',
  host: { class: 'block min-h-dvh' },
  template: `
    <div class="flex min-h-dvh w-full bg-surface">
      <section
        class="relative flex min-h-dvh w-full flex-col justify-center px-6 py-10 sm:px-12 lg:w-1/2 lg:px-16 xl:px-20"
      >
        <div
          class="absolute top-6 left-6 z-10 flex items-center gap-2.5 text-foreground sm:top-8 sm:left-10 lg:left-12 xl:left-16"
        >
          <span class="text-base font-medium tracking-tight">Explore IAM</span>
        </div>

        <div class="mx-auto w-full max-w-md text-center">
          <h1 class="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">验证一次性令牌</h1>
          <p class="mt-2 text-sm text-muted-foreground">
            密码之后还需 One-Time Token。先生成令牌（开发环境见服务日志中的 magic link），再提交。
          </p>

          @if (errorMessage()) {
            <div
              class="mt-6 rounded-lg border border-destructive/30 bg-destructive/5 px-3 py-2 text-left text-sm text-destructive"
              role="alert"
            >
              {{ errorMessage() }}
            </div>
          }

          <form class="mt-8 flex flex-col gap-4 text-left" (ngSubmit)="onGenerate()">
            <label class="flex flex-col gap-1.5">
              <span class="text-sm font-medium text-foreground">用户名</span>
              <input z-input zSize="lg" name="username" autocomplete="username" required [(ngModel)]="username" />
            </label>
            <button z-button zType="default" zSize="lg" zFull type="submit" [zLoading]="generating()" [zDisabled]="generating()">
              发送一次性令牌
            </button>
          </form>

          <form class="mt-8 flex flex-col gap-4 text-left" (ngSubmit)="onSubmitToken()">
            <label class="flex flex-col gap-1.5">
              <span class="text-sm font-medium text-foreground">令牌</span>
              <input
                z-input
                zSize="lg"
                name="token"
                autocomplete="one-time-code"
                required
                [(ngModel)]="token"
              />
            </label>
            <button z-button zType="primary" zSize="lg" zFull type="submit" [zLoading]="submitting()" [zDisabled]="submitting()">
              验证并继续
            </button>
          </form>

          <p class="mt-6 text-sm text-muted-foreground">
            <a routerLink="/login" class="text-primary underline-offset-4 hover:underline">返回密码登录</a>
          </p>
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
      </aside>
    </div>
  `,
})
export class OttLoginPageComponent implements OnInit {
  username = '';
  token = '';

  readonly generating = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor(private readonly route: ActivatedRoute) {}

  ngOnInit(): void {
    const stored = localStorage.getItem(REMEMBER_USERNAME_KEY);
    if (stored) {
      this.username = stored;
    }
    const params = this.route.snapshot.queryParamMap;
    const token = params.get('token')?.trim();
    if (token) {
      this.token = token;
    }
    if (params.has('error')) {
      this.errorMessage.set('一次性令牌无效或已过期，请重新生成。');
    }
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
      this.errorMessage.set('请输入令牌。');
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/login/ott';
    form.style.display = 'none';
    appendHidden(form, 'token', token);
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

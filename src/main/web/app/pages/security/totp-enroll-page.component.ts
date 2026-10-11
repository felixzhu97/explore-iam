import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ZardButtonComponent } from '../../shared/components/button/button.component';
import { ZardInputDirective } from '../../shared/components/input/input.directive';

interface Enrollment {
  secret: string;
  otpAuthUri: string;
  backupCodes: string[];
}

@Component({
  selector: 'app-totp-enroll-page',
  imports: [FormsModule, ZardButtonComponent, ZardInputDirective, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block min-h-dvh' },
  template: `
    <div class="mx-auto max-w-lg px-6 py-12">
      <h1 class="text-2xl font-semibold tracking-tight">注册 TOTP</h1>
      <p class="mt-2 text-sm text-muted-foreground">
        在认证器中添加密钥或 otpauth URI，然后输入当前代码以启用。
      </p>

      @if (!enrollment()) {
        <button class="mt-8" z-button zType="primary" type="button" (click)="start()" [zLoading]="loading()">
          开始注册
        </button>
      } @else {
        <div class="mt-6 space-y-3 text-left text-sm">
          <p><span class="font-medium">Secret:</span> {{ enrollment()!.secret }}</p>
          <p class="break-all"><span class="font-medium">URI:</span> {{ enrollment()!.otpAuthUri }}</p>
          <p><span class="font-medium">备用码:</span> {{ enrollment()!.backupCodes.join(', ') }}</p>
        </div>
        <form class="mt-8 flex flex-col gap-4" (ngSubmit)="confirm()">
          <label class="flex flex-col gap-1.5 text-left">
            <span class="text-sm font-medium">验证码</span>
            <input z-input zSize="lg" name="code" required [(ngModel)]="code" />
          </label>
          <button z-button zType="primary" zSize="lg" type="submit" [zLoading]="loading()">确认启用</button>
        </form>
      }

      @if (message()) {
        <p class="mt-4 text-sm text-primary">{{ message() }}</p>
      }

      <p class="mt-8 text-sm">
        <a routerLink="/" class="text-primary underline-offset-4 hover:underline">返回首页</a>
      </p>
    </div>
  `,
})
export class TotpEnrollPageComponent implements OnInit {
  private readonly http = inject(HttpClient);

  code = '';
  readonly enrollment = signal<Enrollment | null>(null);
  readonly loading = signal(false);
  readonly message = signal<string | null>(null);
  readonly enabled = signal(false);

  ngOnInit(): void {
    this.http.get<{ enabled: boolean }>('/api/v1/totp/status').subscribe({
      next: (s) => this.enabled.set(s.enabled),
    });
  }

  start(): void {
    this.loading.set(true);
    this.message.set(null);
    this.http.post<Enrollment>('/api/v1/totp/enrollments', {}).subscribe({
      next: (body) => {
        this.enrollment.set(body);
        this.loading.set(false);
      },
      error: () => {
        this.message.set('无法开始注册（需要完整 MFA 会话）。');
        this.loading.set(false);
      },
    });
  }

  confirm(): void {
    this.loading.set(true);
    this.http.post('/api/v1/totp/enrollments/confirm', { code: this.code.trim() }).subscribe({
      next: () => {
        this.message.set('TOTP 已启用。');
        this.enabled.set(true);
        this.loading.set(false);
      },
      error: () => {
        this.message.set('验证码无效。');
        this.loading.set(false);
      },
    });
  }
}

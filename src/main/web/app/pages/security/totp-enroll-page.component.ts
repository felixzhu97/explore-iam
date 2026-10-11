import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import QRCode from 'qrcode';

import { ZardButtonComponent } from '../../shared/components/button/button.component';
import { ZardInputDirective } from '../../shared/components/input/input.directive';

interface Enrollment {
  secret: string;
  otpAuthUri: string;
  backupCodes: string[];
}

type Step = 'start' | 'scan' | 'confirm' | 'backup' | 'done';

@Component({
  selector: 'app-totp-enroll-page',
  imports: [FormsModule, ZardButtonComponent, ZardInputDirective, RouterLink],
  host: { class: 'block min-h-dvh' },
  template: `
    <div class="mx-auto max-w-lg px-6 py-12">
      <h1 class="text-2xl font-semibold tracking-tight">验证器</h1>
      <p class="mt-2 text-sm text-muted-foreground">
        用 Google Authenticator 等应用完成绑定，登录时输入 6 位验证码。
      </p>

      @if (enabled() && step() === 'done') {
        <p class="mt-8 text-sm text-foreground">验证器已开启。登录时将要求输入验证码。</p>
      } @else if (enabled() && step() === 'start') {
        <p class="mt-8 text-sm text-foreground">此账户已开启验证器。</p>
        <button class="mt-6" z-button zType="default" type="button" (click)="start()" [zLoading]="loading()">
          重新绑定
        </button>
      } @else if (step() === 'start') {
        <button class="mt-8" z-button zType="primary" type="button" (click)="start()" [zLoading]="loading()">
          开始设置
        </button>
      } @else if (step() === 'scan' && enrollment()) {
        <div class="mt-8 flex flex-col items-start gap-6">
          <p class="text-sm font-medium text-foreground">1. 用认证器扫描二维码</p>
          @if (qrDataUrl()) {
            <img
              [src]="qrDataUrl()!"
              width="220"
              height="220"
              alt="TOTP 注册二维码"
              class="rounded-lg border border-border bg-white p-2"
            />
          }
          <details class="w-full text-left text-sm text-muted-foreground">
            <summary class="cursor-pointer text-primary underline-offset-4 hover:underline">无法扫码？</summary>
            <p class="mt-3 font-mono tracking-wide text-foreground">{{ enrollment()!.secret }}</p>
          </details>
          <button z-button zType="primary" zSize="lg" type="button" (click)="step.set('confirm')">
            下一步
          </button>
        </div>
      } @else if (step() === 'confirm') {
        <p class="mt-8 text-sm font-medium text-foreground">2. 输入认证器中的 6 位验证码</p>
        <form class="mt-4 flex flex-col gap-4" (ngSubmit)="confirm()">
          <label class="flex flex-col gap-1.5 text-left">
            <span class="text-sm font-medium">验证码</span>
            <input
              z-input
              zSize="lg"
              name="code"
              autocomplete="one-time-code"
              inputmode="numeric"
              maxlength="6"
              required
              [(ngModel)]="code"
              class="tracking-[0.2em]"
            />
          </label>
          <button z-button zType="primary" zSize="lg" type="submit" [zLoading]="loading()">确认启用</button>
        </form>
      } @else if (step() === 'backup' && enrollment()) {
        <p class="mt-8 text-sm font-medium text-foreground">3. 保存备用码</p>
        <p class="mt-2 text-sm text-muted-foreground">
          每个备用码只能使用一次。请立即复制并妥善保存，离开本页后将无法再次查看。
        </p>
        <pre
          class="mt-4 overflow-x-auto rounded-lg border border-border bg-muted/40 p-4 text-left text-sm tracking-wide"
          >{{ enrollment()!.backupCodes.join('\n') }}</pre
        >
        <div class="mt-4 flex flex-wrap gap-3">
          <button z-button zType="default" type="button" (click)="copyBackupCodes()">复制备用码</button>
          <button
            z-button
            zType="primary"
            type="button"
            [zDisabled]="!backupAck()"
            (click)="finishBackup()"
          >
            我已保存
          </button>
        </div>
        <label class="mt-4 flex items-center gap-2 text-sm text-muted-foreground">
          <input type="checkbox" class="size-4 rounded border-input accent-primary" [(ngModel)]="backupAckModel" name="backupAck" (ngModelChange)="backupAck.set($event)" />
          我已将备用码保存在安全位置
        </label>
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
  readonly #http = inject(HttpClient);

  code = '';
  backupAckModel = false;

  readonly enrollment = signal<Enrollment | null>(null);
  readonly qrDataUrl = signal<string | null>(null);
  readonly loading = signal(false);
  readonly message = signal<string | null>(null);
  readonly enabled = signal(false);
  readonly step = signal<Step>('start');
  readonly backupAck = signal(false);

  ngOnInit(): void {
    this.#http.get<{ enabled: boolean }>('/api/v1/totp/status').subscribe({
      next: (s) => {
        this.enabled.set(s.enabled);
        if (s.enabled) {
          this.step.set('start');
        }
      },
    });
  }

  start(): void {
    this.loading.set(true);
    this.message.set(null);
    this.qrDataUrl.set(null);
    this.backupAck.set(false);
    this.backupAckModel = false;
    this.#http.post<Enrollment>('/api/v1/totp/enrollments', {}).subscribe({
      next: (body) => {
        this.enrollment.set(body);
        this.loading.set(false);
        this.step.set('scan');
        void this.#renderQr(body.otpAuthUri);
      },
      error: () => {
        this.message.set('无法开始设置（需要完整登录会话）。');
        this.loading.set(false);
      },
    });
  }

  confirm(): void {
    this.loading.set(true);
    this.#http.post('/api/v1/totp/enrollments/confirm', { code: this.code.trim() }).subscribe({
      next: () => {
        this.enabled.set(true);
        this.loading.set(false);
        this.step.set('backup');
        this.message.set(null);
      },
      error: () => {
        this.message.set('验证码无效，请重试。');
        this.loading.set(false);
      },
    });
  }

  async copyBackupCodes(): Promise<void> {
    const codes = this.enrollment()?.backupCodes;
    if (!codes?.length) {
      return;
    }
    try {
      await navigator.clipboard.writeText(codes.join('\n'));
      this.message.set('备用码已复制。');
    } catch {
      this.message.set('无法复制，请手动选中保存。');
    }
  }

  finishBackup(): void {
    if (!this.backupAck()) {
      return;
    }
    this.step.set('done');
    this.message.set('验证器已启用。');
  }

  async #renderQr(otpAuthUri: string): Promise<void> {
    try {
      const url = await QRCode.toDataURL(otpAuthUri, {
        errorCorrectionLevel: 'M',
        margin: 1,
        width: 220,
        color: { dark: '#000000', light: '#ffffff' },
      });
      this.qrDataUrl.set(url);
    } catch {
      this.message.set('二维码生成失败，请展开「无法扫码？」手动添加密钥。');
    }
  }
}

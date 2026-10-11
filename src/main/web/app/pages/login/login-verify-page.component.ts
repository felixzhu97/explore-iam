import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { AuthShellComponent } from '../../shared/auth/auth-shell.component';
import { safeContinuePath } from '../../shared/auth/auth-form.util';
import { ZardButtonComponent } from '../../shared/components/button/button.component';

interface MfaOptions {
  totpEnabled: boolean;
  passkeysAvailable: boolean;
  ottAvailable: boolean;
}

type Factor = 'totp' | 'ott' | 'webauthn';

@Component({
  selector: 'app-login-verify-page',
  imports: [AuthShellComponent, ZardButtonComponent, RouterLink],
  host: { class: 'block min-h-dvh' },
  template: `
    <app-auth-shell>
      <h1 class="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">验证你的身份</h1>
      <p class="mt-2 text-sm text-muted-foreground">密码正确。请完成第二步验证以继续。</p>

      @if (errorMessage()) {
        <div
          class="mt-6 rounded-lg border border-destructive/30 bg-destructive/5 px-3 py-2 text-left text-sm text-destructive"
          role="alert"
        >
          {{ errorMessage() }}
        </div>
      }

      @if (loading()) {
        <p class="mt-10 text-sm text-muted-foreground">正在加载验证方式…</p>
      } @else {
        <div class="mt-10 flex flex-col gap-3 text-left">
          @if (options()?.totpEnabled) {
            <button
              z-button
              zType="primary"
              zSize="lg"
              zFull
              type="button"
              class="h-12 justify-start px-5 text-base"
              (click)="go('totp')"
            >
              使用验证器 App
            </button>
          }
          @if (options()?.passkeysAvailable) {
            <button
              z-button
              [zType]="options()?.totpEnabled ? 'default' : 'primary'"
              zSize="lg"
              zFull
              type="button"
              class="h-12 justify-start px-5 text-base"
              [zLoading]="webauthnBusy()"
              (click)="go('webauthn')"
            >
              使用通行密钥
            </button>
          }
          @if (options()?.ottAvailable) {
            <button
              z-button
              [zType]="primaryIsOtt() ? 'primary' : 'default'"
              zSize="lg"
              zFull
              type="button"
              class="h-12 justify-start px-5 text-base"
              (click)="go('ott')"
            >
              使用邮箱验证码
            </button>
          }
        </div>

        <p class="mt-8 text-sm text-muted-foreground">
          <a routerLink="/login" class="text-primary underline-offset-4 hover:underline">返回密码登录</a>
        </p>
      }
    </app-auth-shell>
  `,
})
export class LoginVerifyPageComponent implements OnInit {
  readonly #http = inject(HttpClient);
  readonly #route = inject(ActivatedRoute);
  readonly #router = inject(Router);

  readonly loading = signal(true);
  readonly webauthnBusy = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly options = signal<MfaOptions | null>(null);
  readonly continuePath = signal<string | null>(null);

  ngOnInit(): void {
    const params = this.#route.snapshot.queryParamMap;
    this.continuePath.set(safeContinuePath(params.get('continue')));
    const preferred = this.#preferredFromQuery(params.getAll('factor.type'));

    this.#http.get<MfaOptions>('/api/login/mfa-options').subscribe({
      next: (opts) => {
        this.options.set(opts);
        this.loading.set(false);
        if (preferred === 'totp' && opts.totpEnabled) {
          this.go('totp');
        } else if (preferred === 'webauthn' && opts.passkeysAvailable) {
          this.go('webauthn');
        } else if (
          preferred === 'ott' &&
          opts.ottAvailable &&
          !opts.totpEnabled &&
          !opts.passkeysAvailable
        ) {
          this.go('ott');
        }
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('登录状态已失效，请重新输入密码。');
      },
    });
  }

  primaryIsOtt(): boolean {
    const opts = this.options();
    if (!opts) {
      return false;
    }
    return !opts.totpEnabled && !opts.passkeysAvailable;
  }

  go(factor: Factor): void {
    const q = this.#continueQuery();
    if (factor === 'totp') {
      void this.#router.navigateByUrl(`/login/totp${q}`);
      return;
    }
    if (factor === 'ott') {
      void this.#router.navigateByUrl(`/login/ott${q}`);
      return;
    }
    void this.#startWebAuthn();
  }

  async #startWebAuthn(): Promise<void> {
    this.webauthnBusy.set(true);
    this.errorMessage.set(null);
    try {
      const csrfMatch = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
      const token = csrfMatch ? decodeURIComponent(csrfMatch[1]) : '';
      const res = await fetch('/webauthn/authenticate/options', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': token,
        },
        body: '{}',
      });
      if (!res.ok) {
        this.errorMessage.set('无法启动通行密钥验证，请改用其他方式。');
        return;
      }
      if (!window.PublicKeyCredential) {
        this.errorMessage.set('此浏览器不支持通行密钥，请改用验证器或邮箱验证码。');
        return;
      }
      const options = (await res.json()) as Record<string, unknown>;
      const publicKey = this.#toPublicKeyRequest(options);
      const credential = (await navigator.credentials.get({
        publicKey,
      })) as PublicKeyCredential | null;
      if (!credential) {
        this.errorMessage.set('未完成通行密钥验证。');
        return;
      }
      const finish = await fetch('/webauthn/authenticate', {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
          'X-XSRF-TOKEN': token,
        },
        body: JSON.stringify(this.#credentialToJson(credential)),
      });
      if (finish.ok || finish.redirected || finish.status === 204) {
        window.location.href = this.continuePath() || '/';
        return;
      }
      this.errorMessage.set('通行密钥验证失败，请重试或改用其他方式。');
    } catch {
      this.errorMessage.set('通行密钥验证已取消或失败。');
    } finally {
      this.webauthnBusy.set(false);
    }
  }

  #toPublicKeyRequest(options: Record<string, unknown>): PublicKeyCredentialRequestOptions {
    const allowCredentials = options['allowCredentials'] as Array<Record<string, unknown>> | undefined;
    return {
      challenge: this.#b64urlToBuffer(String(options['challenge'])),
      rpId: options['rpId'] as string | undefined,
      timeout: options['timeout'] as number | undefined,
      userVerification: options['userVerification'] as UserVerificationRequirement | undefined,
      allowCredentials: allowCredentials?.map((c) => ({
        type: 'public-key' as const,
        id: this.#b64urlToBuffer(String(c['id'])),
        transports: c['transports'] as AuthenticatorTransport[] | undefined,
      })),
    };
  }

  #credentialToJson(credential: PublicKeyCredential): Record<string, unknown> {
    const response = credential.response as AuthenticatorAssertionResponse;
    return {
      id: credential.id,
      rawId: this.#bufferToB64url(credential.rawId),
      type: credential.type,
      response: {
        clientDataJSON: this.#bufferToB64url(response.clientDataJSON),
        authenticatorData: this.#bufferToB64url(response.authenticatorData),
        signature: this.#bufferToB64url(response.signature),
        userHandle: response.userHandle ? this.#bufferToB64url(response.userHandle) : null,
      },
      clientExtensionResults: credential.getClientExtensionResults(),
    };
  }

  #b64urlToBuffer(value: string): ArrayBuffer {
    const pad = '='.repeat((4 - (value.length % 4)) % 4);
    const b64 = (value + pad).replace(/-/g, '+').replace(/_/g, '/');
    const binary = atob(b64);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer;
  }

  #bufferToB64url(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    for (const b of bytes) {
      binary += String.fromCharCode(b);
    }
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  }

  #continueQuery(): string {
    const cont = this.continuePath();
    return cont ? `?continue=${encodeURIComponent(cont)}` : '';
  }

  #preferredFromQuery(types: string[]): Factor | null {
    if (types.includes('totp')) {
      return 'totp';
    }
    if (types.includes('webauthn')) {
      return 'webauthn';
    }
    if (types.includes('ott')) {
      return 'ott';
    }
    return null;
  }
}

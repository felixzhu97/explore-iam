import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import { csrfHeaders } from '../../shared/csrf';
import type { ClientView } from './clients-list-page.component';

const CARD =
  'rounded-lg border border-[var(--console-border)] bg-white shadow-[0_1px_2px_rgba(0,0,0,0.04)]';
const BTN_PRIMARY =
  'inline-flex h-9 items-center justify-center rounded-md border border-transparent bg-[var(--console-accent-soft)] px-4 text-sm font-medium text-white no-underline hover:bg-[var(--console-accent)] disabled:cursor-not-allowed disabled:opacity-50';
const BTN_SECONDARY =
  'inline-flex h-9 items-center justify-center rounded-md border border-[var(--console-border)] bg-white px-4 text-sm font-medium text-[var(--console-fg)] no-underline hover:bg-[var(--console-bg)]';
const FIELD =
  'block h-10 w-full rounded-md border border-[var(--console-border)] bg-white px-3 py-2 text-sm leading-5 text-[var(--console-fg)] outline-none focus:border-[var(--console-accent)] focus:shadow-[0_0_0_3px_rgba(0,81,195,0.18)]';
const FIELD_TEXTAREA =
  'block h-auto min-h-20 w-full rounded-md border border-[var(--console-border)] bg-white px-3 pt-2.5 pb-2 text-sm leading-5 text-[var(--console-fg)] outline-none focus:border-[var(--console-accent)] focus:shadow-[0_0_0_3px_rgba(0,81,195,0.18)]';

@Component({
  selector: 'app-clients-create-page',
  imports: [ConsoleShellComponent, FormsModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-[1.75rem] font-semibold tracking-tight">创建 OAuth 客户端</h1>
          <p class="mt-1 text-sm text-[var(--console-muted)]">创建新的 OAuth 客户端</p>
        </div>
        <a
          href="https://docs.spring.io/spring-authorization-server/reference/"
          target="_blank"
          rel="noopener noreferrer"
          class="${BTN_SECONDARY}"
          >文档</a
        >
      </div>

      @if (errorMessage()) {
        <div
          class="mt-4 rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive"
          role="alert"
        >
          {{ errorMessage() }}
        </div>
      }

      @if (createdClientId()) {
        <section class="${CARD} mt-6 p-6">
          <h2 class="text-lg font-semibold">客户端已创建</h2>
          <p class="mt-1 text-sm text-[var(--console-muted)]">
            请立即保存密钥（仅显示一次，之后无法再查看）。
          </p>
          <dl class="mt-4 space-y-3 text-sm">
            <div>
              <dt class="text-[var(--console-muted)]">client_id</dt>
              <dd class="mt-0.5 break-all font-mono text-[13px]">{{ createdClientId() }}</dd>
            </div>
            @if (createdSecret()) {
              <div>
                <dt class="text-[var(--console-muted)]">client_secret</dt>
                <dd class="mt-0.5 break-all font-mono text-[13px]">{{ createdSecret() }}</dd>
              </div>
            } @else {
              <p class="text-sm text-[var(--console-muted)]">公共客户端（None / PKCE）未生成 client_secret。</p>
            }
          </dl>
          <a routerLink="/clients" class="${BTN_PRIMARY} mt-6">返回列表</a>
        </section>
      } @else {
        <div class="mt-6 grid items-start gap-10 lg:grid-cols-[minmax(0,1fr)_200px]">
          <div>
            <section class="${CARD} p-6 sm:p-8">
              @if (step() === 1) {
                <h2 class="text-lg font-semibold">配置 OAuth 客户端</h2>
                <p class="mt-2 text-sm leading-relaxed text-[var(--console-muted)]">
                  客户端默认视为机密客户端。公共客户端请将令牌身份验证方法设为
                  <strong class="font-medium text-[var(--console-fg)]">None (PKCE)</strong>。
                </p>

                <form class="mt-6 flex flex-col gap-5" (ngSubmit)="goStep2()" id="create-step-1">
                  <label class="flex flex-col gap-1.5 text-left">
                    <span class="text-sm font-semibold">客户端名称</span>
                    <input
                      class="${FIELD}"
                      name="clientName"
                      required
                      [(ngModel)]="clientName"
                      autocomplete="off"
                    />
                  </label>

                  <label class="flex flex-col gap-1.5 text-left">
                    <span class="text-sm font-semibold">响应类型</span>
                    <select class="${FIELD}" name="responseType" required [(ngModel)]="responseType">
                      <option value="code">Code</option>
                    </select>
                  </label>

                  <label class="flex flex-col gap-1.5 text-left">
                    <span class="text-sm font-semibold">授权类型</span>
                    <select class="${FIELD}" name="grantPreset" required [(ngModel)]="grantPreset">
                      <option value="authorization_code">Authorization Code</option>
                      <option value="authorization_code,refresh_token">
                        Authorization Code, Refresh Token
                      </option>
                    </select>
                  </label>

                  <label class="flex flex-col gap-1.5 text-left">
                    <span class="text-sm font-semibold">令牌身份验证方法</span>
                    <select
                      class="${FIELD}"
                      name="tokenAuthMethod"
                      required
                      [(ngModel)]="tokenAuthMethod"
                    >
                      <option value="">选择一种方法</option>
                      <option value="none">None (PKCE)</option>
                      <option value="client_secret_basic">client_secret_basic</option>
                      <option value="client_secret_post">client_secret_post</option>
                    </select>
                  </label>

                  <label class="flex flex-col gap-1.5 text-left">
                    <span class="text-sm font-semibold">重定向（回调）URL</span>
                    <input
                      class="${FIELD}"
                      name="redirectUri"
                      required
                      [(ngModel)]="redirectUri"
                      placeholder="https://example.com/callback"
                    />
                  </label>

                  <label class="flex flex-col gap-1.5 text-left">
                    <span class="text-sm font-semibold"
                      >客户端 URL
                      <span class="font-normal text-[var(--console-muted)]">(optional)</span></span
                    >
                    <input
                      class="${FIELD}"
                      name="clientUri"
                      [(ngModel)]="clientUri"
                      placeholder="https://example.com"
                    />
                  </label>

                  <div class="pt-1">
                    <button
                      type="button"
                      class="flex items-center gap-2 text-sm font-medium text-[var(--console-fg)]"
                      (click)="advancedOpen.set(!advancedOpen())"
                    >
                      <span aria-hidden="true" class="text-[var(--console-muted)]">{{
                        advancedOpen() ? '▾' : '▸'
                      }}</span>
                      高级选项
                    </button>
                    @if (advancedOpen()) {
                      <div class="mt-4 flex flex-col gap-4 border-l-2 border-[var(--console-border)] pl-4">
                        <label class="flex flex-col gap-1.5 text-left">
                          <span class="text-sm font-semibold">额外 Redirect URIs（每行一个）</span>
                          <textarea
                            class="${FIELD_TEXTAREA}"
                            name="extraRedirects"
                            rows="3"
                            [(ngModel)]="extraRedirectUrisText"
                          ></textarea>
                        </label>
                        <label class="flex flex-col gap-1.5 text-left">
                          <span class="text-sm font-semibold">Post-logout Redirect URIs（每行一个）</span>
                          <textarea
                            class="${FIELD_TEXTAREA}"
                            name="postLogout"
                            rows="3"
                            [(ngModel)]="postLogoutUrisText"
                          ></textarea>
                        </label>
                      </div>
                    }
                  </div>
                </form>
              } @else {
                <h2 class="text-lg font-semibold">选择权限范围</h2>
                <p class="mt-2 text-sm leading-relaxed text-[var(--console-muted)]">
                  至少选择 <strong class="font-medium text-[var(--console-fg)]">openid</strong>。创建后将按所选配置写入
                  IAM。
                </p>

                <form class="mt-6 flex flex-col gap-3" (ngSubmit)="onCreate()" id="create-step-2">
                  <label class="flex items-center gap-2.5 text-sm">
                    <input type="checkbox" class="size-4" [(ngModel)]="scopeOpenid" name="scopeOpenid" />
                    openid
                  </label>
                  <label class="flex items-center gap-2.5 text-sm">
                    <input type="checkbox" class="size-4" [(ngModel)]="scopeProfile" name="scopeProfile" />
                    profile
                  </label>
                  <label class="flex items-center gap-2.5 text-sm">
                    <input type="checkbox" class="size-4" [(ngModel)]="scopeEmail" name="scopeEmail" />
                    email
                  </label>
                </form>
              }
            </section>

            <div class="mt-4 flex items-center justify-between gap-3">
              @if (step() === 1) {
                <a routerLink="/clients" class="${BTN_SECONDARY}">取消</a>
                <button
                  type="submit"
                  form="create-step-1"
                  class="${BTN_PRIMARY}"
                  [disabled]="!clientName.trim() || !redirectUri.trim() || !tokenAuthMethod"
                >
                  继续
                </button>
              } @else {
                <button type="button" class="${BTN_SECONDARY}" (click)="step.set(1)">返回</button>
                <button
                  type="submit"
                  form="create-step-2"
                  class="${BTN_PRIMARY}"
                  [disabled]="submitting() || !scopeOpenid"
                >
                  {{ submitting() ? '创建中…' : '创建' }}
                </button>
              }
            </div>
          </div>

          <aside class="pt-1 text-sm">
            <ol class="space-y-5">
              <li class="flex items-start gap-3">
                <span
                  [class]="
                    step() === 1
                      ? 'mt-[0.35rem] size-2 shrink-0 rounded-full bg-[var(--console-fg)]'
                      : 'mt-[0.35rem] size-2 shrink-0 rounded-full bg-[var(--console-accent)]'
                  "
                ></span>
                <p
                  class="leading-snug"
                  [class]="
                    step() === 1
                      ? 'font-semibold text-[var(--console-fg)]'
                      : 'text-[var(--console-muted)]'
                  "
                >
                  配置 OAuth 客户端
                </p>
              </li>
              <li class="flex items-start gap-3">
                <span
                  [class]="
                    step() === 2
                      ? 'mt-[0.35rem] size-2 shrink-0 rounded-full bg-[var(--console-fg)]'
                      : 'mt-[0.35rem] size-2 shrink-0 rounded-full border-2 border-[var(--console-muted)] bg-transparent'
                  "
                ></span>
                <p
                  class="leading-snug"
                  [class]="
                    step() === 2
                      ? 'font-semibold text-[var(--console-fg)]'
                      : 'text-[var(--console-muted)]'
                  "
                >
                  选择权限范围
                </p>
              </li>
            </ol>
          </aside>
        </div>
      }
    </app-console-shell>
  `,
})
export class ClientsCreatePageComponent {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly step = signal(1);
  readonly advancedOpen = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly createdClientId = signal<string | null>(null);
  readonly createdSecret = signal<string | null>(null);

  clientName = '';
  responseType = 'code';
  grantPreset = 'authorization_code,refresh_token';
  tokenAuthMethod = '';
  redirectUri = '';
  clientUri = '';
  extraRedirectUrisText = '';
  postLogoutUrisText = '';

  scopeOpenid = true;
  scopeProfile = true;
  scopeEmail = true;

  goStep2(): void {
    this.errorMessage.set(null);
    if (!this.clientName.trim() || !this.redirectUri.trim() || !this.tokenAuthMethod) {
      return;
    }
    this.step.set(2);
  }

  onCreate(): void {
    const scopes = [
      this.scopeOpenid ? 'openid' : null,
      this.scopeProfile ? 'profile' : null,
      this.scopeEmail ? 'email' : null,
    ].filter((s): s is string => !!s);

    if (!this.scopeOpenid || scopes.length === 0) {
      this.errorMessage.set('scopes must include openid');
      return;
    }

    const redirectUris = [this.redirectUri.trim(), ...splitLines(this.extraRedirectUrisText)];
    const grantTypes = this.grantPreset.split(',').map((g) => g.trim());

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.http
      .post<ClientView>(
        '/api/clients',
        {
          clientName: this.clientName.trim(),
          redirectUris,
          postLogoutRedirectUris: splitLines(this.postLogoutUrisText),
          scopes,
          responseTypes: [this.responseType],
          authorizationGrantTypes: grantTypes,
          clientAuthenticationMethods: [this.tokenAuthMethod],
          clientUri: this.clientUri.trim() || null,
        },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: (created) => {
          this.submitting.set(false);
          this.createdClientId.set(created.clientId);
          this.createdSecret.set(created.clientSecret);
        },
        error: (err: unknown) => {
          this.submitting.set(false);
          this.handleError(err);
        },
      });
  }

  private handleError(err: unknown): void {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        void this.router.navigate(['/login'], {
          queryParams: { continue: '/clients/new' },
        });
        return;
      }
      const message = (err.error as { message?: string } | null)?.message;
      this.errorMessage.set(message || `请求失败（${err.status}）`);
      return;
    }
    this.errorMessage.set('请求失败，请重试。');
  }
}

function splitLines(text: string): string[] {
  return text
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.length > 0);
}

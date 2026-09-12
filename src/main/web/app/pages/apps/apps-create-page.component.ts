import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import {
  BTN_PRIMARY,
  BTN_SECONDARY,
  CARD,
  FIELD,
  FIELD_TEXTAREA,
} from '../../shared/console-ui';
import { csrfHeaders } from '../../shared/csrf';
import type { ClientView } from './apps-list-page.component';

type PermissionPoint = {
  code: string;
  oauth_scope: string;
  module: string;
  action: string;
  resource: string;
  description: string;
};

type PermissionPointsResponse = {
  permission_points: PermissionPoint[];
  next_page_token: string | null;
};

@Component({
  selector: 'app-apps-create-page',
  imports: [ConsoleShellComponent, FormsModule, RouterLink],
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-[1.75rem] font-semibold tracking-tight">创建应用</h1>
          <p class="mt-1 text-sm text-[var(--console-muted)]">注册新的 OAuth 应用</p>
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
          <h2 class="text-lg font-semibold">应用已创建</h2>
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
              <p class="text-sm text-[var(--console-muted)]">
                公共客户端（None / PKCE）未生成 client_secret。
              </p>
            }
          </dl>
          <a routerLink="/apps" class="${BTN_PRIMARY} mt-6">返回列表</a>
        </section>
      } @else {
        <div class="mt-6 grid items-start gap-10 lg:grid-cols-[minmax(0,1fr)_200px]">
          <div>
            <section class="${CARD} p-6 sm:p-8">
              @if (step() === 1) {
                <h2 class="text-lg font-semibold">配置 OAuth 应用</h2>
                <p class="mt-2 text-sm leading-relaxed text-[var(--console-muted)]">
                  应用默认视为机密客户端。公共客户端请将令牌身份验证方法设为
                  <strong class="font-medium text-[var(--console-fg)]">None (PKCE)</strong>。
                </p>

                <form class="mt-6 flex flex-col gap-5" (ngSubmit)="goStep2()" id="create-step-1">
                  <label class="flex flex-col gap-1.5 text-left">
                    <span class="text-sm font-semibold">应用名称</span>
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
                  从 Permission Points 选择 scopes。若目录含
                  <strong class="font-medium text-[var(--console-fg)]">openid</strong>，则必须勾选。
                </p>

                @if (scopesLoading()) {
                  <p class="mt-6 text-sm text-[var(--console-muted)]">加载权限点…</p>
                } @else if (permissionPoints().length === 0) {
                  <p class="mt-6 text-sm text-[var(--console-muted)]">暂无可用权限点。</p>
                } @else {
                  <form class="mt-6 flex flex-col gap-3" (ngSubmit)="onCreate()" id="create-step-2">
                    @for (point of permissionPoints(); track point.code) {
                      <label class="flex items-start gap-2.5 text-sm">
                        <input
                          type="checkbox"
                          class="mt-0.5 size-4"
                          [checked]="selectedScopes().has(scopeOf(point))"
                          (change)="toggleScope(scopeOf(point), $any($event.target).checked)"
                          [name]="'scope-' + point.code"
                        />
                        <span>
                          <span class="font-medium">{{ scopeOf(point) }}</span>
                          <span class="mt-0.5 block text-xs text-[var(--console-muted)]">
                            {{ point.module }} · {{ point.code }}
                            @if (point.description) {
                              — {{ point.description }}
                            }
                          </span>
                        </span>
                      </label>
                    }
                  </form>
                }
              }
            </section>

            <div class="mt-4 flex items-center justify-between gap-3">
              @if (step() === 1) {
                <a routerLink="/apps" class="${BTN_SECONDARY}">取消</a>
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
                  [disabled]="submitting() || !canSubmitScopes()"
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
                  配置 OAuth 应用
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
export class AppsCreatePageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly step = signal(1);
  readonly advancedOpen = signal(false);
  readonly submitting = signal(false);
  readonly scopesLoading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly createdClientId = signal<string | null>(null);
  readonly createdSecret = signal<string | null>(null);
  readonly permissionPoints = signal<PermissionPoint[]>([]);
  readonly selectedScopes = signal(new Set<string>());

  clientName = '';
  responseType = 'code';
  grantPreset = 'authorization_code,refresh_token';
  tokenAuthMethod = '';
  redirectUri = '';
  clientUri = '';
  extraRedirectUrisText = '';
  postLogoutUrisText = '';

  ngOnInit(): void {
    this.loadPermissionPoints();
  }

  scopeOf(point: PermissionPoint): string {
    return point.oauth_scope || point.code;
  }

  toggleScope(scope: string, checked: boolean): void {
    this.selectedScopes.update((prev) => {
      const next = new Set(prev);
      if (checked) {
        next.add(scope);
      } else {
        next.delete(scope);
      }
      return next;
    });
  }

  canSubmitScopes(): boolean {
    const selected = this.selectedScopes();
    if (selected.size === 0) {
      return false;
    }
    const hasOpenid = this.permissionPoints().some((p) => this.scopeOf(p) === 'openid');
    if (hasOpenid && !selected.has('openid')) {
      return false;
    }
    return true;
  }

  goStep2(): void {
    this.errorMessage.set(null);
    if (!this.clientName.trim() || !this.redirectUri.trim() || !this.tokenAuthMethod) {
      return;
    }
    this.step.set(2);
  }

  onCreate(): void {
    if (!this.canSubmitScopes()) {
      this.errorMessage.set('请选择有效的权限范围（若有 openid 则必须包含）');
      return;
    }

    const scopes = [...this.selectedScopes()];
    const redirectUris = [this.redirectUri.trim(), ...splitLines(this.extraRedirectUrisText)];
    const grantTypes = this.grantPreset.split(',').map((g) => g.trim());

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.http
      .post<ClientView>(
        '/api/v1/clients',
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

  private loadPermissionPoints(): void {
    this.scopesLoading.set(true);
    this.http
      .get<PermissionPointsResponse>('/api/v1/permissionPoints', {
        withCredentials: true,
        params: { page_size: '100' },
      })
      .subscribe({
        next: (res) => {
          const points = res.permission_points ?? [];
          this.permissionPoints.set(points);
          const initial = new Set<string>();
          for (const point of points) {
            const scope = this.scopeOf(point);
            if (scope === 'openid' || scope === 'profile' || scope === 'email') {
              initial.add(scope);
            }
          }
          this.selectedScopes.set(initial);
          this.scopesLoading.set(false);
        },
        error: (err: unknown) => {
          this.scopesLoading.set(false);
          this.handleError(err);
        },
      });
  }

  private handleError(err: unknown): void {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        void this.router.navigate(['/login'], {
          queryParams: { continue: '/apps/new' },
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

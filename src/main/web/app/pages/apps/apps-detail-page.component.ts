import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import { BTN_PRIMARY, BTN_SECONDARY, CARD } from '../../shared/console-ui';
import { csrfHeaders } from '../../shared/csrf';
import type { ClientView } from './apps-list-page.component';

type PermissionPoint = {
  code: string;
  oauth_scope: string;
  module: string;
  description: string;
};

type PermissionPointsResponse = {
  permission_points: PermissionPoint[];
  next_page_token: string | null;
};

@Component({
  selector: 'app-apps-detail-page',
  imports: [ConsoleShellComponent, RouterLink],
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p class="m-0 text-xs text-[var(--console-muted)]">
            <a routerLink="/apps" class="text-[var(--console-accent)] no-underline hover:underline"
              >Apps</a
            >
            /
            <span>详情</span>
          </p>
          <h1 class="mt-1 text-[1.75rem] font-semibold tracking-tight">
            {{ client()?.clientName || '应用详情' }}
          </h1>
          @if (client()) {
            <p class="mt-1 font-mono text-sm text-[var(--console-muted)]">
              {{ client()!.clientId }}
            </p>
          }
        </div>
        <a routerLink="/apps" class="${BTN_SECONDARY}">返回列表</a>
      </div>

      @if (errorMessage()) {
        <div
          class="mt-4 rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive"
          role="alert"
        >
          {{ errorMessage() }}
        </div>
      }

      @if (successMessage()) {
        <p class="mt-3 text-sm text-[#0a7a3e]" role="status">{{ successMessage() }}</p>
      }

      @if (loading()) {
        <p class="mt-8 text-sm text-[var(--console-muted)]">加载中…</p>
      } @else if (client()) {
        <section class="${CARD} mt-6 p-6">
          <h2 class="text-base font-semibold">基本信息</h2>
          <dl class="mt-4 grid gap-4 text-sm sm:grid-cols-2">
            <div>
              <dt class="text-[var(--console-muted)]">Visibility</dt>
              <dd class="mt-0.5">
                {{ isPublic(client()!) ? 'Public' : 'Private' }}
              </dd>
            </div>
            <div>
              <dt class="text-[var(--console-muted)]">Redirect URIs</dt>
              <dd class="mt-0.5 break-all text-[var(--console-muted)]">
                {{ client()!.redirectUris.join(', ') || '—' }}
              </dd>
            </div>
            <div>
              <dt class="text-[var(--console-muted)]">Grant Types</dt>
              <dd class="mt-0.5">{{ client()!.authorizationGrantTypes.join(', ') }}</dd>
            </div>
            <div>
              <dt class="text-[var(--console-muted)]">当前 Scopes</dt>
              <dd class="mt-0.5">{{ client()!.scopes.join(', ') || '—' }}</dd>
            </div>
          </dl>
        </section>

        <section class="${CARD} mt-4 p-6">
          <div class="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h2 class="text-base font-semibold">更新权限范围</h2>
              <p class="mt-1 text-sm text-[var(--console-muted)]">
                从 Permission Points 多选，保存后替换该应用的 scopes。
              </p>
            </div>
            <button
              type="button"
              class="${BTN_PRIMARY}"
              [disabled]="saving() || selectedScopes().size === 0"
              (click)="saveScopes()"
            >
              {{ saving() ? '保存中…' : '保存 Scopes' }}
            </button>
          </div>

          @if (pointsLoading()) {
            <p class="mt-6 text-sm text-[var(--console-muted)]">加载权限点…</p>
          } @else {
            <div class="mt-6 flex flex-col gap-3">
              @for (point of permissionPoints(); track point.code) {
                <label class="flex items-start gap-2.5 text-sm">
                  <input
                    type="checkbox"
                    class="mt-0.5 size-4"
                    [checked]="selectedScopes().has(scopeOf(point))"
                    (change)="toggleScope(scopeOf(point), $any($event.target).checked)"
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
            </div>
          }
        </section>
      }
    </app-console-shell>
  `,
})
export class AppsDetailPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly client = signal<ClientView | null>(null);
  readonly permissionPoints = signal<PermissionPoint[]>([]);
  readonly selectedScopes = signal(new Set<string>());
  readonly loading = signal(true);
  readonly pointsLoading = signal(true);
  readonly saving = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

  private clientId = '';

  ngOnInit(): void {
    this.clientId = this.route.snapshot.paramMap.get('clientId') ?? '';
    if (!this.clientId) {
      this.loading.set(false);
      this.errorMessage.set('缺少 clientId');
      return;
    }
    this.loadClient();
    this.loadPermissionPoints();
  }

  scopeOf(point: PermissionPoint): string {
    return point.oauth_scope || point.code;
  }

  isPublic(client: ClientView): boolean {
    return client.clientAuthenticationMethods?.includes('none') ?? false;
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

  saveScopes(): void {
    const scopes = [...this.selectedScopes()];
    if (scopes.length === 0) {
      return;
    }
    this.saving.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.http
      .post<ClientView>(
        `/api/v1/clients/${encodeURIComponent(this.clientId)}:updateScopes`,
        { scopes },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: (updated) => {
          this.client.set(updated);
          this.selectedScopes.set(new Set(updated.scopes ?? []));
          this.saving.set(false);
          this.successMessage.set('Scopes 已更新');
        },
        error: (err: unknown) => {
          this.saving.set(false);
          this.handleError(err);
        },
      });
  }

  private loadClient(): void {
    this.http
      .get<ClientView>(`/api/v1/clients/${encodeURIComponent(this.clientId)}`, {
        withCredentials: true,
      })
      .subscribe({
        next: (view) => {
          this.client.set(view);
          this.selectedScopes.set(new Set(view.scopes ?? []));
          this.loading.set(false);
        },
        error: (err: unknown) => {
          this.loading.set(false);
          this.handleError(err);
        },
      });
  }

  private loadPermissionPoints(): void {
    this.pointsLoading.set(true);
    this.http
      .get<PermissionPointsResponse>('/api/v1/permissionPoints', {
        withCredentials: true,
        params: { page_size: '100' },
      })
      .subscribe({
        next: (res) => {
          this.permissionPoints.set(res.permission_points ?? []);
          this.pointsLoading.set(false);
        },
        error: (err: unknown) => {
          this.pointsLoading.set(false);
          this.handleError(err);
        },
      });
  }

  private handleError(err: unknown): void {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        void this.router.navigate(['/login'], {
          queryParams: { continue: `/apps/${this.clientId}` },
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

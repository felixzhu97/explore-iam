import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import { BTN_PRIMARY, CARD, FIELD, FIELD_TEXTAREA } from '../../shared/console-ui';
import { csrfHeaders } from '../../shared/csrf';

type PermissionPoint = {
  name: string;
  code: string;
  oauth_scope: string;
  module: string;
  action: string;
  resource: string;
  description: string;
};

type ListPermissionPointsResponse = {
  permission_points: PermissionPoint[];
  next_page_token: string | null;
};

@Component({
  selector: 'app-points-page',
  imports: [ConsoleShellComponent, FormsModule],
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-[1.75rem] font-semibold tracking-tight">Permission Points</h1>
          <p class="mt-1 text-sm text-[var(--console-muted)]">权限点目录</p>
        </div>
      </div>

      @if (errorMessage()) {
        <div
          class="mt-4 rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive"
          role="alert"
        >
          {{ errorMessage() }}
        </div>
      }

      <section class="${CARD} mt-6 p-6">
        <h2 class="text-base font-semibold">创建权限点</h2>
        <form class="mt-4 grid gap-3 sm:grid-cols-2" (ngSubmit)="createPoint()">
          <input class="${FIELD}" name="code" placeholder="code" required [(ngModel)]="code" />
          <input class="${FIELD}" name="module" placeholder="module" required [(ngModel)]="module" />
          <input class="${FIELD}" name="action" placeholder="action" required [(ngModel)]="action" />
          <input
            class="${FIELD}"
            name="resource"
            placeholder="resource"
            required
            [(ngModel)]="resource"
          />
          <input
            class="${FIELD} sm:col-span-2"
            name="oauthScope"
            placeholder="oauth_scope（可选，默认 = code）"
            [(ngModel)]="oauthScope"
          />
          <textarea
            class="${FIELD_TEXTAREA} sm:col-span-2"
            name="description"
            rows="2"
            placeholder="description"
            [(ngModel)]="description"
          ></textarea>
          <button type="submit" class="${BTN_PRIMARY} w-fit" [disabled]="creating()">
            {{ creating() ? '创建中…' : '创建' }}
          </button>
        </form>
      </section>

      <div class="mt-5 max-w-xs">
        <label class="flex flex-col gap-1.5 text-sm">
          <span class="font-semibold">按 module 筛选</span>
          <input
            class="${FIELD}"
            type="search"
            name="moduleFilter"
            placeholder="全部"
            [(ngModel)]="moduleFilterModel"
          />
        </label>
      </div>

      <section class="${CARD} mt-4 overflow-hidden">
        @if (loading()) {
          <p class="px-5 py-8 text-sm text-[var(--console-muted)]">加载中…</p>
        } @else if (filtered().length === 0) {
          <p class="px-5 py-10 text-center text-sm text-[var(--console-muted)]">暂无权限点。</p>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full border-collapse text-sm">
              <thead>
                <tr>
                  <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                    Code
                  </th>
                  <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                    Scope
                  </th>
                  <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                    Module
                  </th>
                  <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                    Action / Resource
                  </th>
                </tr>
              </thead>
              <tbody>
                @for (point of filtered(); track point.code) {
                  <tr>
                    <td class="border-b border-[var(--console-border)] px-4 py-3 font-medium">
                      {{ point.code }}
                      @if (point.description) {
                        <div class="text-xs font-normal text-[var(--console-muted)]">
                          {{ point.description }}
                        </div>
                      }
                    </td>
                    <td class="border-b border-[var(--console-border)] px-4 py-3 font-mono text-xs">
                      {{ point.oauth_scope }}
                    </td>
                    <td class="border-b border-[var(--console-border)] px-4 py-3">
                      {{ point.module }}
                    </td>
                    <td class="border-b border-[var(--console-border)] px-4 py-3 text-[var(--console-muted)]">
                      {{ point.action }} · {{ point.resource }}
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </section>
    </app-console-shell>
  `,
})
export class PointsPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly points = signal<PermissionPoint[]>([]);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly moduleFilter = signal('');

  readonly filtered = computed(() => {
    const q = this.moduleFilter().trim().toLowerCase();
    if (!q) {
      return this.points();
    }
    return this.points().filter((p) => p.module.toLowerCase().includes(q));
  });

  code = '';
  module = '';
  action = '';
  resource = '';
  oauthScope = '';
  description = '';

  get moduleFilterModel(): string {
    return this.moduleFilter();
  }
  set moduleFilterModel(value: string) {
    this.moduleFilter.set(value);
  }

  ngOnInit(): void {
    this.reload();
  }

  createPoint(): void {
    if (!this.code.trim() || !this.module.trim() || !this.action.trim() || !this.resource.trim()) {
      return;
    }
    this.creating.set(true);
    this.errorMessage.set(null);
    const body: Record<string, string> = {
      code: this.code.trim(),
      module: this.module.trim(),
      action: this.action.trim(),
      resource: this.resource.trim(),
      description: this.description.trim(),
    };
    if (this.oauthScope.trim()) {
      body['oauth_scope'] = this.oauthScope.trim();
    }
    this.http
      .post<PermissionPoint>('/api/v1/permissionPoints', body, {
        withCredentials: true,
        headers: csrfHeaders(),
      })
      .subscribe({
        next: () => {
          this.creating.set(false);
          this.code = '';
          this.module = '';
          this.action = '';
          this.resource = '';
          this.oauthScope = '';
          this.description = '';
          this.reload();
        },
        error: (err: unknown) => {
          this.creating.set(false);
          this.handleError(err);
        },
      });
  }

  private reload(): void {
    this.loading.set(true);
    this.http
      .get<ListPermissionPointsResponse>('/api/v1/permissionPoints', {
        withCredentials: true,
        params: { page_size: '50' },
      })
      .subscribe({
        next: (res) => {
          this.points.set(res.permission_points ?? []);
          this.loading.set(false);
        },
        error: (err: unknown) => {
          this.loading.set(false);
          this.handleError(err);
        },
      });
  }

  private handleError(err: unknown): void {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        void this.router.navigate(['/login'], {
          queryParams: { continue: '/permissions/points' },
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

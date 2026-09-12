import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import { csrfHeaders } from '../../shared/csrf';
import { BTN_PRIMARY, BTN_SECONDARY, CARD, FIELD, FIELD_TEXTAREA } from '../../shared/console-ui';

type RoleRow = {
  name: string;
  id: string;
  displayName: string;
  arn: string;
};

@Component({
  selector: 'app-roles-page',
  imports: [ConsoleShellComponent, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-console-shell>
      <div>
        <h1 class="text-[1.75rem] font-semibold tracking-tight">Roles</h1>
        <p class="mt-1 text-sm text-[var(--console-muted)]">Users and Access · 角色</p>
      </div>

      @if (errorMessage()) {
        <div
          class="mt-4 rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive"
          role="alert"
        >
          {{ errorMessage() }}
        </div>
      }

      <section class="${CARD} mt-5 p-5">
        <h2 class="text-sm font-semibold">创建角色</h2>
        <div class="mt-3 grid gap-3 sm:grid-cols-2">
          <input class="${FIELD}" name="name" placeholder="角色名" [(ngModel)]="name" />
          <input
            class="${FIELD}"
            name="displayName"
            placeholder="显示名"
            [(ngModel)]="displayName"
          />
        </div>
        <textarea
          class="${FIELD_TEXTAREA} mt-3"
          name="trustPolicyJson"
          placeholder="Trust policy JSON（可选）"
          [(ngModel)]="trustPolicyJson"
        ></textarea>
        <button type="button" class="${BTN_PRIMARY} mt-3" [disabled]="busy()" (click)="create()">
          创建
        </button>
      </section>

      <section class="${CARD} mt-4 overflow-hidden">
        @if (loading()) {
          <p class="px-5 py-8 text-sm text-[var(--console-muted)]">加载中…</p>
        } @else if (roles().length === 0) {
          <p class="px-5 py-10 text-center text-sm text-[var(--console-muted)]">暂无角色。</p>
        } @else {
          <table class="w-full border-collapse text-sm">
            <thead>
              <tr>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                  角色
                </th>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                  ARN
                </th>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-right text-xs font-semibold text-[var(--console-muted)]">
                  操作
                </th>
              </tr>
            </thead>
            <tbody>
              @for (role of roles(); track role.id) {
                <tr>
                  <td class="border-b border-[var(--console-border)] px-4 py-3">
                    <div class="font-medium">{{ role.displayName || role.name }}</div>
                    <code class="text-[11px] text-[var(--console-muted)]">{{ role.name }}</code>
                  </td>
                  <td class="border-b border-[var(--console-border)] px-4 py-3 text-xs text-[var(--console-muted)]">
                    {{ role.arn }}
                  </td>
                  <td class="border-b border-[var(--console-border)] px-4 py-3 text-right">
                    <div class="flex flex-wrap justify-end gap-2">
                      <button type="button" class="${BTN_SECONDARY}" (click)="assign(role)">
                        分配用户
                      </button>
                      <button type="button" class="${BTN_SECONDARY}" (click)="unassign(role)">
                        取消分配
                      </button>
                    </div>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        }
      </section>
    </app-console-shell>
  `,
})
export class RolesPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly roles = signal<RoleRow[]>([]);
  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly errorMessage = signal<string | null>(null);

  name = '';
  displayName = '';
  trustPolicyJson = '';

  ngOnInit(): void {
    this.reload();
  }

  create(): void {
    if (!this.name.trim()) {
      this.errorMessage.set('请填写角色名');
      return;
    }
    this.busy.set(true);
    this.http
      .post(
        '/api/v1/roles',
        {
          name: this.name.trim(),
          displayName: this.displayName.trim() || this.name.trim(),
          trustPolicyJson: this.trustPolicyJson.trim() || null,
        },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.name = '';
          this.displayName = '';
          this.trustPolicyJson = '';
          this.reload();
        },
        error: (err: unknown) => {
          this.busy.set(false);
          this.handleError(err);
        },
      });
  }

  assign(role: RoleRow): void {
    const userId = window.prompt('用户 ID');
    if (!userId?.trim()) {
      return;
    }
    this.http
      .post(
        `/api/v1/roles/${role.id}:assign`,
        { userId: userId.trim() },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) => this.handleError(err),
      });
  }

  unassign(role: RoleRow): void {
    const userId = window.prompt('用户 ID');
    if (!userId?.trim()) {
      return;
    }
    this.http
      .post(
        `/api/v1/roles/${role.id}:unassign`,
        { userId: userId.trim() },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) => this.handleError(err),
      });
  }

  private reload(): void {
    this.loading.set(true);
    this.http
      .get<{ roles: RoleRow[] }>('/api/v1/roles', { withCredentials: true })
      .subscribe({
        next: (res) => {
          this.roles.set(res.roles ?? []);
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
          queryParams: { continue: '/users-and-access/roles' },
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

import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import { csrfHeaders } from '../../shared/csrf';
import { BTN_PRIMARY, BTN_SECONDARY, CARD, FIELD } from '../../shared/console-ui';

type UserRow = {
  name: string;
  id: string;
  username: string;
  email: string;
  enabled: boolean;
};

@Component({
  selector: 'app-people-page',
  imports: [ConsoleShellComponent, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-[1.75rem] font-semibold tracking-tight">People</h1>
          <p class="mt-1 text-sm text-[var(--console-muted)]">Users and Access · 用户</p>
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

      <section class="${CARD} mt-5 p-5">
        <h2 class="text-sm font-semibold">创建用户</h2>
        <div class="mt-3 grid gap-3 sm:grid-cols-3">
          <input class="${FIELD}" name="username" placeholder="用户名" [(ngModel)]="username" />
          <input class="${FIELD}" name="email" type="email" placeholder="邮箱" [(ngModel)]="email" />
          <input
            class="${FIELD}"
            name="password"
            type="password"
            placeholder="密码"
            [(ngModel)]="password"
          />
        </div>
        <button
          type="button"
          class="${BTN_PRIMARY} mt-3"
          [disabled]="creating()"
          (click)="create()"
        >
          {{ creating() ? '创建中…' : '创建' }}
        </button>
      </section>

      <section class="${CARD} mt-4 overflow-hidden">
        @if (loading()) {
          <p class="px-5 py-8 text-sm text-[var(--console-muted)]">加载中…</p>
        } @else if (users().length === 0) {
          <p class="px-5 py-10 text-center text-sm text-[var(--console-muted)]">暂无用户。</p>
        } @else {
          <table class="w-full border-collapse text-sm">
            <thead>
              <tr>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                  用户
                </th>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                  状态
                </th>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-right text-xs font-semibold text-[var(--console-muted)]">
                  操作
                </th>
              </tr>
            </thead>
            <tbody>
              @for (user of users(); track user.id) {
                <tr>
                  <td class="border-b border-[var(--console-border)] px-4 py-3">
                    <div class="font-medium">{{ user.username }}</div>
                    <div class="text-xs text-[var(--console-muted)]">{{ user.email }}</div>
                  </td>
                  <td class="border-b border-[var(--console-border)] px-4 py-3">
                    {{ user.enabled ? '启用' : '禁用' }}
                  </td>
                  <td class="border-b border-[var(--console-border)] px-4 py-3 text-right">
                    <div class="flex flex-wrap justify-end gap-2">
                      @if (user.enabled) {
                        <button type="button" class="${BTN_SECONDARY}" (click)="disable(user)">
                          禁用
                        </button>
                      } @else {
                        <button type="button" class="${BTN_SECONDARY}" (click)="enable(user)">
                          启用
                        </button>
                      }
                      <button type="button" class="${BTN_SECONDARY}" (click)="resetPassword(user)">
                        重置密码
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
export class PeoplePageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly users = signal<UserRow[]>([]);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly errorMessage = signal<string | null>(null);

  username = '';
  email = '';
  password = '';

  ngOnInit(): void {
    this.reload();
  }

  create(): void {
    if (!this.username.trim() || !this.email.trim() || !this.password) {
      this.errorMessage.set('请填写用户名、邮箱与密码');
      return;
    }
    this.creating.set(true);
    this.errorMessage.set(null);
    this.http
      .post(
        '/api/v1/users',
        {
          username: this.username.trim(),
          email: this.email.trim(),
          password: this.password,
        },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => {
          this.creating.set(false);
          this.username = '';
          this.email = '';
          this.password = '';
          this.reload();
        },
        error: (err: unknown) => {
          this.creating.set(false);
          this.handleError(err);
        },
      });
  }

  disable(user: UserRow): void {
    this.postCustom(user.id, 'disable');
  }

  enable(user: UserRow): void {
    this.postCustom(user.id, 'enable');
  }

  resetPassword(user: UserRow): void {
    const password = window.prompt(`为 ${user.username} 设置新密码`);
    if (!password) {
      return;
    }
    this.http
      .post(
        `/api/v1/users/${user.id}:resetPassword`,
        { password },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) => this.handleError(err),
      });
  }

  private postCustom(id: string, method: 'disable' | 'enable'): void {
    this.http
      .post(`/api/v1/users/${id}:${method}`, {}, { withCredentials: true, headers: csrfHeaders() })
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) => this.handleError(err),
      });
  }

  private reload(): void {
    this.loading.set(true);
    this.http
      .get<{ users: UserRow[] }>('/api/v1/users', { withCredentials: true })
      .subscribe({
        next: (res) => {
          this.users.set(res.users ?? []);
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
          queryParams: { continue: '/users-and-access/people' },
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

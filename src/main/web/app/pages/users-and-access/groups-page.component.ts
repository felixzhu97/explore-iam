import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import { csrfHeaders } from '../../shared/csrf';
import { BTN_PRIMARY, BTN_SECONDARY, CARD, FIELD } from '../../shared/console-ui';

type GroupRow = {
  name: string;
  id: string;
  displayName: string;
  memberIds: string[];
};

@Component({
  selector: 'app-groups-page',
  imports: [ConsoleShellComponent, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-console-shell>
      <div>
        <h1 class="text-[1.75rem] font-semibold tracking-tight">Groups</h1>
        <p class="mt-1 text-sm text-[var(--console-muted)]">Users and Access · 用户组</p>
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
        <h2 class="text-sm font-semibold">创建组</h2>
        <div class="mt-3 grid gap-3 sm:grid-cols-2">
          <input class="${FIELD}" name="name" placeholder="组名（唯一）" [(ngModel)]="name" />
          <input
            class="${FIELD}"
            name="displayName"
            placeholder="显示名"
            [(ngModel)]="displayName"
          />
        </div>
        <button type="button" class="${BTN_PRIMARY} mt-3" [disabled]="busy()" (click)="create()">
          创建
        </button>
      </section>

      <section class="${CARD} mt-4 overflow-hidden">
        @if (loading()) {
          <p class="px-5 py-8 text-sm text-[var(--console-muted)]">加载中…</p>
        } @else if (groups().length === 0) {
          <p class="px-5 py-10 text-center text-sm text-[var(--console-muted)]">暂无用户组。</p>
        } @else {
          <table class="w-full border-collapse text-sm">
            <thead>
              <tr>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                  组
                </th>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                  成员数
                </th>
                <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-right text-xs font-semibold text-[var(--console-muted)]">
                  操作
                </th>
              </tr>
            </thead>
            <tbody>
              @for (group of groups(); track group.id) {
                <tr>
                  <td class="border-b border-[var(--console-border)] px-4 py-3">
                    <div class="font-medium">{{ group.displayName || group.name }}</div>
                    <code class="text-[11px] text-[var(--console-muted)]">{{ group.name }}</code>
                  </td>
                  <td class="border-b border-[var(--console-border)] px-4 py-3">
                    {{ group.memberIds.length }}
                  </td>
                  <td class="border-b border-[var(--console-border)] px-4 py-3 text-right">
                    <div class="flex flex-wrap justify-end gap-2">
                      <button type="button" class="${BTN_SECONDARY}" (click)="addMember(group)">
                        添加成员
                      </button>
                      <button type="button" class="${BTN_SECONDARY}" (click)="removeMember(group)">
                        移除成员
                      </button>
                      <button type="button" class="${BTN_SECONDARY}" (click)="remove(group)">
                        删除
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
export class GroupsPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly groups = signal<GroupRow[]>([]);
  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly errorMessage = signal<string | null>(null);

  name = '';
  displayName = '';

  ngOnInit(): void {
    this.reload();
  }

  create(): void {
    if (!this.name.trim()) {
      this.errorMessage.set('请填写组名');
      return;
    }
    this.busy.set(true);
    this.http
      .post(
        '/api/v1/groups',
        { name: this.name.trim(), displayName: this.displayName.trim() || this.name.trim() },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => {
          this.busy.set(false);
          this.name = '';
          this.displayName = '';
          this.reload();
        },
        error: (err: unknown) => {
          this.busy.set(false);
          this.handleError(err);
        },
      });
  }

  addMember(group: GroupRow): void {
    const userId = window.prompt('用户 ID');
    if (!userId?.trim()) {
      return;
    }
    this.http
      .post(
        `/api/v1/groups/${group.id}:addMember`,
        { userId: userId.trim() },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) => this.handleError(err),
      });
  }

  removeMember(group: GroupRow): void {
    const userId = window.prompt('要移除的用户 ID');
    if (!userId?.trim()) {
      return;
    }
    this.http
      .post(
        `/api/v1/groups/${group.id}:removeMember`,
        { userId: userId.trim() },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) => this.handleError(err),
      });
  }

  remove(group: GroupRow): void {
    if (!window.confirm(`删除组 ${group.name}？`)) {
      return;
    }
    this.http
      .delete(`/api/v1/groups/${group.id}`, { withCredentials: true, headers: csrfHeaders() })
      .subscribe({
        next: () => this.reload(),
        error: (err: unknown) => this.handleError(err),
      });
  }

  private reload(): void {
    this.loading.set(true);
    this.http
      .get<{ groups: GroupRow[] }>('/api/v1/groups', { withCredentials: true })
      .subscribe({
        next: (res) => {
          this.groups.set(res.groups ?? []);
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
          queryParams: { continue: '/users-and-access/groups' },
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

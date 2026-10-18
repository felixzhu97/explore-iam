import {
  ChangeDetectionStrategy,
  Component,
  computed,
  HostListener,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';

const CARD =
  'rounded-lg border border-[var(--console-border)] bg-white shadow-[0_1px_2px_rgba(0,0,0,0.04)]';
const BTN_PRIMARY =
  'inline-flex h-9 items-center justify-center rounded-md border border-transparent bg-[var(--console-accent-soft)] px-4 text-sm font-medium text-white no-underline hover:bg-[var(--console-accent)] disabled:cursor-not-allowed disabled:opacity-50';
const BTN_SECONDARY =
  'inline-flex h-9 items-center justify-center rounded-md border border-[var(--console-border)] bg-white px-4 text-sm font-medium text-[var(--console-fg)] no-underline hover:bg-[var(--console-bg)] disabled:cursor-not-allowed disabled:opacity-50';
const BTN_DANGER =
  'inline-flex h-9 items-center justify-center rounded-md border border-transparent bg-destructive px-4 text-sm font-medium text-white hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50';
const FIELD =
  'block h-10 w-full rounded-md border border-[var(--console-border)] bg-white px-3 py-2 text-sm leading-5 text-[var(--console-fg)] outline-none focus:border-[var(--console-accent)] focus:shadow-[0_0_0_3px_rgba(0,81,195,0.18)]';
const TH =
  'border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]';
const TD = 'border-b border-[var(--console-border)] px-4 py-3.5 align-top text-[var(--console-fg)]';
const MENU_ITEM =
  'block w-full cursor-pointer rounded border-none bg-transparent px-[0.6rem] py-[0.4rem] text-left text-[0.8125rem] hover:bg-[var(--console-bg)]';

export interface UserView {
  id: string;
  username: string;
  email: string | null;
  enabled: boolean;
}

@Component({
  selector: 'app-users-list-page',
  imports: [ConsoleShellComponent, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-[1.75rem] font-semibold tracking-tight">用户</h1>
          <p class="mt-1 text-sm text-[var(--console-muted)]">
            创建、停用或删除用户后，会通过 SCIM 同步到已接入的应用。
          </p>
        </div>
        <button type="button" class="${BTN_PRIMARY}" (click)="openCreate()">+ 创建用户</button>
      </div>

      @if (errorMessage()) {
        <div
          class="mt-4 rounded-md border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive"
          role="alert"
        >
          {{ errorMessage() }}
        </div>
      }

      @if (statusMessage()) {
        <p class="mt-3 text-xs text-[var(--console-muted)]" role="status">{{ statusMessage() }}</p>
      }

      <div class="mt-5">
        <label class="relative block max-w-md">
          <span class="sr-only">搜索用户</span>
          <input
            class="${FIELD}"
            type="search"
            name="userSearch"
            placeholder="搜索用户名或邮箱…"
            [(ngModel)]="searchModel"
          />
        </label>
      </div>

      <section class="${CARD} mt-4 overflow-hidden">
        @if (loading()) {
          <p class="px-5 py-8 text-sm text-[var(--console-muted)]">加载中…</p>
        } @else if (filtered().length === 0) {
          <p class="px-5 py-14 text-center text-sm text-[var(--console-muted)]">
            {{ users().length === 0 ? '暂无用户。' : '没有匹配的用户。' }}
          </p>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full border-collapse text-sm">
              <thead>
                <tr>
                  <th class="${TH}">用户名</th>
                  <th class="${TH}">邮箱</th>
                  <th class="${TH}">状态</th>
                  <th class="${TH}"><span class="sr-only">操作</span></th>
                </tr>
              </thead>
              <tbody>
                @for (user of filtered(); track user.id) {
                  <tr class="hover:bg-[#fafafa] last:[&_td]:border-b-0">
                    <td class="${TD}">
                      <div class="font-medium">{{ user.username }}</div>
                      <code class="text-[11px] text-[var(--console-muted)]">{{ user.id }}</code>
                    </td>
                    <td class="${TD} text-[var(--console-muted)]">{{ user.email || '—' }}</td>
                    <td class="${TD}">
                      <span
                        class="inline-flex items-center gap-[0.35rem] rounded-full bg-[#f3f3f3] px-[0.55rem] py-[0.15rem] text-xs"
                      >
                        <span
                          class="size-[0.4rem] rounded-full"
                          [style.background]="user.enabled ? 'var(--console-accent)' : '#8c8c8c'"
                        ></span>
                        {{ user.enabled ? '已启用' : '已停用' }}
                      </span>
                    </td>
                    <td class="${TD} whitespace-nowrap text-right">
                      <div class="relative inline-block">
                        <button
                          type="button"
                          class="inline-flex size-8 items-center justify-center rounded-md text-[var(--console-muted)] hover:bg-[var(--console-bg)] hover:text-[var(--console-fg)] disabled:opacity-50"
                          [attr.aria-label]="'操作 ' + user.username"
                          [attr.aria-expanded]="menuFor() === user.id"
                          [disabled]="busyId() === user.id"
                          (click)="toggleMenu(user.id, $event)"
                        >
                          <svg viewBox="0 0 24 24" class="size-4" fill="currentColor" aria-hidden="true">
                            <circle cx="12" cy="5" r="1.5" />
                            <circle cx="12" cy="12" r="1.5" />
                            <circle cx="12" cy="19" r="1.5" />
                          </svg>
                        </button>
                        @if (menuFor() === user.id) {
                          <div
                            class="absolute right-0 z-20 min-w-32 rounded-md border border-[var(--console-border)] bg-white p-1 shadow-[0_8px_24px_rgba(0,0,0,0.08)]"
                            role="menu"
                          >
                            <button
                              type="button"
                              class="${MENU_ITEM} text-[var(--console-fg)]"
                              role="menuitem"
                              (click)="setEnabled(user, !user.enabled)"
                            >
                              {{ user.enabled ? '停用' : '启用' }}
                            </button>
                            <button
                              type="button"
                              class="${MENU_ITEM} text-destructive"
                              role="menuitem"
                              (click)="askDelete(user)"
                            >
                              删除
                            </button>
                          </div>
                        }
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
          <div
            class="border-t border-[var(--console-border)] px-4 py-2.5 text-xs text-[var(--console-muted)]"
          >
            共 {{ filtered().length }} 个用户
          </div>
        }
      </section>

      @if (createOpen()) {
        <div
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4"
          (click)="createOpen.set(false)"
        >
          <form
            class="${CARD} w-full max-w-sm p-5"
            role="dialog"
            aria-labelledby="create-user-title"
            (click)="$event.stopPropagation()"
            (ngSubmit)="createUser()"
          >
            <h2 id="create-user-title" class="text-base font-semibold">创建用户</h2>
            <label class="mt-4 block text-sm font-medium">
              用户名
              <input class="${FIELD} mt-1" name="newUsername" required autocomplete="off" [(ngModel)]="draft.username" />
            </label>
            <label class="mt-3 block text-sm font-medium">
              邮箱
              <input class="${FIELD} mt-1" name="newEmail" type="email" autocomplete="off" [(ngModel)]="draft.email" />
            </label>
            <label class="mt-3 block text-sm font-medium">
              初始密码
              <input
                class="${FIELD} mt-1"
                name="newPassword"
                type="password"
                required
                autocomplete="new-password"
                [(ngModel)]="draft.password"
              />
            </label>
            <div class="mt-5 flex justify-end gap-2">
              <button type="button" class="${BTN_SECONDARY}" (click)="createOpen.set(false)">取消</button>
              <button
                type="submit"
                class="${BTN_PRIMARY}"
                [disabled]="creating() || !draft.username.trim() || !draft.password"
              >
                {{ creating() ? '创建中…' : '创建' }}
              </button>
            </div>
          </form>
        </div>
      }

      @if (pendingDelete(); as user) {
        <div
          class="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4"
          (click)="pendingDelete.set(null)"
        >
          <div
            class="${CARD} w-full max-w-sm p-5"
            role="alertdialog"
            aria-labelledby="delete-user-title"
            (click)="$event.stopPropagation()"
          >
            <h2 id="delete-user-title" class="text-base font-semibold">删除 {{ user.username }}？</h2>
            <p class="mt-2 text-sm text-[var(--console-muted)]">
              该用户的群组成员关系与凭据会被移除，已接入的应用也会删除对应账号。此操作无法撤销。
            </p>
            <div class="mt-5 flex justify-end gap-2">
              <button type="button" class="${BTN_SECONDARY}" (click)="pendingDelete.set(null)">
                取消
              </button>
              <button
                type="button"
                class="${BTN_DANGER}"
                [disabled]="busyId() === user.id"
                (click)="confirmDelete(user)"
              >
                删除
              </button>
            </div>
          </div>
        </div>
      }
    </app-console-shell>
  `,
})
export class UsersListPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly users = signal<UserView[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly statusMessage = signal<string | null>(null);
  readonly search = signal('');
  readonly menuFor = signal<string | null>(null);
  readonly busyId = signal<string | null>(null);
  readonly pendingDelete = signal<UserView | null>(null);
  readonly createOpen = signal(false);
  readonly creating = signal(false);
  draft = { username: '', email: '', password: '' };

  readonly filtered = computed(() => {
    const q = this.search().trim().toLowerCase();
    if (!q) {
      return this.users();
    }
    return this.users().filter(
      (u) => u.username.toLowerCase().includes(q) || (u.email ?? '').toLowerCase().includes(q),
    );
  });

  get searchModel(): string {
    return this.search();
  }
  set searchModel(value: string) {
    this.search.set(value);
  }

  ngOnInit(): void {
    this.listUsers();
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.menuFor.set(null);
  }

  listUsers(): void {
    this.http.get<{ users: UserView[] }>('/api/v1/users').subscribe({
      next: (response) => {
        this.users.set(response.users ?? []);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.loading.set(false);
        this.handleError(err);
      },
    });
  }

  toggleMenu(userId: string, event: MouseEvent): void {
    event.stopPropagation();
    this.menuFor.set(this.menuFor() === userId ? null : userId);
  }

  setEnabled(user: UserView, enabled: boolean): void {
    this.menuFor.set(null);
    this.busyId.set(user.id);
    this.errorMessage.set(null);
    const action = enabled ? 'enable' : 'disable';
    this.http.post<UserView>(`/api/v1/users/${encodeURIComponent(user.id)}:${action}`, {}).subscribe({
      next: (updated) => {
        this.busyId.set(null);
        this.users.update((list) => list.map((u) => (u.id === updated.id ? updated : u)));
        this.statusMessage.set(`已${enabled ? '启用' : '停用'} ${user.username}`);
      },
      error: (err: unknown) => {
        this.busyId.set(null);
        this.handleError(err);
      },
    });
  }

  openCreate(): void {
    this.draft = { username: '', email: '', password: '' };
    this.createOpen.set(true);
  }

  createUser(): void {
    this.creating.set(true);
    this.errorMessage.set(null);
    const body = {
      username: this.draft.username.trim(),
      email: this.draft.email.trim() || null,
      password: this.draft.password,
    };
    this.http.post<UserView>('/api/v1/users', body).subscribe({
      next: (created) => {
        this.creating.set(false);
        this.createOpen.set(false);
        this.draft = { username: '', email: '', password: '' };
        this.users.update((list) => [...list, created]);
        this.statusMessage.set(`已创建 ${created.username}`);
      },
      error: (err: unknown) => {
        this.creating.set(false);
        this.draft.password = '';
        this.createOpen.set(false);
        this.handleError(err, '用户名已存在。');
      },
    });
  }

  askDelete(user: UserView): void {
    this.menuFor.set(null);
    this.pendingDelete.set(user);
  }

  confirmDelete(user: UserView): void {
    this.busyId.set(user.id);
    this.errorMessage.set(null);
    this.http.delete(`/api/v1/users/${encodeURIComponent(user.id)}`).subscribe({
      next: () => {
        this.busyId.set(null);
        this.pendingDelete.set(null);
        this.users.update((list) => list.filter((u) => u.id !== user.id));
        this.statusMessage.set(`已删除 ${user.username}`);
      },
      error: (err: unknown) => {
        this.busyId.set(null);
        this.pendingDelete.set(null);
        this.handleError(err);
      },
    });
  }

  private handleError(err: unknown, conflictMessage = '不能停用或删除当前登录的账号。'): void {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401) {
        void this.router.navigate(['/login'], { queryParams: { continue: '/users' } });
        return;
      }
      if (err.status === 403) {
        this.errorMessage.set('需要 IAM 管理员权限。');
        return;
      }
      if (err.status === 409) {
        this.errorMessage.set(conflictMessage);
        return;
      }
      this.errorMessage.set(`请求失败（${err.status}）`);
      return;
    }
    this.errorMessage.set('请求失败，请重试。');
  }
}

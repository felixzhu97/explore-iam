import {
  ChangeDetectionStrategy,
  Component,
  computed,
  HostListener,
  OnInit,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { csrfHeaders } from '../shared/csrf';

const COLLAPSED_KEY = 'explore-iam.console.sidebarCollapsed';
const MANAGE_KEY = 'explore-iam.console.manageAccountOpen';
const APPEARANCE_KEY = 'explore-iam.console.appearance';
const LANG_KEY = 'explore-iam.console.lang';
const DOCS_URL = 'https://docs.spring.io/spring-authorization-server/reference/';

type UserSubmenu = 'profile' | 'billing' | 'appearance' | 'language' | 'timezone' | null;

@Component({
  selector: 'app-console-shell',
  imports: [RouterLink, RouterLinkActive, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './console-shell.component.css',
  host: {
    class: 'block h-dvh max-h-dvh overflow-hidden',
    '[attr.data-collapsed]': 'collapsed() ? "true" : "false"',
  },
  template: `
    <div class="flex h-full flex-col overflow-hidden bg-[var(--console-bg)] text-[var(--console-fg)]">
      <header
        class="z-30 flex h-12 shrink-0 border-b border-[var(--console-border)] bg-[var(--console-sidebar)]"
      >
        <div
          class="console-aside-brand hidden shrink-0 items-center px-3 md:flex"
          [class.console-aside-collapsed]="collapsed()"
        >
          <a
            routerLink="/"
            class="flex min-w-0 items-center gap-2 text-sm font-semibold tracking-tight"
            [class.justify-center]="collapsed()"
          >
            <svg
              viewBox="0 0 24 24"
              class="size-5 shrink-0"
              aria-hidden="true"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 3l7 4v5c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V7l7-4z"
              ></path>
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M9.5 12.5l1.75 1.75L14.75 10.5"
              ></path>
            </svg>
            @if (!collapsed()) {
              <span class="truncate">Explore IAM</span>
            }
          </a>
        </div>

        <div class="flex min-w-0 flex-1 items-center justify-between px-3 sm:px-4 md:justify-end">
          <a
            routerLink="/"
            class="flex shrink-0 items-center gap-2 text-sm font-semibold tracking-tight md:hidden"
          >
            <svg
              viewBox="0 0 24 24"
              class="size-5"
              aria-hidden="true"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M12 3l7 4v5c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V7l7-4z"
              ></path>
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                d="M9.5 12.5l1.75 1.75L14.75 10.5"
              ></path>
            </svg>
            <span>Explore IAM</span>
          </a>

          <div class="relative">
          <button
            type="button"
            class="console-user-trigger"
            [attr.aria-expanded]="userMenuOpen()"
            (click)="toggleUserMenu($event)"
          >
            <span class="console-user-avatar" aria-hidden="true">{{ usernameInitial() }}</span>
            <span class="sr-only">账户菜单</span>
          </button>

          @if (userMenuOpen()) {
            <div class="console-user-menu" role="menu" (click)="$event.stopPropagation()">
              <div class="console-user-menu-email">{{ username() }}</div>
              <div class="console-user-menu-sep"></div>

              <button type="button" class="console-user-menu-item" role="menuitem" (click)="openSub('profile')">
                配置文件
              </button>
              <button type="button" class="console-user-menu-item" role="menuitem" (click)="openSub('billing')">
                账单
              </button>
              <button type="button" class="console-user-menu-item" role="menuitem" (click)="openSub('appearance')">
                <span>外观</span>
                <span class="console-user-menu-chevron" aria-hidden="true">›</span>
              </button>
              <button type="button" class="console-user-menu-item" role="menuitem" (click)="openSub('language')">
                <span>语言</span>
                <span class="console-user-menu-chevron" aria-hidden="true">›</span>
              </button>
              <button type="button" class="console-user-menu-item" role="menuitem" (click)="openSub('timezone')">
                <span>时区</span>
                <span class="console-user-menu-chevron" aria-hidden="true">›</span>
              </button>

              <div class="console-user-menu-sep"></div>
              <button
                type="button"
                class="console-user-menu-item console-user-menu-danger"
                role="menuitem"
                [disabled]="loggingOut()"
                (click)="logout()"
              >
                {{ loggingOut() ? '退出中…' : '退出登录' }}
              </button>

              @if (submenu()) {
                <div class="console-user-submenu" role="dialog">
                  <button type="button" class="console-user-submenu-back" (click)="openSub(null)">
                    ‹ 返回
                  </button>
                  @switch (submenu()) {
                    @case ('profile') {
                      <p class="console-user-submenu-title">配置文件</p>
                      <dl class="console-user-submenu-dl">
                        <div>
                          <dt>用户名</dt>
                          <dd>{{ username() }}</dd>
                        </div>
                        <div>
                          <dt>角色</dt>
                          <dd>IAM 用户</dd>
                        </div>
                      </dl>
                    }
                    @case ('billing') {
                      <p class="console-user-submenu-title">账单</p>
                      <p class="console-user-submenu-copy">
                        Explore IAM 本地开发环境不启用计费。生产账单将在后续接入。
                      </p>
                      <a
                        class="console-user-submenu-link"
                        [href]="docsUrl"
                        target="_blank"
                        rel="noopener noreferrer"
                        >查看文档</a
                      >
                    }
                    @case ('appearance') {
                      <p class="console-user-submenu-title">外观</p>
                      <button
                        type="button"
                        class="console-user-menu-item"
                        [class.console-user-menu-item-active]="appearance() === 'light'"
                        (click)="setAppearance('light')"
                      >
                        浅色
                      </button>
                      <button
                        type="button"
                        class="console-user-menu-item"
                        [class.console-user-menu-item-active]="appearance() === 'system'"
                        (click)="setAppearance('system')"
                      >
                        跟随系统
                      </button>
                    }
                    @case ('language') {
                      <p class="console-user-submenu-title">语言</p>
                      <button
                        type="button"
                        class="console-user-menu-item"
                        [class.console-user-menu-item-active]="lang() === 'zh'"
                        (click)="setLang('zh')"
                      >
                        中文
                      </button>
                      <button
                        type="button"
                        class="console-user-menu-item"
                        [class.console-user-menu-item-active]="lang() === 'en'"
                        (click)="setLang('en')"
                      >
                        English
                      </button>
                    }
                    @case ('timezone') {
                      <p class="console-user-submenu-title">时区</p>
                      <p class="console-user-submenu-copy">当前浏览器时区</p>
                      <p class="console-user-submenu-mono">{{ timezone() }}</p>
                    }
                  }
                </div>
              }
            </div>
          }
          </div>
        </div>
      </header>

      <div class="console-body flex min-h-0 flex-1">
        <aside
          class="console-aside hidden h-full shrink-0 flex-col bg-[var(--console-sidebar)] md:flex"
          [class.console-aside-collapsed]="collapsed()"
        >
          <div class="console-aside-search shrink-0 px-3 pt-2 pb-1">
            @if (!collapsed()) {
              <label class="relative block">
                <span class="sr-only">快速搜索</span>
                <input
                  id="console-sidebar-search"
                  class="block h-[2.125rem] w-full rounded-lg border border-[var(--console-border)] bg-white py-0 pr-9 pl-3 text-[0.8125rem] text-[var(--console-nav)] outline-none placeholder:text-[#8c8c8c] focus:border-[var(--console-accent)] focus:shadow-[0_0_0_3px_rgba(0,81,195,0.15)]"
                  type="search"
                  name="sidebarSearch"
                  placeholder="快速搜索…"
                  [(ngModel)]="navQueryModel"
                />
                <kbd
                  class="pointer-events-none absolute top-1/2 right-[0.45rem] -translate-y-1/2 rounded border border-[var(--console-border)] bg-[#f5f5f5] px-[0.35rem] py-[0.1rem] font-[inherit] text-[10px] text-[#8c8c8c]"
                  >⌘K</kbd
                >
              </label>
            } @else {
              <button
                type="button"
                class="flex w-full cursor-pointer items-center justify-center rounded-lg border-none bg-transparent p-1.5 text-[var(--console-nav)] hover:bg-black/[0.04] hover:text-[var(--console-fg)]"
                title="展开侧栏以搜索"
                (click)="setCollapsed(false)"
              >
                <svg viewBox="0 0 24 24" class="console-nav-icon" fill="none" stroke="currentColor" aria-hidden="true">
                  <circle cx="11" cy="11" r="6.5" />
                  <path stroke-linecap="round" d="M16 16l4 4" />
                </svg>
              </button>
            }
          </div>

          <nav class="console-aside-nav flex min-h-0 flex-1 flex-col gap-0.5 overflow-y-auto overscroll-contain px-3 py-1">
            @if (showHome()) {
              <a
                routerLink="/"
                routerLinkActive="console-nav-active"
                [routerLinkActiveOptions]="{ exact: true }"
                class="console-nav-item"
                title="账户主页"
              >
                <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M3 10.5L12 3l9 7.5V20a1 1 0 01-1 1h-5v-6H9v6H4a1 1 0 01-1-1v-9.5z" />
                </svg>
                <span class="console-nav-label">账户主页</span>
              </a>
            }

            @if (!collapsed()) {
              @if (showUsersSection()) {
                <button
                  type="button"
                  class="console-nav-item console-nav-toggle w-full"
                  (click)="toggleManageAccount()"
                >
                  <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2" />
                    <circle cx="9" cy="7" r="3.5" />
                    <path stroke-linecap="round" stroke-linejoin="round" d="M22 21v-2a3.5 3.5 0 00-2.5-3.35M16.5 3.6a3.5 3.5 0 010 6.8" />
                  </svg>
                  <span class="console-nav-label flex-1 text-left">Users and Access</span>
                  <svg
                    class="console-nav-chevron"
                    [class.console-nav-chevron-closed]="!manageAccountOpen()"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    aria-hidden="true"
                  >
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 9l6 6 6-6" />
                  </svg>
                </button>
                <div
                  class="console-nav-sub"
                  [class.console-nav-sub-open]="manageAccountOpen() && showUsersSection()"
                >
                  <div class="console-nav-sub-inner">
                    <div class="console-nav-tree">
                      @if (showPeople()) {
                        <a
                          routerLink="/users-and-access/people"
                          routerLinkActive="console-nav-active"
                          class="console-nav-item console-nav-child"
                          title="People"
                        >
                          <span class="console-nav-label">People</span>
                        </a>
                      }
                      @if (showGroups()) {
                        <a
                          routerLink="/users-and-access/groups"
                          routerLinkActive="console-nav-active"
                          class="console-nav-item console-nav-child"
                          title="Groups"
                        >
                          <span class="console-nav-label">Groups</span>
                        </a>
                      }
                      @if (showRoles()) {
                        <a
                          routerLink="/users-and-access/roles"
                          routerLinkActive="console-nav-active"
                          class="console-nav-item console-nav-child"
                          title="Roles"
                        >
                          <span class="console-nav-label">Roles</span>
                        </a>
                      }
                    </div>
                  </div>
                </div>
              }

              @if (showPermissionsSection()) {
                <p class="console-nav-section-label mt-2 mb-0.5 px-2 text-[0.6875rem] font-semibold tracking-wide text-[var(--console-muted)] uppercase">
                  Permissions
                </p>
                @if (showPoints()) {
                  <a
                    routerLink="/permissions/points"
                    routerLinkActive="console-nav-active"
                    class="console-nav-item"
                    title="Points"
                  >
                    <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M12 3l8 4.5v9L12 21l-8-4.5v-9L12 3z" />
                      <path stroke-linecap="round" d="M12 12l8-4.5M12 12v9M12 12L4 7.5" />
                    </svg>
                    <span class="console-nav-label">Points</span>
                  </a>
                }
                @if (showPolicies()) {
                  <a
                    routerLink="/permissions/policies"
                    routerLinkActive="console-nav-active"
                    class="console-nav-item"
                    title="Policies"
                  >
                    <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M8 4h9a2 2 0 012 2v14l-4-2-4 2-4-2-4 2V6a2 2 0 012-2h1" />
                    </svg>
                    <span class="console-nav-label">Policies</span>
                  </a>
                }
              }

              @if (showApps()) {
                <a
                  routerLink="/apps"
                  routerLinkActive="console-nav-active"
                  class="console-nav-item"
                  title="Apps"
                >
                  <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                    <rect x="4" y="4" width="7" height="7" rx="1.5" />
                    <rect x="13" y="4" width="7" height="7" rx="1.5" />
                    <rect x="4" y="13" width="7" height="7" rx="1.5" />
                    <rect x="13" y="13" width="7" height="7" rx="1.5" />
                  </svg>
                  <span class="console-nav-label">Apps</span>
                </a>
              }

              @if (showActivity()) {
                <a
                  routerLink="/activity"
                  routerLinkActive="console-nav-active"
                  class="console-nav-item"
                  title="Activity"
                >
                  <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 19V5M4 19h16M8 15l3-4 2.5 3L17 9" />
                  </svg>
                  <span class="console-nav-label">Activity</span>
                </a>
              }
            } @else {
              @if (showPeople()) {
                <a
                  routerLink="/users-and-access/people"
                  routerLinkActive="console-nav-active"
                  class="console-nav-item"
                  title="People"
                >
                  <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2" />
                    <circle cx="9" cy="7" r="3.5" />
                  </svg>
                </a>
              }
              @if (showApps()) {
                <a
                  routerLink="/apps"
                  routerLinkActive="console-nav-active"
                  class="console-nav-item"
                  title="Apps"
                >
                  <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                    <rect x="4" y="4" width="7" height="7" rx="1.5" />
                    <rect x="13" y="4" width="7" height="7" rx="1.5" />
                    <rect x="4" y="13" width="7" height="7" rx="1.5" />
                    <rect x="13" y="13" width="7" height="7" rx="1.5" />
                  </svg>
                </a>
              }
              @if (showActivity()) {
                <a
                  routerLink="/activity"
                  routerLinkActive="console-nav-active"
                  class="console-nav-item"
                  title="Activity"
                >
                  <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M4 19V5M4 19h16M8 15l3-4 2.5 3L17 9" />
                  </svg>
                </a>
              }
            }
          </nav>

          <div
            class="box-border flex h-[var(--console-chrome-foot-h)] shrink-0 items-center border-t border-[var(--console-border)] px-3 pt-0 pb-0"
          >
            <button
              type="button"
              class="console-nav-item console-aside-collapse w-auto max-w-full justify-start"
              [title]="collapsed() ? '展开侧栏' : '收起侧栏'"
              [attr.aria-label]="collapsed() ? '展开侧栏' : '收起侧栏'"
              (click)="setCollapsed(!collapsed())"
            >
              <svg class="console-nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" aria-hidden="true">
                @if (collapsed()) {
                  <rect x="4" y="5" width="16" height="14" rx="2" />
                  <path stroke-linecap="round" d="M10 5v14M13 12h5M15.5 9.5L18 12l-2.5 2.5" />
                } @else {
                  <rect x="4" y="5" width="16" height="14" rx="2" />
                  <path stroke-linecap="round" d="M10 5v14M8 12H5M7.5 9.5L5 12l2.5 2.5" />
                }
              </svg>
            </button>
          </div>
        </aside>

        <div class="console-main flex min-h-0 min-w-0 flex-1 flex-col overflow-y-auto overscroll-contain bg-[var(--console-sidebar)]">
          <main class="min-w-0 flex-1 px-4 py-6 sm:px-8 lg:px-10">
            <div class="mx-auto w-full max-w-6xl">
              <ng-content />
            </div>
          </main>

          <footer
            class="box-border flex h-[var(--console-chrome-foot-h)] shrink-0 items-center border-t border-[var(--console-border)] bg-[var(--console-sidebar)] px-4 pt-0 pb-0 text-xs text-[var(--console-muted)] sm:px-8"
          >
            <div class="mx-auto flex w-full max-w-6xl flex-wrap items-center gap-x-4 gap-y-1">
              <a
                [href]="docsUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="hover:text-[var(--console-fg)]"
                >Support</a
              >
              <span class="sm:ml-auto">© {{ year }} Explore IAM</span>
            </div>
          </footer>
        </div>
      </div>
    </div>
  `,
})
export class ConsoleShellComponent implements OnInit {
  readonly docsUrl = DOCS_URL;
  readonly year = new Date().getFullYear();
  readonly collapsed = signal(false);
  readonly manageAccountOpen = signal(true);
  readonly navQuery = signal('');
  readonly userMenuOpen = signal(false);
  readonly submenu = signal<UserSubmenu>(null);
  readonly loggingOut = signal(false);
  readonly appearance = signal<'light' | 'system'>('light');
  readonly lang = signal<'zh' | 'en'>('zh');
  readonly username = signal('demo');
  readonly timezone = signal(Intl.DateTimeFormat().resolvedOptions().timeZone);

  private readonly q = computed(() => this.navQuery().trim().toLowerCase());

  ngOnInit(): void {
    try {
      this.collapsed.set(localStorage.getItem(COLLAPSED_KEY) === '1');
      if (localStorage.getItem(MANAGE_KEY) === '0') {
        this.manageAccountOpen.set(false);
      }
      const appearance = localStorage.getItem(APPEARANCE_KEY);
      if (appearance === 'light' || appearance === 'system') {
        this.appearance.set(appearance);
      }
      const lang = localStorage.getItem(LANG_KEY);
      if (lang === 'zh' || lang === 'en') {
        this.lang.set(lang);
      }
    } catch {
      /* ignore */
    }
  }

  usernameInitial(): string {
    const name = this.username().trim();
    return name ? name.charAt(0).toUpperCase() : 'U';
  }

  @HostListener('document:keydown', ['$event'])
  onKeydown(event: KeyboardEvent): void {
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      if (this.collapsed()) {
        this.setCollapsed(false);
      }
      queueMicrotask(() =>
        document.querySelector<HTMLInputElement>('#console-sidebar-search')?.focus(),
      );
    }
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.userMenuOpen.set(false);
    this.submenu.set(null);
  }

  toggleUserMenu(event: MouseEvent): void {
    event.stopPropagation();
    const next = !this.userMenuOpen();
    this.userMenuOpen.set(next);
    if (!next) {
      this.submenu.set(null);
    }
  }

  openSub(value: UserSubmenu): void {
    this.submenu.set(value);
  }

  setAppearance(value: 'light' | 'system'): void {
    this.appearance.set(value);
    try {
      localStorage.setItem(APPEARANCE_KEY, value);
    } catch {
      /* ignore */
    }
  }

  setLang(value: 'zh' | 'en'): void {
    this.lang.set(value);
    try {
      localStorage.setItem(LANG_KEY, value);
    } catch {
      /* ignore */
    }
  }

  async logout(): Promise<void> {
    this.loggingOut.set(true);
    try {
      await fetch('/logout', {
        method: 'POST',
        credentials: 'same-origin',
        headers: {
          ...csrfHeaders(),
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: '',
      });
    } catch {
      /* still redirect */
    } finally {
      this.loggingOut.set(false);
      this.userMenuOpen.set(false);
      window.location.href = '/login';
    }
  }

  setCollapsed(value: boolean): void {
    this.collapsed.set(value);
    try {
      localStorage.setItem(COLLAPSED_KEY, value ? '1' : '0');
    } catch {
      /* ignore */
    }
  }

  toggleManageAccount(): void {
    const next = !this.manageAccountOpen();
    this.manageAccountOpen.set(next);
    try {
      localStorage.setItem(MANAGE_KEY, next ? '1' : '0');
    } catch {
      /* ignore */
    }
  }

  showHome(): boolean {
    return this.matches('账户主页') || this.matches('home') || this.q() === '';
  }

  showPeople(): boolean {
    return (
      this.matches('people') ||
      this.matches('用户') ||
      this.matches('user') ||
      this.q() === ''
    );
  }

  showGroups(): boolean {
    return this.matches('group') || this.matches('组') || this.q() === '';
  }

  showRoles(): boolean {
    return this.matches('role') || this.matches('角色') || this.q() === '';
  }

  showUsersSection(): boolean {
    return (
      this.showPeople() ||
      this.showGroups() ||
      this.showRoles() ||
      this.matches('users and access') ||
      this.matches('access') ||
      this.q() === ''
    );
  }

  showPoints(): boolean {
    return (
      this.matches('point') ||
      this.matches('权限点') ||
      this.matches('permission') ||
      this.q() === ''
    );
  }

  showPolicies(): boolean {
    return this.matches('polic') || this.matches('策略') || this.q() === '';
  }

  showPermissionsSection(): boolean {
    return this.showPoints() || this.showPolicies() || this.q() === '';
  }

  showApps(): boolean {
    return (
      this.matches('app') ||
      this.matches('应用') ||
      this.matches('oauth') ||
      this.matches('client') ||
      this.matches('客户端') ||
      this.q() === ''
    );
  }

  showActivity(): boolean {
    return (
      this.matches('activity') ||
      this.matches('审计') ||
      this.matches('audit') ||
      this.q() === ''
    );
  }

  private matches(label: string): boolean {
    const query = this.q();
    if (!query) {
      return true;
    }
    return label.toLowerCase().includes(query);
  }

  get navQueryModel(): string {
    return this.navQuery();
  }
  set navQueryModel(value: string) {
    this.navQuery.set(value);
  }
}

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
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';

const DOCS_URL = 'https://docs.spring.io/spring-authorization-server/reference/';

const CARD =
  'rounded-lg border border-[var(--console-border)] bg-white shadow-[0_1px_2px_rgba(0,0,0,0.04)]';
const BTN_PRIMARY =
  'inline-flex h-9 items-center justify-center rounded-md border border-transparent bg-[var(--console-accent-soft)] px-4 text-sm font-medium text-white no-underline hover:bg-[var(--console-accent)] disabled:cursor-not-allowed disabled:opacity-50';
const BTN_SECONDARY =
  'inline-flex h-9 items-center justify-center rounded-md border border-[var(--console-border)] bg-white px-4 text-sm font-medium text-[var(--console-fg)] no-underline hover:bg-[var(--console-bg)]';
const FIELD =
  'block h-10 w-full rounded-md border border-[var(--console-border)] bg-white px-3 py-2 text-sm leading-5 text-[var(--console-fg)] outline-none focus:border-[var(--console-accent)] focus:shadow-[0_0_0_3px_rgba(0,81,195,0.18)]';

export interface ClientView {
  id: string;
  clientId: string;
  clientName: string;
  clientSecret: string | null;
  clientUri: string | null;
  redirectUris: string[];
  postLogoutRedirectUris: string[];
  scopes: string[];
  responseTypes: string[];
  authorizationGrantTypes: string[];
  clientAuthenticationMethods: string[];
}

@Component({
  selector: 'app-clients-list-page',
  imports: [ConsoleShellComponent, RouterLink, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-[1.75rem] font-semibold tracking-tight">OAuth 客户端</h1>
          <p class="mt-1 text-sm text-[var(--console-muted)]">管理您的 OAuth 客户端</p>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <a
            [href]="docsUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="${BTN_SECONDARY}"
            >文档</a
          >
          <a routerLink="/clients/new" class="${BTN_PRIMARY}">+ 创建客户端</a>
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

      @if (copyHint()) {
        <p class="mt-3 text-xs text-[var(--console-muted)]" role="status">{{ copyHint() }}</p>
      }

      <div class="mt-5">
        <label class="relative block max-w-md">
          <span class="sr-only">搜索客户端</span>
          <svg
            class="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-[var(--console-muted)]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            aria-hidden="true"
          >
            <circle cx="11" cy="11" r="6.5" />
            <path stroke-linecap="round" d="M16 16l4 4" />
          </svg>
          <input
            class="${FIELD} !pl-9"
            type="search"
            name="clientSearch"
            placeholder="搜索客户端…"
            [(ngModel)]="searchModel"
          />
        </label>
      </div>

      <section class="${CARD} mt-4 overflow-hidden">
        @if (loading()) {
          <p class="px-5 py-8 text-sm text-[var(--console-muted)]">加载中…</p>
        } @else if (filtered().length === 0) {
          <div class="px-5 py-14 text-center">
            <p class="text-sm text-[var(--console-muted)]">
              {{ clients().length === 0 ? '暂无客户端。创建一个以接入业务应用。' : '没有匹配的客户端。' }}
            </p>
            @if (clients().length === 0) {
              <a routerLink="/clients/new" class="${BTN_PRIMARY} mt-4">+ 创建客户端</a>
            }
          </div>
        } @else {
          <div class="overflow-x-auto">
            <table class="w-full border-collapse text-sm">
              <thead>
                <tr>
                  <th
                    class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]"
                  >
                    客户端名称
                  </th>
                  <th
                    class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]"
                  >
                    范围
                  </th>
                  <th
                    class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]"
                  >
                    Visibility
                  </th>
                  <th
                    class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]"
                  >
                    Redirect
                  </th>
                  <th
                    class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]"
                  ></th>
                </tr>
              </thead>
              <tbody>
                @for (client of filtered(); track client.clientId) {
                  <tr class="hover:bg-[#fafafa] last:[&_td]:border-b-0">
                    <td
                      class="border-b border-[var(--console-border)] px-4 py-3.5 align-top text-[var(--console-fg)]"
                    >
                      <div class="flex items-center gap-2.5">
                        <span
                          class="inline-flex size-7 shrink-0 items-center justify-center rounded bg-[#e8e8e8] text-xs font-semibold text-[var(--console-fg)]"
                          >{{ initial(client.clientName) }}</span
                        >
                        <div class="min-w-0">
                          <div class="font-medium">{{ client.clientName }}</div>
                          <code class="text-[11px] text-[var(--console-muted)]">{{
                            client.clientId
                          }}</code>
                        </div>
                      </div>
                    </td>
                    <td
                      class="border-b border-[var(--console-border)] px-4 py-3.5 align-top text-[var(--console-fg)]"
                    >
                      <button
                        type="button"
                        class="text-sm font-medium text-[var(--console-accent)] hover:underline"
                        [title]="client.scopes.join(', ')"
                      >
                        {{ client.scopes.length }} 个范围
                      </button>
                    </td>
                    <td
                      class="border-b border-[var(--console-border)] px-4 py-3.5 align-top text-[var(--console-fg)]"
                    >
                      <span
                        class="inline-flex items-center gap-[0.35rem] rounded-full bg-[#f3f3f3] px-[0.55rem] py-[0.15rem] text-xs text-[var(--console-fg)]"
                      >
                        <span
                          class="size-[0.4rem] rounded-full"
                          [style.background]="
                            isPublic(client) ? '#d97706' : 'var(--console-accent)'
                          "
                        ></span>
                        {{ isPublic(client) ? 'Public' : 'Private' }}
                      </span>
                    </td>
                    <td
                      class="max-w-[14rem] border-b border-[var(--console-border)] px-4 py-3.5 align-top text-[var(--console-fg)]"
                    >
                      <span class="break-all text-[var(--console-muted)]">{{
                        primaryRedirect(client)
                      }}</span>
                      @if (client.redirectUris.length > 1) {
                        <span class="ml-1 text-xs text-[var(--console-muted)]"
                          >+{{ client.redirectUris.length - 1 }}</span
                        >
                      }
                    </td>
                    <td
                      class="whitespace-nowrap border-b border-[var(--console-border)] px-4 py-3.5 text-right align-top text-[var(--console-fg)]"
                    >
                      <div class="relative inline-block">
                        <button
                          type="button"
                          class="inline-flex size-8 items-center justify-center rounded-md text-[var(--console-muted)] hover:bg-[var(--console-bg)] hover:text-[var(--console-fg)]"
                          [attr.aria-expanded]="menuFor() === client.clientId"
                          (click)="toggleMenu(client.clientId, $event)"
                        >
                          <span class="sr-only">操作</span>
                          <svg viewBox="0 0 24 24" class="size-4" fill="currentColor" aria-hidden="true">
                            <circle cx="12" cy="5" r="1.5" />
                            <circle cx="12" cy="12" r="1.5" />
                            <circle cx="12" cy="19" r="1.5" />
                          </svg>
                        </button>
                        @if (menuFor() === client.clientId) {
                          <div
                            class="absolute right-0 z-20 min-w-32 rounded-md border border-[var(--console-border)] bg-white p-1 shadow-[0_8px_24px_rgba(0,0,0,0.08)]"
                            role="menu"
                          >
                            <button
                              type="button"
                              class="block w-full cursor-pointer rounded border-none bg-transparent px-[0.6rem] py-[0.4rem] text-left text-[0.8125rem] text-[var(--console-fg)] hover:bg-[var(--console-bg)]"
                              role="menuitem"
                              (click)="copyClientId(client.clientId)"
                            >
                              复制 ID
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
            class="flex items-center justify-between border-t border-[var(--console-border)] px-4 py-2.5 text-xs text-[var(--console-muted)]"
          >
            <span
              >显示 1–{{ filtered().length }}，共 {{ filtered().length }} 个客户端</span
            >
          </div>
        }
      </section>
    </app-console-shell>
  `,
})
export class ClientsListPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly docsUrl = DOCS_URL;
  readonly clients = signal<ClientView[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly copyHint = signal<string | null>(null);
  readonly search = signal('');
  readonly menuFor = signal<string | null>(null);

  readonly filtered = computed(() => {
    const q = this.search().trim().toLowerCase();
    if (!q) {
      return this.clients();
    }
    return this.clients().filter(
      (c) =>
        c.clientName.toLowerCase().includes(q) || c.clientId.toLowerCase().includes(q),
    );
  });

  get searchModel(): string {
    return this.search();
  }
  set searchModel(value: string) {
    this.search.set(value);
  }

  ngOnInit(): void {
    const q = this.route.snapshot.queryParamMap.get('q');
    if (q) {
      this.search.set(q);
    }

    this.http.get<ClientView[]>('/api/clients', { withCredentials: true }).subscribe({
      next: (list) => {
        this.clients.set(list);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.loading.set(false);
        this.handleError(err);
      },
    });
  }

  @HostListener('document:click')
  onDocumentClick(): void {
    this.menuFor.set(null);
  }

  initial(name: string): string {
    const trimmed = name.trim();
    return trimmed ? trimmed.charAt(0).toUpperCase() : '?';
  }

  isPublic(client: ClientView): boolean {
    return client.clientAuthenticationMethods?.includes('none') ?? false;
  }

  primaryRedirect(client: ClientView): string {
    return client.redirectUris[0] ?? '—';
  }

  toggleMenu(clientId: string, event: MouseEvent): void {
    event.stopPropagation();
    this.menuFor.set(this.menuFor() === clientId ? null : clientId);
  }

  async copyClientId(clientId: string): Promise<void> {
    this.menuFor.set(null);
    try {
      await navigator.clipboard.writeText(clientId);
      this.copyHint.set(`已复制 ${clientId}`);
      window.setTimeout(() => this.copyHint.set(null), 2000);
    } catch {
      this.copyHint.set('复制失败，请手动选择 Client ID');
    }
  }

  private handleError(err: unknown): void {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401 || err.status === 403) {
        void this.router.navigate(['/login'], { queryParams: { continue: '/clients' } });
        return;
      }
      const message = (err.error as { message?: string } | null)?.message;
      this.errorMessage.set(message || `请求失败（${err.status}）`);
      return;
    }
    this.errorMessage.set('请求失败，请重试。');
  }
}

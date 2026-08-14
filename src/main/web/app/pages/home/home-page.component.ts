import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import type { EChartsCoreOption } from 'echarts/core';
import { NgxEchartsDirective } from 'ngx-echarts';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import {
  buildMetricLineOption,
  buildSparklineOption,
  formatCompact,
  formatDelta,
} from '../../shared/charts/chart-option.util';

interface ClientSummary {
  clientId: string;
  clientName: string;
}

interface MetricCard {
  id: string;
  title: string;
  value: string;
  delta: number | null;
  deltaLabel: string | null;
  empty: boolean;
  chart: EChartsCoreOption | null;
  sparkline: EChartsCoreOption | null;
  wide: boolean;
}

type RangeKey = '24h' | '7d' | '30d';

const RANGE_LABELS: Record<RangeKey, string> = {
  '24h': '过去 24 小时',
  '7d': '过去 7 天',
  '30d': '过去 30 天',
};

const CARD =
  'rounded-lg border border-[var(--console-border)] bg-white shadow-[0_1px_2px_rgba(0,0,0,0.04)]';
const BTN_PRIMARY =
  'inline-flex h-9 items-center justify-center rounded-md border border-transparent bg-[var(--console-accent-soft)] px-4 text-sm font-medium text-white no-underline hover:bg-[var(--console-accent)] disabled:cursor-not-allowed disabled:opacity-50';

@Component({
  selector: 'app-home-page',
  imports: [ConsoleShellComponent, RouterLink, NgxEchartsDirective],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <app-console-shell>
      <div class="w-full">
        <div class="flex flex-col items-center text-center">
          <p class="m-0 text-xs font-medium text-[var(--console-muted)]">Explore IAM</p>
          <h1
            class="mt-[0.35rem] mb-0 text-[clamp(1.75rem,2.5vw,2.25rem)] font-semibold tracking-[-0.03em] leading-[1.15]"
          >
            Ready when you are.
          </h1>

          <label
            class="mt-5 flex h-11 w-full max-w-xl items-center gap-[0.65rem] rounded-lg border border-[var(--console-border)] bg-white px-[0.85rem] focus-within:border-[var(--console-accent)] focus-within:shadow-[0_0_0_3px_rgba(0,81,195,0.12)]"
          >
            <svg
              class="size-4 shrink-0 text-[var(--console-muted)]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
              aria-hidden="true"
            >
              <circle cx="11" cy="11" r="7" />
              <path stroke-linecap="round" d="M20 20l-3.5-3.5" />
            </svg>
            <input
              #homeSearch
              type="search"
              class="min-w-0 flex-1 border-0 bg-transparent text-sm text-[var(--console-fg)] outline-none"
              placeholder="搜索客户端、文档或设置…"
              [value]="searchQuery()"
              (input)="searchQuery.set($any($event.target).value)"
              (keydown.enter)="onSearchEnter()"
            />
            <kbd
              class="shrink-0 rounded border border-[var(--console-border)] bg-[var(--console-bg)] px-[0.4rem] py-[0.1rem] text-[0.6875rem] text-[var(--console-muted)]"
              >⌘ K</kbd
            >
          </label>
        </div>

        <div class="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
          <section class="${CARD} min-h-44 px-[1.1rem] pt-4 pb-[1.1rem]">
            <header class="mb-3 flex items-center justify-between gap-2">
              <h2 class="m-0 text-[0.8125rem] font-semibold">
                <a
                  routerLink="/clients"
                  class="text-inherit no-underline hover:text-[var(--console-accent)]"
                  >OAuth 客户端</a
                >
              </h2>
              <a
                routerLink="/clients"
                class="text-xs text-[var(--console-accent)] no-underline hover:underline"
                >查看全部</a
              >
            </header>
            @if (clientsLoading()) {
              <p class="m-0 text-[0.8125rem] text-[var(--console-muted)]">加载中…</p>
            } @else if (clients().length === 0) {
              <p class="m-0 text-[0.8125rem] text-[var(--console-muted)]">
                尚无客户端。创建后将显示在此处。
              </p>
              <a routerLink="/clients/new" class="${BTN_PRIMARY} mt-3 inline-flex">创建客户端</a>
            } @else {
              <ul class="m-0 list-none p-0">
                @for (c of clients().slice(0, 5); track c.clientId) {
                  <li>
                    <a
                      [routerLink]="['/clients']"
                      [queryParams]="{ q: c.clientId }"
                      class="-mx-1 flex cursor-pointer items-center gap-[0.65rem] rounded-md px-1 py-[0.45rem] text-inherit no-underline hover:bg-[var(--console-bg)]"
                    >
                      <span
                        class="inline-flex size-7 shrink-0 items-center justify-center rounded-[0.35rem] bg-[#e8f1ff] text-xs font-semibold text-[var(--console-accent)]"
                        aria-hidden="true"
                        >{{ initial(c.clientName) }}</span
                      >
                      <span class="flex min-w-0 flex-1 flex-col">
                        <span class="truncate text-[0.8125rem] font-medium">{{ c.clientName }}</span>
                        <span class="truncate text-[0.6875rem] text-[var(--console-muted)]">{{
                          c.clientId
                        }}</span>
                      </span>
                      <span
                        class="text-base leading-none text-[var(--console-muted)]"
                        aria-hidden="true"
                        >›</span
                      >
                    </a>
                  </li>
                }
              </ul>
            }
          </section>

          <section class="${CARD} min-h-44 px-[1.1rem] pt-4 pb-[1.1rem]">
            <header class="mb-3 flex items-center justify-between gap-2">
              <h2 class="m-0 text-[0.8125rem] font-semibold">
                <a
                  routerLink="/clients/new"
                  class="text-inherit no-underline hover:text-[var(--console-accent)]"
                  >快速操作</a
                >
              </h2>
            </header>
            <p class="m-0 text-[0.8125rem] leading-[1.45] text-[var(--console-muted)]">
              注册 Relying Party，获取 client_id / secret。
            </p>
            <a
              routerLink="/clients/new"
              class="${BTN_PRIMARY} mt-4 inline-flex w-full justify-center"
              >Ship something new</a
            >
          </section>

          <section class="${CARD} min-h-44 px-[1.1rem] pt-4 pb-[1.1rem]">
            <header class="mb-3 flex items-center justify-between gap-2">
              <h2 class="m-0 text-[0.8125rem] font-semibold">最近访问</h2>
            </header>
            <ul class="m-0 list-none p-0">
              @for (item of recents; track item.path) {
                <li>
                  @if (item.external) {
                    <a
                      [href]="item.path"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="-mx-1 flex cursor-pointer items-center gap-[0.65rem] rounded-md px-1 py-[0.45rem] text-inherit no-underline hover:bg-[var(--console-bg)]"
                    >
                      <span class="flex min-w-0 flex-1 flex-col">
                        <span class="truncate text-[0.8125rem] font-medium">{{ item.label }}</span>
                        <span class="truncate text-[0.6875rem] text-[var(--console-muted)]">{{
                          item.group
                        }}</span>
                      </span>
                      <span
                        class="text-base leading-none text-[var(--console-muted)]"
                        aria-hidden="true"
                        >›</span
                      >
                    </a>
                  } @else {
                    <a
                      [routerLink]="item.path"
                      class="-mx-1 flex cursor-pointer items-center gap-[0.65rem] rounded-md px-1 py-[0.45rem] text-inherit no-underline hover:bg-[var(--console-bg)]"
                    >
                      <span class="flex min-w-0 flex-1 flex-col">
                        <span class="truncate text-[0.8125rem] font-medium">{{ item.label }}</span>
                        <span class="truncate text-[0.6875rem] text-[var(--console-muted)]">{{
                          item.group
                        }}</span>
                      </span>
                      <span
                        class="text-base leading-none text-[var(--console-muted)]"
                        aria-hidden="true"
                        >›</span
                      >
                    </a>
                  }
                </li>
              }
            </ul>
          </section>
        </div>

        <section class="mt-8">
          <header class="mb-[0.85rem] flex flex-wrap items-center justify-between gap-3">
            <h2 class="m-0 text-base font-semibold">Analytics</h2>
            <div class="flex items-center gap-2">
              <label>
                <span class="sr-only">时间范围</span>
                <select
                  class="h-8 rounded-md border border-[var(--console-border)] bg-white px-[0.65rem] text-[0.8125rem] text-[var(--console-fg)]"
                  [value]="range()"
                  (change)="setRange($any($event.target).value)"
                >
                  @for (key of rangeKeys; track key) {
                    <option [value]="key">{{ rangeLabels[key] }}</option>
                  }
                </select>
              </label>
              <button
                type="button"
                class="inline-flex size-8 cursor-pointer items-center justify-center rounded-md border border-[var(--console-border)] bg-white text-[var(--console-muted)] hover:bg-[var(--console-bg)] hover:text-[var(--console-fg)]"
                title="刷新"
                (click)="refreshMetrics()"
              >
                <svg
                  class="size-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="1.5"
                  aria-hidden="true"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M4 12a8 8 0 0114.32-4.9M20 12a8 8 0 01-14.32 4.9M4 7v5h5M20 17v-5h-5"
                  />
                </svg>
              </button>
            </div>
          </header>

          <div class="grid grid-cols-1 gap-4 min-[900px]:grid-cols-2">
            @for (m of primaryMetrics(); track m.id) {
              <article class="${CARD} flex min-h-56 flex-col px-[1.1rem] pt-4 pb-3">
                <div class="flex items-baseline justify-between gap-2">
                  <h3 class="m-0 text-[0.8125rem] font-medium text-[var(--console-muted)]">
                    {{ m.title }}
                  </h3>
                  @if (m.deltaLabel) {
                    <span
                      [class]="
                        (m.delta ?? 0) > 0
                          ? 'text-xs font-medium text-[#0a7a3e]'
                          : (m.delta ?? 0) < 0
                            ? 'text-xs font-medium text-[#c9372c]'
                            : 'text-xs font-medium text-[var(--console-muted)]'
                      "
                      >{{ m.deltaLabel }}</span
                    >
                  }
                </div>
                <p class="mt-[0.35rem] mb-0 text-[1.75rem] font-semibold tracking-[-0.02em] leading-[1.2]">
                  {{ m.value }}
                </p>
                @if (m.empty) {
                  <p
                    class="mt-auto mb-0 py-8 text-center text-[0.8125rem] text-[var(--console-muted)]"
                  >
                    No data
                  </p>
                } @else if (m.chart) {
                  <div echarts [options]="m.chart" class="mt-auto h-[8.5rem] w-full"></div>
                }
              </article>
            }
          </div>

          <div class="mt-4 grid grid-cols-2 gap-4 min-[900px]:grid-cols-4">
            @for (m of miniMetrics(); track m.id) {
              @if (m.id === 'clients') {
                <a
                  routerLink="/clients"
                  class="${CARD} flex min-h-[7.5rem] cursor-pointer flex-col px-4 pt-[0.9rem] pb-[0.65rem] text-inherit no-underline transition-[border-color] duration-[160ms] hover:border-[var(--console-accent-soft)]"
                >
                  <div class="flex items-baseline justify-between gap-2">
                    <h3 class="m-0 text-[0.8125rem] font-medium text-[var(--console-muted)]">
                      {{ m.title }}
                    </h3>
                  </div>
                  <p
                    class="mt-[0.35rem] mb-0 text-[1.35rem] font-semibold tracking-[-0.02em] leading-[1.2]"
                  >
                    {{ m.value }}
                  </p>
                </a>
              } @else {
                <article class="${CARD} flex min-h-[7.5rem] flex-col px-4 pt-[0.9rem] pb-[0.65rem]">
                  <div class="flex items-baseline justify-between gap-2">
                    <h3 class="m-0 text-[0.8125rem] font-medium text-[var(--console-muted)]">
                      {{ m.title }}
                    </h3>
                    @if (m.deltaLabel) {
                      <span
                        [class]="
                          (m.delta ?? 0) > 0
                            ? 'text-xs font-medium text-[#0a7a3e]'
                            : (m.delta ?? 0) < 0
                              ? 'text-xs font-medium text-[#c9372c]'
                              : 'text-xs font-medium text-[var(--console-muted)]'
                        "
                        >{{ m.deltaLabel }}</span
                      >
                    }
                  </div>
                  <p
                    class="mt-[0.35rem] mb-0 text-[1.35rem] font-semibold tracking-[-0.02em] leading-[1.2]"
                  >
                    {{ m.value }}
                  </p>
                  @if (m.sparkline) {
                    <div echarts [options]="m.sparkline" class="mt-auto h-9 w-full"></div>
                  }
                </article>
              }
            }
          </div>

          <p class="mt-[0.85rem] mb-0 text-[0.6875rem] text-[var(--console-muted)]">
            指标为控制台演示序列；已注册客户端数取自实时 API。正式遥测可后续接入 Actuator /
            OTel。
          </p>
        </section>
      </div>
    </app-console-shell>
  `,
})
export class HomePageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly rangeKeys: RangeKey[] = ['24h', '7d', '30d'];
  readonly rangeLabels = RANGE_LABELS;
  readonly range = signal<RangeKey>('24h');
  readonly searchQuery = signal('');
  readonly clients = signal<ClientSummary[]>([]);
  readonly clientsLoading = signal(true);
  readonly seed = signal(1);

  readonly recents = [
    { label: 'OAuth 客户端', group: '管理账户', path: '/clients', external: false },
    { label: '创建客户端', group: '管理账户', path: '/clients/new', external: false },
    {
      label: 'Authorization Server 文档',
      group: '文档',
      path: 'https://docs.spring.io/spring-authorization-server/reference/',
      external: true,
    },
    { label: '账户主页', group: '导航', path: '/', external: false },
  ] as const;

  private readonly series = computed(() => this.buildSeries(this.range(), this.seed()));

  readonly primaryMetrics = computed((): MetricCard[] => {
    const s = this.series();
    const clientsCount = this.clients().length;
    return [
      {
        id: 'auth-requests',
        title: '认证请求',
        value: formatCompact(s.authTotal),
        delta: s.authDelta,
        deltaLabel: formatDelta(s.authDelta),
        empty: false,
        chart: buildMetricLineOption(s.authSeries),
        sparkline: null,
        wide: true,
      },
      {
        id: 'token-issues',
        title: 'Token 签发',
        value: clientsCount === 0 && s.tokenTotal === 0 ? '0' : formatCompact(s.tokenTotal),
        delta: null,
        deltaLabel: null,
        empty: s.tokenTotal === 0,
        chart: s.tokenTotal === 0 ? null : buildMetricLineOption(s.tokenSeries),
        sparkline: null,
        wide: true,
      },
    ];
  });

  readonly miniMetrics = computed((): MetricCard[] => {
    const s = this.series();
    const clientsCount = this.clients().length;
    return [
      {
        id: 'auth-failures',
        title: '认证失败',
        value: formatCompact(s.failTotal),
        delta: null,
        deltaLabel: null,
        empty: false,
        chart: null,
        sparkline: null,
        wide: false,
      },
      {
        id: 'session-reuse',
        title: '会话复用率',
        value: `${s.reusePct.toFixed(2)}%`,
        delta: s.reuseDelta,
        deltaLabel: formatDelta(s.reuseDelta),
        empty: false,
        chart: null,
        sparkline: buildSparklineOption(s.reuseSeries.map((x) => x.value)),
        wide: false,
      },
      {
        id: 'token-p90',
        title: 'Token 延迟 P90',
        value: `${s.latencyP90} ms`,
        delta: null,
        deltaLabel: null,
        empty: false,
        chart: null,
        sparkline: null,
        wide: false,
      },
      {
        id: 'clients',
        title: '已注册客户端',
        value: String(clientsCount),
        delta: null,
        deltaLabel: null,
        empty: false,
        chart: null,
        sparkline: null,
        wide: false,
      },
    ];
  });

  ngOnInit(): void {
    this.http.get<ClientSummary[]>('/api/clients', { withCredentials: true }).subscribe({
      next: (list) => {
        this.clients.set(list.map((c) => ({ clientId: c.clientId, clientName: c.clientName })));
        this.clientsLoading.set(false);
      },
      error: () => {
        this.clientsLoading.set(false);
      },
    });
  }

  setRange(value: string): void {
    if (value === '24h' || value === '7d' || value === '30d') {
      this.range.set(value);
    }
  }

  refreshMetrics(): void {
    this.seed.update((n) => n + 1);
  }

  onSearchEnter(): void {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) {
      void this.router.navigateByUrl('/clients');
      return;
    }
    if (q.includes('new') || q.includes('创建') || q.includes('注册')) {
      void this.router.navigateByUrl('/clients/new');
      return;
    }
    if (q.includes('doc') || q.includes('文档') || q.includes('oauth')) {
      window.open(
        'https://docs.spring.io/spring-authorization-server/reference/',
        '_blank',
        'noopener,noreferrer',
      );
      return;
    }
    void this.router.navigate(['/clients'], { queryParams: { q: this.searchQuery().trim() } });
  }

  initial(name: string): string {
    const t = name.trim();
    return t ? t.charAt(0).toUpperCase() : '?';
  }

  private buildSeries(range: RangeKey, seed: number) {
    const points = range === '24h' ? 24 : range === '7d' ? 7 : 30;
    const labelFn =
      range === '24h'
        ? (i: number) => `${String(i).padStart(2, '0')}:00`
        : (i: number) => `D${i + 1}`;

    const authSeries = this.wave(points, 40 + seed * 3, 180, seed).map((value, i) => ({
      label: labelFn(i),
      value,
    }));
    const tokenSeries = this.wave(points, 10 + seed, 90, seed + 2).map((value, i) => ({
      label: labelFn(i),
      value,
    }));
    const reuseSeries = this.wave(points, 6, 18, seed + 5).map((value, i) => ({
      label: labelFn(i),
      value: Math.min(40, value),
    }));

    const authTotal = authSeries.reduce((a, b) => a + b.value, 0);
    const tokenTotal = tokenSeries.reduce((a, b) => a + b.value, 0);
    const failTotal = Math.round(authTotal * 0.02);
    const reusePct = Math.min(
      99,
      (reuseSeries.reduce((a, b) => a + b.value, 0) / Math.max(1, points)) * 4.2,
    );

    return {
      authSeries,
      tokenSeries,
      reuseSeries,
      authTotal,
      tokenTotal,
      failTotal,
      reusePct,
      authDelta: 12 + (seed % 7) * 3.2,
      reuseDelta: -8 - (seed % 5) * 1.3,
      latencyP90: 18 + (seed % 9),
    };
  }

  private wave(points: number, base: number, amp: number, seed: number): number[] {
    const out: number[] = [];
    for (let i = 0; i < points; i++) {
      const v =
        base +
        Math.sin((i + seed) / 2.2) * amp * 0.45 +
        Math.cos((i + seed * 1.7) / 3.1) * amp * 0.35 +
        ((i * 17 + seed * 13) % 23);
      out.push(Math.max(0, Math.round(v)));
    }
    return out;
  }
}

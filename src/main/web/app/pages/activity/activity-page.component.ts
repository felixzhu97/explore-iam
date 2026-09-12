import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import { CARD } from '../../shared/console-ui';

type ManagementEvent = {
  id: string;
  actor: string;
  action: string;
  targetType: string;
  targetId: string;
  outcome: string;
  occurredAt: string;
};

type AuthorizationDecision = {
  id: string;
  principalId: string;
  action: string;
  resource: string;
  effect: string;
  reasonCode: string;
  occurredAt: string;
};

type AuditEventsResponse = {
  managementEvents: ManagementEvent[];
  authorizationDecisions: AuthorizationDecision[];
};

@Component({
  selector: 'app-activity-page',
  imports: [ConsoleShellComponent],
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-[1.75rem] font-semibold tracking-tight">Activity</h1>
          <p class="mt-1 text-sm text-[var(--console-muted)]">管理事件与授权决策审计</p>
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

      @if (loading()) {
        <p class="mt-8 text-sm text-[var(--console-muted)]">加载中…</p>
      } @else {
        <section class="${CARD} mt-6 overflow-hidden">
          <header class="border-b border-[var(--console-border)] px-5 py-3">
            <h2 class="m-0 text-base font-semibold">Management Events</h2>
          </header>
          @if (managementEvents().length === 0) {
            <p class="px-5 py-8 text-sm text-[var(--console-muted)]">暂无管理事件。</p>
          } @else {
            <div class="overflow-x-auto">
              <table class="w-full border-collapse text-sm">
                <thead>
                  <tr>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Time
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Actor
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Action
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Target
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Outcome
                    </th>
                  </tr>
                </thead>
                <tbody>
                  @for (event of managementEvents(); track event.id) {
                    <tr>
                      <td class="border-b border-[var(--console-border)] px-4 py-3 text-xs text-[var(--console-muted)]">
                        {{ event.occurredAt }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3">
                        {{ event.actor }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3">
                        {{ event.action }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3 text-[var(--console-muted)]">
                        {{ event.targetType }}/{{ event.targetId }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3">
                        {{ event.outcome }}
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </section>

        <section class="${CARD} mt-4 overflow-hidden">
          <header class="border-b border-[var(--console-border)] px-5 py-3">
            <h2 class="m-0 text-base font-semibold">Authorization Decisions</h2>
          </header>
          @if (authorizationDecisions().length === 0) {
            <p class="px-5 py-8 text-sm text-[var(--console-muted)]">暂无授权决策。</p>
          } @else {
            <div class="overflow-x-auto">
              <table class="w-full border-collapse text-sm">
                <thead>
                  <tr>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Time
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Principal
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Action
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Resource
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Effect
                    </th>
                    <th class="border-b border-[var(--console-border)] bg-[#fafafa] px-4 py-2.5 text-left text-xs font-semibold text-[var(--console-muted)]">
                      Reason
                    </th>
                  </tr>
                </thead>
                <tbody>
                  @for (decision of authorizationDecisions(); track decision.id) {
                    <tr>
                      <td class="border-b border-[var(--console-border)] px-4 py-3 text-xs text-[var(--console-muted)]">
                        {{ decision.occurredAt }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3">
                        {{ decision.principalId }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3">
                        {{ decision.action }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3 text-[var(--console-muted)]">
                        {{ decision.resource }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3">
                        {{ decision.effect }}
                      </td>
                      <td class="border-b border-[var(--console-border)] px-4 py-3 text-[var(--console-muted)]">
                        {{ decision.reasonCode }}
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </section>
      }
    </app-console-shell>
  `,
})
export class ActivityPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly managementEvents = signal<ManagementEvent[]>([]);
  readonly authorizationDecisions = signal<AuthorizationDecision[]>([]);
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    this.http
      .get<AuditEventsResponse>('/api/v1/auditEvents', {
        withCredentials: true,
        params: { page_size: '50' },
      })
      .subscribe({
        next: (res) => {
          this.managementEvents.set(res.managementEvents ?? []);
          this.authorizationDecisions.set(res.authorizationDecisions ?? []);
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
          queryParams: { continue: '/activity' },
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

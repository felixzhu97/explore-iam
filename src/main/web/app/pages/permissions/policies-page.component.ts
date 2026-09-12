import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { ConsoleShellComponent } from '../../layout/console-shell.component';
import { BTN_PRIMARY, BTN_SECONDARY, CARD, FIELD } from '../../shared/console-ui';
import { csrfHeaders } from '../../shared/csrf';

type PolicyView = {
  id: string;
  name: string;
};

@Component({
  selector: 'app-policies-page',
  imports: [ConsoleShellComponent, FormsModule],
  template: `
    <app-console-shell>
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 class="text-[1.75rem] font-semibold tracking-tight">Policies</h1>
          <p class="mt-1 text-sm text-[var(--console-muted)]">策略文档与主体绑定</p>
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
        <h2 class="text-base font-semibold">创建策略</h2>
        <form class="mt-4 grid gap-3 sm:grid-cols-2" (ngSubmit)="createPolicy()">
          <input
            class="${FIELD} sm:col-span-2"
            name="policyName"
            placeholder="策略名称"
            required
            [(ngModel)]="policyName"
          />
          <select class="${FIELD}" name="effect" [(ngModel)]="effect">
            <option value="ALLOW">ALLOW</option>
            <option value="DENY">DENY</option>
          </select>
          <input
            class="${FIELD}"
            name="actions"
            placeholder="actions（逗号分隔）"
            required
            [(ngModel)]="actionsText"
          />
          <input
            class="${FIELD} sm:col-span-2"
            name="resources"
            placeholder="resources（逗号分隔）"
            required
            [(ngModel)]="resourcesText"
          />
          <button type="submit" class="${BTN_PRIMARY} w-fit" [disabled]="creating()">
            {{ creating() ? '创建中…' : '创建' }}
          </button>
        </form>
      </section>

      <section class="${CARD} mt-4 overflow-hidden">
        @if (loading()) {
          <p class="px-5 py-8 text-sm text-[var(--console-muted)]">加载中…</p>
        } @else if (policies().length === 0) {
          <p class="px-5 py-10 text-center text-sm text-[var(--console-muted)]">暂无策略。</p>
        } @else {
          <div class="divide-y divide-[var(--console-border)]">
            @for (policy of policies(); track policy.id) {
              <div class="px-5 py-4">
                <div class="font-medium">{{ policy.name }}</div>
                <code class="text-[11px] text-[var(--console-muted)]">{{ policy.id }}</code>
                <form class="mt-3 flex flex-wrap gap-2" (ngSubmit)="attach(policy.id)">
                  <input
                    class="${FIELD} max-w-md"
                    [name]="'arn-' + policy.id"
                    placeholder="principalArn"
                    [(ngModel)]="attachDrafts[policy.id]"
                  />
                  <button type="submit" class="${BTN_SECONDARY}">Attach</button>
                </form>
              </div>
            }
          </div>
        }
      </section>
    </app-console-shell>
  `,
})
export class PoliciesPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly policies = signal<PolicyView[]>([]);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly errorMessage = signal<string | null>(null);

  policyName = '';
  effect = 'ALLOW';
  actionsText = '';
  resourcesText = '';
  attachDrafts: Record<string, string> = {};

  ngOnInit(): void {
    this.reload();
  }

  createPolicy(): void {
    const actions = splitCsv(this.actionsText);
    const resources = splitCsv(this.resourcesText);
    if (!this.policyName.trim() || actions.length === 0 || resources.length === 0) {
      return;
    }
    this.creating.set(true);
    this.errorMessage.set(null);
    this.http
      .post<PolicyView>(
        '/api/v1/policies',
        {
          name: this.policyName.trim(),
          statements: [{ effect: this.effect, actions, resources }],
        },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => {
          this.creating.set(false);
          this.policyName = '';
          this.actionsText = '';
          this.resourcesText = '';
          this.reload();
        },
        error: (err: unknown) => {
          this.creating.set(false);
          this.handleError(err);
        },
      });
  }

  attach(policyId: string): void {
    const principalArn = (this.attachDrafts[policyId] ?? '').trim();
    if (!principalArn) {
      return;
    }
    this.http
      .post(
        `/api/v1/policies/${encodeURIComponent(policyId)}:attach`,
        { principalArn },
        { withCredentials: true, headers: csrfHeaders() },
      )
      .subscribe({
        next: () => {
          this.attachDrafts[policyId] = '';
        },
        error: (err: unknown) => this.handleError(err),
      });
  }

  private reload(): void {
    this.loading.set(true);
    this.http.get<PolicyView[]>('/api/v1/policies', { withCredentials: true }).subscribe({
      next: (list) => {
        this.policies.set(list ?? []);
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
          queryParams: { continue: '/permissions/policies' },
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

function splitCsv(text: string): string[] {
  return text
    .split(',')
    .map((s) => s.trim())
    .filter((s) => s.length > 0);
}

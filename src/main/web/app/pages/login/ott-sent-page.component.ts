import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { AuthShellComponent } from '../../shared/auth/auth-shell.component';
import { safeContinuePath } from '../../shared/auth/auth-form.util';
import { ZardButtonComponent } from '../../shared/components/button/button.component';

@Component({
  selector: 'app-ott-sent-page',
  imports: [RouterLink, ZardButtonComponent, AuthShellComponent],
  host: { class: 'block min-h-dvh' },
  template: `
    <app-auth-shell>
      <h1 class="text-2xl font-semibold tracking-tight text-foreground sm:text-3xl">验证码已发送</h1>
      <p class="mt-3 text-sm text-muted-foreground">
        请查收邮件中的验证码或链接，然后返回输入以完成登录。
      </p>

      @if (showDevHint()) {
        <details class="mt-6 rounded-lg border border-border bg-muted/40 px-4 py-3 text-left text-sm text-muted-foreground">
          <summary class="cursor-pointer font-medium text-foreground">开发环境提示</summary>
          <p class="mt-2 leading-relaxed">
            本地演示将验证链接打印在应用日志中（含 token 参数）。复制 token 后点下方按钮粘贴即可。
          </p>
        </details>
      }

      <a
        z-button
        zType="primary"
        zSize="lg"
        class="mt-8 inline-flex"
        [routerLink]="['/login/ott']"
        [queryParams]="ottQueryParams()"
      >
        输入验证码
      </a>

      <p class="mt-6 text-sm text-muted-foreground">
        <a
          [routerLink]="['/login/verify']"
          [queryParams]="continueQueryParams()"
          class="text-primary underline-offset-4 hover:underline"
        >
          换一种验证方式
        </a>
      </p>
    </app-auth-shell>
  `,
})
export class OttSentPageComponent implements OnInit {
  readonly #route = inject(ActivatedRoute);

  readonly continuePath = signal<string | null>(null);
  readonly showDevHint = signal(false);

  ngOnInit(): void {
    this.continuePath.set(safeContinuePath(this.#route.snapshot.queryParamMap.get('continue')));
    const host = window.location.hostname;
    this.showDevHint.set(host === 'localhost' || host === '127.0.0.1');
  }

  continueQueryParams(): Record<string, string> {
    const cont = this.continuePath();
    return cont ? { continue: cont } : {};
  }

  ottQueryParams(): Record<string, string> {
    return this.continueQueryParams();
  }
}

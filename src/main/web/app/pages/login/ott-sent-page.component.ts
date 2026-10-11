import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { ZardButtonComponent } from '../../shared/components/button/button.component';

@Component({
  selector: 'app-ott-sent-page',
  imports: [RouterLink, ZardButtonComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styleUrl: './login-page.component.css',
  host: { class: 'block min-h-dvh' },
  template: `
    <div class="flex min-h-dvh w-full items-center justify-center bg-surface px-6">
      <div class="mx-auto w-full max-w-md text-center">
        <h1 class="text-2xl font-semibold tracking-tight text-foreground">令牌已生成</h1>
        <p class="mt-3 text-sm text-muted-foreground">
          开发环境请在应用日志中查找 magic link（含 token 查询参数），打开后完成第二因素验证。
        </p>
        <a z-button zType="primary" zSize="lg" routerLink="/login/ott" class="mt-8 inline-flex">
          前往输入令牌
        </a>
      </div>
    </div>
  `,
})
export class OttSentPageComponent {}

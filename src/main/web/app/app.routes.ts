import { Routes } from '@angular/router';

export const routes: Routes = [
  // More specific login/* paths before `login` (prefix match).
  {
    path: 'login/verify',
    loadComponent: () =>
      import('./pages/login/login-verify-page.component').then((m) => m.LoginVerifyPageComponent),
  },
  {
    path: 'login/ott',
    loadComponent: () =>
      import('./pages/login/ott-login-page.component').then((m) => m.OttLoginPageComponent),
  },
  {
    path: 'login/totp',
    loadComponent: () =>
      import('./pages/login/totp-challenge-page.component').then((m) => m.TotpChallengePageComponent),
  },
  {
    path: 'login',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/login/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'ott/sent',
    loadComponent: () =>
      import('./pages/login/ott-sent-page.component').then((m) => m.OttSentPageComponent),
  },
  {
    path: 'security/totp',
    loadComponent: () =>
      import('./pages/security/totp-enroll-page.component').then((m) => m.TotpEnrollPageComponent),
  },
  {
    path: 'oauth-clients/new',
    loadComponent: () =>
      import('./pages/oauth-clients/oauth-clients-create-page.component').then(
        (m) => m.OAuthClientsCreatePageComponent,
      ),
  },
  {
    path: 'oauth-clients',
    loadComponent: () =>
      import('./pages/oauth-clients/oauth-clients-list-page.component').then((m) => m.OAuthClientsListPageComponent),
  },
  {
    path: 'users',
    loadComponent: () =>
      import('./pages/users/users-list-page.component').then((m) => m.UsersListPageComponent),
  },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/home/home-page.component').then((m) => m.HomePageComponent),
  },
  { path: '**', redirectTo: '' },
];

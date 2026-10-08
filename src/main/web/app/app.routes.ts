import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login-page.component').then((m) => m.LoginPageComponent),
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
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/home/home-page.component').then((m) => m.HomePageComponent),
  },
  { path: '**', redirectTo: '' },
];

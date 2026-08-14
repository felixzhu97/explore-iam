import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'clients/new',
    loadComponent: () =>
      import('./pages/clients/clients-create-page.component').then(
        (m) => m.ClientsCreatePageComponent,
      ),
  },
  {
    path: 'clients',
    loadComponent: () =>
      import('./pages/clients/clients-list-page.component').then((m) => m.ClientsListPageComponent),
  },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/home/home-page.component').then((m) => m.HomePageComponent),
  },
  { path: '**', redirectTo: '' },
];

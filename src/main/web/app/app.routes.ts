import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'apps/new',
    loadComponent: () =>
      import('./pages/apps/apps-create-page.component').then((m) => m.AppsCreatePageComponent),
  },
  {
    path: 'apps/:clientId',
    loadComponent: () =>
      import('./pages/apps/apps-detail-page.component').then((m) => m.AppsDetailPageComponent),
  },
  {
    path: 'apps',
    loadComponent: () =>
      import('./pages/apps/apps-list-page.component').then((m) => m.AppsListPageComponent),
  },
  {
    path: 'users-and-access/people',
    loadComponent: () =>
      import('./pages/users-and-access/people-page.component').then((m) => m.PeoplePageComponent),
  },
  {
    path: 'users-and-access/groups',
    loadComponent: () =>
      import('./pages/users-and-access/groups-page.component').then((m) => m.GroupsPageComponent),
  },
  {
    path: 'users-and-access/roles',
    loadComponent: () =>
      import('./pages/users-and-access/roles-page.component').then((m) => m.RolesPageComponent),
  },
  { path: 'users-and-access', redirectTo: 'users-and-access/people', pathMatch: 'full' },
  {
    path: 'permissions/points',
    loadComponent: () =>
      import('./pages/permissions/points-page.component').then((m) => m.PointsPageComponent),
  },
  {
    path: 'permissions/policies',
    loadComponent: () =>
      import('./pages/permissions/policies-page.component').then((m) => m.PoliciesPageComponent),
  },
  { path: 'permissions', redirectTo: 'permissions/points', pathMatch: 'full' },
  {
    path: 'activity',
    loadComponent: () =>
      import('./pages/activity/activity-page.component').then((m) => m.ActivityPageComponent),
  },
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/home/home-page.component').then((m) => m.HomePageComponent),
  },
  { path: '**', redirectTo: '' },
];

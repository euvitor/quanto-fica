import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    children: []
  },
  {
    path: 'simulacao/:id',
    children:[]
  },
  {
    path: '**',
    redirectTo: ''
  }
];

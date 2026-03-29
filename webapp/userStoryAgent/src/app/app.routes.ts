import { Routes } from '@angular/router';
import { SpecProComponent } from './features/spec-pro/spec-pro.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'spec-pro' },
  { path: 'spec-pro', component: SpecProComponent }
];

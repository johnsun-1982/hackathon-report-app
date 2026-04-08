import { Routes } from '@angular/router';
import { ReportViewerComponent } from './components/report/report-viewer.component';
import { LoginComponent } from './components/auth/login.component';
import { ReportRunFlowComponent } from './components/report/report-run-flow.component';
import { TwoLevelApprovalComponent } from './components/report/two-level-approval.component';
import { authGuard, roleGuard } from './services/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', pathMatch: 'full', redirectTo: 'reports' },
  { path: 'reports', component: ReportViewerComponent, canActivate: [authGuard] },
  { path: 'maker', component: ReportViewerComponent, canActivate: [authGuard, roleGuard], data: { roles: ['MAKER'] } },
  { path: 'checker', component: ReportViewerComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CHECKER'] } },
  { path: 'l1-checker', component: TwoLevelApprovalComponent, canActivate: [authGuard, roleGuard], data: { roles: ['L1_CHECKER'] } },
  { path: 'l2-checker', component: TwoLevelApprovalComponent, canActivate: [authGuard, roleGuard], data: { roles: ['L2_CHECKER'] } },
  { path: 'approval', component: TwoLevelApprovalComponent, canActivate: [authGuard, roleGuard], data: { roles: ['L1_CHECKER', 'L2_CHECKER'] } },
  { path: 'runs/:id/flow', component: ReportRunFlowComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'reports' }
];

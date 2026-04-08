import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ReportService, ReportRun, ReportAuditEvent } from '../../services/report.service';
import { AuthService } from '../../services/auth.service';

// 2层审批相关接口
export interface ApprovalStatusInfo {
  runId: number;
  status: string;
  currentApprovalStage: number;
  firstApproval?: ApprovalInfo;
  secondApproval?: ApprovalInfo;
}

export interface ApprovalInfo {
  approver: string;
  time: string;
  comment: string;
}

export interface ApprovalRequest {
  approve: boolean;
  comment: string;
}

@Component({
  selector: 'app-two-level-approval',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './two-level-approval.component.html',
  styleUrls: ['./two-level-approval.component.css']
})
export class TwoLevelApprovalComponent implements OnInit {
  // 审批相关状态
  pendingLevel1Runs: ReportRun[] = [];
  pendingLevel2Runs: ReportRun[] = [];
  selectedRun: ReportRun | null = null;
  approvalStatus: ApprovalStatusInfo | null = null;

  // 审批操作相关
  approvalRequest: ApprovalRequest = {
    approve: true,
    comment: ''
  };
  approvalMessage: string | null = null;
  approvalError: string | null = null;
  submittingApproval = false;

  // 审计轨迹
  auditTrail: ReportAuditEvent[] = [];
  auditError: string | null = null;

  // 我的审批历史
  myApprovalHistory: ReportRun[] = [];
  historyError: string | null = null;

  // 视图控制
  activeTab: 'pending' | 'history' = 'pending';
  activeLevel: 'level1' | 'level2' | 'all' = 'all';

  // 加载状态
  loading = false;
  loadingError: string | null = null;

  // 用户权限
  currentUser: any = null;
  canApproveLevel1 = false;
  canApproveLevel2 = false;

  constructor(
    private reportService: ReportService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }

    this.currentUser = this.authService.getCurrentUser();
    this.checkPermissions();
    this.loadPendingApprovals();
    this.loadMyApprovalHistory();
  }

  private checkPermissions() {
    if (!this.currentUser || !this.currentUser.role) {
      return;
    }

    this.canApproveLevel1 = this.currentUser.role.includes('L1_CHECKER');
    this.canApproveLevel2 = this.currentUser.role.includes('L2_CHECKER');
  }

  private loadPendingApprovals() {
    this.loading = true;
    this.loadingError = null;

    // 根据用户权限加载对应的待审批列表
    if (this.canApproveLevel1 && this.canApproveLevel2) {
      // 用户同时有L1和L2权限，加载所有
      this.loadLevel1Pending();
      this.loadLevel2Pending();
    } else if (this.canApproveLevel1) {
      // 只有L1权限
      this.loadLevel1Pending();
    } else if (this.canApproveLevel2) {
      // 只有L2权限
      this.loadLevel2Pending();
    } else {
      this.loadingError = '当前用户没有审批权限';
      this.loading = false;
    }
  }

  private loadLevel1Pending() {
    this.reportService.getPendingApprovals(1).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.pendingLevel1Runs = response.data.items || [];
        } else {
          this.loadingError = response.message || '加载一级待审批列表失败';
        }
        this.checkLoadingComplete();
      },
      error: (err) => {
        this.loadingError = '加载一级待审批列表失败: ' + (err.error?.message || err.message || '');
        this.checkLoadingComplete();
      }
    });
  }

  private loadLevel2Pending() {
    this.reportService.getPendingApprovals(2).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.pendingLevel2Runs = response.data.items || [];
        } else {
          this.loadingError = response.message || '加载二级待审批列表失败';
        }
        this.checkLoadingComplete();
      },
      error: (err) => {
        this.loadingError = '加载二级待审批列表失败: ' + (err.error?.message || err.message || '');
        this.checkLoadingComplete();
      }
    });
  }

  private checkLoadingComplete() {
    // 简单的加载完成检查
    setTimeout(() => {
      if (!this.loadingError) {
        this.loading = false;
      }
    }, 100);
  }

  private loadMyApprovalHistory() {
    this.reportService.getMyApprovalHistory().subscribe({
      next: (response: any) => {
        if (response.success) {
          this.myApprovalHistory = response.data.items || [];
        } else {
          this.historyError = response.message || '加载审批历史失败';
        }
      },
      error: (err) => {
        this.historyError = '加载审批历史失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  selectRun(run: ReportRun) {
    this.selectedRun = run;
    this.approvalRequest.comment = '';
    this.approvalMessage = null;
    this.approvalError = null;
    this.loadApprovalStatus(run.id);
    this.loadAuditTrail(run.id);
  }

  private loadApprovalStatus(runId: number) {
    this.reportService.getApprovalStatus(runId).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.approvalStatus = response.data;
        } else {
          this.approvalError = response.message || '获取审批状态失败';
        }
      },
      error: (err) => {
        this.approvalError = '获取审批状态失败: ' + (err.error?.message || err.message || '');
      }
    });
  }

  private loadAuditTrail(runId: number) {
    this.auditError = null;
    this.reportService.getAuditTrail(runId).subscribe({
      next: (events) => {
        this.auditTrail = events;
      },
      error: (err) => {
        this.auditError = '加载审计轨迹失败: ' + (err.error?.message || err.message || '');
        this.auditTrail = [];
      }
    });
  }

  submitApproval() {
    if (!this.selectedRun) {
      return;
    }

    // 验证拒绝时必须填写意见
    if (!this.approvalRequest.approve && !this.approvalRequest.comment.trim()) {
      this.approvalError = '拒绝审批时必须填写意见';
      return;
    }

    this.submittingApproval = true;
    this.approvalMessage = null;
    this.approvalError = null;

    // 根据当前状态确定是一级还是二级审批
    const isLevel1Approval = this.selectedRun.status === 'Submitted';
    const isLevel2Approval = this.selectedRun.status === 'L2Submitted';

    if (isLevel1Approval) {
      this.submitLevel1Approval();
    } else if (isLevel2Approval) {
      this.submitLevel2Approval();
    } else {
      this.approvalError = '当前报表状态不允许审批';
      this.submittingApproval = false;
    }
  }

  private submitLevel1Approval() {
    if (!this.canApproveLevel1) {
      this.approvalError = '当前用户没有一级审批权限';
      this.submittingApproval = false;
      return;
    }

    this.reportService.firstLevelApproval(this.selectedRun!.id, this.approvalRequest).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.approvalMessage = response.message || '一级审批完成';
          this.loadPendingApprovals(); // 重新加载待审批列表
          this.selectedRun = null; // 清除选择
          this.approvalStatus = null;
        } else {
          this.approvalError = response.message || '一级审批失败';
        }
        this.submittingApproval = false;
      },
      error: (err) => {
        this.approvalError = '一级审批失败: ' + (err.error?.message || err.message || '');
        this.submittingApproval = false;
      }
    });
  }

  private submitLevel2Approval() {
    if (!this.canApproveLevel2) {
      this.approvalError = '当前用户没有二级审批权限';
      this.submittingApproval = false;
      return;
    }

    this.reportService.secondLevelApproval(this.selectedRun!.id, this.approvalRequest).subscribe({
      next: (response: any) => {
        if (response.success) {
          this.approvalMessage = response.message || '二级审批完成';
          this.loadPendingApprovals(); // 重新加载待审批列表
          this.selectedRun = null; // 清除选择
          this.approvalStatus = null;
        } else {
          this.approvalError = response.message || '二级审批失败';
        }
        this.submittingApproval = false;
      },
      error: (err) => {
        this.approvalError = '二级审批失败: ' + (err.error?.message || err.message || '');
        this.submittingApproval = false;
      }
    });
  }

  reExecuteReport(runId: number) {
    if (!confirm('确定要重新执行这个报表吗？这将重置所有审批状态。')) {
      return;
    }

    this.reportService.reExecuteReport(runId).subscribe({
      next: (response: any) => {
        if (response.success) {
          alert('报表重新执行成功');
          this.loadMyApprovalHistory(); // 重新加载历史
        } else {
          alert('重新执行失败: ' + response.message);
        }
      },
      error: (err) => {
        alert('重新执行失败: ' + (err.error?.message || err.message || ''));
      }
    });
  }

  exportReport(run: ReportRun) {
    this.reportService.downloadRun(run.id).subscribe({
      next: (blob) => {
        const filename = `${run.reportName}-${run.id}.xlsx`;
        this.triggerDownload(blob, filename);
      },
      error: (err) => {
        alert('导出失败: ' + (err.error?.message || err.message || ''));
      }
    });
  }

  private triggerDownload(blob: Blob, filename: string) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  // 状态显示相关方法
  getStatusDisplay(status: string): string {
    const statusMap: { [key: string]: string } = {
      'Generated': '已生成',
      'Submitted': '已提交',
      'L1Approved': '一级已批准',
      'L1Rejected': '一级已拒绝',
      'L2Submitted': '二级待审批',
      'L2Approved': '二级已批准',
      'L2Rejected': '二级已拒绝'
    };
    return statusMap[status] || status;
  }

  getStatusBadgeClass(status: string): string {
    const classMap: { [key: string]: string } = {
      'Generated': 'badge-secondary',
      'Submitted': 'badge-warning',
      'L1Approved': 'badge-info',
      'L1Rejected': 'badge-danger',
      'L2Submitted': 'badge-warning',
      'L2Approved': 'badge-success',
      'L2Rejected': 'badge-danger'
    };
    return classMap[status] || 'badge-secondary';
  }

  getApprovalStageDisplay(stage: number): string {
    const stageMap: { [key: number]: string } = {
      0: '未提交',
      1: '一级审批中',
      2: '二级审批中',
      3: '审批完成'
    };
    return stageMap[stage] || '未知';
  }

  // 过滤方法
  getFilteredPendingRuns(): ReportRun[] {
    switch (this.activeLevel) {
      case 'level1':
        return this.pendingLevel1Runs;
      case 'level2':
        return this.pendingLevel2Runs;
      default:
        return [...this.pendingLevel1Runs, ...this.pendingLevel2Runs];
    }
  }

  // 检查是否可以审批
  canApprove(run: ReportRun): boolean {
    if (!this.currentUser || !this.currentUser.role) {
      return false;
    }

    const isLevel1Approval = run.status === 'Submitted';
    const isLevel2Approval = run.status === 'L2Submitted';

    if (isLevel1Approval) {
      return this.canApproveLevel1;
    } else if (isLevel2Approval) {
      return this.canApproveLevel2;
    }

    return false;
  }

  // 获取审批级别显示
  getApprovalLevelDisplay(run: ReportRun): string {
    if (run.status === 'Submitted') {
      return '一级审批';
    } else if (run.status === 'L2Submitted') {
      return '二级审批';
    }
    return '';
  }

  // 格式化时间
  formatDateTime(dateStr: string | undefined): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleString('zh-CN');
  }

  // 清除消息
  clearMessages() {
    this.approvalMessage = null;
    this.approvalError = null;
  }

  // 刷新
  refresh() {
    this.loadPendingApprovals();
    this.loadMyApprovalHistory();
    this.clearMessages();
  }
}

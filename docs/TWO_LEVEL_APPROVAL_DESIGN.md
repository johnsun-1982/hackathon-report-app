# 2层审批状态机设计

## 概述

本文档详细设计了报表系统的2层审批状态机，扩展现有的单层审批流程，支持更严格的内部控制和风险管理。

## 当前状态机回顾

### 现有状态流转
```
[创建报表] → [执行报表] → [Generated] → [提交审批] → [Submitted] → [Approved/Rejected] → [导出结果]
```

### 现有状态定义
- **Generated**: 报表执行完成，等待提交审批
- **Submitted**: 已提交审批，等待审批决策
- **Approved**: 审批通过，可以导出
- **Rejected**: 审批拒绝，需要重新执行

## 2层审批状态机设计

### 新状态流转图
```
[创建报表] → [执行报表] → [Generated] → [提交审批] → [Submitted] 
                                                              ↓
                                                       [L1Approved] → [L2Submitted] → [L2Approved] → [导出结果]
                                                              ↑
                                                       [L1Rejected] ← [L2Rejected]
```

### 新状态定义

#### 核心状态
- **Generated**: 报表执行完成，等待提交审批（保持不变）
- **Submitted**: 已提交审批，等待一级审批（新增）
- **L1Approved**: 一级审批通过，等待二级审批（新增）
- **L1Rejected**: 一级审批拒绝（新增）
- **L2Submitted**: 已提交二级审批（新增）
- **L2Approved**: 二级审批通过，可以导出（新增）
- **L2Rejected**: 二级审批拒绝（新增）

#### 状态转换规则

##### 1. 提交审批
```
Generated → Submitted
触发条件: MAKER用户提交审批
业务规则: 只能提交自己生成的报表
```

##### 2. 一级审批
```
Submitted → L1Approved
触发条件: 一级审批者批准
业务规则: 需要一级审批权限

Submitted → L1Rejected
触发条件: 一级审批者拒绝
业务规则: 拒绝时必须填写备注
```

##### 3. 提交二级审批
```
L1Approved → L2Submitted
触发条件: 系统自动提交或一级审批者提交
业务规则: 只有L1Approved状态可以提交二级审批
```

##### 4. 二级审批
```
L2Submitted → L2Approved
触发条件: 二级审批者批准
业务规则: 需要二级审批权限

L2Submitted → L2Rejected
触发条件: 二级审批者拒绝
业务规则: 拒绝时必须填写备注
```

##### 5. 重新执行
```
L1Rejected → Generated
触发条件: MAKER用户重新执行报表
业务规则: 拒绝后可以重新执行

L2Rejected → Generated
触发条件: MAKER用户重新执行报表
业务规则: 拒绝后可以重新执行
```

## 数据库设计

### ReportRun表扩展

#### 新增字段
```sql
-- 一级审批字段
ALTER TABLE report_run ADD COLUMN first_approver_username VARCHAR(50);
ALTER TABLE report_run ADD COLUMN first_approval_time TIMESTAMP;
ALTER TABLE report_run ADD COLUMN first_approval_comment TEXT;

-- 二级审批字段
ALTER TABLE report_run ADD COLUMN second_approver_username VARCHAR(50);
ALTER TABLE report_run ADD COLUMN second_approval_time TIMESTAMP;
ALTER TABLE report_run ADD COLUMN second_approval_comment TEXT;

-- 审批级别配置
ALTER TABLE report_run ADD COLUMN approval_level INT DEFAULT 2;
ALTER TABLE report_run ADD COLUMN current_approval_stage INT DEFAULT 0;
-- 0=未提交, 1=一级审批中, 2=二级审批中, 3=审批完成
```

#### 审批配置表
```sql
CREATE TABLE approval_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_type VARCHAR(50) NOT NULL,
    approval_level INT NOT NULL DEFAULT 2,
    first_approver_role VARCHAR(50) NOT NULL,
    second_approver_role VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_report_type (report_type),
    INDEX idx_is_active (is_active)
);
```

#### 审批权限表
```sql
CREATE TABLE approval_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    approval_level INT NOT NULL, -- 1=一级审批, 2=二级审批
    report_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_user_level_type (username, approval_level, report_type),
    INDEX idx_username (username),
    INDEX idx_approval_level (approval_level)
);
```

## 业务逻辑设计

### 审批角色定义

#### 一级审批者 (Level 1 Approver)
- **角色**: L1_CHECKER
- **权限**: 可以进行一级审批
- **范围**: 指定报表类型或全部报表

#### 二级审批者 (Level 2 Approver)
- **角色**: L2_CHECKER
- **权限**: 可以进行二级审批
- **范围**: 指定报表类型或全部报表

### 审批流程

#### 1. 提交审批
```java
@Transactional
public ReportRun submitForApproval(Long runId) {
    User currentUser = currentUserService.getCurrentUserOrThrow();
    currentUserService.requireRole(currentUser, "MAKER");

    ReportRun run = reportRunRepository.findById(runId)
            .orElseThrow(() -> new RuntimeException("报表运行实例不存在"));

    // 验证状态
    if (!"Generated".equals(run.getStatus())) {
        throw new RuntimeException("只能提交 Generated 状态的报表运行实例");
    }

    // 验证权限
    if (!currentUser.getUsername().equals(run.getMakerUsername())) {
        throw new RuntimeException("只能提交由当前 MAKER 自己生成的报表运行实例");
    }

    // 更新状态
    run.setStatus("Submitted");
    run.setCurrentApprovalStage(1); // 进入一级审批
    run.setSubmittedAt(LocalDateTime.now());

    ReportRun saved = reportRunRepository.save(run);

    // 记录审计事件
    auditService.recordEvent(
            saved.getId(),
            saved.getReportId(),
            currentUser.getUsername(),
            currentUser.getRole(),
            "Submitted",
            null
    );

    return saved;
}
```

#### 2. 一级审批
```java
@Transactional
public ReportRun firstLevelApproval(Long runId, boolean approve, String comment) {
    User currentUser = currentUserService.getCurrentUserOrThrow();
    currentUserService.requireRole(currentUser, "L1_CHECKER");

    ReportRun run = reportRunRepository.findById(runId)
            .orElseThrow(() -> new RuntimeException("报表运行实例不存在"));

    // 验证状态
    if (!"Submitted".equals(run.getStatus())) {
        throw new RuntimeException("只能对 Submitted 状态的报表运行实例进行一级审批");
    }

    // 验证审批权限
    if (!approvalPermissionService.hasPermission(currentUser.getUsername(), 1, run.getReportType())) {
        throw new RuntimeException("当前用户无权进行一级审批");
    }

    // 验证备注
    if (!approve && (comment == null || comment.trim().isEmpty())) {
        throw new RuntimeException("拒绝审批时必须填写备注");
    }

    // 更新状态
    if (approve) {
        run.setStatus("L1Approved");
        run.setCurrentApprovalStage(2); // 进入二级审批
    } else {
        run.setStatus("L1Rejected");
        run.setCurrentApprovalStage(0); // 回到初始状态
    }

    run.setFirstApproverUsername(currentUser.getUsername());
    run.setFirstApprovalTime(LocalDateTime.now());
    run.setFirstApprovalComment(comment);

    ReportRun saved = reportRunRepository.save(run);

    // 记录审计事件
    auditService.recordEvent(
            saved.getId(),
            saved.getReportId(),
            currentUser.getUsername(),
            currentUser.getRole(),
            approve ? "L1Approved" : "L1Rejected",
            comment
    );

    // 如果一级审批通过，自动提交二级审批
    if (approve) {
        submitForSecondLevelApproval(saved.getId());
    }

    return saved;
}
```

#### 3. 二级审批
```java
@Transactional
public ReportRun secondLevelApproval(Long runId, boolean approve, String comment) {
    User currentUser = currentUserService.getCurrentUserOrThrow();
    currentUserService.requireRole(currentUser, "L2_CHECKER");

    ReportRun run = reportRunRepository.findById(runId)
            .orElseThrow(() -> new RuntimeException("报表运行实例不存在"));

    // 验证状态
    if (!"L2Submitted".equals(run.getStatus()) && !"L1Approved".equals(run.getStatus())) {
        throw new RuntimeException("只能对 L2Submitted 状态的报表运行实例进行二级审批");
    }

    // 验证审批权限
    if (!approvalPermissionService.hasPermission(currentUser.getUsername(), 2, run.getReportType())) {
        throw new RuntimeException("当前用户无权进行二级审批");
    }

    // 验证备注
    if (!approve && (comment == null || comment.trim().isEmpty())) {
        throw new RuntimeException("拒绝审批时必须填写备注");
    }

    // 更新状态
    if (approve) {
        run.setStatus("L2Approved");
        run.setCurrentApprovalStage(3); // 审批完成
    } else {
        run.setStatus("L2Rejected");
        run.setCurrentApprovalStage(0); // 回到初始状态
    }

    run.setSecondApproverUsername(currentUser.getUsername());
    run.setSecondApprovalTime(LocalDateTime.now());
    run.setSecondApprovalComment(comment);

    ReportRun saved = reportRunRepository.save(run);

    // 记录审计事件
    auditService.recordEvent(
            saved.getId(),
            saved.getReportId(),
            currentUser.getUsername(),
            currentUser.getRole(),
            approve ? "L2Approved" : "L2Rejected",
            comment
    );

    return saved;
}
```

## API设计

### 新增端点

#### 1. 一级审批
```
POST /api/reports/runs/{runId}/first-approval
Request Body:
{
    "approve": true,
    "comment": "审批意见"
}

Response:
{
    "id": 123,
    "status": "L1Approved",
    "firstApproverUsername": "l1_checker",
    "firstApprovalTime": "2026-04-08T10:30:00",
    "currentApprovalStage": 2
}
```

#### 2. 二级审批
```
POST /api/reports/runs/{runId}/second-approval
Request Body:
{
    "approve": true,
    "comment": "审批意见"
}

Response:
{
    "id": 123,
    "status": "L2Approved",
    "secondApproverUsername": "l2_checker",
    "secondApprovalTime": "2026-04-08T11:30:00",
    "currentApprovalStage": 3
}
```

#### 3. 审批状态查询
```
GET /api/reports/runs/{runId}/approval-status

Response:
{
    "id": 123,
    "status": "L2Approved",
    "currentApprovalStage": 3,
    "firstApproval": {
        "approver": "l1_checker",
        "time": "2026-04-08T10:30:00",
        "comment": "一级审批意见"
    },
    "secondApproval": {
        "approver": "l2_checker",
        "time": "2026-04-08T11:30:00",
        "comment": "二级审批意见"
    }
}
```

### 修改现有端点

#### 1. 导出报表
```
GET /api/reports/{id}/export
修改: 只能导出 L2Approved 状态的报表
```

#### 2. 获取待审批列表
```
GET /api/reports/pending-approval?level=1
Response: 一级审批待处理列表

GET /api/reports/pending-approval?level=2
Response: 二级审批待处理列表
```

## 前端界面设计

### 审批列表页面
- 显示不同级别的待审批报表
- 支持筛选和搜索
- 显示审批进度和状态

### 审批详情页面
- 显示报表基本信息
- 显示一级审批结果和意见
- 显示二级审批结果和意见
- 提供审批操作按钮

### 审批历史页面
- 显示完整的审批轨迹
- 支持按时间、用户、状态筛选
- 显示审批耗时统计

## 兼容性设计

### 数据迁移
```sql
-- 迁移现有单层审批数据
UPDATE report_run SET 
    first_approver_username = checker_username,
    first_approval_time = decided_at,
    current_approval_stage = 3,
    status = CASE 
        WHEN status = 'Approved' THEN 'L2Approved'
        WHEN status = 'Rejected' THEN 'L1Rejected'
        ELSE status
    END
WHERE status IN ('Approved', 'Rejected') 
AND checker_username IS NOT NULL;
```

### 向后兼容
- 保持现有API端点的兼容性
- 支持配置审批级别（1层或2层）
- 渐进式迁移策略

## 测试策略

### 单元测试
- 状态转换逻辑测试
- 权限验证测试
- 边界条件测试

### 集成测试
- 完整审批流程测试
- 数据库一致性测试
- API端点测试

### 用户验收测试
- 审批流程用户体验测试
- 性能测试
- 兼容性测试

## 监控和告警

### 关键指标
- 审批处理时间
- 审批通过率
- 审批拒绝率
- 系统响应时间

### 告警规则
- 审批超时告警
- 审批积压告警
- 系统异常告警

## 部署计划

### 阶段1: 数据库变更
- 执行数据库迁移脚本
- 验证数据完整性
- 性能基准测试

### 阶段2: 后端部署
- 部署新的服务代码
- 配置审批权限
- 验证API功能

### 阶段3: 前端部署
- 部署新的界面组件
- 配置路由和权限
- 验证用户体验

### 阶段4: 验证和监控
- 端到端功能验证
- 性能监控
- 用户培训和文档更新

## 风险缓解

### 技术风险
- **数据迁移风险**: 完整备份和回滚计划
- **性能影响**: 性能测试和优化
- **兼容性问题**: 充分测试和渐进式部署

### 业务风险
- **用户适应**: 培训和文档支持
- **流程变更**: 变更管理和沟通
- **权限配置**: 详细的权限配置指南

## 总结

这个2层审批状态机设计提供了：

- **完整的审批流程**: 支持严格的2层审批控制
- **灵活的配置**: 可配置的审批级别和权限
- **完整的审计**: 全面的审批轨迹记录
- **向后兼容**: 平滑的迁移和兼容性支持
- **可扩展性**: 支持未来扩展到更多审批层级

通过这个设计，系统能够满足更严格的内部控制要求，同时保持良好的用户体验和系统性能。

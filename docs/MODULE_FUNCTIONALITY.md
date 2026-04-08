# 模块功能文档

## 后端模块详解

### 1. 控制器层 (Controller Layer)

#### ReportController
**文件位置**: `src/main/java/com/legacy/report/controller/ReportController.java`

**职责**: 处理报表相关的HTTP请求

**主要接口**:
- `GET /api/test` - 系统健康检查
- `GET /api/test-db` - 数据库连接测试
- `GET /api/reports` - 获取所有报表列表
- `GET /api/reports/{id}` - 获取指定报表详情
- `POST /api/reports/run` - 直接执行SQL报表 (⚠️ 安全风险)
- `POST /api/reports/generate` - 生成报表数据
- `POST /api/reports` - 创建新报表
- `POST /api/reports/{id}/execute` - 执行报表并创建运行记录
- `GET /api/reports/{id}/export` - 导出报表为Excel

**特点**:
- 支持CORS跨域
- 统一返回JSON格式
- 缺乏统一的错误处理机制

#### AuthController
**文件位置**: `src/main/java/com/legacy/report/controller/AuthController.java`

**职责**: 用户认证和授权

**主要功能**:
- 用户登录验证
- JWT Token生成
- Token刷新机制
- 用户登出处理

#### ReportRunController
**文件位置**: `src/main/java/com/legacy/report/controller/ReportRunController.java`

**职责**: 报表运行流程管理

**主要接口**:
- `POST /api/runs/{id}/submit` - 提交报表运行
- `POST /api/runs/{id}/approve` - 审批通过
- `POST /api/runs/{id}/reject` - 审批拒绝
- `GET /api/runs/pending` - 获取待审批列表
- `GET /api/runs/history` - 获取审批历史

### 2. 服务层 (Service Layer)

#### ReportService
**文件位置**: `src/main/java/com/legacy/report/service/ReportService.java`

**职责**: 报表核心业务逻辑

**核心方法**:
```java
// 获取所有报表
public List<Report> getAllReports()

// 根据ID获取报表
public Report getReportById(Long id)

// 直接执行SQL (严重安全漏洞)
public List<Map<String, Object>> runReport(String sql)

// 创建报表
public void createReport(Report report)

// 生成报表数据
public Map<String, Object> generateReport(Long reportId, String params)
```

**问题分析**:
- 直接执行用户输入的SQL，存在SQL注入风险
- 缺乏参数验证和异常处理
- 业务逻辑过于集中，违反单一职责原则

#### ReportRunService
**文件位置**: `src/main/java/com/legacy/report/service/ReportRunService.java`

**职责**: 报表运行流程管理

**核心流程**: Generated → Submitted → Approved/Rejected

**主要方法**:
```java
// 执行报表并创建运行记录
@Transactional
public List<Map<String, Object>> executeReportWithRun(Long reportId)

// 提交报表运行
@Transactional
public ReportRun submitRun(Long runId)

// 审批决策
@Transactional
public ReportRun decideRun(Long runId, boolean approve, String comment)

// 获取Maker的最新运行记录
public ReportRun getLatestRunForCurrentMaker(Long reportId)

// 获取当前Maker的所有运行记录
public List<ReportRun> getRunsForCurrentMaker()

// 获取待审批的运行记录
public List<ReportRun> getSubmittedRunsForChecker()

// 获取当前Checker的审批历史
public List<ReportRun> getHistoryRunsForCurrentChecker()

// 获取审计事件
public List<ReportAuditEvent> getAuditEventsForRun(Long reportRunId)
```

**监控指标**:
- `report_run_generated_total` - 生成计数器
- `report_run_submitted_total` - 提交计数器
- `report_run_approved_total` - 通过计数器
- `report_run_rejected_total` - 拒绝计数器
- `report_run_approval_duration_seconds` - 审批耗时计时器

#### AuthService
**文件位置**: `src/main/java/com/legacy/report/service/AuthService.java`

**职责**: 用户认证服务

**主要功能**:
- 用户身份验证
- JWT Token生成
- Token解析和验证
- 用户权限检查

#### ReportExcelExportService
**文件位置**: `src/main/java/com/legacy/report/service/ReportExcelExportService.java`

**职责**: Excel导出服务

**主要功能**:
- 基于JXLS模板导出Excel
- 支持复杂数据格式
- 文件下载响应处理

#### AuditService
**文件位置**: `src/main/java/com/legacy/report/service/AuditService.java`

**职责**: 审计日志服务

**主要功能**:
- 记录所有报表操作
- 生成审计轨迹
- 支持操作追溯

#### CurrentUserService
**文件位置**: `src/main/java/com/legacy/report/service/CurrentUserService.java`

**职责**: 当前用户服务

**主要功能**:
- 获取当前登录用户
- 用户角色验证
- 权限检查

### 3. 数据访问层 (Data Access Layer)

#### Repository层 (JPA)
**位置**: `src/main/java/com/legacy/report/repository/`

##### ReportRunRepository
```java
public interface ReportRunRepository extends JpaRepository<ReportRun, Long> {
    List<ReportRun> findByMakerUsernameAndReportIdOrderByGeneratedAtDesc(
        String makerUsername, Long reportId);
    List<ReportRun> findByMakerUsernameOrderByGeneratedAtDesc(String makerUsername);
    List<ReportRun> findByStatusOrderBySubmittedAtAsc(String status);
    List<ReportRun> findByCheckerUsernameOrderByDecidedAtDesc(String checkerUsername);
}
```

##### ReportAuditEventRepository
```java
public interface ReportAuditEventRepository extends JpaRepository<ReportAuditEvent, Long> {
    List<ReportAuditEvent> findByReportRunIdOrderByEventTimeAsc(Long reportRunId);
}
```

##### UserRepository
```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

#### DAO层 (JDBC)
**位置**: `src/main/java/com/legacy/report/dao/`

##### ReportDao
**职责**: 报表配置数据访问

**主要方法**:
```java
// 查找所有报表
public List<Report> findAll()

// 根据ID查找报表
public Report findById(Long id)

// 保存报表
public void save(Report report)

// 直接执行SQL (安全风险)
public List<Map<String, Object>> executeSql(String sql)
```

### 4. 模型层 (Model Layer)

#### Report
**文件位置**: `src/main/java/com/legacy/report/model/Report.java`

**属性**:
- `id` - 报表ID
- `name` - 报表名称
- `sql` - SQL查询语句
- `description` - 报表描述

**特点**:
- 简单POJO对象
- 缺乏JPA注解，未映射到数据库表

#### ReportRun
**文件位置**: `src/main/java/com/legacy/report/model/ReportRun.java`

**属性**:
- `id` - 运行实例ID
- `version` - 版本号 (乐观锁)
- `reportId` - 报表配置ID
- `reportName` - 报表名称快照
- `status` - 运行状态 (Generated/Submitted/Approved/Rejected)
- `makerUsername` - Maker用户名
- `checkerUsername` - Checker用户名
- `generatedAt` - 生成时间
- `submittedAt` - 提交时间
- `decidedAt` - 决策时间
- `parametersJson` - 参数JSON
- `resultSnapshot` - 结果快照

**特点**:
- 完整的JPA实体映射
- 支持审计和时间追踪
- 包含乐观锁机制

#### User
**文件位置**: `src/main/java/com/legacy/report/model/User.java`

**属性**:
- `id` - 用户ID
- `username` - 用户名
- `password` - 密码 (加密)
- `role` - 用户角色 (MAKER/CHECKER)
- `enabled` - 是否启用

#### ReportAuditEvent
**文件位置**: `src/main/java/com/legacy/report/model/ReportAuditEvent.java`

**属性**:
- `id` - 事件ID
- `reportRunId` - 报表运行ID
- `reportId` - 报表ID
- `username` - 操作用户
- `userRole` - 用户角色
- `eventType` - 事件类型
- `eventTime` - 事件时间
- `comment` - 备注信息

### 5. 安全配置模块

#### SecurityConfig
**文件位置**: `src/main/java/com/legacy/report/config/SecurityConfig.java`

**功能**:
- Spring Security配置
- JWT认证过滤器配置
- 路径权限控制
- CORS配置

#### UserInitializer
**文件位置**: `src/main/java/com/legacy/report/config/UserInitializer.java`

**功能**:
- 系统启动时初始化用户数据
- 创建默认的MAKER和CHECKER用户
- 密码加密存储

### 6. 安全工具模块

#### JwtTokenProvider
**文件位置**: `src/main/java/com/legacy/report/security/JwtTokenProvider.java`

**功能**:
- JWT Token生成
- Token解析和验证
- Token过期检查
- 用户信息提取

#### JwtAuthenticationFilter
**文件位置**: `src/main/java/com/legacy/report/security/JwtAuthenticationFilter.java`

**功能**:
- HTTP请求拦截
- JWT Token验证
- 用户身份设置
- 异常处理

### 7. 异常处理模块

#### GlobalExceptionHandler
**文件位置**: `src/main/java/com/legacy/report/exception/GlobalExceptionHandler.java`

**功能**:
- 全局异常处理
- 统一错误响应格式
- 日志记录

#### ReportExportException
**文件位置**: `src/main/java/com/legacy/report/exception/ReportExportException.java`

**功能**:
- 报表导出专用异常
- 自定义错误信息

## 前端模块详解

### 1. 应用结构

#### AppComponent
**文件位置**: `src/app/app.component.ts`

**功能**:
- 应用根组件
- 路由出口配置
- 全局样式设置

### 2. 路由模块

#### AppRoutingModule
**文件位置**: `src/app/app.routes.ts`

**路由配置**:
```typescript
export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', pathMatch: 'full', redirectTo: 'reports' },
  { path: 'reports', component: ReportViewerComponent, canActivate: [authGuard] },
  { path: 'maker', component: ReportViewerComponent, canActivate: [authGuard, roleGuard], data: { roles: ['MAKER'] } },
  { path: 'checker', component: ReportViewerComponent, canActivate: [authGuard, roleGuard], data: { roles: ['CHECKER'] } },
  { path: 'runs/:id/flow', component: ReportRunFlowComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'reports' }
];
```

### 3. 组件模块

#### LoginComponent
**文件位置**: `src/app/components/auth/login.component.ts`

**功能**:
- 用户登录界面
- 表单验证
- JWT Token处理
- 路由跳转

#### ReportViewerComponent
**文件位置**: `src/app/components/report/report-viewer.component.ts`

**功能**:
- 报表列表展示
- 报表详情查看
- 报表执行
- Excel导出

#### ReportRunFlowComponent
**文件位置**: `src/app/components/report/report-run-flow.component.ts`

**功能**:
- 报表运行流程管理
- 审批界面
- 状态跟踪
- 审计日志查看

### 4. 服务模块

#### AuthService
**文件位置**: `src/app/services/auth.service.ts`

**功能**:
- 用户认证
- Token管理
- 用户状态
- 登出处理

#### ReportService
**文件位置**: `src/app/services/report.service.ts`

**功能**:
- 报表数据获取
- 报表执行
- 文件下载
- HTTP请求封装

#### AuthGuard
**文件位置**: `src/app/services/auth.guard.ts`

**功能**:
- 路由守卫
- 身份验证
- 未授权重定向

#### RoleGuard
**文件位置**: `src/app/services/role.guard.ts`

**功能**:
- 角色权限守卫
- 细粒度权限控制
- 访问拒绝处理

### 5. 数据传输对象 (DTO)

#### LoginRequest
**文件位置**: `src/main/java/com/legacy/report/dto/LoginRequest.java`

**属性**:
- `username` - 用户名
- `password` - 密码

#### LoginResponse
**文件位置**: `src/main/java/com/legacy/report/dto/LoginResponse.java`

**属性**:
- `token` - JWT Token
- `user` - 用户信息

#### UserDto
**文件位置**: `src/main/java/com/legacy/report/dto/UserDto.java`

**属性**:
- `username` - 用户名
- `role` - 用户角色
- `enabled` - 是否启用

## 模块间交互

### 典型请求流程
1. **用户登录**
   - LoginComponent → AuthService → AuthController → AuthService → JwtTokenProvider

2. **报表执行**
   - ReportViewerComponent → ReportService → ReportController → ReportRunService → ReportDao

3. **审批流程**
   - ReportRunFlowComponent → ReportService → ReportRunController → ReportRunService → AuditService

### 数据流向
- **前端 → 后端**: HTTP请求 (JSON格式)
- **后端 → 数据库**: JPA/JDBC操作
- **后端 → 前端**: JSON响应或文件流

### 依赖关系
- Controller依赖Service
- Service依赖Repository/DAO
- Repository依赖Entity
- 前端组件依赖Service
- Service depend on HTTP Client

---

## Business Logic Integration

### User Roles and Permissions

#### MAKER Role
**Responsibilities**: Report creators and executors

**Permission Scope**:
- Create new report configurations
- Execute report queries
- View own execution history
- Submit report run approvals
- Export report results

**Operation Limits**:
- Can only submit report runs they created
- Cannot approve reports
- Cannot view others' reports

#### CHECKER Role
**Responsibilities**: Report approvers

**Permission Scope**:
- View pending report runs
- Approve or reject reports
- View approval history
- View audit logs
- Export approved reports

**Operation Limits**:
- Cannot create reports
- Cannot execute reports
- Can only approve submitted reports

### Report Lifecycle

#### Complete Lifecycle
```
Create Report -> Execute Report -> Submit Approval -> Approval Decision -> Export Results
      down            down            down               down              down
    Create        Execute         Submit            Approve/Reject    Export
```

#### Business Rules

#### Report Creation Rules
- Reports must have valid SQL syntax
- Reports must have meaningful names and descriptions
- Reports cannot contain DROP or DELETE statements
- Reports must be testable before creation

#### Execution Rules
- Reports can only be executed by MAKERs
- Reports must pass security validation
- Reports must complete within timeout period
- Reports must log execution details

#### Approval Rules
- Only CHECKERs can approve reports
- Reports must be submitted for approval
- CHECKERs must provide approval/rejection reasons
- Approved reports can be exported

#### Security Rules
- SQL injection protection required
- Parameter validation mandatory
- Access logging for all operations
- Role-based access control enforced

### Data Integrity Rules

#### Transaction Rules
- All report runs must be atomic
- Failed executions must be rolled back
- Audit trails must be maintained
- Data consistency must be preserved

#### Validation Rules
- Input parameters must be validated
- SQL queries must be sanitized
- Report results must be verified
- Export data must be formatted correctly

### Performance Rules

#### Execution Limits
- Reports must complete within 30 seconds
- Memory usage must be monitored
- Concurrent executions must be limited
- Resource usage must be tracked

#### Caching Rules
- Report results can be cached
- Cache invalidation must be handled
- Cache size must be limited
- Cache performance must be monitored

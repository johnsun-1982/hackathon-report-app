# 报表系统架构文档

## 项目概述

这是一个基于 Spring Boot + Angular 的报表管理系统，支持报表的创建、执行、审批和导出功能。系统采用前后端分离架构，实现了基本的用户认证和角色权限管理。

## 技术栈

### 后端技术栈
- **框架**: Spring Boot 3.2.4
- **Java版本**: Java 17
- **数据库**: H2 Database (内存数据库)
- **ORM**: Spring Data JPA
- **安全框架**: Spring Security + JWT
- **构建工具**: Gradle 8.6
- **监控**: Spring Actuator + Micrometer + Prometheus
- **Excel导出**: JXLS 2.14.0
- **测试框架**: JUnit 5

### 前端技术栈
- **框架**: Angular 17.3.0
- **语言**: TypeScript 5.4.0
- **路由**: Angular Router
- **HTTP客户端**: Angular HTTP Client
- **测试框架**: Jasmine + Karma
- **构建工具**: Angular CLI

## 系统架构

### 整体架构
```
┌─────────────────┐    HTTP/REST API    ┌─────────────────┐
│   Angular 前端   │ ◄─────────────────► │  Spring Boot    │
│   (端口: 4200)   │                     │  后端           │
└─────────────────┘                     │  (默认端口:8080) │
                                        └─────────────────┘
                                                 │
                                                 ▼
                                        ┌─────────────────┐
                                        │   H2 Database   │
                                        │   (内存数据库)   │
                                        └─────────────────┘
```

## 后端模块架构

### 1. 控制器层 (Controller)
**位置**: `src/main/java/com/legacy/report/controller/`

#### ReportController
- **路径**: `/api`
- **功能**: 报表相关的HTTP接口
- **主要接口**:
  - `GET /api/test` - 基础功能测试
  - `GET /api/test-db` - 数据库连接测试
  - `GET /api/reports` - 获取所有报表
  - `GET /api/reports/{id}` - 获取指定报表
  - `POST /api/reports/run` - 执行SQL报表 (存在安全风险)
  - `POST /api/reports/generate` - 生成报表
  - `POST /api/reports` - 创建报表
  - `POST /api/reports/{id}/execute` - 执行报表并创建运行记录
  - `GET /api/reports/{id}/export` - 导出报表为Excel

#### AuthController
- **功能**: 用户认证相关接口
- **主要接口**:
  - 登录认证
  - Token刷新

#### ReportRunController
- **功能**: 报表运行流程管理
- **主要接口**:
  - 提交报表运行
  - 审批报表运行
  - 查询运行状态

### 2. 服务层 (Service)
**位置**: `src/main/java/com/legacy/report/service/`

#### ReportService
- **功能**: 报表核心业务逻辑
- **主要方法**:
  - `getAllReports()` - 获取所有报表
  - `getReportById(Long id)` - 根据ID获取报表
  - `runReport(String sql)` - 直接执行SQL (严重安全漏洞)
  - `createReport(Report report)` - 创建报表
  - `generateReport(Long reportId, String params)` - 生成报表

#### ReportRunService
- **功能**: 报表运行流程管理
- **核心流程**: Generated → Submitted → Approved/Rejected
- **主要方法**:
  - `executeReportWithRun(Long reportId)` - 执行报表并创建运行记录
  - `submitRun(Long runId)` - 提交报表运行
  - `decideRun(Long runId, boolean approve, String comment)` - 审批决策
  - `getLatestRunForCurrentMaker(Long reportId)` - 获取Maker的最新运行记录
  - `getRunsForCurrentMaker()` - 获取当前Maker的所有运行记录
  - `getSubmittedRunsForChecker()` - 获取待审批的运行记录

#### AuthService
- **功能**: 用户认证服务
- **主要功能**: JWT Token生成和验证

#### ReportExcelExportService
- **功能**: Excel导出服务
- **依赖**: JXLS库

#### AuditService
- **功能**: 审计日志服务
- **记录**: 所有报表操作的审计轨迹

### 3. 数据访问层 (Repository/DAO)
**位置**: `src/main/java/com/legacy/report/repository/` 和 `src/main/java/com/legacy/report/dao/`

#### Repository层 (JPA)
- `ReportRunRepository` - 报表运行记录数据访问
- `ReportAuditEventRepository` - 审计事件数据访问
- `UserRepository` - 用户数据访问

#### DAO层 (JDBC)
- `ReportDao` - 报表配置数据访问，直接执行SQL

### 4. 模型层 (Model)
**位置**: `src/main/java/com/legacy/report/model/`

#### 核心实体
- **Report**: 报表配置实体
  - id: 报表ID
  - name: 报表名称
  - sql: SQL查询语句
  - description: 报表描述

- **ReportRun**: 报表运行实例
  - id: 运行实例ID
  - reportId: 报表配置ID
  - reportName: 报表名称快照
  - status: 运行状态 (Generated/Submitted/Approved/Rejected)
  - makerUsername: Maker用户名
  - checkerUsername: Checker用户名
  - generatedAt: 生成时间
  - submittedAt: 提交时间
  - decidedAt: 决策时间
  - parametersJson: 参数JSON
  - resultSnapshot: 结果快照

- **User**: 用户实体
  - 支持MAKER和CHECKER角色

- **ReportAuditEvent**: 审计事件
  - 记录所有报表操作的详细轨迹

### 5. 安全配置
**位置**: `src/main/java/com/legacy/report/config/`

#### SecurityConfig
- Spring Security配置
- JWT认证过滤器配置
- 路径权限控制

#### UserInitializer
- 初始化用户数据
- 创建默认的MAKER和CHECKER用户

### 6. 工具类
**位置**: `src/main/java/com/legacy/report/security/`

#### JwtTokenProvider
- JWT Token生成和验证
- Token解析工具

#### JwtAuthenticationFilter
- JWT认证过滤器
- 请求拦截和用户身份验证

## 前端架构

### 1. 应用结构
**位置**: `src/app/`

#### 主要组件
- **AppComponent**: 根组件，应用入口
- **LoginComponent**: 登录组件
- **ReportViewerComponent**: 报表查看器组件
- **ReportRunFlowComponent**: 报表运行流程组件

#### 服务层
**位置**: `src/app/services/`
- **AuthService**: 认证服务
- **ReportService**: 报表服务
- **AuthGuard**: 路由守卫
- **RoleGuard**: 角色守卫

### 2. 路由配置
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

## 核心业务逻辑

### 1. 报表生命周期
```
创建报表 → 执行报表 → 提交审批 → 审批决策 → 导出结果
    ↓         ↓         ↓         ↓         ↓
  Report   Generated  Submitted  Approved/Rejected  Excel
```

### 2. 用户角色权限
- **MAKER角色**: 
  - 创建报表
  - 执行报表
  - 提交审批
  - 查看自己的运行记录

- **CHECKER角色**:
  - 审批报表运行
  - 查看待审批列表
  - 查看审批历史

### 3. 审计流程
- 所有操作都会记录审计事件
- 包含用户、时间、操作类型、备注等信息
- 支持完整的操作轨迹追踪

### 4. 监控指标
- 报表生成计数器
- 提交计数器
- 审批通过/拒绝计数器
- 审批耗时计时器

## 安全问题

### 严重安全漏洞
1. **SQL注入风险**: `ReportService.runReport()` 直接执行用户输入的SQL
2. **参数拼接**: `generateReport()` 方法直接拼接SQL参数
3. **缺乏输入验证**: 没有对SQL内容进行安全检查
4. **权限控制不完善**: 部分接口缺乏细粒度权限控制

### 建议改进
1. 使用预编译SQL
2. 添加SQL内容白名单验证
3. 实现参数化查询
4. 加强权限控制
5. 添加操作日志记录

## 数据库设计

### 主要表结构
- **report_config**: 报表配置表
- **report_run**: 报表运行记录表
- **report_audit_event**: 审计事件表
- **users**: 用户表

## 部署说明

### 后端部署
```bash
# 编译项目
./gradlew build

# 运行应用
java -jar build/libs/report-backend-1.0.0.jar
```

### 前端部署
```bash
# 安装依赖
npm install

# 开发模式
npm run start

# 生产构建
npm run build
```

## 总结

这是一个功能相对完整的报表管理系统，具备了基本的CRUD操作、用户认证、角色权限和审计功能。但是存在严重的安全漏洞，特别是SQL注入风险，需要在生产环境使用前进行安全加固。系统架构清晰，采用了分层设计，便于后续的功能扩展和维护。

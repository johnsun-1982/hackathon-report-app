# 2层审批需求分析

## 需求概述

**标题**: 报表系统2层审批流程实现  
**类型**: Feature Requirements  
**优先级**: P1 - High  
**请求者**: 业务团队  
**日期**: 2026-04-08  

## 需求描述

当前报表系统采用单层审批流程（MAKER提交 → CHECKER审批），现需要升级为2层审批流程以增强内部控制和风险管理。

### 当前流程
```
创建报表 → 执行报表 → 提交审批 → 审批决策 → 导出结果
    ↓         ↓         ↓         ↓         ↓
  Create   Execute   Submit    Approve/  Export
                      Reject
```

### 目标流程
```
创建报表 → 执行报表 → 提交审批 → 一级审批 → 二级审批 → 导出结果
    ↓         ↓         ↓         ↓         ↓         ↓
  Create   Execute   Submit   L1Approve  L2Approve   Export
                      ↓         ↓         ↓
                   Reject   Reject   Reject
```

## 接受标准

- [ ] 所有报表需要经过2层审批才能导出
- [ ] 一级审批通过后才能进入二级审批
- [ ] 任何一层拒绝都可以重新提交
- [ ] 审批历史完整记录两个层级的决策
- [ ] 支持不同角色的审批权限配置
- [ ] 现有单层审批数据兼容迁移

## 业务价值

**解决的问题**: 
- 单层审批风险控制不足
- 缺乏分级授权机制
- 合规要求提升

**预期结果**:
- 增强内部控制和风险管理
- 满足合规要求
- 提高审批决策质量

**成功指标**:
- 审批错误率降低50%
- 合规审计通过率100%
- 用户满意度保持85%以上

## 约束条件

**技术约束**:
- 必须保持现有API兼容性
- 数据迁移不能影响业务连续性
- 性能不能显著下降

**时间约束**:
- 2周内完成开发和测试
- 1周内完成部署和验证

**资源约束**:
- 需要2名开发人员
- 需要1名测试人员
- 需要业务团队配合验证

## 影响分析

### 代码影响: Medium
**受影响的组件**:
- `ReportService.java` - 审批业务逻辑
- `ReportController.java` - 审批API端点
- `ReportRun.java` - 状态枚举和字段
- `ReportAuditEvent.java` - 审计事件记录
- 前端审批组件 - UI流程更新

### 数据影响: Schema
**数据库变更**:
```sql
-- ReportRun表添加字段
ALTER TABLE report_run ADD COLUMN first_approver_username VARCHAR(50);
ALTER TABLE report_run ADD COLUMN first_approval_time TIMESTAMP;
ALTER TABLE report_run ADD COLUMN second_approver_username VARCHAR(50);
ALTER TABLE report_run ADD COLUMN second_approval_time TIMESTAMP;
ALTER TABLE report_run ADD COLUMN approval_level INT DEFAULT 2; -- 1=单层, 2=双层

-- 可能需要审批配置表
CREATE TABLE approval_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_type VARCHAR(50) NOT NULL,
    approval_level INT NOT NULL DEFAULT 2,
    first_approver_role VARCHAR(50) NOT NULL,
    second_approver_role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 用户影响: Major
**界面变更**:
- 审批列表显示审批层级
- 审批详情页面显示两级决策
- 新增二级审批操作界面

**流程变更**:
- 用户需要适应新的审批流程
- 培训文档更新

### 集成影响: Internal
**API变更**:
- 新增二级审批端点
- 修改现有审批状态查询
- 审计事件API扩展

## 风险评估

### 技术风险: Medium
- **数据迁移风险**: 现有数据可能不兼容
- **状态机复杂性**: 2层审批状态管理复杂
- **性能影响**: 额外的审批查询可能影响性能

### 业务风险: Low
- **用户适应**: 用户需要时间适应新流程
- **流程变更**: 可能影响现有工作效率

### 安全风险: Low
- **权限控制**: 需要确保审批权限正确配置
- **审计完整性**: 确保两级审批都有完整审计

## 确认

- [x] 利益相关者确认需求
- [x] 技术团队确认可行性
- [x] 时间表确认
- [x] 资源分配确认

## 实施计划

### 阶段1: 设计和架构 (3天)
1. 详细设计2层审批状态机
2. 创建ADR文档
3. 设计数据库变更脚本
4. 设计API变更

### 阶段2: 开发 (7天)
1. 后端业务逻辑实现
2. 数据库迁移脚本
3. API端点开发和测试
4. 前端界面开发

### 阶段3: 测试 (3天)
1. 单元测试开发
2. 集成测试
3. 用户验收测试
4. 性能测试

### 阶段4: 部署 (1天)
1. 生产环境部署
2. 数据迁移执行
3. 功能验证
4. 监控和回滚准备

## 依赖关系

**技术依赖**:
- 现有审批系统稳定性
- 数据库备份可用性
- 测试环境就绪

**资源依赖**:
- 开发人员可用性
- 测试环境资源
- 业务团队验证时间

## 成功标准

### 功能标准
- [ ] 所有报表支持2层审批
- [ ] 审批状态正确流转
- [ ] 审计记录完整准确
- [ ] 数据迁移成功

### 性能标准
- [ ] 审批操作响应时间 < 2秒
- [ ] 系统整体性能下降 < 10%
- [ ] 数据库查询优化完成

### 质量标准
- [ ] 代码覆盖率 > 80%
- [ ] 所有测试用例通过
- [ ] 安全扫描无高危漏洞
- [ ] 用户验收测试通过

## 回滚计划

### 回滚触发条件
- 数据迁移失败
- 关键功能异常
- 性能严重下降
- 用户反馈严重问题

### 回滚步骤
1. 停止新功能服务
2. 恢复数据库备份
3. 回滚代码版本
4. 验证系统功能
5. 通知相关方

## 沟通计划

### 变更公告
- **时间**: 实施前1周
- **受众**: 所有用户和利益相关者
- **内容**: 变更描述、影响、时间表、联系方式

### 变更通知
- **时间**: 实施前24小时
- **受众**: 所有用户
- **内容**: 维护窗口、预期停机时间、替代流程

### 实施后沟通
- **时间**: 完成后立即
- **受众**: 所有用户
- **内容**: 变更完成、新功能说明、已知问题、支持联系

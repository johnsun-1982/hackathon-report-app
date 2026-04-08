package com.legacy.report.service;

import org.springframework.stereotype.Service;

/**
 * 审批权限服务
 * 用于检查用户是否有权限进行特定级别的审批
 */
@Service
public class ApprovalPermissionService {

    /**
     * 检查用户是否有权限进行指定级别的审批
     * @param username 用户名
     * @param level 审批级别 (1=一级, 2=二级)
     * @param reportType 报表类型
     * @return 是否有权限
     */
    public boolean hasPermission(String username, int level, String reportType) {
        // 简单的权限检查逻辑
        // 在实际应用中，这里应该查询数据库或配置文件
        // 现在先返回true，让应用能够启动
        return true;
    }
}

package com.legacy.report.controller;

import com.legacy.report.service.TwoLevelApprovalService;
import com.legacy.report.model.User;
import com.legacy.report.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 2层审批控制器
 * 提供报表2层审批相关的API端点
 */
@RestController
@RequestMapping("/api/reports")
public class TwoLevelApprovalController {

    @Autowired
    private TwoLevelApprovalService twoLevelApprovalService;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * 提交审批
     * @param runId 报表运行ID
     * @return 提交结果
     */
    @PostMapping("/runs/{runId}/submit")
    public ResponseEntity<?> submitForApproval(@PathVariable Long runId) {
        try {
            var result = twoLevelApprovalService.submitForApproval(runId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "提交审批成功",
                "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 一级审批
     * @param runId 报表运行ID
     * @param request 审批请求
     * @return 审批结果
     */
    @PostMapping("/runs/{runId}/first-approval")
    public ResponseEntity<?> firstLevelApproval(
            @PathVariable Long runId,
            @RequestBody ApprovalRequest request) {
        try {
            var result = twoLevelApprovalService.firstLevelApproval(runId, request.isApprove(), request.getComment());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", request.isApprove() ? "一级审批通过" : "一级审批拒绝",
                "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 二级审批
     * @param runId 报表运行ID
     * @param request 审批请求
     * @return 审批结果
     */
    @PostMapping("/runs/{runId}/second-approval")
    public ResponseEntity<?> secondLevelApproval(
            @PathVariable Long runId,
            @RequestBody ApprovalRequest request) {
        try {
            var result = twoLevelApprovalService.secondLevelApproval(runId, request.isApprove(), request.getComment());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", request.isApprove() ? "二级审批通过" : "二级审批拒绝",
                "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 重新执行报表
     * @param runId 报表运行ID
     * @return 重新执行结果
     */
    @PostMapping("/runs/{runId}/re-execute")
    public ResponseEntity<?> reExecuteReport(@PathVariable Long runId) {
        try {
            var result = twoLevelApprovalService.reExecuteReport(runId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "重新执行成功",
                "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取审批状态
     * @param runId 报表运行ID
     * @return 审批状态信息
     */
    @GetMapping("/runs/{runId}/approval-status")
    public ResponseEntity<?> getApprovalStatus(@PathVariable Long runId) {
        try {
            var result = twoLevelApprovalService.getApprovalStatus(runId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取审批状态成功",
                "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取待审批列表
     * @param level 审批级别 (1=一级, 2=二级)
     * @return 待审批列表
     */
    @GetMapping("/pending-approval")
    public ResponseEntity<?> getPendingApprovalList(@RequestParam(required = false) Integer level) {
        try {
            User currentUser = currentUserService.getCurrentUser();
            
            // 根据用户角色和审批级别获取待审批列表
            // 这里需要实现具体的查询逻辑
            var pendingList = getPendingApprovalListByLevel(currentUser, level);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取待审批列表成功",
                "data", pendingList
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 获取我的审批历史
     * @return 审批历史列表
     */
    @GetMapping("/my-approval-history")
    public ResponseEntity<?> getMyApprovalHistory() {
        try {
            User currentUser = currentUserService.getCurrentUser();
            
            // 获取当前用户的审批历史
            var approvalHistory = getApprovalHistoryByUser(currentUser);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "获取审批历史成功",
                "data", approvalHistory
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 审批请求DTO
     */
    public static class ApprovalRequest {
        private boolean approve;
        private String comment;

        public boolean isApprove() { return approve; }
        public void setApprove(boolean approve) { this.approve = approve; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    /**
     * 根据审批级别获取待审批列表
     * @param user 当前用户
     * @param level 审批级别
     * @return 待审批列表
     */
    private Object getPendingApprovalListByLevel(User user, Integer level) {
        // 这里需要实现具体的查询逻辑
        // 可以从ReportRunRepository中查询对应状态的报表运行
        // 根据用户角色过滤
        
        if (level == null) {
            // 返回所有级别的待审批列表
            return Map.of(
                "level1", getPendingListByStatus(user, "Submitted"),
                "level2", getPendingListByStatus(user, "L2Submitted")
            );
        } else if (level == 1) {
            // 返回一级审批待处理列表
            return getPendingListByStatus(user, "Submitted");
        } else if (level == 2) {
            // 返回二级审批待处理列表
            return getPendingListByStatus(user, "L2Submitted");
        } else {
            throw new IllegalArgumentException("无效的审批级别: " + level);
        }
    }

    /**
     * 根据状态获取待审批列表
     * @param user 当前用户
     * @param status 状态
     * @return 待审批列表
     */
    private Object getPendingListByStatus(User user, String status) {
        // 这里需要实现具体的查询逻辑
        // 示例实现，实际需要根据业务需求调整
        return Map.of(
            "status", status,
            "count", 0,
            "items", new Object[0]
        );
    }

    /**
     * 获取用户的审批历史
     * @param user 当前用户
     * @return 审批历史
     */
    private Object getApprovalHistoryByUser(User user) {
        // 这里需要实现具体的查询逻辑
        // 查询用户作为审批者的所有记录
        return Map.of(
            "username", user.getUsername(),
            "role", user.getRole(),
            "totalCount", 0,
            "approvedCount", 0,
            "rejectedCount", 0,
            "items", new Object[0]
        );
    }
}

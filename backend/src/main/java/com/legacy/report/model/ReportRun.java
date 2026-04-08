package com.legacy.report.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_run")
public class ReportRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    // 对应的报表模板 ID（来自 report_config 表）
    @Column(name = "report_id", nullable = false)
    private Long reportId;

    // 生成时的报表名称快照，方便审计和查询
    @Column(name = "report_name", nullable = false)
    private String reportName;

    // 报表运行状态：Generated / Submitted / L1Approved / L1Rejected / L2Submitted / L2Approved / L2Rejected
    @Column(name = "status", nullable = false, length = 32)
    private String status;

    // Maker 用户名
    @Column(name = "maker_username", nullable = false)
    private String makerUsername;

    // Checker 用户名（只有在审批后才有值）
    @Column(name = "checker_username")
    private String checkerUsername;

    // 一级审批者用户名
    @Column(name = "first_checker_username")
    private String firstCheckerUsername;

    // 一级审批时间
    @Column(name = "first_approval_time")
    private LocalDateTime firstApprovalTime;

    // 一级审批意见
    @Lob
    @Column(name = "first_approval_comment")
    private String firstApprovalComment;

    // 二级审批者用户名
    @Column(name = "second_checker_username")
    private String secondCheckerUsername;

    // 二级审批时间
    @Column(name = "second_approval_time")
    private LocalDateTime secondApprovalTime;

    // 二级审批意见
    @Lob
    @Column(name = "second_approval_comment")
    private String secondApprovalComment;

    // 是否需要二级审批
    @Column(name = "requires_second_approval", nullable = false)
    private Boolean requiresSecondApproval = true;

    // 审批级别：1=单层审批, 2=双层审批
    @Column(name = "approval_level", nullable = false)
    private Integer approvalLevel = 2;

    // 当前审批阶段：0=未提交, 1=一级审批中, 2=二级审批中, 3=审批完成
    @Column(name = "current_approval_stage", nullable = false)
    private Integer currentApprovalStage = 0;

    // 生成时间（执行报表成功时）
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    // 提交审批时间
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    // 审批决策时间（通过或拒绝）
    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    // 本次执行使用的参数（JSON 或简单字符串）
    @Lob
    @Column(name = "parameters_json")
    private String parametersJson;

    // 结果快照（可选，先用 JSON 字符串存储，后续可以演进为外部存储引用）
    @Lob
    @Column(name = "result_snapshot")
    private String resultSnapshot;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMakerUsername() {
        return makerUsername;
    }

    public void setMakerUsername(String makerUsername) {
        this.makerUsername = makerUsername;
    }

    public String getCheckerUsername() {
        return checkerUsername;
    }

    public void setCheckerUsername(String checkerUsername) {
        this.checkerUsername = checkerUsername;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }

    public String getParametersJson() {
        return parametersJson;
    }

    public void setParametersJson(String parametersJson) {
        this.parametersJson = parametersJson;
    }

    public String getResultSnapshot() {
        return resultSnapshot;
    }

    public void setResultSnapshot(String resultSnapshot) {
        this.resultSnapshot = resultSnapshot;
    }

    public String getFirstCheckerUsername() {
        return firstCheckerUsername;
    }

    public void setFirstCheckerUsername(String firstCheckerUsername) {
        this.firstCheckerUsername = firstCheckerUsername;
    }

    public LocalDateTime getFirstApprovalTime() {
        return firstApprovalTime;
    }

    public void setFirstApprovalTime(LocalDateTime firstApprovalTime) {
        this.firstApprovalTime = firstApprovalTime;
    }

    public String getFirstApprovalComment() {
        return firstApprovalComment;
    }

    public void setFirstApprovalComment(String firstApprovalComment) {
        this.firstApprovalComment = firstApprovalComment;
    }

    public String getSecondCheckerUsername() {
        return secondCheckerUsername;
    }

    public void setSecondCheckerUsername(String secondCheckerUsername) {
        this.secondCheckerUsername = secondCheckerUsername;
    }

    public Boolean getRequiresSecondApproval() {
        return requiresSecondApproval;
    }

    public void setRequiresSecondApproval(Boolean requiresSecondApproval) {
        this.requiresSecondApproval = requiresSecondApproval;
    }

    public LocalDateTime getSecondApprovalTime() {
        return secondApprovalTime;
    }

    public void setSecondApprovalTime(LocalDateTime secondApprovalTime) {
        this.secondApprovalTime = secondApprovalTime;
    }

    public String getSecondApprovalComment() {
        return secondApprovalComment;
    }

    public void setSecondApprovalComment(String secondApprovalComment) {
        this.secondApprovalComment = secondApprovalComment;
    }

    public Integer getApprovalLevel() {
        return approvalLevel;
    }

    public void setApprovalLevel(Integer approvalLevel) {
        this.approvalLevel = approvalLevel;
    }

    public Integer getCurrentApprovalStage() {
        return currentApprovalStage;
    }

    public void setCurrentApprovalStage(Integer currentApprovalStage) {
        this.currentApprovalStage = currentApprovalStage;
    }
    
    // 添加getReportType方法
    public String getReportType() {
        // 简单返回报表类型，实际应用中可以根据需要实现
        return "FINANCIAL"; // 默认为财务类型
    }
}

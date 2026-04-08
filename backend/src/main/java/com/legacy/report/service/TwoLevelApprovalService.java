package com.legacy.report.service;

import com.legacy.report.repository.ReportRunRepository;
import com.legacy.report.model.ReportRun;
import com.legacy.report.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 2-level approval service
 * Handles two-level approval business logic for reports
 */
@Service
public class TwoLevelApprovalService {

    @Autowired
    private ReportRunRepository reportRunRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ApprovalPermissionService approvalPermissionService;

    /**
     * Submit report for approval
     * @param runId Report run ID
     * @return Submitted report run
     */
    @Transactional
    public ReportRun submitForApproval(Long runId) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "MAKER");

        ReportRun run = reportRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Report run instance not found"));

        // Validate status
        if (!"Generated".equals(run.getStatus())) {
            throw new RuntimeException("Only generated reports can be submitted for approval");
        }

        // Validate permissions
        if (!currentUser.getUsername().equals(run.getMakerUsername())) {
            throw new RuntimeException("Only the MAKER who created the report can submit it for approval");
        }

        // Update status
        run.setStatus("Submitted");
        run.setCurrentApprovalStage(1); // Enter first-level approval
        run.setSubmittedAt(LocalDateTime.now());

        ReportRun saved = reportRunRepository.save(run);

        // Record audit event
        auditService.recordEvent(
                saved.getId(),
                saved.getReportId(),
                currentUser.getUsername(),
                currentUser.getRole(),
                "Submitted",
                "Submitted report for two-level approval"
        );

        return saved;
    }

    /**
     * First-level approval
     * @param runId Report run ID
     * @param approve Whether to approve
     * @param comment Approval comment
     * @return Approved report run
     */
    @Transactional
    public ReportRun firstLevelApproval(Long runId, boolean approve, String comment) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "CHECKER");

        ReportRun run = reportRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Report run instance not found"));

        // Validate status
        if (!"Submitted".equals(run.getStatus())) {
            throw new RuntimeException("Only submitted reports can be approved");
        }

        // Validate permissions
        if (!approvalPermissionService.hasPermission(currentUser.getUsername(), 1, run.getReportType())) {
            throw new RuntimeException("Current user does not have first-level approval permission");
        }

        // Update status
        if (approve) {
            run.setStatus("L1Approved");
            run.setCurrentApprovalStage(2); // Enter second-level approval
        } else {
            run.setStatus("L1Rejected");
            run.setCurrentApprovalStage(0); // Back to initial state
        }

        run.setFirstCheckerUsername(currentUser.getUsername());
        run.setFirstApprovalTime(LocalDateTime.now());
        run.setFirstApprovalComment(comment);

        ReportRun saved = reportRunRepository.save(run);

        // Record audit event
        auditService.recordEvent(
                saved.getId(),
                saved.getReportId(),
                currentUser.getUsername(),
                currentUser.getRole(),
                approve ? "L1Approved" : "L1Rejected",
                comment
        );

        return saved;
    }

    /**
     * Second-level approval
     * @param runId Report run ID
     * @param approve Whether to approve
     * @param comment Approval comment
     * @return Approved report run
     */
    @Transactional
    public ReportRun secondLevelApproval(Long runId, boolean approve, String comment) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "CHECKER");

        ReportRun run = reportRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Report run instance not found"));

        // Validate status
        if (!"L1Approved".equals(run.getStatus())) {
            throw new RuntimeException("Only L1Approved reports can undergo second-level approval");
        }

        // Validate permissions
        if (!approvalPermissionService.hasPermission(currentUser.getUsername(), 2, run.getReportType())) {
            throw new RuntimeException("Current user does not have second-level approval permission");
        }

        // Update status
        if (approve) {
            run.setStatus("L2Approved");
            run.setCurrentApprovalStage(3); // Approval complete
        } else {
            run.setStatus("L2Rejected");
            run.setCurrentApprovalStage(0); // Back to initial state
        }

        run.setSecondCheckerUsername(currentUser.getUsername());
        run.setSecondApprovalTime(LocalDateTime.now());
        run.setSecondApprovalComment(comment);

        ReportRun saved = reportRunRepository.save(run);

        // Record audit event
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

    /**
     * Re-execute report
     * @param runId Report run ID
     * @return Re-executed report run
     */
    @Transactional
    public ReportRun reExecuteReport(Long runId) {
        User currentUser = currentUserService.getCurrentUserOrThrow();
        currentUserService.requireRole(currentUser, "MAKER");

        ReportRun run = reportRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Report run instance not found"));

        // Validate status
        if (!"L1Rejected".equals(run.getStatus()) && !"L2Rejected".equals(run.getStatus())) {
            throw new RuntimeException("Only rejected report runs can be re-executed");
        }

        // Validate permissions
        if (!currentUser.getUsername().equals(run.getMakerUsername())) {
            throw new RuntimeException("Only the MAKER who created the report can re-execute it");
        }

        // Reset status and approval fields
        run.setStatus("Generated");
        run.setCurrentApprovalStage(0);
        run.setFirstCheckerUsername(null);
        run.setFirstApprovalTime(null);
        run.setFirstApprovalComment(null);
        run.setSecondCheckerUsername(null);
        run.setSecondApprovalTime(null);
        run.setSecondApprovalComment(null);
        
        ReportRun saved = reportRunRepository.save(run);

        auditService.recordEvent(
                saved.getId(),
                saved.getReportId(),
                currentUser.getUsername(),
                currentUser.getRole(),
                "ReExecuted",
                "Re-executed rejected report"
        );

        return saved;
    }

    /**
     * Get approval status
     * @param runId Report run ID
     * @return Approval status information
     */
    public ApprovalStatusInfo getApprovalStatus(Long runId) {
        ReportRun run = reportRunRepository.findById(runId)
                .orElseThrow(() -> new RuntimeException("Report run instance not found"));

        ApprovalStatusInfo info = new ApprovalStatusInfo();
        info.setRunId(run.getId());
        info.setStatus(run.getStatus());
        info.setCurrentApprovalStage(run.getCurrentApprovalStage());
        
        if (run.getFirstCheckerUsername() != null) {
            ApprovalInfo firstApproval = new ApprovalInfo();
            firstApproval.setApprover(run.getFirstCheckerUsername());
            firstApproval.setTime(run.getFirstApprovalTime());
            firstApproval.setComment(run.getFirstApprovalComment());
            info.setFirstApproval(firstApproval);
        }
        
        if (run.getSecondCheckerUsername() != null) {
            ApprovalInfo secondApproval = new ApprovalInfo();
            secondApproval.setApprover(run.getSecondCheckerUsername());
            secondApproval.setTime(run.getSecondApprovalTime());
            secondApproval.setComment(run.getSecondApprovalComment());
            info.setSecondApproval(secondApproval);
        }

        return info;
    }

    /**
     * Approval information inner class
     */
    public static class ApprovalInfo {
        private String approver;
        private LocalDateTime time;
        private String comment;

        public String getApprover() {
            return approver;
        }

        public void setApprover(String approver) {
            this.approver = approver;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    /**
     * Approval status information inner class
     */
    public static class ApprovalStatusInfo {
        private Long runId;
        private String status;
        private Integer currentApprovalStage;
        private ApprovalInfo firstApproval;
        private ApprovalInfo secondApproval;

        public Long getRunId() {
            return runId;
        }

        public void setRunId(Long runId) {
            this.runId = runId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getCurrentApprovalStage() {
            return currentApprovalStage;
        }

        public void setCurrentApprovalStage(Integer currentApprovalStage) {
            this.currentApprovalStage = currentApprovalStage;
        }

        public ApprovalInfo getFirstApproval() {
            return firstApproval;
        }

        public void setFirstApproval(ApprovalInfo firstApproval) {
            this.firstApproval = firstApproval;
        }

        public ApprovalInfo getSecondApproval() {
            return secondApproval;
        }

        public void setSecondApproval(ApprovalInfo secondApproval) {
            this.secondApproval = secondApproval;
        }
    }
}

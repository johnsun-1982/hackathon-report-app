package com.legacy.report.service;

import com.legacy.report.model.ReportRun;
import com.legacy.report.model.User;
import com.legacy.report.repository.ReportRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoLevelApprovalServiceTest {

    @Mock
    private ReportRunRepository reportRunRepository;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private AuditService auditService;

    @Mock
    private ApprovalPermissionService approvalPermissionService;

    @InjectMocks
    private TwoLevelApprovalService twoLevelApprovalService;

    private User testUser;
    private ReportRun testRun;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setRole("MAKER");

        testRun = new ReportRun();
        testRun.setId(1L);
        testRun.setReportId(100L);
        testRun.setReportName("Test Report");
        testRun.setStatus("Generated");
        testRun.setMakerUsername("testuser");
        testRun.setGeneratedAt(LocalDateTime.now());
        testRun.setCurrentApprovalStage(0);
    }

    @Test
    void shouldSubmitForApprovalSuccessfully() {
        // Given
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(testUser);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(reportRunRepository.save(any(ReportRun.class))).thenReturn(testRun);

        // When
        ReportRun result = twoLevelApprovalService.submitForApproval(1L);

        // Then
        assertNotNull(result);
        assertEquals("Submitted", result.getStatus());
        assertEquals(1, result.getCurrentApprovalStage());
        assertNotNull(result.getSubmittedAt());

        verify(reportRunRepository, times(1)).findById(1L);
        verify(reportRunRepository, times(1)).save(testRun);
        verify(auditService, times(1)).recordEvent(
            eq(1L), eq(100L), eq("testuser"), eq("MAKER"), eq("Submitted"), isNull()
        );
    }

    @Test
    void shouldThrowExceptionWhenSubmitNonGeneratedStatus() {
        // Given
        testRun.setStatus("Approved");
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(testUser);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> twoLevelApprovalService.submitForApproval(1L));
        assertEquals("只能提交 Generated 状态的报表运行实例", exception.getMessage());

        verify(reportRunRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenSubmitOtherUsersReport() {
        // Given
        testRun.setMakerUsername("otheruser");
        when(currentUserService.getCurrentUserOrThrow()).thenReturn(testUser);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> twoLevelApprovalService.submitForApproval(1L));
        assertEquals("只能提交由当前 MAKER 自己生成的报表运行实例", exception.getMessage());

        verify(reportRunRepository, never()).save(any());
    }

    @Test
    void shouldFirstLevelApproveSuccessfully() {
        // Given
        User l1Checker = new User();
        l1Checker.setUsername("l1_checker");
        l1Checker.setRole("L1_CHECKER");

        testRun.setStatus("Submitted");
        testRun.setCurrentApprovalStage(1);

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(l1Checker);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(approvalPermissionService.hasPermission("l1_checker", 1, "FINANCIAL")).thenReturn(true);
        when(reportRunRepository.save(any(ReportRun.class))).thenReturn(testRun);

        // When
        ReportRun result = twoLevelApprovalService.firstLevelApproval(1L, true, "一级审批通过");

        // Then
        assertNotNull(result);
        assertEquals("L1Approved", result.getStatus());
        assertEquals(2, result.getCurrentApprovalStage());
        assertEquals("l1_checker", result.getFirstApproverUsername());
        assertNotNull(result.getFirstApprovalTime());
        assertEquals("一级审批通过", result.getFirstApprovalComment());

        verify(reportRunRepository, times(1)).save(testRun);
        verify(auditService, times(1)).recordEvent(
            eq(1L), eq(100L), eq("l1_checker"), eq("L1_CHECKER"), eq("L1Approved"), eq("一级审批通过")
        );
    }

    @Test
    void shouldFirstLevelRejectSuccessfully() {
        // Given
        User l1Checker = new User();
        l1Checker.setUsername("l1_checker");
        l1Checker.setRole("L1_CHECKER");

        testRun.setStatus("Submitted");
        testRun.setCurrentApprovalStage(1);

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(l1Checker);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(approvalPermissionService.hasPermission("l1_checker", 1, "FINANCIAL")).thenReturn(true);
        when(reportRunRepository.save(any(ReportRun.class))).thenReturn(testRun);

        // When
        ReportRun result = twoLevelApprovalService.firstLevelApproval(1L, false, "需要补充数据");

        // Then
        assertNotNull(result);
        assertEquals("L1Rejected", result.getStatus());
        assertEquals(0, result.getCurrentApprovalStage());
        assertEquals("l1_checker", result.getFirstApproverUsername());
        assertNotNull(result.getFirstApprovalTime());
        assertEquals("需要补充数据", result.getFirstApprovalComment());

        verify(reportRunRepository, times(1)).save(testRun);
        verify(auditService, times(1)).recordEvent(
            eq(1L), eq(100L), eq("l1_checker"), eq("L1_CHECKER"), eq("L1Rejected"), eq("需要补充数据")
        );
    }

    @Test
    void shouldThrowExceptionWhenFirstLevelApproveWithoutPermission() {
        // Given
        User l1Checker = new User();
        l1Checker.setUsername("l1_checker");
        l1Checker.setRole("L1_CHECKER");

        testRun.setStatus("Submitted");
        testRun.setCurrentApprovalStage(1);

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(l1Checker);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(approvalPermissionService.hasPermission("l1_checker", 1, "FINANCIAL")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> twoLevelApprovalService.firstLevelApproval(1L, true, "审批通过"));
        assertEquals("当前用户无权进行一级审批", exception.getMessage());

        verify(reportRunRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenFirstLevelRejectWithoutComment() {
        // Given
        User l1Checker = new User();
        l1Checker.setUsername("l1_checker");
        l1Checker.setRole("L1_CHECKER");

        testRun.setStatus("Submitted");
        testRun.setCurrentApprovalStage(1);

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(l1Checker);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(approvalPermissionService.hasPermission("l1_checker", 1, "FINANCIAL")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> twoLevelApprovalService.firstLevelApproval(1L, false, ""));
        assertEquals("拒绝审批时必须填写备注", exception.getMessage());

        verify(reportRunRepository, never()).save(any());
    }

    @Test
    void shouldSecondLevelApproveSuccessfully() {
        // Given
        User l2Checker = new User();
        l2Checker.setUsername("l2_checker");
        l2Checker.setRole("L2_CHECKER");

        testRun.setStatus("L2Submitted");
        testRun.setCurrentApprovalStage(2);
        testRun.setFirstApproverUsername("l1_checker");
        testRun.setFirstApprovalTime(LocalDateTime.now().minusHours(1));

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(l2Checker);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(approvalPermissionService.hasPermission("l2_checker", 2, "FINANCIAL")).thenReturn(true);
        when(reportRunRepository.save(any(ReportRun.class))).thenReturn(testRun);

        // When
        ReportRun result = twoLevelApprovalService.secondLevelApproval(1L, true, "二级审批通过");

        // Then
        assertNotNull(result);
        assertEquals("L2Approved", result.getStatus());
        assertEquals(3, result.getCurrentApprovalStage());
        assertEquals("l2_checker", result.getSecondApproverUsername());
        assertNotNull(result.getSecondApprovalTime());
        assertEquals("二级审批通过", result.getSecondApprovalComment());

        verify(reportRunRepository, times(1)).save(testRun);
        verify(auditService, times(1)).recordEvent(
            eq(1L), eq(100L), eq("l2_checker"), eq("L2_CHECKER"), eq("L2Approved"), eq("二级审批通过")
        );
    }

    @Test
    void shouldSecondLevelRejectSuccessfully() {
        // Given
        User l2Checker = new User();
        l2Checker.setUsername("l2_checker");
        l2Checker.setRole("L2_CHECKER");

        testRun.setStatus("L2Submitted");
        testRun.setCurrentApprovalStage(2);
        testRun.setFirstApproverUsername("l1_checker");
        testRun.setFirstApprovalTime(LocalDateTime.now().minusHours(1));

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(l2Checker);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(approvalPermissionService.hasPermission("l2_checker", 2, "FINANCIAL")).thenReturn(true);
        when(reportRunRepository.save(any(ReportRun.class))).thenReturn(testRun);

        // When
        ReportRun result = twoLevelApprovalService.secondLevelApproval(1L, false, "数据不准确");

        // Then
        assertNotNull(result);
        assertEquals("L2Rejected", result.getStatus());
        assertEquals(0, result.getCurrentApprovalStage());
        assertEquals("l2_checker", result.getSecondApproverUsername());
        assertNotNull(result.getSecondApprovalTime());
        assertEquals("数据不准确", result.getSecondApprovalComment());

        verify(reportRunRepository, times(1)).save(testRun);
        verify(auditService, times(1)).recordEvent(
            eq(1L), eq(100L), eq("l2_checker"), eq("L2_CHECKER"), eq("L2Rejected"), eq("数据不准确")
        );
    }

    @Test
    void shouldReExecuteReportSuccessfully() {
        // Given
        testRun.setStatus("L1Rejected");
        testRun.setCurrentApprovalStage(0);
        testRun.setFirstApproverUsername("l1_checker");

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(testUser);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));
        when(reportRunRepository.save(any(ReportRun.class))).thenReturn(testRun);

        // When
        ReportRun result = twoLevelApprovalService.reExecuteReport(1L);

        // Then
        assertNotNull(result);
        assertEquals("Generated", result.getStatus());
        assertEquals(0, result.getCurrentApprovalStage());
        assertNull(result.getFirstApproverUsername());
        assertNull(result.getFirstApprovalTime());
        assertNull(result.getFirstApprovalComment());
        assertNull(result.getSecondApproverUsername());
        assertNull(result.getSecondApprovalTime());
        assertNull(result.getSecondApprovalComment());

        verify(reportRunRepository, times(1)).save(testRun);
        verify(auditService, times(1)).recordEvent(
            eq(1L), eq(100L), eq("testuser"), eq("MAKER"), eq("ReExecuted"), eq("重新执行被拒绝的报表")
        );
    }

    @Test
    void shouldThrowExceptionWhenReExecuteNonRejectedReport() {
        // Given
        testRun.setStatus("L2Approved");

        when(currentUserService.getCurrentUserOrThrow()).thenReturn(testUser);
        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> twoLevelApprovalService.reExecuteReport(1L));
        assertEquals("只能对被拒绝的报表运行实例重新执行", exception.getMessage());

        verify(reportRunRepository, never()).save(any());
    }

    @Test
    void shouldGetApprovalStatusSuccessfully() {
        // Given
        testRun.setStatus("L2Approved");
        testRun.setCurrentApprovalStage(3);
        testRun.setFirstApproverUsername("l1_checker");
        testRun.setFirstApprovalTime(LocalDateTime.now().minusHours(2));
        testRun.setFirstApprovalComment("一级审批意见");
        testRun.setSecondApproverUsername("l2_checker");
        testRun.setSecondApprovalTime(LocalDateTime.now().minusHours(1));
        testRun.setSecondApprovalComment("二级审批意见");

        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));

        // When
        var result = twoLevelApprovalService.getApprovalStatus(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRunId());
        assertEquals("L2Approved", result.getStatus());
        assertEquals(3, result.getCurrentApprovalStage());
        
        assertNotNull(result.getFirstApproval());
        assertEquals("l1_checker", result.getFirstApproval().getApprover());
        assertEquals("一级审批意见", result.getFirstApproval().getComment());
        
        assertNotNull(result.getSecondApproval());
        assertEquals("l2_checker", result.getSecondApproval().getApprover());
        assertEquals("二级审批意见", result.getSecondApproval().getComment());

        verify(reportRunRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenGetApprovalStatusForNonExistentRun() {
        // Given
        when(reportRunRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> twoLevelApprovalService.getApprovalStatus(1L));
        assertEquals("报表运行实例不存在", exception.getMessage());
    }

    @Test
    void shouldHandleNullValuesInApprovalInfo() {
        // Given
        testRun.setStatus("Submitted");
        testRun.setCurrentApprovalStage(1);
        // 审批信息都为null

        when(reportRunRepository.findById(1L)).thenReturn(Optional.of(testRun));

        // When
        var result = twoLevelApprovalService.getApprovalStatus(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getRunId());
        assertEquals("Submitted", result.getStatus());
        assertEquals(1, result.getCurrentApprovalStage());
        assertNull(result.getFirstApproval());
        assertNull(result.getSecondApproval());
    }
}

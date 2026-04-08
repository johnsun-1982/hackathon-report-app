package com.legacy.report.service;

import com.legacy.report.dao.ReportDao;
import com.legacy.report.model.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportDao reportDao;

    @InjectMocks
    private ReportService reportService;

    private Report testReport;

    @BeforeEach
    void setUp() {
        testReport = new Report(1L, "Test Report", "SELECT * FROM test", "Test Description");
    }

    @Test
    void shouldGetAllReports() {
        // Given
        List<Report> expectedReports = Arrays.asList(
            new Report(1L, "Report 1", "SELECT 1", "Description 1"),
            new Report(2L, "Report 2", "SELECT 2", "Description 2")
        );
        when(reportDao.findAll()).thenReturn(expectedReports);

        // When
        List<Report> actualReports = reportService.getAllReports();

        // Then
        assertEquals(2, actualReports.size());
        assertEquals("Report 1", actualReports.get(0).getName());
        assertEquals("Report 2", actualReports.get(1).getName());
        verify(reportDao, times(1)).findAll();
    }

    @Test
    void shouldGetReportById() {
        // Given
        when(reportDao.findById(1L)).thenReturn(testReport);

        // When
        Report actualReport = reportService.getReportById(1L);

        // Then
        assertNotNull(actualReport);
        assertEquals(1L, actualReport.getId());
        assertEquals("Test Report", actualReport.getName());
        assertEquals("SELECT * FROM test", actualReport.getSql());
        verify(reportDao, times(1)).findById(1L);
    }

    @Test
    void shouldReturnNullWhenReportNotFound() {
        // Given
        when(reportDao.findById(999L)).thenReturn(null);

        // When
        Report actualReport = reportService.getReportById(999L);

        // Then
        assertNull(actualReport);
        verify(reportDao, times(1)).findById(999L);
    }

    @Test
    void shouldRunReport() {
        // Given
        String sql = "SELECT * FROM test";
        List<Map<String, Object>> expectedResults = Arrays.asList(
            Map.of("id", 1, "name", "Test"),
            Map.of("id", 2, "name", "Test2")
        );
        when(reportDao.executeSql(sql)).thenReturn(expectedResults);

        // When
        List<Map<String, Object>> actualResults = reportService.runReport(sql);

        // Then
        assertEquals(2, actualResults.size());
        assertEquals(1, actualResults.get(0).get("id"));
        assertEquals("Test", actualResults.get(0).get("name"));
        verify(reportDao, times(1)).executeSql(sql);
    }

    @Test
    void shouldCreateReportSuccessfully() {
        // Given
        Report newReport = new Report(null, "New Report", "SELECT * FROM new", "New Description");
        doNothing().when(reportDao).save(any(Report.class));

        // When
        assertDoesNotThrow(() -> reportService.createReport(newReport));

        // Then
        verify(reportDao, times(1)).save(newReport);
    }

    @Test
    void shouldThrowExceptionWhenCreatingReportWithNullName() {
        // Given
        Report invalidReport = new Report(null, null, "SELECT * FROM test", "Description");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> reportService.createReport(invalidReport));
        assertEquals("Name cannot be null", exception.getMessage());
        verify(reportDao, never()).save(any(Report.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingReportWithEmptyName() {
        // Given
        Report invalidReport = new Report(null, "", "SELECT * FROM test", "Description");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> reportService.createReport(invalidReport));
        assertEquals("Name cannot be null", exception.getMessage());
        verify(reportDao, never()).save(any(Report.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingReportWithNullSql() {
        // Given
        Report invalidReport = new Report(null, "Test Report", null, "Description");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> reportService.createReport(invalidReport));
        assertEquals("SQL cannot be null", exception.getMessage());
        verify(reportDao, never()).save(any(Report.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingReportWithEmptySql() {
        // Given
        Report invalidReport = new Report(null, "Test Report", "", "Description");

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> reportService.createReport(invalidReport));
        assertEquals("SQL cannot be null", exception.getMessage());
        verify(reportDao, never()).save(any(Report.class));
    }

    @Test
    void shouldGenerateReportSuccessfully() {
        // Given
        Long reportId = 1L;
        String params = "WHERE id = 1";
        String expectedSql = "SELECT * FROM test WHERE id = 1";
        List<Map<String, Object>> mockData = Arrays.asList(
            Map.of("id", 1, "name", "Test"),
            Map.of("id", 2, "name", "Test2")
        );
        
        when(reportDao.findById(reportId)).thenReturn(testReport);
        when(reportDao.executeSql(expectedSql)).thenReturn(mockData);

        // When
        Map<String, Object> result = reportService.generateReport(reportId, params);

        // Then
        assertNotNull(result);
        assertEquals("Test Report", result.get("reportName"));
        assertEquals(mockData, result.get("data"));
        assertEquals(2, result.get("count"));
        verify(reportDao, times(1)).findById(reportId);
        verify(reportDao, times(1)).executeSql(expectedSql);
    }

    @Test
    void shouldGenerateReportWithoutParams() {
        // Given
        Long reportId = 1L;
        String params = null;
        String expectedSql = "SELECT * FROM test";
        List<Map<String, Object>> mockData = Arrays.asList(
            Map.of("id", 1, "name", "Test")
        );
        
        when(reportDao.findById(reportId)).thenReturn(testReport);
        when(reportDao.executeSql(expectedSql)).thenReturn(mockData);

        // When
        Map<String, Object> result = reportService.generateReport(reportId, params);

        // Then
        assertNotNull(result);
        assertEquals("Test Report", result.get("reportName"));
        assertEquals(mockData, result.get("data"));
        assertEquals(1, result.get("count"));
        verify(reportDao, times(1)).findById(reportId);
        verify(reportDao, times(1)).executeSql(expectedSql);
    }

    @Test
    void shouldGenerateReportWithEmptyParams() {
        // Given
        Long reportId = 1L;
        String params = "";
        String expectedSql = "SELECT * FROM test";
        List<Map<String, Object>> mockData = Arrays.asList(
            Map.of("id", 1, "name", "Test")
        );
        
        when(reportDao.findById(reportId)).thenReturn(testReport);
        when(reportDao.executeSql(expectedSql)).thenReturn(mockData);

        // When
        Map<String, Object> result = reportService.generateReport(reportId, params);

        // Then
        assertNotNull(result);
        assertEquals("Test Report", result.get("reportName"));
        assertEquals(mockData, result.get("data"));
        assertEquals(1, result.get("count"));
        verify(reportDao, times(1)).findById(reportId);
        verify(reportDao, times(1)).executeSql(expectedSql);
    }

    @Test
    void shouldThrowExceptionWhenGeneratingReportForNonExistentReport() {
        // Given
        Long reportId = 999L;
        String params = "WHERE id = 1";
        
        when(reportDao.findById(reportId)).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> reportService.generateReport(reportId, params));
        assertEquals("Report not found", exception.getMessage());
        verify(reportDao, times(1)).findById(reportId);
        verify(reportDao, never()).executeSql(anyString());
    }

    @Test
    void shouldHandleSqlExecutionErrorInGenerateReport() {
        // Given
        Long reportId = 1L;
        String params = "WHERE id = 1";
        String expectedSql = "SELECT * FROM test WHERE id = 1";
        
        when(reportDao.findById(reportId)).thenReturn(testReport);
        when(reportDao.executeSql(expectedSql)).thenThrow(new RuntimeException("SQL Error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> reportService.generateReport(reportId, params));
        assertEquals("SQL Error", exception.getMessage());
        verify(reportDao, times(1)).findById(reportId);
        verify(reportDao, times(1)).executeSql(expectedSql);
    }

    @Test
    void shouldHandleSqlExecutionErrorInRunReport() {
        // Given
        String sql = "INVALID SQL";
        
        when(reportDao.executeSql(sql)).thenThrow(new RuntimeException("SQL Syntax Error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> reportService.runReport(sql));
        assertEquals("SQL Syntax Error", exception.getMessage());
        verify(reportDao, times(1)).executeSql(sql);
    }

    @Test
    void shouldAppendParamsToSqlCorrectly() {
        // Given
        Long reportId = 1L;
        String params = "id = 1 AND status = 'active'";
        String expectedSql = "SELECT * FROM test WHERE id = 1 AND status = 'active'";
        List<Map<String, Object>> mockData = Arrays.asList(Map.of("id", 1));
        
        when(reportDao.findById(reportId)).thenReturn(testReport);
        when(reportDao.executeSql(expectedSql)).thenReturn(mockData);

        // When
        Map<String, Object> result = reportService.generateReport(reportId, params);

        // Then
        assertNotNull(result);
        verify(reportDao, times(1)).executeSql(expectedSql);
    }

    @Test
    void shouldReturnEmptyDataWhenQueryReturnsNoResults() {
        // Given
        Long reportId = 1L;
        String params = "WHERE id = 999";
        String expectedSql = "SELECT * FROM test WHERE id = 999";
        List<Map<String, Object>> emptyData = Arrays.asList();
        
        when(reportDao.findById(reportId)).thenReturn(testReport);
        when(reportDao.executeSql(expectedSql)).thenReturn(emptyData);

        // When
        Map<String, Object> result = reportService.generateReport(reportId, params);

        // Then
        assertNotNull(result);
        assertEquals("Test Report", result.get("reportName"));
        assertEquals(emptyData, result.get("data"));
        assertEquals(0, result.get("count"));
        verify(reportDao, times(1)).findById(reportId);
        verify(reportDao, times(1)).executeSql(expectedSql);
    }
}

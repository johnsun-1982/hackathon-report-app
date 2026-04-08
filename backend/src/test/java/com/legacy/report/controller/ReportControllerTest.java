package com.legacy.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.legacy.report.model.Report;
import com.legacy.report.service.ReportExcelExportService;
import com.legacy.report.service.ReportRunService;
import com.legacy.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private ReportRunService reportRunService;

    @MockBean
    private ReportExcelExportService reportExcelExportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnOkStatusForTestEndpoint() throws Exception {
        mockMvc.perform(get("/api/test"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("ok"))
               .andExpect(jsonPath("$.message").value("Backend is working"));
    }

    @Test
    void shouldReturnDatabaseTestSuccess() throws Exception {
        List<Report> mockReports = Arrays.asList(
            new Report(1L, "Test Report", "SELECT * FROM test", "Test Description")
        );
        
        when(reportService.getAllReports()).thenReturn(mockReports);

        mockMvc.perform(get("/api/test-db"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("ok"))
               .andExpect(jsonPath("$.message").value("Database connection working"))
               .andExpect(jsonPath("$.count").value(1));

        verify(reportService, times(1)).getAllReports();
    }

    @Test
    void shouldReturnDatabaseTestError() throws Exception {
        when(reportService.getAllReports()).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/test-db"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value("error"))
               .andExpect(jsonPath("$.message").value("Database connection failed: Database error"));

        verify(reportService, times(1)).getAllReports();
    }

    @Test
    void shouldReturnAllReports() throws Exception {
        List<Report> mockReports = Arrays.asList(
            new Report(1L, "Report 1", "SELECT * FROM table1", "Description 1"),
            new Report(2L, "Report 2", "SELECT * FROM table2", "Description 2")
        );
        
        when(reportService.getAllReports()).thenReturn(mockReports);

        mockMvc.perform(get("/api/reports"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$", hasSize(2)))
               .andExpect(jsonPath("$[0].id").value(1))
               .andExpect(jsonPath("$[0].name").value("Report 1"))
               .andExpect(jsonPath("$[0].sql").value("SELECT * FROM table1"))
               .andExpect(jsonPath("$[1].id").value(2))
               .andExpect(jsonPath("$[1].name").value("Report 2"));

        verify(reportService, times(1)).getAllReports();
    }

    @Test
    void shouldReturnReportById() throws Exception {
        Report mockReport = new Report(1L, "Test Report", "SELECT * FROM test", "Test Description");
        
        when(reportService.getReportById(1L)).thenReturn(mockReport);

        mockMvc.perform(get("/api/reports/1"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.name").value("Test Report"))
               .andExpect(jsonPath("$.sql").value("SELECT * FROM test"))
               .andExpect(jsonPath("$.description").value("Test Description"));

        verify(reportService, times(1)).getReportById(1L);
    }

    @Test
    void shouldRunReport() throws Exception {
        Map<String, Object> mockResult = Map.of("id", 1, "name", "Test");
        List<Map<String, Object>> mockResults = Arrays.asList(mockResult);
        
        when(reportService.runReport(anyString())).thenReturn(mockResults);

        Map<String, String> request = Map.of("sql", "SELECT * FROM test");

        mockMvc.perform(post("/api/reports/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id").value(1))
               .andExpect(jsonPath("$[0].name").value("Test"));

        verify(reportService, times(1)).runReport("SELECT * FROM test");
    }

    @Test
    void shouldGenerateReport() throws Exception {
        Map<String, Object> mockReportData = Map.of(
            "reportName", "Test Report",
            "data", Arrays.asList(Map.of("id", 1, "name", "Test")),
            "count", 1
        );
        
        when(reportService.generateReport(anyLong(), anyString())).thenReturn(mockReportData);

        Map<String, Object> request = Map.of(
            "reportId", 1,
            "params", "WHERE id = 1"
        );

        mockMvc.perform(post("/api/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.reportName").value("Test Report"))
               .andExpect(jsonPath("$.count").value(1));

        verify(reportService, times(1)).generateReport(1L, "WHERE id = 1");
    }

    @Test
    void shouldCreateReport() throws Exception {
        Report newReport = new Report(1L, "New Report", "SELECT * FROM new", "New Description");

        doNothing().when(reportService).createReport(any(Report.class));

        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newReport)))
               .andExpect(status().isOk());

        verify(reportService, times(1)).createReport(any(Report.class));
    }

    @Test
    void shouldExecuteReport() throws Exception {
        List<Map<String, Object>> mockResults = Arrays.asList(
            Map.of("id", 1, "name", "Test Result")
        );
        
        when(reportRunService.executeReportWithRun(anyLong())).thenReturn(mockResults);

        mockMvc.perform(post("/api/reports/1/execute"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id").value(1));

        verify(reportRunService, times(1)).executeReportWithRun(1L);
    }

    @Test
    void shouldExportReport() throws Exception {
        byte[] mockExcelData = "mock excel data".getBytes();
        
        when(reportExcelExportService.exportLatestByReportId(anyLong())).thenReturn(mockExcelData);

        mockMvc.perform(get("/api/reports/1/export"))
               .andExpect(status().isOk())
               .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
               .andExpect(header().exists("Content-Disposition"));

        verify(reportExcelExportService, times(1)).exportLatestByReportId(1L);
    }

    @Test
    void shouldHandleCreateReportWithNullName() throws Exception {
        Report invalidReport = new Report(1L, null, "SELECT * FROM test", "Description");

        doThrow(new RuntimeException("Name cannot be null"))
            .when(reportService).createReport(any(Report.class));

        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReport)))
               .andExpect(status().isInternalServerError());

        verify(reportService, times(1)).createReport(any(Report.class));
    }

    @Test
    void shouldHandleGenerateReportNotFound() throws Exception {
        when(reportService.generateReport(anyLong(), anyString()))
            .thenThrow(new RuntimeException("Report not found"));

        Map<String, Object> request = Map.of(
            "reportId", 999L,
            "params", "WHERE id = 1"
        );

        mockMvc.perform(post("/api/reports/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isInternalServerError());

        verify(reportService, times(1)).generateReport(999L, "WHERE id = 1");
    }

    @Test
    void shouldHandleRunReportSqlError() throws Exception {
        when(reportService.runReport(anyString()))
            .thenThrow(new RuntimeException("SQL syntax error"));

        Map<String, String> request = Map.of("sql", "INVALID SQL");

        mockMvc.perform(post("/api/reports/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isInternalServerError());

        verify(reportService, times(1)).runReport("INVALID SQL");
    }
}

// Basic Report Template
// Usage: Replace placeholders with your specific report requirements

package com.legacy.report.service;

import com.legacy.report.model.Report;
import com.legacy.report.dao.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class {{ReportName}}Service {
    
    @Autowired
    private ReportDao reportDao;
    
    @Autowired
    private ReportValidationService validationService;
    
    @Autowired
    private ReportPerformanceService performanceService;
    
    /**
     * Create new {{ReportName}} report
     */
    public Report create{{ReportName}}Report() {
        // Validate business requirements
        validationService.validateReportRequirements("{{ReportName}}");
        
        Report report = new Report();
        report.setName("{{ReportDisplayName}}");
        report.setSql(get{{ReportName}}Sql());
        report.setDescription("{{ReportDescription}}");
        
        // Performance check
        performanceService.validateQueryPerformance(get{{ReportName}}Sql());
        
        return reportDao.save(report);
    }
    
    /**
     * Execute {{ReportName}} report
     */
    public List<Map<String, Object>> execute{{ReportName}}Report() {
        String sql = get{{ReportName}}Sql();
        
        // Security validation
        validationService.validateSqlSecurity(sql);
        
        // Performance monitoring
        return performanceService.executeWithMonitoring(sql, "{{ReportName}}");
    }
    
    /**
     * Get {{ReportName}} SQL query
     */
    private String get{{ReportName}}Sql() {
        return """
            {{SQL_QUERY}}
            """;
    }
    
    /**
     * Validate {{ReportName}} specific business rules
     */
    public void validate{{ReportName}}Rules(Map<String, Object> parameters) {
        // Add specific validation logic here
        validationService.validateBusinessRules("{{ReportName}}", parameters);
    }
    
    /**
     * Export {{ReportName}} to Excel
     */
    public byte[] export{{ReportName}}ToExcel() {
        List<Map<String, Object>> data = execute{{ReportName}}Report();
        return excelExportService.exportToExcel(data, "{{ReportDisplayName}}");
    }
}

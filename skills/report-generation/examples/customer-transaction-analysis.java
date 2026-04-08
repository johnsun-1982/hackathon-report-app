// Example: Customer Transaction Analysis Report
// This is a complete working example of the report generation skill

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
public class CustomerTransactionAnalysisService {
    
    @Autowired
    private ReportDao reportDao;
    
    @Autowired
    private ReportValidationService validationService;
    
    @Autowired
    private ReportPerformanceService performanceService;
    
    /**
     * Create Customer Transaction Analysis report
     */
    public Report createCustomerTransactionAnalysisReport() {
        // Validate business requirements
        validationService.validateReportRequirements("CustomerTransactionAnalysis");
        
        Report report = new Report();
        report.setName("Customer Transaction Analysis");
        report.setSql(getCustomerTransactionAnalysisSql());
        report.setDescription("Analysis of customer transaction patterns and volumes");
        
        // Performance check
        performanceService.validateQueryPerformance(getCustomerTransactionAnalysisSql());
        
        return reportDao.save(report);
    }
    
    /**
     * Execute Customer Transaction Analysis report
     */
    public List<Map<String, Object>> executeCustomerTransactionAnalysisReport() {
        String sql = getCustomerTransactionAnalysisSql();
        
        // Security validation
        validationService.validateSqlSecurity(sql);
        
        // Performance monitoring
        return performanceService.executeWithMonitoring(sql, "CustomerTransactionAnalysis");
    }
    
    /**
     * Get Customer Transaction Analysis SQL query
     */
    private String getCustomerTransactionAnalysisSql() {
        return """
            SELECT 
                c.name as customer_name,
                c.type as customer_type,
                c.status as customer_status,
                c.credit_score,
                COUNT(t.id) as transaction_count,
                COALESCE(SUM(t.amount), 0) as total_amount,
                COALESCE(AVG(t.amount), 0) as avg_transaction_amount,
                COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as total_income,
                COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as total_expense,
                (COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) - 
                 COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0)) as net_profit
            FROM customer c 
            LEFT JOIN transaction t ON c.id = t.customer_id 
            WHERE t.status = 'SUCCESS' OR t.status IS NULL
            GROUP BY c.id, c.name, c.type, c.status, c.credit_score
            ORDER BY total_amount DESC
            """;
    }
    
    /**
     * Validate Customer Transaction Analysis specific business rules
     */
    public void validateCustomerTransactionAnalysisRules(Map<String, Object> parameters) {
        // Add specific validation logic here
        validationService.validateBusinessRules("CustomerTransactionAnalysis", parameters);
        
        // Ensure we have customer data
        if (parameters.containsKey("require_customer_data") && 
            Boolean.TRUE.equals(parameters.get("require_customer_data"))) {
            validationService.validateCustomerDataExists();
        }
    }
    
    /**
     * Export Customer Transaction Analysis to Excel
     */
    public byte[] exportCustomerTransactionAnalysisToExcel() {
        List<Map<String, Object>> data = executeCustomerTransactionAnalysisReport();
        return excelExportService.exportToExcel(data, "Customer Transaction Analysis");
    }
    
    /**
     * Get customer segment analysis
     */
    public List<Map<String, Object>> getCustomerSegmentAnalysis() {
        String segmentSql = """
            SELECT 
                c.type as customer_segment,
                COUNT(DISTINCT c.id) as customer_count,
                COUNT(t.id) as total_transactions,
                COALESCE(SUM(t.amount), 0) as total_volume,
                COALESCE(AVG(t.amount), 0) as avg_transaction
            FROM customer c 
            LEFT JOIN transaction t ON c.id = t.customer_id 
            WHERE t.status = 'SUCCESS' OR t.status IS NULL
            GROUP BY c.type
            ORDER BY total_volume DESC
            """;
        
        return performanceService.executeWithMonitoring(segmentSql, "CustomerSegmentAnalysis");
    }
    
    /**
     * Get high-value customers (top 10 by transaction volume)
     */
    public List<Map<String, Object>> getHighValueCustomers() {
        String highValueSql = """
            SELECT 
                c.name as customer_name,
                c.type as customer_type,
                c.credit_score,
                COUNT(t.id) as transaction_count,
                COALESCE(SUM(t.amount), 0) as total_amount
            FROM customer c 
            LEFT JOIN transaction t ON c.id = t.customer_id 
            WHERE t.status = 'SUCCESS' OR t.status IS NULL
            GROUP BY c.id, c.name, c.type, c.credit_score
            HAVING COALESCE(SUM(t.amount), 0) > 0
            ORDER BY total_amount DESC
            LIMIT 10
            """;
        
        return performanceService.executeWithMonitoring(highValueSql, "HighValueCustomers");
    }
}

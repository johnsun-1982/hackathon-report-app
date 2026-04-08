// Parameterized Report Template
// Usage: For reports that accept dynamic parameters

package com.legacy.report.service;

import com.legacy.report.model.Report;
import com.legacy.report.dao.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class {{ReportName}}ParameterizedService {
    
    @Autowired
    private ReportDao reportDao;
    
    @Autowired
    private ReportValidationService validationService;
    
    @Autowired
    private ReportPerformanceService performanceService;
    
    /**
     * Execute {{ReportName}} report with parameters
     */
    public List<Map<String, Object>> execute{{ReportName}}Report(Map<String, Object> parameters) {
        // Validate parameters
        validate{{ReportName}}Parameters(parameters);
        
        // Build parameterized SQL
        String sql = buildParameterizedSql(parameters);
        
        // Security validation
        validationService.validateSqlSecurity(sql);
        
        // Execute with parameters
        return reportDao.executeParameterizedQuery(sql, getParameterValues(parameters));
    }
    
    /**
     * Build parameterized SQL query
     */
    private String buildParameterizedSql(Map<String, Object> parameters) {
        String baseSql = get{{ReportName}}BaseSql();
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        Map<String, Object> paramValues = new HashMap<>();
        
        // Add parameter conditions
        {{PARAMETER_CONDITIONS}}
        
        return baseSql + whereClause.toString();
    }
    
    /**
     * Get parameter values for prepared statement
     */
    private Object[] getParameterValues(Map<String, Object> parameters) {
        List<Object> values = new ArrayList<>();
        
        {{PARAMETER_VALUES}}
        
        return values.toArray();
    }
    
    /**
     * Validate {{ReportName}} specific parameters
     */
    private void validate{{ReportName}}Parameters(Map<String, Object> parameters) {
        // Required parameters
        {{REQUIRED_PARAMETERS_VALIDATION}}
        
        // Parameter type validation
        {{TYPE_VALIDATION}}
        
        // Parameter range validation
        {{RANGE_VALIDATION}}
    }
    
    /**
     * Get base SQL query
     */
    private String get{{ReportName}}BaseSql() {
        return """
            {{BASE_SQL_QUERY}}
            """;
    }
    
    /**
     * Get available parameters for {{ReportName}}
     */
    public Map<String, ParameterDefinition> get{{ReportName}}Parameters() {
        Map<String, ParameterDefinition> parameters = new HashMap<>();
        
        {{PARAMETER_DEFINITIONS}}
        
        return parameters;
    }
    
    /**
     * Parameter definition class
     */
    public static class ParameterDefinition {
        private String name;
        private String type;
        private boolean required;
        private String description;
        private Object defaultValue;
        
        // Constructor and getters
        public ParameterDefinition(String name, String type, boolean required, String description, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.description = description;
            this.defaultValue = defaultValue;
        }
        
        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public boolean isRequired() { return required; }
        public String getDescription() { return description; }
        public Object getDefaultValue() { return defaultValue; }
    }
}

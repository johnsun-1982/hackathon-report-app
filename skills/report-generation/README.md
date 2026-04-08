# Report Generation Skill

## Overview
This skill enables the creation of new reports with custom SQL queries, business logic, and complete integration with the existing report system.

## Capabilities
- Create new report configurations
- Add dynamic parameters to reports
- Implement report execution with security validation
- Generate comprehensive tests
- Performance optimization
- Excel export functionality

## Usage

### 1. Create New Report
```bash
# Use the report generator
./skills/report-generation/scripts/generate-report.sh "Customer Analysis Report"
```

### 2. Add Report with SQL
```java
// Use the ReportService
Report report = new Report();
report.setName("Customer Analysis");
report.setSql("SELECT c.name, COUNT(t.id) as transaction_count FROM customer c LEFT JOIN transaction t ON c.id = t.customer_id WHERE c.status = 'ACTIVE' GROUP BY c.id");
report.setDescription("Analysis of active customer transaction patterns");
reportService.createReport(report);
```

### 3. Execute Report
```bash
# Test the report
curl -X POST http://localhost:8080/api/reports/execute \
  -H "Content-Type: application/json" \
  -d '{"reportId": 1, "parameters": {}}'
```

## Templates

### Basic Report Template
See `templates/basic-report.java` for the standard report structure.

### Parameterized Report Template
See `templates/parameterized-report.java` for reports with dynamic parameters.

### Performance-Optimized Report Template
See `templates/optimized-report.java` for high-performance reports.

## Quality Assurance
- SQL syntax validation
- Performance benchmarking
- Security vulnerability scanning
- Unit test generation
- Integration test creation

## Automation
- Automatic test generation
- Performance monitoring
- Security validation
- Documentation updates

## Examples
See `examples/` directory for complete working examples of different report types.

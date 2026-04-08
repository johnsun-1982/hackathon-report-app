# change management guide

## overview

this document provides a systematic approach to managing changes in the report system, ensuring minimal disruption and maximum traceability.

## change classification

### type 1: schema changes
**impact**: high
**approval required**: technical lead + business owner
**timeline**: 2-4 weeks

#### subcategories:
- **type 1a**: table structure changes (add/drop columns, change data types)
- **type 1b**: constraint changes (primary keys, foreign keys, unique constraints)
- **type 1c**: index changes (add/remove/modify indexes)

### type 2: business logic changes
**impact**: medium
**approval required**: technical lead
**timeline**: 1-2 weeks

#### subcategories:
- **type 2a**: report sql modifications
- **type 2b**: calculation formula changes
- **type 2c**: filtering logic updates
- **type 2d**: approval workflow changes

### type 3: configuration changes
**impact**: low
**approval required**: team lead
**timeline**: 3-5 days

#### subcategories:
- **type 3a**: report metadata updates
- **type 3b**: ui/display changes
- **type 3c**: performance tuning

## change request process

### step 1: change initiation
1. **create change request** document
2. **identify affected components** using dependency matrix
3. **assess impact level** (low/medium/high)
4. **estimate effort** and timeline

### step 2: impact analysis
1. **technical impact assessment**
   - affected reports (using data_dependency.md)
   - database performance implications
   - api compatibility issues

2. **business impact assessment**
   - report result changes
   - user workflow impact
   - data consistency concerns

3. **risk assessment**
   - data migration risks
   - rollback complexity
   - testing requirements

### step 3: design phase
1. **solution design**
   - detailed implementation plan
   - rollback strategy
   - testing approach

2. **review and approval**
   - technical review
   - business review
   - security review (if applicable)

### step 4: implementation

#### 4.1: pre-change validation
1. **compilation check**
   - run full project compilation: `./gradlew compileJava`
   - verify no compilation errors or warnings
   - check all affected modules compile successfully
   - validate dependency resolution

2. **unit testing**
   - run all unit tests: `./gradlew test`
   - ensure 100% test pass rate
   - validate test coverage meets requirements
   - check no regression in existing functionality

3. **change proposal**
   - document all planned modifications in detail
   - identify affected components and dependencies
   - assess risk level and impact scope
   - **require explicit user approval before proceeding**

4. **approval workflow**
   - present change proposal to user for review
   - wait for explicit confirmation/approval
   - document approval decision and timestamp
   - only proceed with implementation after approval

#### 4.2: development (after approval)
1. **implementation**
   - implement approved changes only
   - follow coding standards and best practices
   - make incremental changes with frequent commits
   - update documentation simultaneously

2. **unit testing**
   - write comprehensive unit tests for new code
   - run tests after each significant change
   - ensure all edge cases are covered
   - maintain test coverage above 80%

3. **code review**
   - self-review code for quality and standards
   - check for security vulnerabilities
   - validate performance implications
   - ensure documentation is complete

#### 4.3: integration testing
1. **system integration**
   - test affected reports end-to-end
   - validate data consistency across modules
   - check API compatibility
   - test user workflows

2. **performance testing**
   - measure performance impact of changes
   - compare against baseline metrics
   - validate memory and CPU usage
   - test under load conditions

### step 5: deployment
1. **pre-deployment checks**
   - backup current state
   - validate deployment scripts
   - prepare rollback plan

2. **deployment execution**
   - schedule maintenance window
   - execute deployment
   - validate deployment

3. **post-deployment validation**
   - run smoke tests
   - validate report results
   - monitor system performance

## specific change guidelines

### schema changes

#### adding new columns
```sql
-- example: adding new customer field
-- 1. assess impact using dependency matrix
-- 2. add column with default value
ALTER TABLE customer ADD COLUMN loyalty_points INT DEFAULT 0;

-- 3. update affected reports
-- check reports: 1, 2, 6, 10

-- 4. update documentation
-- update data_dependency.md
-- update sql_analysis.md
```

#### modifying existing columns
```sql
-- example: extending customer.name length
-- 1. assess impact: reports 1, 2, 6, 10
-- 2. check data constraints
ALTER TABLE customer MODIFY COLUMN name VARCHAR(200);

-- 3. validate affected reports
-- run reports 1, 2, 6, 10 and compare results
```

#### changing data types
```sql
-- example: decimal precision change
-- 1. high impact - affects calculations
-- 2. backup data first
CREATE TABLE transaction_backup AS SELECT * FROM transaction;

-- 3. modify column
ALTER TABLE transaction MODIFY COLUMN amount DECIMAL(20,4);

-- 4. validate calculations in reports 1, 2, 3, 5, 7, 10, 11, 12
```

### business logic changes

#### report sql modifications
```sql
-- example: adding new filter condition
-- before:
SELECT ... FROM customer c LEFT JOIN transaction t ON c.id = t.customer_id WHERE t.status = 'SUCCESS'

-- after:
SELECT ... FROM customer c LEFT JOIN transaction t ON c.id = t.customer_id 
WHERE t.status = 'SUCCESS' AND t.transaction_date >= '2024-01-01'

-- impact assessment:
- reports affected: 1, 2, 3, 6, 7, 10, 12
- test cases: validate date filtering
- documentation: update sql_analysis.md
```

#### calculation formula changes
```sql
-- example: changing profit calculation
-- before:
(SUM(oi.total_price) - (p.cost * SUM(oi.quantity))) as total_profit

-- after:
(SUM(oi.total_price) * 0.95 - (p.cost * SUM(oi.quantity))) as total_profit

-- impact assessment:
- reports affected: 5, 11
- business validation: 5% fee calculation
- test cases: compare before/after results
```

#### approval workflow changes
```java
-- example: implementing two-level approval
-- before: single-level approval
public ReportRun decideRun(Long runId, boolean approve, String comment) {
    // Single approval logic
    run.setStatus(approve ? "Approved" : "Rejected");
    run.setCheckerUsername(currentUser.getUsername());
    return reportRunRepository.save(run);
}

-- after: two-level approval
public ReportRun firstLevelApprove(Long runId, boolean approve, String comment) {
    // First level approval logic
    if (approve) {
        run.setStatus("L1Approved");
        run.setFirstApproverUsername(currentUser.getUsername());
        run.setFirstApprovalTime(LocalDateTime.now());
    } else {
        run.setStatus("L1Rejected");
        run.setFirstApproverUsername(currentUser.getUsername());
        run.setFirstApprovalTime(LocalDateTime.now());
    }
    return reportRunRepository.save(run);
}

public ReportRun secondLevelApprove(Long runId, boolean approve, String comment) {
    // Second level approval logic
    if ("L1Approved".equals(run.getStatus())) {
        if (approve) {
            run.setStatus("Approved");
            run.setSecondApproverUsername(currentUser.getUsername());
            run.setSecondApprovalTime(LocalDateTime.now());
        } else {
            run.setStatus("L2Rejected");
            run.setSecondApproverUsername(currentUser.getUsername());
            run.setSecondApprovalTime(LocalDateTime.now());
        }
    }
    return reportRunRepository.save(run);
}

-- impact assessment:
- affected components: ReportService, ReportController, ReportRun entity
- database changes: add approval fields to report_run table
- ui changes: approval workflow interface
- testing: comprehensive approval workflow testing
```

### configuration changes

#### adding new reports
```sql
-- example: adding new report
INSERT INTO report_config (name, sql, description) VALUES 
('New Customer Analysis', 
 'SELECT c.name, COUNT(t.id) as transaction_count FROM customer c LEFT JOIN transaction t ON c.id = t.customer_id GROUP BY c.id, c.name',
 'New customer transaction count analysis');

-- impact assessment:
- new report - no existing impact
- testing: validate sql syntax and results
- documentation: update sql_analysis.md
```

## testing strategy

### unit testing
```java
// example: testing report sql changes
@Test
public void testCustomerTransactionAnalysisWithNewFilter() {
    // setup test data
    createTestData();
    
    // execute report with new logic
    List<Map<String, Object>> results = reportService.runReport(updatedSql);
    
    // validate results
    assertThat(results).hasSize(expectedSize);
    assertThat(results.get(0).get("total_amount")).isEqualTo(expectedAmount);
}
```

### integration testing
```java
// example: testing end-to-end report execution
@Test
public void testReportExecutionWithSchemaChange() {
    // 1. execute report before change
    List<Map<String, Object>> beforeResults = reportController.executeReport(reportId);
    
    // 2. apply schema change
    applySchemaChange();
    
    // 3. execute report after change
    List<Map<String, Object>> afterResults = reportController.executeReport(reportId);
    
    // 4. validate consistency
    validateResultConsistency(beforeResults, afterResults);
}
```

### regression testing
```bash
# automated regression test script
#!/bin/bash

# run all affected reports
reports=(1 2 3 6 7 10 12)  # transaction.status change impact

for report_id in "${reports[@]}"; do
    echo "Testing report $report_id..."
    curl -X POST "http://localhost:8080/api/reports/$report_id/execute" \
         -H "Content-Type: application/json" \
         -d '{}' > "results_$report_id.json"
    
    # validate results
    python validate_results.py "results_$report_id.json" "expected_$report_id.json"
done
```

## rollback procedures

### schema rollback
```sql
-- example: rolling back column addition
-- 1. identify rollback script
ALTER TABLE customer DROP COLUMN loyalty_points;

-- 2. validate rollback
SELECT COUNT(*) FROM customer WHERE loyalty_points IS NOT NULL; -- should be 0

-- 3. restart application services
```

### business logic rollback
```java
// example: rolling back report sql change
// 1. restore previous sql version
String previousSql = getPreviousSqlVersion(reportId);
updateReportSql(reportId, previousSql);

// 2. clear application cache
cacheManager.clearCache("report_" + reportId);

// 3. validate rollback
List<Map<String, Object>> results = reportService.runReport(reportId);
validateRollbackResults(results);
```

## monitoring and alerting

### change monitoring
```yaml
# application.yml monitoring configuration
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  metrics:
    export:
      prometheus:
        enabled: true
  endpoint:
    health:
      show-details: always
```

### alerting rules
```yaml
# prometheus alerting rules
groups:
  - name: report-system
    rules:
      - alert: reportexecutionfailure
        expr: rate(report_execution_errors_total[5m]) > 0.1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Report execution failure rate high"
          
      - alert: reportperformance
        expr: histogram_quantile(0.95, rate(report_execution_duration_seconds_bucket[5m])) > 30
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Report execution performance degraded"
```

## documentation requirements

### pre-change documentation
- change request details
- impact assessment results
- implementation plan
- testing strategy
- rollback plan

### post-change documentation
- change summary
- test results
- performance impact
- lessons learned
- updated dependencies

### ongoing documentation
- change log updates
- dependency matrix updates
- performance baselines
- known issues

## approval matrix

| change type | approver(s) | documentation | testing | rollback plan |
|-------------|-------------|---------------|---------|---------------|
| type 1a (table structure) | tech lead + business owner | detailed | comprehensive | mandatory |
| type 1b (constraints) | tech lead | detailed | comprehensive | mandatory |
| type 1c (indexes) | tech lead | standard | standard | optional |
| type 2a (report sql) | tech lead | standard | comprehensive | mandatory |
| type 2b (calculations) | tech lead + business owner | detailed | comprehensive | mandatory |
| type 2c (filtering) | tech lead | standard | standard | optional |
| type 2d (approval workflow) | tech lead + business owner | detailed | comprehensive | mandatory |
| type 3a (metadata) | team lead | minimal | basic | optional |
| type 3b (ui/display) | team lead | minimal | basic | optional |
| type 3c (performance) | tech lead | standard | standard | optional |

## communication plan

### change announcement
- **timing**: 1 week before implementation
- **audience**: affected users and stakeholders
- **content**: change description, impact, timeline, contact info

### change notification
- **timing**: 24 hours before implementation
- **audience**: all users
- **content**: maintenance window, expected downtime, alternative procedures

### post-change communication
- **timing**: immediately after completion
- **audience**: all users
- **content**: change completion, new features, known issues, support contacts

## quality gates

### pre-deployment gates
- [ ] all tests pass
- [ ] code review completed
- [ ] documentation updated
- [ ] backup completed
- [ ] rollback plan validated

### post-deployment gates
- [ ] smoke tests pass
- [ ] performance metrics within limits
- [ ] error rates below threshold
- [ ] user acceptance validated
- [ ] monitoring active

## continuous improvement

### metrics collection
- change success rate
- rollback frequency
- deployment time
- defect density
- user satisfaction

### process improvement
- quarterly process review
- stakeholder feedback collection
- best practice documentation
- tooling enhancements

## code modification workflow

### mandatory pre-change validation

#### 1. compilation verification
- **required command**: `./gradlew compileJava`
- **success criteria**: zero compilation errors, zero warnings
- **verification steps**:
  1. Clean build: `./gradlew clean`
  2. Full compilation: `./gradlew compileJava`
  3. Check all affected modules
  4. Validate dependency resolution
- **blocking condition**: must pass 100% before proceeding

#### 2. unit testing validation
- **required command**: `./gradlew test`
- **success criteria**: 100% test pass rate
- **verification steps**:
  1. Run all unit tests
  2. Check test coverage (>80% required)
  3. Validate no regression in existing functionality
  4. Review test results for failures
- **blocking condition**: must pass 100% before proceeding

#### 3. change proposal documentation
- **required content**:
  1. Detailed description of planned changes
  2. List of affected files and components
  3. Risk assessment (low/medium/high)
  4. Implementation timeline and effort estimate
  5. Rollback strategy and test plan
- **format**: Structured markdown document

#### 4. explicit user approval
- **required step**: Present change proposal to user
- **approval methods**:
  - Written confirmation in chat/dialog
  - Explicit "approve" or "reject" response
  - Approval timestamp documentation
- **blocking condition**: Must receive explicit user approval before implementation

### implementation phase (after approval)

#### 5. approved implementation
- **requirement**: Implement only approved changes
- **process**:
  1. Follow approved scope exactly
  2. Adhere to coding standards
  3. Update documentation simultaneously
  4. Commit changes with clear messages

#### 6. post-implementation validation
- **required steps**:
  1. Re-run compilation check
  2. Re-run unit tests
  3. Verify 100% success rate
  4. Document actual vs planned changes

### compliance requirements

#### mandatory checks before any code change
- [ ] `./gradlew compileJava` passes without errors
- [ ] `./gradlew test` passes 100%
- [ ] Change proposal documented and reviewed
- [ ] Explicit user approval received
- [ ] Rollback plan prepared

#### prohibited actions
- **NEVER** implement code changes without compilation verification
- **NEVER** proceed without unit testing validation
- **NEVER** implement without explicit user approval
- **NEVER** deploy without all compliance checks passed

## conclusion

this change management framework ensures:
- **systematic approach** to all changes
- **minimal disruption** to business operations
- **complete traceability** of all modifications
- **rapid recovery** from issues
- **continuous improvement** of the process

by following this guide, the team can:
- reduce change-related incidents by 80%
- improve deployment confidence
- maintain system stability
- provide clear audit trails
- support business agility
- **ensure code quality through mandatory validation**
- **prevent unauthorized changes through explicit approval workflow**

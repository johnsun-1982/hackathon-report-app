#  data dependency and impact analysis

## table dependency mapping

### core business tables and their relationships

#### customer table
**table**: `customer`
**primary key**: `id`
**fields**: `name`, `type`, `status`, `email`, `phone`, `address`, `registration_date`, `credit_score`, `account_balance`, `create_time`

**dependent tables**:
- `transaction.customer_id` (foreign key)

**affected reports**:
1. **report 1**: customer transaction analysis - uses `c.name`, `c.type`, `c.credit_score`
2. **report 2**: vip customer revenue report - uses `c.name`, `c.email`, `c.account_balance`, `c.type`
3. **report 6**: customer segmentation analysis - uses all customer fields for segmentation
4. **report 10**: customer-merchant revenue matrix - uses `c.name`

#### transaction table
**table**: `transaction`
**primary key**: `id`
**foreign keys**: `customer_id`, `merchant_id`
**fields**: `amount`, `type`, `status`, `category`, `description`, `transaction_date`, `reference_number`, `merchant_id`, `create_time`

**dependent tables**: none (leaf table)

**affected reports**:
1. **report 1**: customer transaction analysis - core data source
2. **report 2**: vip customer revenue report - income/expense calculations
3. **report 3**: merchant performance analysis - transaction volume analysis
4. **report 6**: customer segmentation analysis - transaction behavior analysis
5. **report 7**: monthly revenue trend analysis - time-based analysis
6. **report 10**: customer-merchant revenue matrix - cross-analysis
7. **report 12**: financial health scorecard - aggregated metrics

#### merchant table
**table**: `merchant`
**primary key**: `id`
**fields**: `name`, `category`, `status`, `commission_rate`, `create_time`

**dependent tables**:
- `transaction.merchant_id` (foreign key)

**affected reports**:
1. **report 3**: merchant performance analysis - main subject
2. **report 10**: customer-merchant revenue matrix - cross-analysis

#### product table
**table**: `product`
**primary key**: `id`
**fields**: `name`, `category`, `price`, `cost`, `stock_quantity`, `supplier_id`, `create_time`

**dependent tables**:
- `order_items.product_id` (foreign key)

**affected reports**:
1. **report 5**: product profitability report - main subject
2. **report 11**: inventory velocity analysis - stock analysis

#### orders table
**table**: `orders`
**primary key**: `id`
**foreign keys**: `customer_id`
**fields**: `order_date`, `total_amount`, `status`, `shipping_address`, `create_time`

**dependent tables**:
- `order_items.order_id` (foreign key)

**affected reports**:
1. **report 8**: order fulfillment analysis - main subject

#### order_items table
**table**: `order_items`
**primary key**: `id`
**foreign keys**: `order_id`, `product_id`
**fields**: `quantity`, `unit_price`, `total_price`, `create_time`

**dependent tables**: none (junction table)

**affected reports**:
1. **report 5**: product profitability report - sales data
2. **report 11**: inventory velocity analysis - sales data

#### department table
**table**: `department`
**primary key**: `id`
**fields**: `name`, `manager`, `budget`, `location`, `create_time`

**dependent tables**:
- `employee.department_id` (foreign key)

**affected reports**:
1. **report 4**: department budget analysis - main subject
2. **report 9**: employee performance metrics - budget reference

#### employee table
**table**: `employee`
**primary key**: `id`
**foreign keys**: `department_id`
**fields**: `name`, `email`, `position`, `salary`, `hire_date`, `status`, `create_time`

**dependent tables**: none

**affected reports**:
1. **report 4**: department budget analysis - salary aggregation
2. **report 9**: employee performance metrics - main subject

## field-level impact analysis

### high-impact fields (changes affect multiple reports)

#### customer.type
**reports affected**: 2, 6
**impact level**: high
**change considerations**:
- vip filtering logic in report 2
- customer segmentation logic in report 6
- business rule validation needed

#### transaction.type
**reports affected**: 2, 6, 7, 12
**impact level**: high
**change considerations**:
- income/expense classification in multiple reports
- financial calculations in report 2 and 12
- trend analysis in report 7

#### transaction.status
**reports affected**: 1, 2, 3, 6, 7, 10, 12
**impact level**: high
**change considerations**:
- filtering logic in almost all transaction-based reports
- success status is primary filter condition
- new statuses may require filter updates

#### product.price / product.cost
**reports affected**: 5, 11
**impact level**: medium
**change considerations**:
- profit calculations in report 5
- margin calculations in both reports
- historical data consistency

#### employee.salary
**reports affected**: 4, 9
**impact level**: medium
**change considerations**:
- budget calculations in report 4
- performance metrics in report 9
- percentage calculations

### medium-impact fields

#### customer.credit_score
**reports affected**: 1, 6
**impact level**: medium
**usage**: display and potential segmentation

#### merchant.commission_rate
**reports affected**: 3
**impact level**: medium
**usage**: commission estimation

#### department.budget
**reports affected**: 4, 9
**impact level**: medium
**usage**: budget variance and utilization

## report dependency matrix

| table | report 1 | report 2 | report 3 | report 4 | report 5 | report 6 | report 7 | report 8 | report 9 | report 10 | report 11 | report 12 |
|-------|----------|----------|----------|----------|----------|----------|----------|----------|----------|-----------|-----------|-----------|
| customer | x | x | | | | x | | | | x | | |
| transaction | x | x | x | | | x | x | | | x | | x |
| merchant | | | x | | | | | | | x | | |
| product | | | | | x | | | | | | x | |
| orders | | | | | | | | x | | | | |
| order_items | | | | | x | | | | | | x | |
| department | | | | x | | | | | x | | | |
| employee | | | | x | | | | | x | | | |

## change impact assessment framework

### field change impact levels

#### level 1: critical (affects core business logic)
- primary key fields
- foreign key fields
- status/type fields used in filtering

#### level 2: high (affects calculations)
- amount/price/cost fields
- rate/percentage fields
- date fields used in time analysis

#### level 3: medium (affects display and grouping)
- name/description fields
- category fields
- address/contact fields

#### level 4: low (affects audit and logging)
- create_time/timestamp fields
- reference numbers
- optional descriptive fields

### change validation checklist

#### before change
1. identify all affected reports using dependency matrix
2. review business logic dependencies
3. check data migration requirements
4. assess performance impact

#### after change
1. run all affected reports and verify results
2. validate business calculations
3. check ui/display implications
4. update documentation

## specific change scenarios

### scenario 1: adding new customer type
**affected reports**: 2, 6
**validation needed**:
- vip filtering logic in report 2
- segmentation logic in report 6
- business rule updates

### scenario 2: changing transaction status values
**affected reports**: 1, 2, 3, 6, 7, 10, 12
**validation needed**:
- all filtering conditions
- success status assumptions
- historical data consistency

### scenario 3: modifying product pricing structure
**affected reports**: 5, 11
**validation needed**:
- profit calculation formulas
- margin percentage calculations
- historical price data handling

### scenario 4: updating department budget fields
**affected reports**: 4, 9
**validation needed**:
- budget variance calculations
- utilization percentage formulas
- salary aggregation logic

## automated impact detection

### sql analysis queries

#### find reports using specific table
```sql
-- find all reports using customer table
SELECT name, sql, description 
FROM report_config 
WHERE sql LIKE '%customer%' 
  AND sql LIKE '%FROM%'
  AND is_deleted = 0;
```

#### find reports using specific field
```sql
-- find all reports using customer.type field
SELECT name, sql, description 
FROM report_config 
WHERE sql LIKE '%customer.type%' 
  OR sql LIKE '%c.type%'
  AND is_deleted = 0;
```

#### find reports with specific patterns
```sql
-- find reports with income/expense logic
SELECT name, sql, description 
FROM report_config 
WHERE sql LIKE '%INCOME%' 
  OR sql LIKE '%EXPENSE%'
  AND is_deleted = 0;
```

### code analysis tools

#### java code dependency mapping
```bash
# find all java files referencing customer table
grep -r "customer" src/main/java/ --include="*.java"

# find all files using transaction.status
grep -r "transaction.status\|t.status" src/main/java/ --include="*.java"
```

## maintenance recommendations

### regular dependency audits
1. quarterly review of report dependencies
2. update dependency matrix after schema changes
3. validate documentation accuracy

### change management process
1. mandatory impact assessment for schema changes
2. automated regression testing for affected reports
3. documentation updates before deployment

### monitoring and alerting
1. monitor report execution performance
2. alert on sql execution failures
3. track data quality issues

## conclusion

this dependency mapping provides:
- **quick impact assessment** for any field/table changes
- **systematic validation** process for modifications
- **reduced risk** of unintended side effects
- **clear documentation** for maintenance teams

when planning changes:
1. consult the dependency matrix
2. identify all affected reports

---

## SQL Query Analysis Integration

### Database Schema Overview

#### Core Business Tables
- **customer**: Customer basic information
- **transaction**: Customer transaction records
- **product**: Product information
- **order_items**: Order details
- **orders**: Order master table
- **merchant**: Merchant information
- **department**: Department information
- **employee**: Employee information
- **report_config**: Report configuration and SQL storage

### Report SQL Query Analysis

#### Report 1: Customer Transaction Analysis
**SQL Query**:
```sql
SELECT c.name, c.type, c.credit_score, 
       SUM(t.amount) as total_amount, 
       COUNT(t.id) as tx_count, 
       AVG(t.amount) as avg_transaction 
FROM customer c 
LEFT JOIN transaction t ON c.id = t.customer_id 
WHERE t.status = 'SUCCESS' 
GROUP BY c.id, c.name, c.type, c.credit_score 
ORDER BY total_amount DESC
```

**Query Logic**:
- **Join**: customer × transaction (one-to-many)
- **Filter**: Only successful transactions (status = 'SUCCESS')
- **Aggregate**: Group by customer to calculate totals, counts, averages
- **Sort**: Order by total transaction amount descending

**Business Value**: Analyze customer transaction behavior, identify high-value customers

#### Report 2: Product Sales Analysis
**SQL Query**:
```sql
SELECT p.name, p.category, p.price,
       SUM(oi.quantity) as total_sold,
       SUM(oi.quantity * p.price) as total_revenue
FROM product p
INNER JOIN order_items oi ON p.id = oi.product_id
INNER JOIN orders o ON oi.order_id = o.id
WHERE o.status = 'COMPLETED'
GROUP BY p.id, p.name, p.category, p.price
ORDER BY total_revenue DESC
```

**Query Logic**:
- **Joins**: product × order_items × orders
- **Filter**: Only completed orders
- **Aggregate**: Calculate total sold and revenue per product
- **Sort**: Order by revenue descending

**Business Value**: Identify best-selling products and revenue trends

### SQL Query Patterns

#### Common Patterns Identified

##### 1. Aggregation Pattern
```sql
SELECT [columns], 
       SUM([column]) as total,
       COUNT([column]) as count,
       AVG([column]) as average
FROM [table]
[JOIN conditions]
[WHERE conditions]
GROUP BY [grouping columns]
ORDER BY [ordering column]
```

##### 2. Filter Pattern
```sql
SELECT [columns]
FROM [table]
[JOIN conditions]
WHERE [field] = '[value]' OR [field] IN ([values])
GROUP BY [grouping columns]
```

### Query Dependency Analysis

#### High-Impact Queries
1. **Customer Transaction Analysis** - High dependency on customer and transaction tables
2. **Product Sales Analysis** - High dependency on product, order_items, and orders tables
3. **Merchant Performance** - High dependency on merchant and orders tables

#### Field-Level Dependencies
- **customer.status**: Affects 3 reports
- **transaction.status**: Affects 2 reports
- **orders.status**: Affects 2 reports
- **product.category**: Affects 1 report

### Change Impact Assessment Framework

#### SQL Query Change Impact
1. **Schema Changes**: Identify all affected queries
2. **Performance Impact**: Analyze query execution plans
3. **Business Logic Impact**: Verify business rules still apply
4. **Security Impact**: Ensure access controls remain effective

#### Query Modification Process
1. **Analyze Current Query**: Understand existing logic
2. **Identify Dependencies**: List all affected components
3. **Test New Query**: Verify results and performance
4. **Update Documentation**: Reflect changes in relevant docs
5. **Communicate Changes**: Notify stakeholders of impact
3. validate business logic impact
4. update test cases and documentation
5. perform regression testing

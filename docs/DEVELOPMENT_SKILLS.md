# Development Skills and Capabilities

## Overview

This document outlines the specific development skills and capabilities available for the report system, organized by functional areas. Each skill includes prerequisites, implementation approach, and quality assurance measures.

## Backend Development Skills

### 1. New Report Generation

#### Skill: Create Custom Report
**Capability**: Generate new reports with custom SQL queries and business logic

**Prerequisites**:
- Understanding of database schema (see SQL_ANALYSIS.md)
- Knowledge of business requirements
- SQL query writing skills

**Implementation Process**:
```java
// 1. Create Report Entity
Report newReport = new Report();
newReport.setName("Report Name");
newReport.setSql("SELECT ... FROM ... WHERE ...");
newReport.setDescription("Business description");

// 2. Save to Database
reportService.createReport(newReport);

// 3. Add to Report Config Table
// SQL: INSERT INTO report_config (name, sql, description) VALUES (...)
```

**Quality Checks**:
- [ ] SQL syntax validation
- [ ] Performance analysis (execution time < 30s)
- [ ] Security check (no SQL injection risks)
- [ ] Business logic validation
- [ ] Test coverage (unit + integration tests)

**Automation Support**:
- Auto-generate unit tests for new report
- Performance benchmarking
- Security vulnerability scanning
- Documentation generation

#### Skill: Report Parameterization
**Capability**: Add dynamic parameters to existing reports

**Implementation**:
```java
// Parameterized report execution
public List<Map<String, Object>> executeParameterizedReport(Long reportId, Map<String, Object> params) {
    Report report = reportService.getReportById(reportId);
    String sql = report.getSql();
    
    // Replace parameters safely
    String finalSql = replaceParameters(sql, params);
    
    return jdbcTemplate.queryForList(finalSql, params);
}
```

**Quality Assurance**:
- Parameter validation
- SQL injection prevention
- Type checking
- Default value handling

### 2. Module Development

#### Skill: Add New Business Module
**Capability**: Create complete business modules with full CRUD operations

**Module Structure**:
```
src/main/java/com/legacy/report/newmodule/
    NewModuleController.java
    NewModuleService.java
    NewModuleRepository.java
    NewModule.java (entity)
    NewModuleDto.java
```

**Implementation Template**:
```java
// Controller
@RestController
@RequestMapping("/api/newmodule")
public class NewModuleController {
    @Autowired
    private NewModuleService service;
    
    @GetMapping
    public List<NewModule> getAll() { return service.findAll(); }
    
    @PostMapping
    public NewModule create(@RequestBody @Valid NewModuleDto dto) {
        return service.create(dto);
    }
    
    @PutMapping("/{id}")
    public NewModule update(@PathVariable Long id, @RequestBody @Valid NewModuleDto dto) {
        return service.update(id, dto);
    }
    
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}

// Service
@Service
@Transactional
public class NewModuleService {
    @Autowired
    private NewModuleRepository repository;
    
    public List<NewModule> findAll() { return repository.findAll(); }
    
    public NewModule create(NewModuleDto dto) {
        NewModule entity = convertToEntity(dto);
        return repository.save(entity);
    }
    
    // Additional business methods...
}

// Repository
@Repository
public interface NewModuleRepository extends JpaRepository<NewModule, Long> {
    // Custom queries as needed
}
```

**Quality Requirements**:
- [ ] Full CRUD operations
- [ ] Input validation
- [ ] Error handling
- [ ] Transaction management
- [ ] Security annotations
- [ ] Unit tests (80% coverage)
- [ ] Integration tests
- [ ] API documentation

#### Skill: Database Module Integration
**Capability**: Add new database tables and integrate with existing system

**Database Changes**:
```sql
-- Create new table
CREATE TABLE new_module (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Add indexes
CREATE INDEX idx_new_module_status ON new_module(status);
CREATE INDEX idx_new_module_name ON new_module(name);
```

**Integration Steps**:
1. Update schema.sql
2. Create JPA entity
3. Create repository
4. Implement service layer
5. Add REST endpoints
6. Update security configuration
7. Add tests

### 3. API Development

#### Skill: REST API Enhancement
**Capability**: Extend existing APIs with new endpoints and functionality

**API Design Patterns**:
```java
// Standard CRUD API
GET    /api/resource        - List all resources
GET    /api/resource/{id}  - Get single resource
POST   /api/resource        - Create resource
PUT    /api/resource/{id}  - Update resource
DELETE /api/resource/{id}  - Delete resource

// Custom business APIs
POST   /api/resource/{id}/action    - Business action
GET    /api/resource/search         - Search functionality
GET    /api/resource/export          - Export functionality
```

**Quality Standards**:
- RESTful design principles
- Consistent error handling
- Input validation
- HTTP status codes
- API documentation (Swagger/OpenAPI)
- Rate limiting
- Security annotations

#### Skill: API Versioning
**Capability**: Implement API versioning for backward compatibility

**Implementation**:
```java
@RestController
@RequestMapping("/api/v1/resource")
public class ResourceV1Controller {
    // Version 1 implementation
}

@RestController
@RequestMapping("/api/v2/resource")
public class ResourceV2Controller {
    // Version 2 implementation with enhanced features
}
```

## Frontend Development Skills

### 1. Component Development

#### Skill: Create Angular Component
**Capability**: Build reusable Angular components with full functionality

**Component Structure**:
```typescript
// component.ts
@Component({
  selector: 'app-new-component',
  templateUrl: './new-component.component.html',
  styleUrls: ['./new-component.component.css']
})
export class NewComponentComponent implements OnInit {
  data: any[];
  form: FormGroup;
  
  constructor(
    private service: NewService,
    private fb: FormBuilder
  ) {}
  
  ngOnInit() {
    this.initializeForm();
    this.loadData();
  }
  
  private initializeForm() {
    this.form = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      status: ['ACTIVE']
    });
  }
  
  loadData() {
    this.service.getAll().subscribe(data => {
      this.data = data;
    });
  }
  
  onSubmit() {
    if (this.form.valid) {
      this.service.create(this.form.value).subscribe(() => {
        this.loadData();
        this.form.reset();
      });
    }
  }
}
```

**Template (HTML)**:
```html
<div class="new-component">
  <h2>New Component</h2>
  
  <form [formGroup]="form" (ngSubmit)="onSubmit()">
    <div class="form-group">
      <label for="name">Name:</label>
      <input id="name" formControlName="name" type="text">
      <div *ngIf="form.get('name')?.invalid && form.get('name')?.touched" class="error">
        Name is required
      </div>
    </div>
    
    <div class="form-group">
      <label for="description">Description:</label>
      <textarea id="description" formControlName="description"></textarea>
    </div>
    
    <div class="form-group">
      <label for="status">Status:</label>
      <select id="status" formControlName="status">
        <option value="ACTIVE">Active</option>
        <option value="INACTIVE">Inactive</option>
      </select>
    </div>
    
    <button type="submit" [disabled]="form.invalid">Submit</button>
  </form>
  
  <div class="data-list">
    <h3>Existing Data</h3>
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Description</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let item of data">
          <td>{{item.name}}</td>
          <td>{{item.description}}</td>
          <td>{{item.status}}</td>
          <td>
            <button (click)="edit(item)">Edit</button>
            <button (click)="delete(item)">Delete</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
```

**Quality Requirements**:
- [ ] Responsive design
- [ ] Form validation
- [ ] Error handling
- [ ] Loading states
- [ ] Accessibility (ARIA labels)
- [ ] Unit tests
- [ ] E2E tests
- [ ] Performance optimization

#### Skill: Service Development
**Capability**: Create Angular services for API communication

**Service Template**:
```typescript
@Injectable({
  providedIn: 'root'
})
export class NewService {
  private apiUrl = '/api/newresource';
  
  constructor(private http: HttpClient) {}
  
  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }
  
  getById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }
  
  create(data: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, data);
  }
  
  update(id: number, data: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, data);
  }
  
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  
  // Custom business methods
  search(criteria: any): Observable<any[]> {
    return this.http.post<any[]>(`${this.apiUrl}/search`, criteria);
  }
  
  export(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/export`, {
      responseType: 'blob'
    });
  }
}
```

### 2. Page Development

#### Skill: Create New Page/View
**Capability**: Build complete page views with routing and navigation

**Page Development Process**:
1. **Create Component**
2. **Add Routing**
3. **Update Navigation**
4. **Add Guards (if needed)**
5. **Implement Business Logic**
6. **Add Tests**

**Routing Configuration**:
```typescript
// app.routes.ts
export const routes: Routes = [
  // Existing routes...
  
  {
    path: 'newpage',
    component: NewPageComponent,
    canActivate: [authGuard],
    data: { title: 'New Page', roles: ['MAKER', 'CHECKER'] }
  },
  
  {
    path: 'newpage/:id',
    component: NewPageDetailComponent,
    canActivate: [authGuard]
  }
];
```

**Navigation Update**:
```html
<!-- app.component.html or navigation component -->
<nav>
  <a routerLink="/reports" routerLinkActive="active">Reports</a>
  <a routerLink="/newpage" routerLinkActive="active">New Page</a>
</nav>

<router-outlet></router-outlet>
```

#### Skill: Form Development
**Capability**: Create complex forms with validation and dynamic behavior

**Advanced Form Example**:
```typescript
@Component({
  selector: 'app-advanced-form',
  templateUrl: './advanced-form.component.html'
})
export class AdvancedFormComponent implements OnInit {
  form: FormGroup;
  submitted = false;
  
  constructor(
    private fb: FormBuilder,
    private service: FormService
  ) {}
  
  ngOnInit() {
    this.form = this.fb.group({
      basicInfo: this.fb.group({
        name: ['', [Validators.required, Validators.minLength(3)]],
        email: ['', [Validators.required, Validators.email]],
        phone: ['', Validators.pattern(/^\d{10}$/)]
      }),
      
      address: this.fb.group({
        street: [''],
        city: [''],
        state: [''],
        zipCode: ['', Validators.pattern(/^\d{5}$/)]
      }),
      
      preferences: this.fb.array([
        this.createPreferenceGroup()
      ])
    });
  }
  
  createPreferenceGroup(): FormGroup {
    return this.fb.group({
      type: ['', Validators.required],
      value: ['', Validators.required]
    });
  }
  
  addPreference() {
    const preferences = this.form.get('preferences') as FormArray;
    preferences.push(this.createPreferenceGroup());
  }
  
  removePreference(index: number) {
    const preferences = this.form.get('preferences') as FormArray;
    preferences.removeAt(index);
  }
  
  onSubmit() {
    this.submitted = true;
    
    if (this.form.valid) {
      this.service.submit(this.form.value).subscribe(
        result => {
          // Handle success
        },
        error => {
          // Handle error
        }
      );
    }
  }
}
```

### 3. UI/UX Enhancement

#### Skill: Responsive Design Implementation
**Capability**: Make components responsive for different screen sizes

**CSS Implementation**:
```css
/* responsive.component.css */
.container {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
  padding: 1rem;
}

@media (min-width: 768px) {
  .container {
    grid-template-columns: 1fr 1fr;
  }
}

@media (min-width: 1024px) {
  .container {
    grid-template-columns: 1fr 1fr 1fr;
  }
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.error {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.button {
  background-color: #007bff;
  color: white;
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.button:hover {
  background-color: #0056b3;
}

.button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}
```

#### Skill: Data Visualization
**Capability**: Add charts and graphs for data presentation

**Chart Implementation**:
```typescript
// chart.component.ts
import { Component, OnInit } from '@angular/core';
import { ChartData, ChartType } from 'chart.js';

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html'
})
export class ChartComponent implements OnInit {
  chartData: ChartData;
  chartType: ChartType = 'bar';
  
  constructor(private service: DataService) {}
  
  ngOnInit() {
    this.loadData();
  }
  
  loadData() {
    this.service.getChartData().subscribe(data => {
      this.chartData = {
        labels: data.labels,
        datasets: [{
          label: 'Report Count',
          data: data.values,
          backgroundColor: ['#007bff', '#28a745', '#ffc107', '#dc3545'],
          borderColor: ['#0056b3', '#1e7e34', '#d39e00', '#bd2130'],
          borderWidth: 1
        }]
      };
    });
  }
}
```

## Integration Skills

### 1. Database Integration

#### Skill: Database Schema Evolution
**Capability**: Safely evolve database schema with migration scripts

**Migration Process**:
```sql
-- V1__Create_initial_schema.sql
CREATE TABLE customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- V2__Add_customer_status.sql
ALTER TABLE customer ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
CREATE INDEX idx_customer_status ON customer(status);

-- V3__Add_customer_type.sql
ALTER TABLE customer ADD COLUMN type VARCHAR(20) DEFAULT 'NORMAL';
CREATE INDEX idx_customer_type ON customer(type);
```

**Migration Script**:
```java
@Component
public class DatabaseMigrationService {
    
    @EventListener
    public void handleApplicationReady(ApplicationReadyEvent event) {
        runMigrations();
    }
    
    private void runMigrations() {
        // Check current version
        int currentVersion = getCurrentDatabaseVersion();
        
        // Run pending migrations
        List<String> pendingMigrations = getPendingMigrations(currentVersion);
        
        for (String migration : pendingMigrations) {
            executeMigration(migration);
            updateVersion(migration);
        }
    }
}
```

### 2. Security Integration

#### Skill: Security Enhancement
**Capability**: Add security features to existing functionality

**Security Implementation**:
```java
// Method-level security
@PreAuthorize("hasRole('MAKER') and @permissionService.canAccessReport(#reportId, authentication.name)")
public Report getReport(Long reportId, Authentication authentication) {
    return reportService.getReportById(reportId);
}

// Data-level security
@PostFilter("hasRole('CHECKER') or filterObject.owner == authentication.name")
public List<Report> getAllReports() {
    return reportService.getAllReports();
}

// Custom permission evaluation
@Component
public class PermissionService {
    
    public boolean canAccessReport(Long reportId, String username) {
        Report report = reportService.getReportById(reportId);
        User user = userService.findByUsername(username);
        
        return switch (user.getRole()) {
            case "ADMIN" -> true;
            case "MAKER" -> report.getMakerUsername().equals(username);
            case "CHECKER" -> true; // Checkers can see all reports
            default -> false;
        };
    }
}
```

### 3. Performance Optimization

#### Skill: Query Optimization
**Capability**: Optimize database queries for better performance

**Optimization Techniques**:
```java
// Before optimization
public List<Report> getReportsWithDetails() {
    List<Report> reports = reportRepository.findAll();
    for (Report report : reports) {
        report.setRuns(reportRunRepository.findByReportId(report.getId()));
    }
    return reports;
}

// After optimization
@Query("SELECT r FROM Report r LEFT JOIN FETCH r.runs WHERE r.id = :id")
public Report findReportWithRuns(@Param("id") Long id);

// Batch processing
@Query("SELECT r FROM Report r WHERE r.id IN :ids")
public List<Report> findReportsByIds(@Param("ids") List<Long> ids);

// Pagination
@Query("SELECT r FROM Report r ORDER BY r.createTime DESC")
public Page<Report> findAllReports(Pageable pageable);
```

## Quality Assurance Skills

### 1. Automated Testing

#### Skill: Test Generation
**Capability**: Generate comprehensive tests for new functionality

**Test Generation Template**:
```java
// Auto-generated unit test
@ExtendWith(MockitoExtension.class)
class NewServiceTest {
    
    @Mock
    private NewRepository repository;
    
    @InjectMocks
    private NewService service;
    
    @Test
    void shouldCreateNewEntity() {
        // Given
        NewEntityDto dto = new NewEntityDto();
        dto.setName("Test");
        
        NewEntity saved = new NewEntity();
        saved.setId(1L);
        saved.setName("Test");
        
        when(repository.save(any(NewEntity.class))).thenReturn(saved);
        
        // When
        NewEntity result = service.create(dto);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test");
        verify(repository).save(any(NewEntity.class));
    }
    
    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        // Given
        NewEntityDto dto = new NewEntityDto();
        dto.setName("");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.create(dto);
        });
    }
}
```

### 2. Performance Testing

#### Skill: Load Test Creation
**Capability**: Create load tests for new functionality

**Load Test Example**:
```java
@Test
@Timeout(value = 30, unit = TimeUnit.SECONDS)
void shouldHandleConcurrentRequests() throws InterruptedException {
    int threadCount = 20;
    int requestsPerThread = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    
    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                for (int j = 0; j < requestsPerThread; j++) {
                    service.createNewEntity(createTestDto());
                    successCount.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await();
    assertThat(successCount.get()).isEqualTo(threadCount * requestsPerThread);
}
```

## Deployment Skills

### 1. Configuration Management

#### Skill: Environment Configuration
**Capability**: Manage configurations for different environments

**Configuration Structure**:
```yaml
# application.yml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  
---
# Development
spring:
  config:
    activate:
      on-profile: dev
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

---
# Production
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 3600
```

### 2. Monitoring Integration

#### Skill: Metrics Addition
**Capability**: Add custom metrics for new functionality

**Metrics Implementation**:
```java
@Component
public class NewServiceMetrics {
    
    private final Counter newEntityCounter;
    private final Timer newEntityTimer;
    
    public NewServiceMetrics(MeterRegistry meterRegistry) {
        this.newEntityCounter = Counter.builder("new_entity_created")
            .description("Number of new entities created")
            .register(meterRegistry);
        
        this.newEntityTimer = Timer.builder("new_entity_creation_time")
            .description("Time taken to create new entity")
            .register(meterRegistry);
    }
    
    public void recordEntityCreation() {
        newEntityCounter.increment();
    }
    
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopTimer(Timer.Sample sample) {
        sample.stop(newEntityTimer);
    }
}
```

## Skill Usage Guidelines

### 1. Skill Selection
- **New Reports**: Use Report Generation skills
- **New Modules**: Use Module Development skills
- **UI Changes**: Use Frontend Development skills
- **Performance Issues**: Use Optimization skills
- **Security Needs**: Use Security Integration skills

### 2. Quality Assurance
- Every skill implementation includes automated testing
- Performance benchmarks are required
- Security validation is mandatory
- Documentation updates are required

### 3. Integration Requirements
- Follow existing patterns and conventions
- Maintain backward compatibility
- Update relevant documentation
- Communicate changes to stakeholders

## Conclusion

These development skills provide:
- **Comprehensive Coverage**: All aspects of system development
- **Quality Assurance**: Built-in testing and validation
- **Best Practices**: Industry-standard approaches
- **Maintainability**: Consistent patterns and documentation
- **Scalability**: Designed for future growth

When implementing new features, I will:
1. **Select appropriate skills** based on requirements
2. **Follow established patterns** for consistency
3. **Generate comprehensive tests** automatically
4. **Ensure quality standards** are met
5. **Update documentation** as needed
6. **Provide implementation guidance** throughout the process

This ensures all development work is systematic, high-quality, and maintainable.

# Architecture Decision Records (ADRs)

## Overview

This document contains Architecture Decision Records (ADRs) for the report system. ADRs capture important architectural decisions, their context, and consequences.

## ADR Template

Each ADR follows this format:

- **Status**: Proposed | Accepted | Deprecated | Superseded
- **Date**: YYYY-MM-DD
- **Decision Makers**: [Names/Roles]
- **Status**: [Current status]
- **Context**: [Problem statement and context]
- **Decision**: [The decision made]
- **Consequences**: [Results of the decision, both positive and negative]
- **Related ADRs**: [Links to related decisions]
- **Implementation**: [Implementation details and status]

---

## ADR-001: Frontend Framework Selection

**Status**: Accepted  
**Date**: 2023-12-01  
**Decision Makers**: Architecture Team, Tech Lead  
**Context**: 
- Need to choose frontend framework for report system
- Requirements: component-based, good ecosystem, enterprise-ready
- Team expertise in JavaScript/TypeScript

**Decision**: 
- Selected Angular 17.3.0 as frontend framework
- Used TypeScript for type safety
- Adopted standalone components approach

**Consequences**:
- **Positive**: Strong typing, good tooling, enterprise support
- **Positive**: Large ecosystem and community support
- **Negative**: Steeper learning curve compared to React
- **Negative**: More verbose syntax for simple components

**Related ADRs**: ADR-002, ADR-003

**Implementation**: 
- [x] Angular CLI project setup
- [x] Component architecture defined
- [x] TypeScript configuration completed

---

## ADR-002: Backend Framework Selection

**Status**: Accepted  
**Date**: 2023-12-01  
**Decision Makers**: Architecture Team, Backend Lead  
**Context**: 
- Need robust backend framework for enterprise reporting
- Requirements: security, data access, REST APIs
- Java expertise available in team

**Decision**: 
- Selected Spring Boot 3.2.4 as backend framework
- Used Java 17 for modern language features
- Adopted Spring Security for authentication

**Consequences**:
- **Positive**: Mature ecosystem, strong security features
- **Positive**: Excellent data access capabilities
- **Negative**: Higher memory footprint
- **Negative**: More complex configuration

**Related ADRs**: ADR-001, ADR-004

**Implementation**: 
- [x] Spring Boot project setup
- [x] Security configuration completed
- [x] REST API structure defined

---

## ADR-003: Database Technology Choice

**Status**: Accepted  
**Date**: 2023-12-01  
**Decision Makers**: Architecture Team, DBA Team  
**Context**: 
- Need database for reporting system
- Requirements: SQL support, transaction management, ease of deployment
- Development and testing phases

**Decision**: 
- Selected H2 Database for development/testing
- Planned migration to PostgreSQL for production
- Used JPA for ORM, JDBC for complex queries

**Consequences**:
- **Positive**: Easy setup for development, in-memory performance
- **Positive**: Full SQL compatibility
- **Negative**: Not suitable for production scale
- **Negative**: Limited features compared to enterprise databases

**Related ADRs**: ADR-002, ADR-007

**Implementation**: 
- [x] H2 database configuration
- [x] Schema and data scripts created
- [x] JPA entities defined

---

## ADR-004: Authentication Strategy

**Status**: Accepted  
**Date**: 2023-12-02  
**Decision Makers**: Architecture Team, Security Lead  
**Context**: 
- Need secure authentication for report system
- Requirements: stateless, scalable, role-based access
- Frontend-backend separation

**Decision**: 
- Adopted JWT (JSON Web Token) for authentication
- Implemented role-based access control (MAKER/CHECKER)
- Used Spring Security for framework support

**Consequences**:
- **Positive**: Stateless authentication, good scalability
- **Positive**: Standardized approach, good library support
- **Negative**: Token management complexity
- **Negative**: No built-in token revocation

**Related ADRs**: ADR-002, ADR-005

**Implementation**: 
- [x] JWT token provider implemented
- [x] Authentication filters created
- [x] Role-based access control implemented

---

## ADR-005: Report Execution Architecture

**Status**: Accepted  
**Date**: 2023-12-03  
**Decision Makers**: Architecture Team, Backend Lead  
**Context**: 
- Need flexible report execution mechanism
- Requirements: dynamic SQL, parameter support, security
- Business user requirements for custom reports

**Decision**: 
- Direct SQL execution approach (with security concerns)
- Stored report configurations in database
- Used JDBC template for query execution

**Consequences**:
- **Positive**: Maximum flexibility for report creation
- **Positive**: Simple implementation
- **Negative**: **CRITICAL SECURITY RISK** - SQL injection vulnerability
- **Negative**: No query validation or optimization

**Related ADRs**: ADR-006, ADR-008

**Implementation**: 
- [x] Report configuration table created
- [x] SQL execution service implemented
- [x] Report API endpoints created

**Security Note**: This decision created critical security vulnerabilities that must be addressed in ADR-008.

---

## ADR-006: Audit Trail Implementation

**Status**: Accepted  
**Date**: 2023-12-04  
**Decision Makers**: Architecture Team, Compliance Lead  
**Context**: 
- Need comprehensive audit trail for compliance
- Requirements: user actions, data changes, timestamps
- Regulatory requirements for financial reporting

**Decision**: 
- Implemented detailed audit logging
- Created separate audit events table
- Logged all report lifecycle events

**Consequences**:
- **Positive**: Complete audit trail for compliance
- **Positive**: Detailed operation history
- **Negative**: Additional storage requirements
- **Negative**: Performance overhead for logging

**Related ADRs**: ADR-004, ADR-005

**Implementation**: 
- [x] Audit event entities created
- [x] Audit service implemented
- [x] Automatic event logging integrated

---

## ADR-007: Data Access Strategy

**Status**: Accepted  
**Date**: 2023-12-05  
**Decision Makers**: Architecture Team, Data Team  
**Context**: 
- Need efficient data access patterns
- Requirements: simple CRUD, complex queries, performance
- Mixed access patterns needed

**Decision**: 
- Hybrid approach: JPA for simple operations, JDBC for complex queries
- Used Repository pattern for JPA entities
- Used DAO pattern for direct SQL operations

**Consequences**:
- **Positive**: Best of both worlds - ORM flexibility + SQL performance
- **Positive**: Clear separation of concerns
- **Negative**: Increased complexity with two patterns
- **Negative**: Potential for inconsistent data access

**Related ADRs**: ADR-002, ADR-003, ADR-005

**Implementation**: 
- [x] JPA repositories created
- [x] JDBC DAOs implemented
- [x] Service layer integration completed

---

## ADR-008: Security Hardening Strategy

**Status**: Proposed  
**Date**: 2026-04-08  
**Decision Makers**: Architecture Team, Security Lead  
**Context**: 
- Critical security vulnerabilities identified in ADR-005
- SQL injection risks in direct SQL execution
- Need comprehensive security overhaul

**Decision**: 
- **PROPOSED**: Replace direct SQL execution with parameterized queries
- **PROPOSED**: Implement SQL validation and whitelisting
- **PROPOSED**: Add comprehensive input validation
- **PROPOSED**: Implement query result caching

**Consequences**:
- **Positive**: Eliminates SQL injection risks
- **Positive**: Better performance with caching
- **Positive**: Improved security posture
- **Negative**: Reduced flexibility for ad-hoc queries
- **Negative**: More complex report configuration

**Related ADRs**: ADR-005, ADR-004, ADR-007

**Implementation**: 
- [ ] SQL validation service to be implemented
- [ ] Parameterized query engine to be created
- [ ] Input validation filters to be added
- [ ] Query caching mechanism to be implemented

---

## ADR-009: Monitoring and Observability

**Status**: Accepted  
**Date**: 2023-12-06  
**Decision Makers**: Architecture Team, Ops Team  
**Context**: 
- Need comprehensive monitoring for production
- Requirements: performance metrics, error tracking, health checks
- DevOps and SRE requirements

**Decision**: 
- Adopted Micrometer + Prometheus for metrics
- Implemented Spring Actuator for health checks
- Added custom business metrics for reporting

**Consequences**:
- **Positive**: Comprehensive monitoring capabilities
- **Positive**: Standard tooling integration
- **Negative**: Additional infrastructure complexity
- **Negative**: Metrics storage and management overhead

**Related ADRs**: ADR-002

**Implementation**: 
- [x] Micrometer integration completed
- [x] Prometheus metrics exposed
- [x] Custom business metrics added

---

## ADR-010: Frontend-Backend Communication

**Status**: Accepted  
**Date**: 2023-12-07  
**Decision Makers**: Architecture Team, Frontend Lead  
**Context**: 
- Need robust communication between frontend and backend
- Requirements: RESTful, error handling, authentication
- Modern web application requirements

**Decision**: 
- RESTful API design with HTTP status codes
- Angular HTTP Client with interceptors
- JWT token management in frontend

**Consequences**:
- **Positive**: Standardized API approach
- **Positive**: Good error handling and user experience
- **Negative**: More complex frontend state management
- **Negative**: Additional network overhead

**Related ADRs**: ADR-001, ADR-002, ADR-004

**Implementation**: 
- [x] REST API endpoints defined
- [x] Angular services created
- [x] Authentication interceptors implemented

---

## ADR-011: Excel Export Strategy

**Status**: Accepted  
**Date**: 2023-12-08  
**Decision Makers**: Architecture Team, Business Team  
**Context**: 
- Need Excel export capability for reports
- Requirements: formatting, large datasets, performance
- Business user requirements for data export

**Decision**: 
- Adopted JXLS library for Excel generation
- Used template-based approach for formatting
- Implemented streaming for large datasets

**Consequences**:
- **Positive**: Professional Excel formatting
- **Positive**: Template-based flexibility
- **Negative**: Additional dependency
- **Negative**: Memory usage for large exports

**Related ADRs**: ADR-005

**Implementation**: 
- [x] JXLS integration completed
- [x] Excel templates created
- [x] Export endpoints implemented

---

## ADR-012: Deployment Architecture

**Status**: Accepted  
**Date**: 2023-12-09  
**Decision Makers**: Architecture Team, DevOps Team  
**Context**: 
- Need deployment strategy for different environments
- Requirements: development, testing, production
- Containerization and CI/CD requirements

**Decision**: 
- Separated frontend and backend deployment
- Used Docker containers for consistency
- Implemented environment-specific configurations

**Consequences**:
- **Positive**: Deployment consistency across environments
- **Positive**: Scalable architecture
- **Negative**: Increased deployment complexity
- **Negative**: Additional infrastructure requirements

**Related ADRs**: ADR-001, ADR-002

**Implementation**: 
- [x] Docker configurations created
- [x] Environment-specific configs implemented
- [x] CI/CD pipeline established

---

## Framework Change Impact Analysis

### When Framework Changes Occur

This section helps identify when ADRs need updates based on framework changes.

#### Frontend Framework Changes (Angular)
**Triggers for ADR Updates**:
- Angular version major upgrade
- Component architecture changes
- Routing or state management changes
- Build tool changes

**Affected ADRs**: ADR-001, ADR-010, ADR-012

**Review Checklist**:
- [ ] Component compatibility assessment
- [ ] Build process updates needed
- [ ] API integration impact
- [ ] Deployment configuration changes

#### Backend Framework Changes (Spring Boot)
**Triggers for ADR Updates**:
- Spring Boot major version upgrade
- Security framework changes
- Data access framework changes
- Dependency injection changes

**Affected ADRs**: ADR-002, ADR-004, ADR-007, ADR-009

**Review Checklist**:
- [ ] Security configuration updates
- [ ] Data access layer compatibility
- [ ] Monitoring integration changes
- [ ] API endpoint compatibility

#### Database Technology Changes
**Triggers for ADR Updates**:
- Database vendor change
- Major version upgrade
- Schema migration requirements
- Performance characteristic changes

**Affected ADRs**: ADR-003, ADR-007, ADR-008

**Review Checklist**:
- [ ] SQL syntax compatibility
- [ ] Performance optimization needs
- [ ] Migration strategy requirements
- [ ] Feature compatibility assessment

#### Security Framework Changes
**Triggers for ADR Updates**:
- Authentication method changes
- Authorization model changes
- Encryption standards updates
- Compliance requirement changes

**Affected ADRs**: ADR-004, ADR-008

**Review Checklist**:
- [ ] Token management updates
- [ ] Permission model changes
- [ ] API security impact
- [ ] Frontend authentication changes

### ADR Maintenance Process

#### Regular Review Schedule
- **Quarterly**: Review all Accepted ADRs for relevance
- **Major Framework Release**: Immediate impact assessment
- **Security Incident**: Review security-related ADRs
- **Performance Issues**: Review performance-critical ADRs

#### ADR Update Process
1. **Impact Assessment**: Identify affected ADRs
2. **Stakeholder Review**: Get input from relevant teams
3. **Decision Update**: Update decision or create new ADR
4. **Implementation Update**: Update implementation status
5. **Documentation**: Update related documentation

#### ADR Deprecation Process
1. **Identify Obsolete ADRs**: No longer relevant decisions
2. **Create Superseding ADR**: New decision replaces old one
3. **Mark as Deprecated**: Update status and add note
4. **Update References**: Update all cross-references
5. **Communicate**: Notify stakeholders of changes

## Current Architecture Issues

### Critical Issues
1. **ADR-005 Security Risk**: Direct SQL execution creates SQL injection vulnerability
2. **No Rate Limiting**: API endpoints lack rate limiting protection
3. **No Input Validation**: Insufficient input validation across the system

### Improvement Opportunities
1. **Performance**: Query optimization and caching needed
2. **Scalability**: Current architecture may not scale to enterprise needs
3. **Monitoring**: Need more comprehensive observability

### Future Considerations
1. **Microservices**: Consider service decomposition for scalability
2. **Event-Driven**: Consider event-driven architecture for real-time updates
3. **Cloud-Native**: Consider cloud-native deployment patterns

---

## ADR-013: Two-Level Approval Architecture

**Status**: Proposed  
**Date**: 2026-04-08  
**Decision Makers**: Architecture Team, Business Team, Security Lead  
**Context**: 
- Current single-level approval (MAKER → CHECKER) insufficient for risk control
- Business requirement for enhanced internal controls
- Compliance requirements demand multi-level approval
- Need to maintain backward compatibility during transition

**Decision**: 
- **PROPOSED**: Implement two-level approval workflow
- **PROPOSED**: Extend existing state machine to support L1/L2 approval
- **PROPOSED**: Add configurable approval levels per report type
- **PROPOSED**: Maintain audit trail for both approval levels

**Consequences**:
- **Positive**: Enhanced risk control and compliance
- **Positive**: Flexible approval configuration
- **Positive**: Complete audit trail for regulatory requirements
- **Negative**: Increased complexity in approval workflow
- **Negative**: Additional database storage for approval tracking
- **Negative**: More complex user interface and training requirements

**Related ADRs**: ADR-004, ADR-005, ADR-006

**Implementation**: 
- [ ] Database schema changes to support two-level approval
- [ ] State machine extension for L1/L2 approval states
- [ ] Service layer modifications for approval logic
- [ ] API endpoints for two-level approval operations
- [ ] Frontend UI updates for approval workflow
- [ ] Data migration strategy for existing single-level approvals
- [ ] Comprehensive testing for approval workflow

---

## Conclusion

This ADR collection provides:
- **Decision Traceability**: Why architectural decisions were made
- **Impact Analysis**: How changes affect the system
- **Maintenance Guide**: How to keep architecture current
- **Risk Management**: Identify and mitigate architectural risks

When framework changes occur:
1. **Consult this document** for affected ADRs
2. **Review impact checklist** for relevant areas
3. **Update ADRs** as needed
4. **Communicate changes** to all stakeholders

This ensures architectural decisions remain relevant and system evolves coherently.

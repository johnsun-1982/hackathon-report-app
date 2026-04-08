# Database Migration Skill

## Overview
This skill enables safe database schema evolution with migration scripts, rollback procedures, and data validation.

## Capabilities
- Schema evolution management
- Migration script generation
- Rollback procedures
- Data validation scripts
- Performance impact analysis

## Usage

### 1. Create Migration
```bash
# Generate new migration
./skills/database-migration/scripts/generate-migration.sh "AddCustomerTypeField"
```

### 2. Apply Migration
```bash
# Apply migration with validation
./skills/database-migration/scripts/apply-migration.sh "V2__AddCustomerTypeField.sql"
```

## Templates

### Migration Template
See `templates/migration.sql` for standard migration structure.

### Rollback Template
See `templates/rollback.sql` for rollback procedures.

## Quality Assurance
- Schema validation
- Data integrity checks
- Performance impact assessment
- Rollback testing

## Automation
- Migration script generation
- Automated validation
- Rollback testing
- Performance monitoring

## Examples
See `examples/` directory for complete migration examples.

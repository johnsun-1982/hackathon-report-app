# Testing Automation Skill

## Overview
This skill provides comprehensive testing automation capabilities including unit tests, integration tests, E2E tests, and performance tests.

## Capabilities
- Automated test generation
- Unit test creation
- Integration test setup
- E2E test automation
- Performance testing
- Test coverage analysis
- Quality assurance

## Usage

### 1. Generate Tests for New Code
```bash
# Generate tests for a new service
./skills/testing-automation/scripts/generate-tests.sh "CustomerService" "service"

# Generate tests for a new component
./skills/testing-automation/scripts/generate-tests.sh "CustomerComponent" "component"
```

### 2. Run Test Suite
```bash
# Run all tests
./skills/testing-automation/scripts/run-tests.sh

# Run specific test type
./skills/testing-automation/scripts/run-tests.sh "unit"
./skills/testing-automation/scripts/run-tests.sh "integration"
./skills/testing-automation/scripts/run-tests.sh "e2e"
```

## Templates

### Unit Test Template
See `templates/unit-test/` for standard unit test structure.

### Integration Test Template
See `templates/integration-test/` for integration test setup.

### E2E Test Template
See `templates/e2e-test/` for end-to-end test automation.

## Quality Assurance
- Test coverage requirements (minimum 80%)
- Performance benchmarks
- Security testing
- Accessibility testing
- Cross-browser testing

## Automation
- Automatic test generation
- Continuous integration
- Test result reporting
- Quality metrics tracking

## Examples
See `examples/` directory for complete working examples of different test types.

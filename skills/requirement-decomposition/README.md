# Requirement Decomposition Skill

## Overview
This skill provides the capability to decompose large requirements into manageable sub-functions for independent development, testing, and integration.

## Capabilities
- Requirement complexity analysis
- Decomposition pattern selection
- Sub-function generation
- Independent development guidance
- Integration testing strategy
- Quality assurance automation

## Usage

### 1. Analyze Requirement Complexity
```bash
# Analyze requirement and determine if decomposition is needed
./skills/requirement-decomposition/scripts/analyze-complexity.sh "large-requirement.json"
```

### 2. Generate Sub-Functions
```bash
# Generate sub-function templates
./skills/requirement-decomposition/scripts/generate-sub-functions.sh "large-requirement.json" "output-dir"
```

### 3. Setup Development Environment
```bash
# Setup isolated development environment for sub-functions
./skills/requirement-decomposition/scripts/setup-dev-env.sh "sub-function-name"
```

## Templates

### Requirement Analysis Template
See `templates/requirement-analysis.md` for standard requirement analysis format.

### Sub-Function Template
See `templates/sub-function.md` for sub-function specification format.

### Integration Plan Template
See `templates/integration-plan.md` for integration testing plan format.

## Quality Assurance
- Complexity threshold validation
- Dependency analysis
- Integration testing automation
- Quality gate enforcement

## Automation
- Automated complexity analysis
- Sub-function template generation
- Integration test generation
- Quality metric tracking

## Examples
See `examples/` directory for complete decomposition examples of different requirement types.

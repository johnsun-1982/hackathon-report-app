#!/bin/bash

# Sub-Function Generation Script
# Usage: ./generate-sub-functions.sh <requirement-file> <output-dir>

set -e

REQUIREMENT_FILE="$1"
OUTPUT_DIR="$2"

if [ -z "$REQUIREMENT_FILE" ] || [ -z "$OUTPUT_DIR" ]; then
    echo "Usage: $0 <requirement-file> <output-dir>"
    echo "Example: $0 large-requirement.json sub-functions"
    exit 1
fi

if [ ! -f "$REQUIREMENT_FILE" ]; then
    echo "Error: Requirement file '$REQUIREMENT_FILE' not found"
    exit 1
fi

echo "Generating sub-function templates..."

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Generate sub-function templates
python3 ../scripts/sub-function-generator.py "$REQUIREMENT_FILE" "$OUTPUT_DIR"

# Generate dependency matrix
python3 ../scripts/dependency-matrix-generator.py "$REQUIREMENT_FILE" "$OUTPUT_DIR"

# Generate integration plan
python3 ../scripts/integration-plan-generator.py "$REQUIREMENT_FILE" "$OUTPUT_DIR"

echo "Sub-function templates generated in: $OUTPUT_DIR"
echo ""
echo "Generated files:"
echo "- Sub-function specifications"
echo "- Dependency matrix"
echo "- Integration plan"
echo "- Development guidelines"
echo ""
echo "Next steps:"
echo "1. Review generated sub-function specifications"
echo "2. Adjust boundaries and dependencies as needed"
echo "3. Setup development environment for each sub-function"
echo "4. Begin independent development"

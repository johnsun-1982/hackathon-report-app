#!/bin/bash

# Requirement Complexity Analysis Script
# Usage: ./analyze-complexity.sh <requirement-file>

set -e

REQUIREMENT_FILE="$1"

if [ -z "$REQUIREMENT_FILE" ]; then
    echo "Usage: $0 <requirement-file>"
    echo "Example: $0 large-requirement.json"
    exit 1
fi

if [ ! -f "$REQUIREMENT_FILE" ]; then
    echo "Error: Requirement file '$REQUIREMENT_FILE' not found"
    exit 1
fi

echo "Analyzing requirement complexity for: $REQUIREMENT_FILE"

# Check if Python is available
if ! command -v python3 &> /dev/null; then
    echo "Error: Python 3 is required for complexity analysis"
    exit 1
fi

# Run complexity analysis
python3 ../scripts/requirement-analyzer.py "$REQUIREMENT_FILE"

# Generate decomposition recommendation
python3 ../scripts/decomposition-advisor.py "$REQUIREMENT_FILE"

echo "Complexity analysis completed!"
echo ""
echo "Next steps:"
echo "1. Review the complexity score and recommendation"
echo "2. If decomposition is recommended, run: ./generate-sub-functions.sh $REQUIREMENT_FILE"
echo "3. Set up development environment for each sub-function"
echo "4. Follow the development and testing guidelines"

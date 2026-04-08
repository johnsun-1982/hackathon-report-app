#!/bin/bash

# Report Generation Script
# Usage: ./generate-report.sh "Report Name" "Description" "SQL Query"

set -e

REPORT_NAME="$1"
DESCRIPTION="$2"
SQL_QUERY="$3"

if [ -z "$REPORT_NAME" ]; then
    echo "Usage: $0 \"Report Name\" \"Description\" \"SQL Query\""
    echo "Example: $0 \"Customer Analysis\" \"Customer transaction analysis\" \"SELECT c.name, COUNT(t.id) FROM customer c LEFT JOIN transaction t ON c.id = t.customer_id\""
    exit 1
fi

# Generate class name (convert to CamelCase)
CLASS_NAME=$(echo "$REPORT_NAME" | sed 's/[^a-zA-Z0-9 ]//g' | sed 's/\b\([a-z]\)/\u\1/g' | sed 's/ //g')

echo "Generating report: $REPORT_NAME"
echo "Class name: $CLASS_NAME"

# Create report service from template
TEMPLATE_FILE="templates/basic-report.java"
OUTPUT_FILE="generated/${CLASS_NAME}Service.java"

# Create output directory
mkdir -p generated

# Replace placeholders in template
sed "s/{{ReportName}}/$CLASS_NAME/g" "$TEMPLATE_FILE" | \
sed "s/{{ReportDisplayName}}/$REPORT_NAME/g" | \
sed "s/{{ReportDescription}}/$DESCRIPTION/g" | \
sed "s/{{SQL_QUERY}}/$SQL_QUERY/g" > "$OUTPUT_FILE"

echo "Generated: $OUTPUT_FILE"

# Generate test
TEST_TEMPLATE="templates/basic-report-test.java"
TEST_OUTPUT_FILE="generated/${CLASS_NAME}ServiceTest.java"

sed "s/{{ReportName}}/$CLASS_NAME/g" "$TEST_TEMPLATE" > "$TEST_OUTPUT_FILE"

echo "Generated test: $TEST_OUTPUT_FILE"

# Generate controller endpoint
echo "Generating controller endpoint..."
cat >> "generated/${CLASS_NAME}Controller.java" << EOF
// Auto-generated controller for $REPORT_NAME
@RestController
@RequestMapping("/api/reports")
public class ${CLASS_NAME}Controller {
    
    @Autowired
    private ${CLASS_NAME}Service ${CLASS_NAME,,}Service;
    
    @PostMapping("/$CLASS_NAME,,/execute")
    public ResponseEntity<List<Map<String, Object>>> execute${CLASS_NAME}Report() {
        List<Map<String, Object>> results = ${CLASS_NAME,,}Service.execute${CLASS_NAME}Report();
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/$CLASS_NAME,,/export")
    public ResponseEntity<byte[]> export${CLASS_NAME}Report() {
        byte[] excelData = ${CLASS_NAME,,}Service.export${CLASS_NAME}ToExcel();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("$REPORT_NAME.xlsx").build());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
EOF

echo "Generated controller: generated/${CLASS_NAME}Controller.java"

# Run validation
echo "Running validation..."
./scripts/validate-report.sh "$OUTPUT_FILE"

echo "Report generation completed!"
echo "Files generated:"
echo "  - Service: $OUTPUT_FILE"
echo "  - Test: $TEST_OUTPUT_FILE"
echo "  - Controller: generated/${CLASS_NAME}Controller.java"

# Next steps
echo ""
echo "Next steps:"
echo "1. Review generated files"
echo "2. Run tests: mvn test -Dtest=${CLASS_NAME}ServiceTest"
echo "3. Add to application context"
echo "4. Update security configuration"
echo "5. Update frontend to call new endpoint"

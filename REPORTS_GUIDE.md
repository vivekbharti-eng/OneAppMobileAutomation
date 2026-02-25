# Test Reports Structure

This framework generates two types of reports with timestamped directories for each test run.

## Report Directory Structure

```
target/reports/
├── allure/
│   └── {timestamp}/
│       ├── allure-results/     # Allure test results (JSON files)
│       └── allure-report/      # Generated Allure HTML report
└── extent/
    └── {timestamp}/
        └── ExtentReport_{timestamp}.html  # Extent HTML report
```

### Timestamp Format
- Format: `yyyyMMdd_HHmmss`
- Example: `20260211_155231` (Feb 11, 2026 at 15:52:31)

## Report Types

### 1. Extent Reports
**Location**: `target/reports/extent/{timestamp}/ExtentReport_{timestamp}.html`

**Features**:
- Beautiful HTML report with charts and graphs
- Test execution timeline
- Device information
- Screenshot attachments on failures
- Test step details with pass/fail status

**How to View**:
```bash
# Open the HTML file in browser
start target/reports/extent/{timestamp}/ExtentReport_{timestamp}.html
```

### 2. Allure Reports
**Location**: `target/reports/allure/{timestamp}/`

**Features**:
- Interactive HTML report with trends
- Test history tracking
- Categorization and tags
- Detailed test steps
- Screenshot attachments

**How to Generate and View**:
```bash
# Serve Allure report (opens in browser automatically)
allure serve target/reports/allure/{timestamp}/allure-results

# Or generate static HTML report
allure generate target/reports/allure/{timestamp}/allure-results -o target/reports/allure/{timestamp}/allure-report --clean

# Then open the report
start target/reports/allure/{timestamp}/allure-report/index.html
```

## Report Management

### Automatic Cleanup
Reports are organized by timestamp, making it easy to:
- Identify reports from specific test runs
- Compare results between different executions
- Clean up old reports by deleting timestamp directories

### Keeping Reports
To preserve reports from a specific run:
1. Note the timestamp (printed at test start)
2. Copy the entire timestamped directory to a safe location
3. Example: Copy `target/reports/extent/20260211_155231/` to archive location

### Cleaning Old Reports
```bash
# Clean all reports older than 7 days (PowerShell)
Get-ChildItem -Path "target/reports" -Recurse -Directory | 
    Where-Object { $_.CreationTime -lt (Get-Date).AddDays(-7) } | 
    Remove-Item -Recurse -Force

# Clean all reports (start fresh)
mvn clean
```

## CI/CD Integration

### Jenkins
Update Jenkinsfile to archive reports with timestamps:

```groovy
post {
    always {
        // Archive Extent Reports
        publishHTML([
            reportDir: "target/reports/extent/\${env.BUILD_TIMESTAMP}",
            reportFiles: 'ExtentReport_*.html',
            reportName: 'Extent Report'
        ])
        
        // Archive Allure Reports
        allure([
            results: [[path: "target/reports/allure/\${env.BUILD_TIMESTAMP}/allure-results"]]
        ])
    }
}
```

### GitHub Actions
```yaml
- name: Upload Extent Report
  uses: actions/upload-artifact@v3
  with:
    name: extent-report-${{ github.run_id }}
    path: target/reports/extent/*/ExtentReport_*.html

- name: Upload Allure Results
  uses: actions/upload-artifact@v3
  with:
    name: allure-results-${{ github.run_id }}
    path: target/reports/allure/*/allure-results
```

## Troubleshooting

### Reports Not Generated
1. Check test execution logs for errors
2. Verify `ReportPathManager.initializeReportPaths()` is called in `@BeforeSuite`
3. Ensure write permissions on `target/` directory

### Allure Reports Empty
1. Verify Allure plugin is in TestRunner:
   ```java
   plugin = { "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm" }
   ```
2. Check `allure.properties` file is updated correctly
3. Generate report: `allure serve target/reports/allure/{timestamp}/allure-results`

### Extent Reports Missing Data
1. Check `ExtentReportManager.initReport()` is called
2. Verify `ExtentReportManager.flushReport()` is called in `@AfterSuite`
3. Look for exceptions in test logs

## Report Locations (After Test Run)

The framework prints report locations at the end of test execution:

```
========================================
  TEST SUITE COMPLETED
========================================
Reports Generated:
  Extent Report: target/reports/extent/20260211_155231/ExtentReport_20260211_155231.html
  Allure Results: target/reports/allure/20260211_155231/allure-results
========================================
To view Allure report, run:
  allure serve target/reports/allure/20260211_155231/allure-results
========================================
```

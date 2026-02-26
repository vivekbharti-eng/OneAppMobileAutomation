#!/usr/bin/env pwsh
# ============================================================
# generate_allure_report.ps1
# Generates Allure HTML report from the latest allure-results
# and stores it in a timestamped output folder.
# Usage:  .\generate_allure_report.ps1
#         .\generate_allure_report.ps1 -ResultsPath "target\reports\allure\20260226_173230\allure-results"
#         .\generate_allure_report.ps1 -Open
# ============================================================

param(
    [string]$ResultsPath = "",
    [switch]$Open
)

$CYAN   = "`e[36m"
$GREEN  = "`e[32m"
$RED    = "`e[31m"
$BOLD   = "`e[1m"
$RESET  = "`e[0m"

Write-Host "${CYAN}${BOLD}============================================${RESET}"
Write-Host "${GREEN}${BOLD}   ALLURE HTML REPORT GENERATOR            ${RESET}"
Write-Host "${CYAN}${BOLD}============================================${RESET}"

# ── 1. Find allure-results directory ────────────────────────
if ($ResultsPath -eq "") {
    $allureBase = "target\reports\allure"
    if (-not (Test-Path $allureBase)) {
        Write-Host "${RED}ERROR: No allure results found at '$allureBase'.${RESET}"
        Write-Host "Run tests first:  mvn clean test"
        exit 1
    }

    # Pick the latest timestamped sub-folder that contains allure-results
    $latestDir = Get-ChildItem $allureBase -Directory |
        Sort-Object Name -Descending |
        Where-Object { Test-Path "$($_.FullName)\allure-results" } |
        Select-Object -First 1

    if ($null -eq $latestDir) {
        Write-Host "${RED}ERROR: No allure-results sub-folder found under '$allureBase'.${RESET}"
        exit 1
    }

    $ResultsPath = "$allureBase\$($latestDir.Name)\allure-results"
    $runTimestamp = $latestDir.Name
} else {
    # Derive timestamp from provided path
    $runTimestamp = (Get-Item $ResultsPath).Parent.Name
}

Write-Host "  Results dir : ${GREEN}$ResultsPath${RESET}"

# ── 2. Generate timestamp for output folder ──────────────────
$reportTimestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportDir       = "target\reports\allure-html\$reportTimestamp"

Write-Host "  Report dir  : ${GREEN}$reportDir${RESET}"
Write-Host ""

# ── 3. Run mvn allure:report ─────────────────────────────────
Write-Host "${CYAN}Generating Allure HTML report...${RESET}"

$resultsAbsolute = (Resolve-Path $ResultsPath).Path

mvn io.qameta.allure:allure-maven:2.15.0:report `
    "-Dallure.results.directory=$resultsAbsolute" `
    "-Dreport.timestamp=$reportTimestamp" `
    --no-transfer-progress

if ($LASTEXITCODE -ne 0) {
    Write-Host "${RED}ERROR: allure-maven report generation failed (exit $LASTEXITCODE).${RESET}"
    exit 1
}

# ── 4. Locate the generated report ───────────────────────────
# allure-maven writes to target/site/allure-maven-plugin by default when
# reportDirectory uses a property; copy it to our timestamped folder.
$mvnDefaultOut = "target\site\allure-maven-plugin"

if (Test-Path $mvnDefaultOut) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
    Copy-Item -Path "$mvnDefaultOut\*" -Destination $reportDir -Recurse -Force
    Write-Host "${GREEN}Report copied to: $reportDir${RESET}"
} elseif (Test-Path $reportDir) {
    Write-Host "${GREEN}Report generated at: $reportDir${RESET}"
} else {
    Write-Host "${RED}ERROR: Could not locate generated report.${RESET}"
    exit 1
}

# ── 5. Print index.html path ─────────────────────────────────
$indexHtml = "$reportDir\index.html"
if (-not (Test-Path $indexHtml)) {
    # Search recursively
    $found = Get-ChildItem $reportDir -Recurse -Filter "index.html" | Select-Object -First 1
    if ($found) { $indexHtml = $found.FullName }
}

Write-Host ""
Write-Host "${CYAN}${BOLD}============================================${RESET}"
Write-Host "${GREEN}${BOLD}  ALLURE REPORT GENERATED SUCCESSFULLY     ${RESET}"
Write-Host "${CYAN}${BOLD}============================================${RESET}"
Write-Host "  Timestamp   : ${GREEN}$reportTimestamp${RESET}"
Write-Host "  Report Path : ${GREEN}$indexHtml${RESET}"
Write-Host "${CYAN}${BOLD}============================================${RESET}"

# ── 6. Open the report ───────────────────────────────────────
if ($Open -or $true) {
    Write-Host ""
    Write-Host "${CYAN}Opening Allure report in browser...${RESET}"
    Start-Process $indexHtml
}

#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Quick test launcher for EcoCash automation
.DESCRIPTION
    Simplified test runner with common presets
.PARAMETER Test
    Test type: smoke, login, logout, regression, all (default: smoke)
.EXAMPLE
    .\quick_test.ps1
    .\quick_test.ps1 smoke
    .\quick_test.ps1 login
    .\quick_test.ps1 regression
#>

param(
    [Parameter(Position=0)]
    [ValidateSet('smoke', 'login', 'logout', 'regression', 'all')]
    [string]$Test = 'smoke'
)

Write-Host ""
Write-Host "================================================" -ForegroundColor Yellow
Write-Host "EcoCash Quick Test Launcher" -ForegroundColor Yellow
Write-Host "================================================" -ForegroundColor Yellow
Write-Host ""

# Map test types to tags
$tagMap = @{
    'smoke' = '@smoke'
    'login' = '@login'
    'logout' = '@logout'
    'regression' = '@regression'
    'all' = ''
}

$tags = $tagMap[$Test]

Write-Host "Running: $Test tests" -ForegroundColor Cyan
if ($tags) {
    Write-Host "Tags: $tags" -ForegroundColor Cyan
}
Write-Host ""

# Run tests using the main runner
& .\run_ecocash.ps1 -Device real -Tags $tags -Clean -Report

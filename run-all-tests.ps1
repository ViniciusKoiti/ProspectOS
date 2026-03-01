# 🚀 ProspectOS MVP Week 1 - Master Test Script
# Execute este script para rodar todos os testes automaticamente

param(
    [switch]$Quick,      # Executa apenas testes básicos
    [switch]$Demo,       # Executa cenários de demonstração
    [switch]$Full        # Executa suite completa
)

# Configuração
$baseUrl = "http://localhost:8080"
$testResults = @()

# Funções auxiliares
function Write-TestResult {
    param($TestName, $Status, $Details = "")
    
    $result = @{
        Test = $TestName
        Status = $Status
        Details = $Details
        Timestamp = Get-Date
    }
    
    $script:testResults += $result
    
    $color = if ($Status -eq "PASS") { "Green" } elseif ($Status -eq "FAIL") { "Red" } else { "Yellow" }
    Write-Host "[$Status] $TestName" -ForegroundColor $color
    if ($Details) { Write-Host "    $Details" -ForegroundColor Gray }
}

function Test-Endpoint {
    param($Url, $TestName)
    
    try {
        $response = Invoke-RestMethod -Uri $Url -TimeoutSec 10
        Write-TestResult $TestName "PASS" "Response received"
        return $true
    }
    catch {
        Write-TestResult $TestName "FAIL" $_.Exception.Message
        return $false
    }
}

function Test-SearchEndpoint {
    param($Query, $TestName, $ExpectedPattern = $null)
    
    $body = @{
        query = $Query
        limit = 5
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/api/leads/search" -Method Post -Body $body -ContentType "application/json"
        
        if ($response.status -eq "COMPLETED") {
            if ($ExpectedPattern -and $response.leads.Count -eq 0) {
                Write-TestResult $TestName "WARN" "No results found for: $Query"
            } else {
                Write-TestResult $TestName "PASS" "Found $($response.leads.Count) leads"
            }
        } else {
            Write-TestResult $TestName "FAIL" "Status: $($response.status)"
        }
    }
    catch {
        Write-TestResult $TestName "FAIL" $_.Exception.Message
    }
}

# Main Test Execution
Write-Host "🚀 ProspectOS MVP Week 1 - Automated Tests" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# STEP 1: Environment Check
Write-Host "`n📋 STEP 1: Environment Check" -ForegroundColor Yellow

# Check if application is running
if (Test-Endpoint "$baseUrl/api/companies" "Application Running Check") {
    Write-Host "✅ Application is already running" -ForegroundColor Green
} else {
    Write-Host "❌ Application not running. Please start with:" -ForegroundColor Red
    Write-Host "./gradlew bootRun --args='--spring.profiles.active=mock'" -ForegroundColor White
    exit 1
}

# STEP 2: Basic Endpoints
Write-Host "`n📡 STEP 2: Basic Endpoints" -ForegroundColor Yellow

Test-Endpoint "$baseUrl/api/companies" "Companies Endpoint"
Test-Endpoint "$baseUrl/api/icps" "ICPs Endpoint"
Test-Endpoint "$baseUrl/h2-console" "H2 Console Access"

# Data Volume Check
try {
    $companies = Invoke-RestMethod -Uri "$baseUrl/api/companies"
    $icps = Invoke-RestMethod -Uri "$baseUrl/api/icps"
    
    if ($companies.Count -ge 10) {
        Write-TestResult "Data Volume - Companies" "PASS" "$($companies.Count) companies found"
    } else {
        Write-TestResult "Data Volume - Companies" "WARN" "Only $($companies.Count) companies"
    }
    
    if ($icps.Count -ge 3) {
        Write-TestResult "Data Volume - ICPs" "PASS" "$($icps.Count) ICPs found"
    } else {
        Write-TestResult "Data Volume - ICPs" "WARN" "Only $($icps.Count) ICPs"
    }
} catch {
    Write-TestResult "Data Volume Check" "FAIL" $_.Exception.Message
}

if ($Quick) {
    Write-Host "`n✅ Quick tests completed!" -ForegroundColor Green
    return
}

# STEP 3: Lead Search Tests
Write-Host "`n🔍 STEP 3: Lead Search Functionality" -ForegroundColor Yellow

Test-SearchEndpoint "fintech" "Fintech Search"
Test-SearchEndpoint "tecnologia" "Technology Search" 
Test-SearchEndpoint "agronegócio" "Agribusiness Search"
Test-SearchEndpoint "startup" "Startup Search"

# STEP 4: CNPJ Integration
Write-Host "`n🇧🇷 STEP 4: CNPJ.ws Integration" -ForegroundColor Yellow

$cnpjBody = @{
    query = "tecnologia SP"
    sources = @("cnpj-ws")
    limit = 3
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/leads/search" -Method Post -Body $cnpjBody -ContentType "application/json"
    if ($response.status -eq "COMPLETED") {
        $cnpjResults = $response.leads | Where-Object { $_.source.sourceName -eq "cnpj-ws" }
        if ($cnpjResults.Count -gt 0) {
            Write-TestResult "CNPJ.ws Source" "PASS" "$($cnpjResults.Count) results from CNPJ source"
        } else {
            Write-TestResult "CNPJ.ws Source" "WARN" "No CNPJ-specific results found"
        }
    }
} catch {
    Write-TestResult "CNPJ.ws Integration" "FAIL" $_.Exception.Message
}

if ($Demo) {
    # STEP 5: Demo Scenarios
    Write-Host "`n🎪 STEP 5: Demo Scenarios" -ForegroundColor Yellow
    
    # Demo 1: CTOs Startups
    Test-SearchEndpoint "CTO startup tecnologia" "Demo - CTO Startups"
    
    # Demo 2: Agro Directors  
    Test-SearchEndpoint "diretor agronegócio" "Demo - Agro Directors"
    
    # Demo 3: Fintech Founders
    Test-SearchEndpoint "founder fintech" "Demo - Fintech Founders"
    
    # Performance Test
    Write-Host "`n⚡ Performance Test" -ForegroundColor Yellow
    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    
    try {
        $testBody = @{ query = "tecnologia"; limit = 10 } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "$baseUrl/api/leads/search" -Method Post -Body $testBody -ContentType "application/json"
        $stopwatch.Stop()
        
        $elapsed = $stopwatch.ElapsedMilliseconds
        if ($elapsed -lt 5000) {
            Write-TestResult "Search Performance" "PASS" "${elapsed}ms (< 5s target)"
        } else {
            Write-TestResult "Search Performance" "WARN" "${elapsed}ms (>= 5s)"
        }
    } catch {
        Write-TestResult "Search Performance" "FAIL" $_.Exception.Message
    }
}

# Generate Report
Write-Host "`n📊 Test Summary Report" -ForegroundColor Cyan
Write-Host "=====================" -ForegroundColor Cyan

$passCount = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count  
$warnCount = ($testResults | Where-Object { $_.Status -eq "WARN" }).Count
$totalCount = $testResults.Count

Write-Host "Total Tests: $totalCount" -ForegroundColor White
Write-Host "Passed: $passCount" -ForegroundColor Green
Write-Host "Failed: $failCount" -ForegroundColor Red
Write-Host "Warnings: $warnCount" -ForegroundColor Yellow

if ($failCount -eq 0) {
    Write-Host "`n🎉 All critical tests passed! MVP Week 1 is ready for demo." -ForegroundColor Green
} else {
    Write-Host "`n❌ Some tests failed. Please review the failures above." -ForegroundColor Red
    
    Write-Host "`nFailed Tests:" -ForegroundColor Red
    $testResults | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "  - $($_.Test): $($_.Details)" -ForegroundColor Red
    }
}

# Save detailed report
$reportFile = "test-results-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$testResults | ConvertTo-Json -Depth 3 | Out-File $reportFile
Write-Host "`n📄 Detailed report saved to: $reportFile" -ForegroundColor Cyan
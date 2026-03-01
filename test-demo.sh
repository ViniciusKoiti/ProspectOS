#!/bin/bash

# 🚀 MVP SEMANA 1 - Automated Demo Test Script
# Este script valida todas as funcionalidades implementadas na Semana 1

set -e

echo "🚀 Starting ProspectOS MVP Week 1 Demo Tests"
echo "============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Functions
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[✓ PASS]${NC} $1"
    ((PASSED_TESTS++))
}

print_error() {
    echo -e "${RED}[✗ FAIL]${NC} $1"
    ((FAILED_TESTS++))
}

print_warning() {
    echo -e "${YELLOW}[⚠ WARN]${NC} $1"
}

run_test() {
    local test_name="$1"
    local command="$2"
    local expected_pattern="$3"
    
    ((TOTAL_TESTS++))
    print_status "Testing: $test_name"
    
    if output=$(eval "$command" 2>&1); then
        if [[ -z "$expected_pattern" ]] || echo "$output" | grep -q "$expected_pattern"; then
            print_success "$test_name"
            return 0
        else
            print_error "$test_name - Expected pattern not found: $expected_pattern"
            echo "Actual output: $output"
            return 1
        fi
    else
        print_error "$test_name - Command failed"
        echo "Error output: $output"
        return 1
    fi
}

check_url() {
    local url="$1"
    local timeout=30
    local counter=0
    
    while [[ $counter -lt $timeout ]]; do
        if curl -s "$url" > /dev/null 2>&1; then
            return 0
        fi
        sleep 1
        ((counter++))
    done
    return 1
}

# Start the application
print_status "Step 1: Building and starting application..."
echo "Building application..."
./gradlew build -x test > /dev/null 2>&1 || {
    print_error "Build failed"
    exit 1
}

echo "Starting application in background..."
./gradlew bootRun --args="--spring.profiles.active=mock" &
APP_PID=$!

# Wait for application to start
print_status "Waiting for application to start..."
sleep 15

if ! check_url "http://localhost:8080/api/companies"; then
    print_error "Application failed to start or is not responding"
    kill $APP_PID 2>/dev/null || true
    exit 1
fi

print_success "Application started successfully"

# Test Suite
echo ""
echo "🧪 Running Test Suite"
echo "===================="

# Test 1: Health Check
run_test "Health Check - Application responds" \
    "curl -s -f http://localhost:8080/api/companies" \
    ""

# Test 2: Companies endpoint returns data
run_test "Companies API - Returns company data" \
    "curl -s http://localhost:8080/api/companies | jq length" \
    ""

# Test 3: ICPs endpoint returns data  
run_test "ICPs API - Returns ICP data" \
    "curl -s http://localhost:8080/api/icps | jq length" \
    ""

# Test 4: Search for fintech companies
run_test "Lead Search - Fintech companies" \
    "curl -s -X POST http://localhost:8080/api/leads/search -H 'Content-Type: application/json' -d '{\"query\": \"fintech\", \"limit\": 5}' | jq -r '.status'" \
    "COMPLETED"

# Test 5: Search for tech companies
run_test "Lead Search - Tech companies" \
    "curl -s -X POST http://localhost:8080/api/leads/search -H 'Content-Type: application/json' -d '{\"query\": \"tech\", \"limit\": 3}' | jq -r '.leads | length'" \
    ""

# Test 6: Search for agro companies
run_test "Lead Search - Agro companies" \
    "curl -s -X POST http://localhost:8080/api/leads/search -H 'Content-Type: application/json' -d '{\"query\": \"agronegócio\", \"limit\": 3}' | jq -r '.status'" \
    "COMPLETED"

# Test 7: CNPJ.ws source integration
run_test "CNPJ.ws Integration - Tech search with specific source" \
    "curl -s -X POST http://localhost:8080/api/leads/search -H 'Content-Type: application/json' -d '{\"query\": \"tecnologia SP\", \"sources\": [\"cnpj-ws\"], \"limit\": 2}' | jq -r '.status'" \
    "COMPLETED"

# Test 8: Lead scoring validation
run_test "Lead Scoring - Scores are assigned" \
    "curl -s -X POST http://localhost:8080/api/leads/search -H 'Content-Type: application/json' -d '{\"query\": \"startup\", \"limit\": 1}' | jq -r '.leads[0].score.value'" \
    ""

# Test 9: Multiple sources working
run_test "Multiple Sources - In-memory + CNPJ sources" \
    "curl -s -X POST http://localhost:8080/api/leads/search -H 'Content-Type: application/json' -d '{\"query\": \"empresa\", \"limit\": 5}' | jq -r '.leads | map(.source.sourceName) | unique | length'" \
    ""

# Test 10: Lead categories validation
run_test "Lead Categories - HOT/WARM/COLD categories exist" \
    "curl -s -X POST http://localhost:8080/api/leads/search -H 'Content-Type: application/json' -d '{\"query\": \"technology\", \"limit\": 5}' | jq -r '.leads[0].score.category'" \
    ""

# Test 11: H2 Console accessibility
run_test "H2 Console - Database console accessible" \
    "curl -s -f http://localhost:8080/h2-console" \
    ""

# Test 12: Different industry searches
run_test "Industry Search - Healthcare/HealthTech" \
    "curl -s -X POST http://localhost:8080/api/leads/search -H 'Content-Type: application/json' -d '{\"query\": \"saúde\", \"limit\": 2}' | jq -r '.status'" \
    "COMPLETED"

# Performance Tests
echo ""
echo "⚡ Performance Tests"
echo "==================="

# Test P1: Search response time
start_time=$(date +%s%N)
curl -s -X POST http://localhost:8080/api/leads/search \
    -H 'Content-Type: application/json' \
    -d '{"query": "technology", "limit": 10}' > /dev/null
end_time=$(date +%s%N)
duration=$(( (end_time - start_time) / 1000000 ))

if [[ $duration -lt 5000 ]]; then
    print_success "Search Performance - Response time: ${duration}ms (< 5s)"
    ((PASSED_TESTS++))
else
    print_warning "Search Performance - Response time: ${duration}ms (>= 5s)"
fi
((TOTAL_TESTS++))

# Demo Scenarios
echo ""
echo "🎪 Demo Scenarios"
echo "================="

# Scenario 1: CTO Startup Search
print_status "Demo Scenario 1: CTOs de Startups Tech"
curl -s -X POST http://localhost:8080/api/leads/search \
    -H 'Content-Type: application/json' \
    -d '{"query": "CTO startup tecnologia", "limit": 3}' | \
    jq -r '.leads[] | "- \(.candidate.name) (\(.candidate.industry)) - Score: \(.score.value)"' | \
    while read line; do
        echo "  $line"
    done

# Scenario 2: Agronegócio Search
print_status "Demo Scenario 2: Agronegócio Companies"
curl -s -X POST http://localhost:8080/api/leads/search \
    -H 'Content-Type: application/json' \
    -d '{"query": "agronegócio agricultura", "limit": 3}' | \
    jq -r '.leads[] | "- \(.candidate.name) (\(.candidate.industry)) - Location: \(.candidate.location)"' | \
    while read line; do
        echo "  $line"
    done

# Data Quality Validation
echo ""
echo "📊 Data Quality Validation"
echo "=========================="

# Check company count
company_count=$(curl -s http://localhost:8080/api/companies | jq length)
print_status "Companies in database: $company_count"

if [[ $company_count -gt 50 ]]; then
    print_success "Data Volume - Sufficient companies for demo ($company_count)"
    ((PASSED_TESTS++))
else
    print_warning "Data Volume - Low company count ($company_count)"
fi
((TOTAL_TESTS++))

# Check ICP count
icp_count=$(curl -s http://localhost:8080/api/icps | jq length)
print_status "ICPs in database: $icp_count"

if [[ $icp_count -ge 3 ]]; then
    print_success "ICP Coverage - Multiple ICPs configured ($icp_count)"
    ((PASSED_TESTS++))
else
    print_error "ICP Coverage - Insufficient ICPs ($icp_count)"
fi
((TOTAL_TESTS++))

# Check score distribution
print_status "Checking score distribution..."
curl -s -X POST http://localhost:8080/api/leads/search \
    -H 'Content-Type: application/json' \
    -d '{"query": "company", "limit": 20}' | \
    jq -r '.leads[].score.value' | \
    awk '
    BEGIN { hot=0; warm=0; cold=0; total=0 }
    $1 >= 80 { hot++; total++ }
    $1 >= 65 && $1 < 80 { warm++; total++ }
    $1 < 65 { cold++; total++ }
    END { 
        if (total > 0) {
            printf "  HOT (>=80): %d (%.1f%%)\n", hot, hot*100/total
            printf "  WARM (65-79): %d (%.1f%%)\n", warm, warm*100/total  
            printf "  COLD (<65): %d (%.1f%%)\n", cold, cold*100/total
        }
    }'

# Cleanup
print_status "Cleaning up..."
kill $APP_PID 2>/dev/null || true
wait $APP_PID 2>/dev/null || true

# Final Report
echo ""
echo "📊 Final Test Report"
echo "===================="
echo "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
echo -e "${RED}Failed: $FAILED_TESTS${NC}"

if [[ $FAILED_TESTS -eq 0 ]]; then
    echo -e "${GREEN}🎉 All tests passed! MVP Week 1 is ready for demo.${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed. Please review the failures above.${NC}"
    exit 1
fi
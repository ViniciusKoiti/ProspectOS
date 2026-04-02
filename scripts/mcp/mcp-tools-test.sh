#!/bin/bash

# MCP Tools Testing Script
# Tests all available MCP tools with sample data

set -e

# Configuration
MCP_BASE_URL="${MCP_BASE_URL:-http://localhost:8082}"
API_KEY="${MCP_API_KEY:-mcp-dev-key-2024}"
TIMEOUT="${TIMEOUT:-30}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_test() { echo -e "${PURPLE}[TEST]${NC} $1"; }

# Test counter
test_count=0
success_count=0

# Generic tool test function
test_tool() {
    local tool_name="$1"
    local payload="$2"
    local description="$3"
    
    ((test_count++))
    log_test "Testing $tool_name: $description"
    
    local response=$(curl -s --max-time $TIMEOUT \
        -H "Content-Type: application/json" \
        -H "X-MCP-API-KEY: $API_KEY" \
        -d "$payload" \
        "$MCP_BASE_URL/mcp/tools/call" 2>/dev/null || echo "")
    
    if [ -n "$response" ]; then
        local has_result=$(echo "$response" | jq -r '.result // empty' 2>/dev/null)
        local has_error=$(echo "$response" | jq -r '.error // empty' 2>/dev/null)
        
        if [ -n "$has_result" ]; then
            ((success_count++))
            log_success "$tool_name executed successfully"
            
            # Pretty print key results
            case $tool_name in
                "get_query_metrics")
                    local queries=$(echo "$response" | jq -r '.result.totalQueries // "N/A"' 2>/dev/null)
                    local cost=$(echo "$response" | jq -r '.result.totalCost // "N/A"' 2>/dev/null)
                    local success_rate=$(echo "$response" | jq -r '.result.successRate // "N/A"' 2>/dev/null)
                    log_info "  Queries: $queries, Cost: \$$cost, Success Rate: $success_rate"
                    ;;
                "update_provider_routing")
                    local new_strategy=$(echo "$response" | jq -r '.result.newStrategy // "N/A"' 2>/dev/null)
                    local savings=$(echo "$response" | jq -r '.result.estimatedSavingsPercent // "N/A"' 2>/dev/null)
                    log_info "  Strategy: $new_strategy, Estimated Savings: $savings%"
                    ;;
                "get_provider_health")
                    local provider_count=$(echo "$response" | jq -r '.result | length' 2>/dev/null || echo "0")
                    log_info "  Providers checked: $provider_count"
                    ;;
                "search_international_leads")
                    local leads_found=$(echo "$response" | jq -r '.result.leads | length' 2>/dev/null || echo "0")
                    local country=$(echo "$response" | jq -r '.result.country // "N/A"' 2>/dev/null)
                    local industry=$(echo "$response" | jq -r '.result.industry // "N/A"' 2>/dev/null)
                    log_info "  Found $leads_found leads in $country ($industry sector)"
                    ;;
                "enrich_international_lead")
                    local enrichment_score=$(echo "$response" | jq -r '.result.enrichmentScore // "N/A"' 2>/dev/null)
                    local contacts_count=$(echo "$response" | jq -r '.result.contacts | length' 2>/dev/null || echo "0")
                    log_info "  Enrichment Score: $enrichment_score, Contacts: $contacts_count"
                    ;;
                "optimize_search_strategy")
                    local sources_count=$(echo "$response" | jq -r '.result.recommendedSources | length' 2>/dev/null || echo "0")
                    local estimated_cost=$(echo "$response" | jq -r '.result.estimatedCost // "N/A"' 2>/dev/null)
                    log_info "  Recommended Sources: $sources_count, Estimated Cost: \$$estimated_cost"
                    ;;
                "analyze_market_coverage")
                    local market_size=$(echo "$response" | jq -r '.result.totalMarketSize // "N/A"' 2>/dev/null)
                    local coverage=$(echo "$response" | jq -r '.result.coveredMarketShare // "N/A"' 2>/dev/null)
                    log_info "  Market Size: $market_size, Coverage: $coverage"
                    ;;
                "test_provider_configuration")
                    local test_status=$(echo "$response" | jq -r '.result.testStatus // "N/A"' 2>/dev/null)
                    local success_rate=$(echo "$response" | jq -r '.result.overallSuccessRate // "N/A"' 2>/dev/null)
                    log_info "  Test Status: $test_status, Success Rate: $success_rate"
                    ;;
            esac
            return 0
        elif [ -n "$has_error" ]; then
            local error_msg=$(echo "$response" | jq -r '.error.message // "Unknown error"' 2>/dev/null)
            log_error "$tool_name failed: $error_msg"
            return 1
        else
            log_warning "$tool_name response format unexpected"
            echo "Response: $response" | head -c 200
            return 1
        fi
    else
        log_error "$tool_name failed - no response"
        return 1
    fi
}

# Test resource access
test_resource() {
    local resource_uri="$1"
    local description="$2"
    
    ((test_count++))
    log_test "Testing resource: $resource_uri ($description)"
    
    local response=$(curl -s --max-time $TIMEOUT \
        -H "X-MCP-API-KEY: $API_KEY" \
        "$MCP_BASE_URL/mcp/resources/$resource_uri" 2>/dev/null || echo "")
    
    if [ -n "$response" ]; then
        local is_error=$(echo "$response" | grep -i "error" || echo "")
        if [ -z "$is_error" ]; then
            ((success_count++))
            log_success "Resource $resource_uri accessed successfully"
            return 0
        else
            log_error "Resource $resource_uri returned error: $response"
            return 1
        fi
    else
        log_error "Resource $resource_uri failed - no response"
        return 1
    fi
}

# Main testing routine
main() {
    echo "=========================================="
    echo "         MCP Tools Test Suite"
    echo "=========================================="
    echo "Server: $MCP_BASE_URL"
    echo "API Key: ${API_KEY:0:8}..."
    echo "Timeout: ${TIMEOUT}s"
    echo ""
    
    # Test 1: Query Metrics
    test_tool "get_query_metrics" '{
        "jsonrpc": "2.0",
        "id": "test1",
        "method": "tools/call",
        "params": {
            "name": "get_query_metrics",
            "arguments": {
                "timeWindow": "24h",
                "provider": "nominatim"
            }
        }
    }' "Get 24-hour metrics for nominatim provider"
    echo ""
    
    # Test 2: Provider Routing
    test_tool "update_provider_routing" '{
        "jsonrpc": "2.0",
        "id": "test2",
        "method": "tools/call",
        "params": {
            "name": "update_provider_routing",
            "arguments": {
                "strategy": "COST_OPTIMIZED",
                "providerPriority": "nominatim,bing-maps",
                "conditions": "max_cost=0.01,min_success_rate=0.95"
            }
        }
    }' "Update routing to cost-optimized strategy"
    echo ""
    
    # Test 3: Provider Health
    test_tool "get_provider_health" '{
        "jsonrpc": "2.0",
        "id": "test3",
        "method": "tools/call",
        "params": {
            "name": "get_provider_health",
            "arguments": {}
        }
    }' "Check health status of all providers"
    echo ""
    
    # Test 4: Provider Configuration Test
    test_tool "test_provider_configuration" '{
        "jsonrpc": "2.0",
        "id": "test4",
        "method": "tools/call",
        "params": {
            "name": "test_provider_configuration",
            "arguments": {
                "providers": "nominatim,bing-maps"
            }
        }
    }' "Test configuration for nominatim and bing-maps"
    echo ""
    
    # Test 5: International Lead Search
    test_tool "search_international_leads" '{
        "jsonrpc": "2.0",
        "id": "test5",
        "method": "tools/call",
        "params": {
            "name": "search_international_leads",
            "arguments": {
                "country": "brazil",
                "industry": "technology",
                "maxResults": "10",
                "budgetLimit": "25.0",
                "minQualityScore": "0.8"
            }
        }
    }' "Search for tech companies in Brazil"
    echo ""
    
    # Test 6: Lead Enrichment
    test_tool "enrich_international_lead" '{
        "jsonrpc": "2.0",
        "id": "test6",
        "method": "tools/call",
        "params": {
            "name": "enrich_international_lead",
            "arguments": {
                "leadId": "test-lead-123",
                "companyName": "Tech Innovations Brazil",
                "website": "https://techinnovations.com.br",
                "sources": "linkedin,web-scraping"
            }
        }
    }' "Enrich lead with additional intelligence"
    echo ""
    
    # Test 7: Search Strategy Optimization
    test_tool "optimize_search_strategy" '{
        "jsonrpc": "2.0",
        "id": "test7",
        "method": "tools/call",
        "params": {
            "name": "optimize_search_strategy",
            "arguments": {
                "market": "latin-america",
                "budget": "150.0",
                "qualityThreshold": "0.85"
            }
        }
    }' "Optimize search strategy for Latin America"
    echo ""
    
    # Test 8: Market Coverage Analysis
    test_tool "analyze_market_coverage" '{
        "jsonrpc": "2.0",
        "id": "test8",
        "method": "tools/call",
        "params": {
            "name": "analyze_market_coverage",
            "arguments": {
                "country": "brazil",
                "competitors": "Local Leader,International Corp,Regional Player"
            }
        }
    }' "Analyze market coverage in Brazil"
    echo ""
    
    # Test Resources (if implemented)
    echo "=========================================="
    echo "         Testing MCP Resources"
    echo "=========================================="
    
    test_resource "query-history://24h/nominatim" "24-hour query history for nominatim"
    echo ""
    
    test_resource "provider-performance://nominatim/response_time" "Response time performance for nominatim"
    echo ""
    
    test_resource "market-analysis://brazil/technology" "Market analysis for Brazil tech sector"
    echo ""
    
    # Summary
    echo "=========================================="
    echo "           Test Results Summary"
    echo "=========================================="
    
    local success_rate=$(( (success_count * 100) / test_count ))
    
    if [ $success_count -eq $test_count ]; then
        log_success "All tests passed ($success_count/$test_count) - 100% success rate"
        echo ""
        echo "✅ All MCP tools are functioning correctly"
        exit 0
    elif [ $success_rate -ge 80 ]; then
        log_success "Most tests passed ($success_count/$test_count) - $success_rate% success rate"
        echo ""
        echo "✅ MCP tools are mostly functional with minor issues"
        exit 0
    elif [ $success_rate -ge 50 ]; then
        log_warning "Some tests failed ($success_count/$test_count) - $success_rate% success rate"
        echo ""
        echo "⚠️  MCP tools have significant issues that need attention"
        exit 1
    else
        log_error "Many tests failed ($success_count/$test_count) - $success_rate% success rate"
        echo ""
        echo "❌ MCP tools are not functioning properly"
        exit 2
    fi
}

# Check dependencies
if ! command -v curl &> /dev/null; then
    log_error "curl is required but not installed"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    log_warning "jq is not installed - JSON parsing will be limited"
fi

# Run main function
main "$@"
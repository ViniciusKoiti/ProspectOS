#!/bin/bash

# MCP Health Check Script
# Verifies MCP server status and basic functionality

set -e

# Configuration
MCP_BASE_URL="${MCP_BASE_URL:-http://localhost:8082}"
API_KEY="${MCP_API_KEY:-mcp-dev-key-2024}"
TIMEOUT="${TIMEOUT:-10}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if server is running
check_server_status() {
    log_info "Checking MCP server status..."
    
    if curl -s --max-time $TIMEOUT "$MCP_BASE_URL/actuator/health" > /dev/null; then
        log_success "MCP server is running at $MCP_BASE_URL"
        return 0
    else
        log_error "MCP server is not responding at $MCP_BASE_URL"
        return 1
    fi
}

# Check MCP-specific health endpoint
check_mcp_health() {
    log_info "Checking MCP-specific health..."
    
    local response=$(curl -s --max-time $TIMEOUT "$MCP_BASE_URL/actuator/health/mcp" 2>/dev/null || echo "")
    
    if [ -n "$response" ]; then
        local status=$(echo "$response" | jq -r '.status // "UNKNOWN"' 2>/dev/null || echo "UNKNOWN")
        if [ "$status" = "UP" ]; then
            log_success "MCP health check passed"
            return 0
        else
            log_warning "MCP health status: $status"
            return 1
        fi
    else
        log_warning "MCP health endpoint not available"
        return 1
    fi
}

# Test tools discovery endpoint
test_tools_discovery() {
    log_info "Testing tools discovery..."
    
    local response=$(curl -s --max-time $TIMEOUT \
        -H "X-MCP-API-KEY: $API_KEY" \
        "$MCP_BASE_URL/mcp/tools/list" 2>/dev/null || echo "")
    
    if [ -n "$response" ]; then
        local tool_count=$(echo "$response" | jq '. | length' 2>/dev/null || echo "0")
        if [ "$tool_count" -gt 0 ]; then
            log_success "Tools discovery successful - found $tool_count tools"
            echo "$response" | jq -r '.[] | "  - " + .name + ": " + .description' 2>/dev/null || echo "$response"
            return 0
        else
            log_warning "Tools discovery returned empty results"
            return 1
        fi
    else
        log_error "Tools discovery failed - no response"
        return 1
    fi
}

# Test authentication
test_authentication() {
    log_info "Testing authentication..."
    
    # Test with invalid API key
    local response=$(curl -s -w "%{http_code}" --max-time $TIMEOUT \
        -H "X-MCP-API-KEY: invalid-key" \
        "$MCP_BASE_URL/mcp/tools/list" 2>/dev/null || echo "000")
    
    local http_code="${response: -3}"
    if [ "$http_code" = "401" ]; then
        log_success "Authentication properly rejects invalid API keys"
    else
        log_warning "Authentication test inconclusive (HTTP $http_code)"
    fi
    
    # Test with valid API key
    local response=$(curl -s -w "%{http_code}" --max-time $TIMEOUT \
        -H "X-MCP-API-KEY: $API_KEY" \
        "$MCP_BASE_URL/mcp/tools/list" 2>/dev/null || echo "000")
    
    local http_code="${response: -3}"
    if [ "$http_code" = "200" ]; then
        log_success "Authentication accepts valid API keys"
        return 0
    else
        log_error "Authentication failed with valid API key (HTTP $http_code)"
        return 1
    fi
}

# Test rate limiting (optional - may trigger actual limits)
test_rate_limiting() {
    log_info "Testing rate limiting (light test)..."
    
    local response=$(curl -s -I --max-time $TIMEOUT \
        -H "X-MCP-API-KEY: $API_KEY" \
        "$MCP_BASE_URL/mcp/tools/list" 2>/dev/null || echo "")
    
    if echo "$response" | grep -q "X-RateLimit-Limit"; then
        local limit=$(echo "$response" | grep -i "X-RateLimit-Limit" | cut -d' ' -f2 | tr -d '\r')
        local remaining=$(echo "$response" | grep -i "X-RateLimit-Remaining" | cut -d' ' -f2 | tr -d '\r')
        log_success "Rate limiting headers present - Limit: $limit, Remaining: $remaining"
        return 0
    else
        log_warning "Rate limiting headers not found"
        return 1
    fi
}

# Test a simple tool execution
test_tool_execution() {
    log_info "Testing tool execution (get_query_metrics)..."
    
    local payload='{
        "jsonrpc": "2.0",
        "id": "health-check-1",
        "method": "tools/call",
        "params": {
            "name": "get_query_metrics",
            "arguments": {
                "timeWindow": "1h"
            }
        }
    }'
    
    local response=$(curl -s --max-time $TIMEOUT \
        -H "Content-Type: application/json" \
        -H "X-MCP-API-KEY: $API_KEY" \
        -d "$payload" \
        "$MCP_BASE_URL/mcp/tools/call" 2>/dev/null || echo "")
    
    if [ -n "$response" ]; then
        local has_result=$(echo "$response" | jq -r '.result // empty' 2>/dev/null)
        local has_error=$(echo "$response" | jq -r '.error // empty' 2>/dev/null)
        
        if [ -n "$has_result" ]; then
            log_success "Tool execution successful"
            local total_queries=$(echo "$response" | jq -r '.result.totalQueries // "N/A"' 2>/dev/null)
            log_info "  Total queries: $total_queries"
            return 0
        elif [ -n "$has_error" ]; then
            local error_msg=$(echo "$response" | jq -r '.error.message // "Unknown error"' 2>/dev/null)
            log_error "Tool execution failed: $error_msg"
            return 1
        else
            log_warning "Tool execution response format unexpected"
            return 1
        fi
    else
        log_error "Tool execution failed - no response"
        return 1
    fi
}

# Performance test
test_performance() {
    log_info "Testing response time performance..."
    
    local start_time=$(date +%s%3N)
    
    curl -s --max-time $TIMEOUT \
        -H "X-MCP-API-KEY: $API_KEY" \
        "$MCP_BASE_URL/mcp/tools/list" > /dev/null 2>&1
    
    local end_time=$(date +%s%3N)
    local duration=$((end_time - start_time))
    
    if [ $duration -lt 1000 ]; then
        log_success "Response time: ${duration}ms (excellent)"
    elif [ $duration -lt 2000 ]; then
        log_success "Response time: ${duration}ms (good)"
    elif [ $duration -lt 5000 ]; then
        log_warning "Response time: ${duration}ms (acceptable)"
    else
        log_warning "Response time: ${duration}ms (slow)"
    fi
}

# Main health check routine
main() {
    echo "=========================================="
    echo "       MCP Health Check Report"
    echo "=========================================="
    echo "Server: $MCP_BASE_URL"
    echo "API Key: ${API_KEY:0:8}..."
    echo "Timeout: ${TIMEOUT}s"
    echo ""
    
    local success_count=0
    local total_checks=7
    
    # Run all checks
    check_server_status && ((success_count++))
    echo ""
    
    check_mcp_health && ((success_count++))
    echo ""
    
    test_tools_discovery && ((success_count++))
    echo ""
    
    test_authentication && ((success_count++))
    echo ""
    
    test_rate_limiting && ((success_count++))
    echo ""
    
    test_tool_execution && ((success_count++))
    echo ""
    
    test_performance && ((success_count++))
    echo ""
    
    # Summary
    echo "=========================================="
    echo "           Health Check Summary"
    echo "=========================================="
    
    if [ $success_count -eq $total_checks ]; then
        log_success "All checks passed ($success_count/$total_checks)"
        echo ""
        echo "✅ MCP server is fully operational"
        exit 0
    elif [ $success_count -ge $((total_checks * 3 / 4)) ]; then
        log_warning "Most checks passed ($success_count/$total_checks)"
        echo ""
        echo "⚠️  MCP server is mostly operational with minor issues"
        exit 1
    else
        log_error "Multiple checks failed ($success_count/$total_checks)"
        echo ""
        echo "❌ MCP server has significant issues"
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
#!/usr/bin/env python3
"""
MCP Client Demo - Python implementation
Demonstrates how to interact with the ProspectOS MCP server
"""

import json
import requests
import time
from typing import Dict, List, Optional, Any
from dataclasses import dataclass
from datetime import datetime


@dataclass
class McpClientConfig:
    base_url: str = "http://localhost:8082"
    api_key: str = "mcp-dev-key-2024"
    timeout: int = 30


class McpClient:
    """Simple MCP client for testing and demonstration"""
    
    def __init__(self, config: McpClientConfig):
        self.config = config
        self.session = requests.Session()
        self.session.headers.update({
            'Content-Type': 'application/json',
            'X-MCP-API-KEY': config.api_key
        })
        self.request_id = 0
    
    def _get_next_id(self) -> str:
        """Generate next request ID"""
        self.request_id += 1
        return f"demo-{self.request_id}"
    
    def _make_request(self, method: str, url: str, data: Optional[Dict] = None) -> Dict:
        """Make HTTP request with error handling"""
        try:
            response = self.session.request(
                method, f"{self.config.base_url}{url}", 
                json=data, timeout=self.config.timeout
            )
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            return {"error": str(e)}
    
    def list_tools(self) -> Dict:
        """List available MCP tools"""
        print("📋 Discovering available MCP tools...")
        return self._make_request('GET', '/mcp/tools/list')
    
    def call_tool(self, tool_name: str, arguments: Dict) -> Dict:
        """Call a specific MCP tool"""
        payload = {
            "jsonrpc": "2.0",
            "id": self._get_next_id(),
            "method": "tools/call",
            "params": {
                "name": tool_name,
                "arguments": arguments
            }
        }
        
        print(f"🔧 Calling tool: {tool_name}")
        return self._make_request('POST', '/mcp/tools/call', payload)
    
    def get_resource(self, resource_uri: str) -> Dict:
        """Access MCP resource"""
        print(f"📊 Accessing resource: {resource_uri}")
        return self._make_request('GET', f'/mcp/resources/{resource_uri}')
    
    def health_check(self) -> Dict:
        """Check server health"""
        print("❤️  Checking server health...")
        return self._make_request('GET', '/actuator/health')


def print_response(response: Dict, title: str):
    """Pretty print response"""
    print(f"\n{'='*50}")
    print(f"📋 {title}")
    print(f"{'='*50}")
    
    if "error" in response:
        print(f"❌ Error: {response['error']}")
        return
    
    # Pretty print JSON with color-like formatting
    try:
        formatted = json.dumps(response, indent=2, default=str)
        print(formatted)
    except Exception:
        print(response)
    
    print("")


def demo_query_metrics(client: McpClient):
    """Demo query metrics functionality"""
    print("\n🎯 === QUERY METRICS DEMO ===")
    
    # Test different time windows
    time_windows = ["1h", "24h", "7d"]
    
    for window in time_windows:
        response = client.call_tool("get_query_metrics", {
            "timeWindow": window
        })
        
        if "result" in response:
            result = response["result"]
            print(f"📊 {window} metrics: {result.get('totalQueries', 'N/A')} queries, "
                  f"${result.get('totalCost', 'N/A')} cost, "
                  f"{result.get('successRate', 'N/A'):.2f} success rate")
        time.sleep(1)
    
    # Test with specific provider
    response = client.call_tool("get_query_metrics", {
        "timeWindow": "24h",
        "provider": "nominatim"
    })
    print_response(response, "24h Metrics for Nominatim Provider")


def demo_provider_routing(client: McpClient):
    """Demo provider routing functionality"""
    print("\n🔄 === PROVIDER ROUTING DEMO ===")
    
    # Check provider health
    health_response = client.call_tool("get_provider_health", {})
    print_response(health_response, "Provider Health Status")
    
    # Update routing strategy
    routing_response = client.call_tool("update_provider_routing", {
        "strategy": "COST_OPTIMIZED",
        "providerPriority": "nominatim,bing-maps,google-places",
        "conditions": "max_cost=0.02,min_success_rate=0.90"
    })
    print_response(routing_response, "Routing Strategy Update")
    
    # Test configuration
    test_response = client.call_tool("test_provider_configuration", {
        "providers": "nominatim,bing-maps"
    })
    print_response(test_response, "Provider Configuration Test")


def demo_international_search(client: McpClient):
    """Demo international search functionality"""
    print("\n🌍 === INTERNATIONAL SEARCH DEMO ===")
    
    # Search for leads
    search_response = client.call_tool("search_international_leads", {
        "country": "brazil",
        "industry": "technology",
        "maxResults": "5",
        "budgetLimit": "50.0",
        "minQualityScore": "0.8",
        "requiredFields": "website,email"
    })
    print_response(search_response, "International Lead Search")
    
    # Enrich a lead
    enrich_response = client.call_tool("enrich_international_lead", {
        "leadId": "demo-lead-001",
        "companyName": "Brazilian Tech Solutions",
        "website": "https://braziltech.com.br",
        "sources": "linkedin,web-scraping,crunchbase"
    })
    print_response(enrich_response, "Lead Enrichment")
    
    # Optimize search strategy
    strategy_response = client.call_tool("optimize_search_strategy", {
        "market": "latin-america",
        "budget": "200.0",
        "qualityThreshold": "0.85"
    })
    print_response(strategy_response, "Search Strategy Optimization")
    
    # Market analysis
    market_response = client.call_tool("analyze_market_coverage", {
        "country": "brazil",
        "competitors": "Local Leader SA,International Corp,Regional Consulting"
    })
    print_response(market_response, "Market Coverage Analysis")


def demo_resources(client: McpClient):
    """Demo MCP resources access"""
    print("\n📚 === MCP RESOURCES DEMO ===")
    
    resources = [
        ("query-history://24h/nominatim", "Query History"),
        ("provider-performance://nominatim/response_time", "Performance Metrics"),
        ("market-analysis://brazil/technology", "Market Analysis")
    ]
    
    for resource_uri, description in resources:
        try:
            response = client.get_resource(resource_uri)
            print_response(response, f"{description} ({resource_uri})")
        except Exception as e:
            print(f"❌ Failed to access {resource_uri}: {e}")
        time.sleep(1)


def performance_test(client: McpClient):
    """Test MCP performance"""
    print("\n⚡ === PERFORMANCE TEST ===")
    
    test_calls = [
        ("get_query_metrics", {"timeWindow": "1h"}),
        ("get_provider_health", {}),
        ("search_international_leads", {
            "country": "argentina", "industry": "finance", "maxResults": "3"
        })
    ]
    
    for tool_name, args in test_calls:
        start_time = time.time()
        response = client.call_tool(tool_name, args)
        end_time = time.time()
        
        duration = (end_time - start_time) * 1000  # Convert to ms
        status = "✅" if "result" in response else "❌"
        
        print(f"{status} {tool_name}: {duration:.1f}ms")


def autonomous_workflow_demo(client: McpClient):
    """Demonstrate autonomous AI workflow"""
    print("\n🤖 === AUTONOMOUS WORKFLOW DEMO ===")
    print("Simulating an AI agent optimizing international prospecting...")
    
    # Step 1: Analyze current metrics
    print("\n1. 📊 Analyzing current performance...")
    metrics = client.call_tool("get_query_metrics", {"timeWindow": "24h"})
    
    if "result" in metrics:
        success_rate = metrics["result"].get("successRate", 0)
        total_cost = float(metrics["result"].get("totalCost", 0))
        
        print(f"   Current success rate: {success_rate:.2f}")
        print(f"   Current daily cost: ${total_cost:.2f}")
        
        # Step 2: Decide optimization strategy
        print("\n2. 🎯 Determining optimization strategy...")
        if success_rate < 0.90:
            strategy = "PERFORMANCE_OPTIMIZED"
            print("   → Choosing PERFORMANCE_OPTIMIZED (low success rate)")
        elif total_cost > 10.0:
            strategy = "COST_OPTIMIZED"
            print("   → Choosing COST_OPTIMIZED (high costs)")
        else:
            strategy = "BALANCED"
            print("   → Choosing BALANCED (metrics are acceptable)")
        
        # Step 3: Check provider health
        print("\n3. ❤️  Checking provider health...")
        health = client.call_tool("get_provider_health", {})
        
        if "result" in health:
            healthy_providers = [p["provider"] for p in health["result"] if p["status"] == "HEALTHY"]
            print(f"   Healthy providers: {', '.join(healthy_providers)}")
            
            # Step 4: Update routing
            print("\n4. 🔄 Updating provider routing...")
            routing = client.call_tool("update_provider_routing", {
                "strategy": strategy,
                "providerPriority": ",".join(healthy_providers[:3]),  # Top 3 healthy
                "conditions": f"max_cost={total_cost * 0.8:.2f},min_success_rate=0.95"
            })
            
            if "result" in routing:
                savings = routing["result"].get("estimatedSavingsPercent", 0)
                print(f"   → Estimated savings: {savings}% per day")
        
        # Step 5: Plan international expansion
        print("\n5. 🌍 Planning international expansion...")
        strategy_opt = client.call_tool("optimize_search_strategy", {
            "market": "south-america",
            "budget": str(total_cost * 1.5),  # 50% budget increase
            "qualityThreshold": "0.85"
        })
        
        if "result" in strategy_opt:
            sources = strategy_opt["result"].get("recommendedSources", [])
            estimated_quality = strategy_opt["result"].get("estimatedQuality", 0)
            print(f"   → Recommended sources: {', '.join(sources)}")
            print(f"   → Estimated quality improvement: {estimated_quality:.2f}")
    
    print("\n🎉 Autonomous optimization complete!")


def main():
    """Main demo function"""
    print("🚀 ProspectOS MCP Client Demo")
    print("=" * 50)
    
    # Initialize client
    config = McpClientConfig()
    client = McpClient(config)
    
    try:
        # Health check
        health = client.health_check()
        if "error" in health:
            print(f"❌ Server not available: {health['error']}")
            return
        
        print("✅ Server is running")
        
        # Discover tools
        tools = client.list_tools()
        if "error" not in tools:
            print(f"✅ Found {len(tools)} available tools")
        
        # Run demos
        demo_query_metrics(client)
        demo_provider_routing(client)
        demo_international_search(client)
        demo_resources(client)
        performance_test(client)
        autonomous_workflow_demo(client)
        
        print("\n🎉 Demo completed successfully!")
        
    except KeyboardInterrupt:
        print("\n👋 Demo interrupted by user")
    except Exception as e:
        print(f"\n❌ Demo failed: {e}")


if __name__ == "__main__":
    main()
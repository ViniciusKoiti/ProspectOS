# MCP Testing Strategy

## Scope

This repository now validates the MCP surface in three layers:

1. API contracts under `src/test/java/dev/prospectos/api/mcp`
2. Infrastructure adapters under `src/test/java/dev/prospectos/infrastructure/mcp`
3. Manual end-to-end validation scripts under `scripts/mcp`

## Automated test matrix

### Contract tests
- `RoutingStrategyTest`
- `QueryTimeWindowTest`
- DTO mapping tests for MCP-facing responses

### Tool and resource tests
- `QueryMetricsMcpToolsTest`
- `ProviderRoutingMcpToolsTest`
- `InternationalSearchMcpToolsTest`
- `QueryHistoryMcpResourcesTest`

### Configuration and security tests
- `McpServerHealthIndicatorTest`
- `McpApiKeyAuthenticationFilterTest`
- `McpRateLimitingFilterTest`

## Recommended local command

Use the focused MCP suite while iterating locally:

```powershell
$env:GRADLE_USER_HOME='D:\Cursos\prospectos\.gradle-user-home'
./gradlew test --tests 'dev.prospectos.api.mcp.*' --tests 'dev.prospectos.infrastructure.mcp.*' -x jacocoTestReport -x jacocoTestCoverageVerification
```

This skips the repository-wide JaCoCo gate, which is useful when validating only the MCP slice. Full CI validation should still run the normal `./gradlew test` workflow.

## End-to-end validation

Start the server with the MCP profile:

```powershell
./gradlew bootRun --args="--spring.profiles.active=mcp"
```

Then validate the HTTP surface:

```bash
scripts/mcp/mcp-health-check.sh
scripts/mcp/mcp-tools-test.sh
```

For local iterations, prefer in-memory services and focused automated tests over standalone demo clients.

## Security checks

The MCP HTTP surface is protected by servlet filters scoped to `/mcp/*`:
- API key authentication through `X-MCP-API-KEY` or `Authorization: Bearer ...`
- Per-client rate limiting with response headers and `429` handling
- Audit logging for authentication and throttling events

The automated security tests cover both successful and rejected requests.

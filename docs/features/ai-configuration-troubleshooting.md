# AI Configuration Troubleshooting Guide

## Quick Diagnosis

### Check AI Status
```bash
# Verify AI services are enabled
curl -s http://localhost:8080/actuator/health | jq '.components.ai'

# Check active profile
curl -s http://localhost:8080/actuator/env | jq '.activeProfiles'

# Verify AI-related properties
curl -s http://localhost:8080/actuator/configprops | jq '.contexts.application.beans | to_entries[] | select(.key | contains("ai"))'
```

## Common Issues

### 1. AI Services Not Starting

#### Symptom
```
Error: No qualifying bean of type 'ChatModel' found
```

#### Root Causes & Solutions

##### Missing AI Enable Property
```bash
# Check if AI is enabled
curl -s http://localhost:8080/actuator/env/prospectos.ai.enabled
```

**Solution**: Set the master AI toggle
```properties
prospectos.ai.enabled=true
```

##### No Provider Configured
```bash
# Check provider configurations
curl -s http://localhost:8080/actuator/env | jq '.propertySources[] | select(.name | contains("ai"))'
```

**Solution**: Enable at least one provider
```properties
prospectos.ai.enabled=true
prospectos.ai.groq.enabled=true
prospectos.ai.groq.api-key=${GROQ_API_KEY}
```

##### Test Profile Interference
**Symptom**: AI works locally but fails in tests

**Solution**: Verify test profile configuration
```properties
# application-test.properties
prospectos.ai.enabled=false  # Correct for tests
```

### 2. Groq Configuration Issues

#### Invalid API Key Format
```
Error: Groq API key is required but not configured
```

**Solutions**:
```bash
# Check API key format (should start with 'gsk_')
echo $GROQ_API_KEY

# Verify environment variable is loaded
curl -s http://localhost:8080/actuator/env/prospectos.ai.groq.api-key
```

**Fix**:
```bash
export GROQ_API_KEY="gsk_your_actual_api_key_here"
```

#### Connection Failed
```
Error: Unable to initialize Groq ChatModel
Caused by: RestClientException: Connection refused
```

**Diagnosis**:
```bash
# Test API endpoint directly
curl -H "Authorization: Bearer $GROQ_API_KEY" \
     https://api.groq.com/openai/v1/models

# Check base URL configuration
curl -s http://localhost:8080/actuator/env/prospectos.ai.groq.base-url
```

**Solutions**:
1. Verify internet connectivity
2. Check firewall/proxy settings
3. Validate base URL format:
   ```properties
   prospectos.ai.groq.base-url=https://api.groq.com/openai  # No /v1 suffix
   ```

### 3. Vector Store Configuration

#### PGVector Not Available
```
Error: Driver org.postgresql.Driver claims to not accept jdbcUrl
```

**Solution**: Switch to in-memory for development
```properties
prospectos.vectorization.backend=in-memory
prospectos.vectorization.pgvector.enabled=false
```

#### Vector Dimension Mismatch
```
Error: Vector dimension mismatch: expected 256, got 384
```

**Solution**: Align embedding dimensions
```properties
prospectos.vectorization.embedding-dimension=384
# OR ensure your embedding model produces 256 dimensions
```

### 4. Profile Configuration Issues

#### Wrong Profile Active
**Symptom**: AI enabled in test environment or disabled in production

**Diagnosis**:
```bash
# Check active profiles
curl -s http://localhost:8080/actuator/env | jq '.activeProfiles'

# Check profile-specific properties
ls src/main/resources/application-*.properties
```

**Solution**: Set correct profile
```bash
export SPRING_PROFILES_ACTIVE=development
# OR
java -jar app.jar --spring.profiles.active=production
```

#### Property Override Issues
**Symptom**: Properties not taking effect despite being set

**Diagnosis**:
```bash
# Check property source priority
curl -s http://localhost:8080/actuator/env/prospectos.ai.enabled | jq '.propertySources'
```

**Solution**: Use correct property source priority:
1. Command line: `--prospectos.ai.enabled=true`
2. Environment: `PROSPECTOS_AI_ENABLED=true`
3. Profile file: `application-dev.properties`

### 5. Service Integration Issues

#### Scoring Service Not Working
```
Error: AI scoring failed with null response
```

**Diagnosis**:
```bash
# Check if scoring-specific chat client is configured
curl -s http://localhost:8080/actuator/beans | jq '.contexts.application.beans | keys[] | select(. | contains("scoring"))'

# Test manual scoring
curl -X POST http://localhost:8080/api/companies/1/score \
     -H "Content-Type: application/json"
```

**Solution**: Verify service configuration
```java
// Check @ConditionalOnProperty annotations
@ConditionalOnProperty(
    name = "prospectos.ai.enabled",
    havingValue = "true",
    matchIfMissing = true  // This might be the issue
)
```

#### Function Calling Not Available
```
Error: Function 'scrapeWebsite' not found
```

**Solution**: Enable function calling features
```properties
prospectos.discovery.llm.enabled=true
scraper.enabled=true
scraper.ai.enabled=true
```

## Debug Mode

### Enable Debug Logging
```properties
# AI-specific logging
logging.level.dev.prospectos.ai=DEBUG
logging.level.org.springframework.ai=DEBUG

# Configuration loading
logging.level.org.springframework.boot.autoconfigure=DEBUG
logging.level.org.springframework.boot.context.config=DEBUG

# Bean creation
logging.level.org.springframework.beans=DEBUG
```

### Startup Debugging
```bash
# Run with debug enabled
./gradlew bootRun --debug

# Or with specific debug options
java -jar -Ddebug=true \
     -Dlogging.level.dev.prospectos.ai=DEBUG \
     app.jar
```

## Performance Issues

### Slow AI Response Times
**Symptom**: API calls taking >30 seconds

**Diagnosis**:
```bash
# Check model configuration
curl -s http://localhost:8080/actuator/env/prospectos.ai.groq.model

# Monitor request times
curl -w "@curl-format.txt" -X POST http://localhost:8080/api/companies/1/score
```

**Solutions**:
1. Use faster model:
   ```properties
   prospectos.ai.groq.model=llama3-8b-8192  # Faster
   # Instead of: llama3-70b-8192  # Slower but higher quality
   ```

2. Implement caching:
   ```properties
   spring.cache.type=caffeine
   prospectos.ai.cache.enabled=true
   ```

### Memory Issues
**Symptom**: OutOfMemoryError during AI operations

**Solution**: Tune JVM and AI parameters
```bash
# Increase heap size
export JAVA_OPTS="-Xmx4g -Xms2g"

# Limit concurrent AI requests
prospectos.ai.max-concurrent-requests=5
```

## Environment-Specific Troubleshooting

### Docker/Container Issues

#### Environment Variables Not Loading
```bash
# Check environment variables in container
docker exec -it prospectos-app env | grep GROQ

# Check if .env file is properly mounted
docker exec -it prospectos-app ls -la /.env
```

**Solution**: Ensure proper environment variable passing
```yaml
# docker-compose.yml
environment:
  - GROQ_API_KEY=${GROQ_API_KEY}
  - SPRING_PROFILES_ACTIVE=production
```

#### Network Connectivity
```bash
# Test external connectivity from container
docker exec -it prospectos-app curl -I https://api.groq.com

# Check DNS resolution
docker exec -it prospectos-app nslookup api.groq.com
```

### Kubernetes Issues

#### ConfigMap Not Applied
```bash
# Check ConfigMap
kubectl get configmap prospectos-config -o yaml

# Check pod environment
kubectl exec prospectos-app -- env | grep GROQ

# Check if secret is mounted
kubectl exec prospectos-app -- cat /etc/secrets/groq-api-key
```

#### Service Discovery
```bash
# Check if services can communicate
kubectl exec prospectos-app -- curl http://prospectos-service:8080/actuator/health
```

## Recovery Procedures

### Graceful Degradation
When AI services fail, the application should continue functioning:

```properties
# Enable fallback mechanisms
prospectos.ai.fallback.enabled=true
prospectos.scoring.fallback-strategy=rule-based
```

### Service Restart
```bash
# Restart specific AI service
curl -X POST http://localhost:8080/actuator/restart

# Or restart entire application
kubectl rollout restart deployment/prospectos-app
```

### Configuration Rollback
```bash
# Rollback to previous configuration
kubectl rollout undo deployment/prospectos-app

# Or revert to safe configuration
kubectl patch configmap prospectos-config --patch='{"data":{"prospectos.ai.enabled":"false"}}'
```

## Prevention Strategies

### Health Checks
Implement comprehensive health checks:
```java
@Component
public class AIHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        // Test actual AI provider connectivity
        try {
            chatModel.call("ping");
            return Health.up().withDetail("provider", "groq").build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("provider", "groq")
                .build();
        }
    }
}
```

### Configuration Validation
Add startup validation:
```java
@PostConstruct
public void validateConfiguration() {
    if (aiEnabled && !hasValidProvider()) {
        throw new IllegalStateException(
            "AI is enabled but no valid provider is configured");
    }
}
```

### Monitoring Alerts
Set up alerts for common issues:
```yaml
# Prometheus alerts
- alert: AIServiceDown
  expr: up{job="prospectos-ai"} == 0
  for: 2m
  
- alert: AIResponseTimeSlow
  expr: histogram_quantile(0.95, ai_request_duration_seconds) > 10
  for: 5m
```

## Support Information

### Gathering Debug Information
When reporting issues, include:

```bash
#!/bin/bash
# debug-info.sh
echo "=== System Information ==="
java -version
echo "Active Profile: $(curl -s localhost:8080/actuator/env | jq -r '.activeProfiles[]')"

echo -e "\n=== AI Configuration ==="
curl -s localhost:8080/actuator/env | jq '.propertySources[] | select(.name | contains("ai")) | .properties'

echo -e "\n=== Health Status ==="
curl -s localhost:8080/actuator/health | jq '.components'

echo -e "\n=== Recent Logs ==="
tail -100 logs/application.log | grep -i "ai\|error"
```

### Log Analysis
Key log patterns to look for:
```bash
# Configuration errors
grep "ConditionalOnProperty" logs/application.log

# API connectivity issues  
grep -i "connection\|timeout\|refused" logs/application.log

# AI provider errors
grep -i "groq\|openai\|anthropic" logs/application.log
```

### Performance Profiling
```bash
# Enable JFR profiling
java -XX:+FlightRecorder \
     -XX:StartFlightRecording=duration=60s,filename=ai-profile.jfr \
     -jar app.jar

# Analyze with jfr
jfr print --events jdk.CPULoad ai-profile.jfr
```

This troubleshooting guide covers the most common issues encountered with the AI configuration system and provides step-by-step solutions for quick resolution.
package dev.prospectos.ai.client.impl;

import dev.prospectos.ai.client.LLMClient;
import dev.prospectos.ai.client.LLMProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação mock do LLMClient para testes
 * Simula respostas realistas sem dependência de APIs externas
 */
@Slf4j
public class MockLLMClient implements LLMClient {
    
    @Override
    public String query(String prompt) {
        log.debug("🤖 Mock LLM Query: {}", prompt.substring(0, Math.min(100, prompt.length())));
        
        if (prompt.toLowerCase().contains("yes or no") || prompt.toLowerCase().contains("sim ou não")) {
            return "YES";
        }
        
        if (prompt.toLowerCase().contains("empresa") || prompt.toLowerCase().contains("company")) {
            return "This is a technology company with great potential for our ICP. " +
                   "Based on website analysis, they use modern technologies like Java and Spring, " +
                   "have a team of 50-200 employees and are in expansion phase.";
        }
        
        if (prompt.toLowerCase().contains("estratégia") || prompt.toLowerCase().contains("strategy")) {
            return "I recommend LinkedIn approach targeting CTO or VP Engineering. " +
                   "Ideal timing: next 2 weeks. Pain points: scalability and technical modernization. " +
                   "Value proposition: 30% reduction in operational costs.";
        }
        
        return "Mock response for: " + prompt.substring(0, Math.min(50, prompt.length())) + "...";
    }
    
    @Override
    public String queryWithFunctions(String prompt, String... functions) {
        log.debug("🤖 Mock LLM Query with functions: {}", String.join(", ", functions));
        
        return "Mock response with data collected via functions: " + String.join(", ", functions) + 
               ". Complete company analysis shows strong ICP fit.";
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T queryStructured(String prompt, Class<T> responseClass) {
        log.debug("🤖 Mock LLM Structured Query: {}", responseClass.getSimpleName());
        
        try {
            if (responseClass.getSimpleName().contains("ScoringResult")) {
                return (T) createMockScoringResult();
            }
            
            if (responseClass.getSimpleName().contains("OutreachMessage")) {
                return (T) createMockOutreachMessage();
            }
            
            if (responseClass.getSimpleName().contains("StrategyRecommendation")) {
                return (T) createMockStrategyRecommendation();
            }
            
            return responseClass.getDeclaredConstructor().newInstance();
            
        } catch (Exception e) {
            throw new RuntimeException("Error creating mock for " + responseClass.getSimpleName(), e);
        }
    }
    
    @Override
    public LLMProvider getProvider() {
        return LLMProvider.MOCK;
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    private Object createMockScoringResult() {
        try {
            Class<?> scoringClass = Class.forName("dev.prospectos.ai.dto.ScoringResult");
            Class<?> priorityClass = Class.forName("dev.prospectos.ai.dto.PriorityLevel");
            
            Object priority = Enum.valueOf((Class<Enum>) priorityClass, "HOT");
            
            return scoringClass.getDeclaredConstructor(
                int.class,
                priorityClass,
                String.class,
                java.util.Map.class,
                String.class
            ).newInstance(
                85,
                priority,
                "Company with strong ICP fit. High score due to modern technology, growth and qualified team.",
                java.util.Map.of(
                    "icpFit", 28,
                    "signals", 22,
                    "companySize", 18,
                    "timing", 12,
                    "accessibility", 5
                ),
                "Prioritize immediate contact. Growing company with needs aligned to our product."
            );
        } catch (Exception e) {
            throw new RuntimeException("Error creating mock ScoringResult", e);
        }
    }
    
    private Object createMockOutreachMessage() {
        try {
            Class<?> outreachClass = Class.forName("dev.prospectos.ai.dto.OutreachMessage");
            
            return outreachClass.getDeclaredConstructor(
                String.class,
                String.class, 
                String.class,
                String.class,
                String[].class
            ).newInstance(
                "Performance optimization for [COMPANY]",
                "Hi [NAME], I noticed [COMPANY] has been growing rapidly. Our product helped similar companies reduce operational costs by 30%. How about a quick 15-min chat?",
                "linkedin",
                "consultative",
                new String[]{"Schedule demo", "Download case study"}
            );
        } catch (Exception e) {
            throw new RuntimeException("Error creating mock OutreachMessage", e);
        }
    }
    
    private Object createMockStrategyRecommendation() {
        try {
            Class<?> strategyClass = Class.forName("dev.prospectos.ai.dto.StrategyRecommendation");
            
            return strategyClass.getDeclaredConstructor(
                String.class,
                String.class,
                String.class,
                java.util.List.class,
                String.class,
                String.class
            ).newInstance(
                "linkedin",
                "CTO",
                "this_week",
                java.util.List.of("Scalability", "Operational costs", "Technical modernization"),
                "30% reduction in operational costs with our platform",
                "LinkedIn is the best channel to reach CTOs. Ideal timing as company is growing."
            );
        } catch (Exception e) {
            throw new RuntimeException("Error creating mock StrategyRecommendation", e);
        }
    }
}
package dev.prospectos.ai.function;

import dev.prospectos.ai.client.ScraperClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Map;
import java.util.function.Function;

/**
 * Functions que LLMs podem chamar para fazer scraping
 */
@Slf4j
@Configuration
public class ScrapingFunctions {
    
    private final ScraperClient scraperClient;
    
    public ScrapingFunctions(ScraperClient scraperClient) {
        this.scraperClient = scraperClient;
    }
    
    /**
     * LLM pode chamar esta função para fazer scraping de website
     */
    @Bean
    @Description("""
        Faz scraping de um website corporativo para extrair dados.
        Use quando precisar de informações sobre uma empresa que não foram fornecidas.
        
        Retorna: emails, telefones, tech stack, sobre a empresa, etc.
        """)
    public Function<ScrapingRequest, Map<String, Object>> scrapeWebsite() {
        return request -> {
            log.info("🤖 LLM called scrapeWebsite: {}", request.website());
            
            ScraperClient.ScrapingResponse response = scraperClient.scrapeWebsiteSync(
                request.website(),
                request.deep()
            );
            
            if (response.success()) {
                return response.data();
            } else {
                return Map.of(
                    "error", response.error(),
                    "success", false
                );
            }
        };
    }
    
    /**
     * Request DTO para scraping
     */
    public record ScrapingRequest(
        @Description("URL do website a ser analisado") 
        String website,
        
        @Description("Se true, faz scraping profundo (múltiplas páginas)") 
        boolean deep
    ) {}
}
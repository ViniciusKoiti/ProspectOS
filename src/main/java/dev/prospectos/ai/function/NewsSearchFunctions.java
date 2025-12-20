package dev.prospectos.ai.function;

import dev.prospectos.ai.client.ScraperClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Configuration
public class NewsSearchFunctions {
    
    private final ScraperClient scraperClient;
    
    public NewsSearchFunctions(ScraperClient scraperClient) {
        this.scraperClient = scraperClient;
    }
    
    @Bean
    @Description("""
        Busca notícias recentes sobre uma empresa.
        Use para identificar sinais de interesse como:
        - Rodadas de investimento
        - Expansão para novas regiões
        - Lançamento de produtos
        - Contratações em massa
        - Parcerias estratégicas
        """)
    public Function<NewsRequest, List<String>> searchCompanyNews() {
        return request -> {
            log.info("🤖 LLM called searchCompanyNews: {}", request.companyName());
            
            return scraperClient.searchNews(
                request.companyName(),
                request.daysBack()
            ).news();
        };
    }
    
    public record NewsRequest(
        @Description("Nome da empresa")
        String companyName,
        
        @Description("Quantos dias para trás buscar (padrão: 30)")
        int daysBack
    ) {
        public NewsRequest(String companyName) {
            this(companyName, 30);
        }
    }
}
package dev.prospectos.ai.function;

import dev.prospectos.ai.client.ScraperClientInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Slf4j
@Configuration
public class NewsSearchFunctions {

    private final ScraperClientInterface scraperClient;

    public NewsSearchFunctions(ScraperClientInterface scraperClient) {
        this.scraperClient = scraperClient;
    }
    
    @Bean
    @Description("""
        Searches recent news about a company.
        Use to identify interest signals such as:
        - Funding rounds
        - Expansion to new regions
        - Product launches
        - Mass hiring
        - Strategic partnerships
        """)
    public Function<NewsRequest, List<String>> searchCompanyNews() {
        return request -> {
            log.info("LLM called searchCompanyNews: {}", request.companyName());
            
            return scraperClient.searchNews(
                request.companyName(),
                request.daysBack()
            ).news();
        };
    }
    
    public record NewsRequest(
        @Description("Company name")
        String companyName,
        
        @Description("How many days back to search (default: 30)")
        int daysBack
    ) {
        public NewsRequest(String companyName) {
            this(companyName, 30);
        }
    }
}

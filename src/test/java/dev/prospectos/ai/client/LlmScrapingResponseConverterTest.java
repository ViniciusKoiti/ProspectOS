package dev.prospectos.ai.client;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LlmScrapingResponseConverterTest {

    private final LlmScrapingResponseConverter converter = new LlmScrapingResponseConverter();

    @Test
    void convertsMarkdownJsonWithCommentsAndReferences() {
        String response = """
            Based on official company information, here's the extracted data in the requested JSON format:

            ```json
            {
              "company_name": "Stripe",
              "description": "Stripe is a technology company that provides online payment processing.",
              "emails": ["info@stripe.com", "contact@stripe.com"], // Based on contact page
              "phone": "None", // No public phone
              "technologies": ["AWS", "Java", "Kubernetes"],
              "industry": "Financial Technology",
              "size": "Large",
              "recent_news": ["Stripe raises funding", "Stripe adds crypto support"]
            }
            ```

            References:
            [1] https://stripe.com/contact
            """;

        Map<String, Object> converted = converter.convert(response);

        assertThat(converted.get("company_name")).isEqualTo("Stripe");
        assertThat(converted.get("industry")).isEqualTo("Financial Technology");
        assertThat(converted.get("phone")).isNull();
        assertThat(converted.get("emails")).isEqualTo(List.of("info@stripe.com", "contact@stripe.com"));
        assertThat(converted.get("ai_processed")).isEqualTo(true);
    }

    @Test
    void fallsBackToDescriptionWhenNoStructuredContent() {
        String response = "Could not find structured fields, but company appears to sell software.";

        Map<String, Object> converted = converter.convert(response);

        assertThat(converted.get("description")).isEqualTo(response);
        assertThat(converted.get("source")).isEqualTo("ai_web_search");
        assertThat(converted.get("ai_processed")).isEqualTo(true);
    }

    @Test
    void parsesLooseFieldsWhenJsonIsNotBalanced() {
        String response = """
            "company_name": "Acme",
            "description": "B2B SaaS company",
            "emails": ["sales@acme.com", "info@acme.com"],
            "phone": "N/A",
            "technologies": ["Java", "Spring"],
            "industry": "Software",
            "size": "small",
            "recent_news": ["Launched new product"]
            """;

        Map<String, Object> converted = converter.convert(response);

        assertThat(converted.get("company_name")).isEqualTo("Acme");
        assertThat(converted.get("description")).isEqualTo("B2B SaaS company");
        assertThat(converted.get("phone")).isNull();
        assertThat(converted.get("emails")).isEqualTo(List.of("sales@acme.com", "info@acme.com"));
        assertThat(converted.get("technologies")).isEqualTo(List.of("Java", "Spring"));
        assertThat(converted.get("source")).isEqualTo("ai_web_search");
    }
}

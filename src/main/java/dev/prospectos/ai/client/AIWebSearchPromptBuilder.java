package dev.prospectos.ai.client;

final class AIWebSearchPromptBuilder {

    private AIWebSearchPromptBuilder() {
    }

    static String scrapePrompt(String website, boolean useDeepSearch) {
        String searchDepth = useDeepSearch ? "comprehensive" : "focused";
        return """
            Research the company at %s and extract the following information:

            1. Company name and business description
            2. Contact information (emails, phone numbers)
            3. Technology stack or tools they use
            4. Industry and company size indicators
            5. Recent developments or news

            Perform a %s search focusing on official company information.

            Return the data in the following JSON format:
            {
              "company_name": "string",
              "description": "string",
              "emails": ["email1", "email2"],
              "phone": "string",
              "technologies": ["tech1", "tech2"],
              "industry": "string",
              "size": "string",
              "recent_news": ["news1", "news2"]
            }

            If you cannot find specific information, use null for that field.
            Only include verified information from reliable sources.
            Return ONLY valid JSON.
            Do not use markdown code fences.
            Do not include references, citations, comments, or explanatory text outside JSON.
            """.formatted(website, searchDepth);
    }

    static String newsPrompt(String companyName, int daysBack) {
        return """
            Search for recent news and developments about "%s" from the last %d days.

            Focus on business-relevant information such as:
            - Funding rounds or investments
            - Product launches or updates
            - Strategic partnerships
            - Hiring announcements or expansion
            - Market developments
            - Awards or recognitions

            Return 3-5 most relevant and recent news items as a simple list.
            Each item should be a brief, factual summary (1-2 sentences).
            Only include verified information from reliable news sources.

            Format as a simple list of news items, one per line.
            """.formatted(companyName, daysBack);
    }
}

/**
 * AI Module
 * 
 * <p>Artificial Intelligence module using Spring AI to orchestrate
 * Large Language Models (LLMs) for B2B prospecting.
 * 
 * <h2>Supported LLMs</h2>
 * <ul>
 *   <li>OpenAI GPT-4 Turbo - Best overall quality</li>
 *   <li>Anthropic Claude 3.5 Sonnet - Best for complex analysis</li>
 *   <li>Ollama (llama3.1, mixtral) - Local, no cost</li>
 * </ul>
 * 
 * <h2>Capabilities</h2>
 * <ul>
 *   <li>Company-to-ICP fit analysis</li>
 *   <li>Scoring inteligente (0-100)</li>
 *   <li>Outreach strategy recommendation</li>
 *   <li>Personalized message generation</li>
 *   <li>Interest signal analysis</li>
 * </ul>
 * 
 * <h2>Function Calling</h2>
 * <p>LLMs can call Java functions automatically:
 * <ul>
 *   <li>{@code scrapeWebsite()} - Scraping via Python</li>
 *   <li>{@code searchNews()} - Search news</li>
 *   <li>{@code analyzeSignals()} - Analyze signals</li>
 * </ul>
 * 
 * <h2>Structured Outputs</h2>
 * <p>Responses parsed automatically into POJOs:
 * <pre>
 * ScoringResult result = chatClient.prompt()
 *     .user("Score this company...")
 *     .call()
 *     .entity(ScoringResult.class);
 * </pre>
 * 
 * <h2>Prompts</h2>
 * <p>StringTemplate templates in {@code src/main/resources/prompts/}
 * 
 * <h2>Configuration</h2>
 * <pre>
 * OPENAI_API_KEY=sk-...
 * ANTHROPIC_API_KEY=sk-ant-...
 * OLLAMA_BASE_URL=http://localhost:11434
 * </pre>
 * 
 * @see org.springframework.ai.chat.client.ChatClient
 * @see dev.prospectos.ai.config.SpringAIConfig
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "AI Module",
    allowedDependencies = {"core"}
)
@org.springframework.lang.NonNullApi
package dev.prospectos.ai;

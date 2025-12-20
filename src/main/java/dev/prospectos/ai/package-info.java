/**
 * AI Module
 * 
 * <p>Módulo de Inteligência Artificial usando Spring AI para orquestração
 * de Large Language Models (LLMs) na prospecção B2B.
 * 
 * <h2>LLMs Suportados</h2>
 * <ul>
 *   <li>OpenAI GPT-4 Turbo - Melhor qualidade geral</li>
 *   <li>Anthropic Claude 3.5 Sonnet - Melhor análise complexa</li>
 *   <li>Ollama (llama3.1, mixtral) - Local, sem custo</li>
 * </ul>
 * 
 * <h2>Capacidades</h2>
 * <ul>
 *   <li>Análise de adequação empresa-ICP</li>
 *   <li>Scoring inteligente (0-100)</li>
 *   <li>Recomendação de estratégia de abordagem</li>
 *   <li>Geração de mensagens personalizadas</li>
 *   <li>Análise de sinais de interesse</li>
 * </ul>
 * 
 * <h2>Function Calling</h2>
 * <p>LLMs podem chamar funções Java automaticamente:
 * <ul>
 *   <li>{@code scrapeWebsite()} - Scraping via Python</li>
 *   <li>{@code searchNews()} - Buscar notícias</li>
 *   <li>{@code analyzeSignals()} - Analisar sinais</li>
 * </ul>
 * 
 * <h2>Structured Outputs</h2>
 * <p>Respostas parseadas automaticamente para POJOs:
 * <pre>
 * ScoringResult result = chatClient.prompt()
 *     .user("Score this company...")
 *     .call()
 *     .entity(ScoringResult.class);
 * </pre>
 * 
 * <h2>Prompts</h2>
 * <p>Templates StringTemplate em {@code src/main/resources/prompts/}
 * 
 * <h2>Configuração</h2>
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
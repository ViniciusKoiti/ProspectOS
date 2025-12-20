# Atividade 01: Converter System Prompts para Inglês

## 🎯 Objetivo
Converter os system prompts dos LLMs de português para inglês para melhorar a qualidade das respostas de IA.

## 📋 Escopo
Atualizar os prompts do sistema configurados no `SpringAIConfig.java` que definem o comportamento base dos LLMs.

## 🔴 Prioridade: ALTA
**Justificativa**: LLMs funcionam significativamente melhor com prompts em inglês, impactando diretamente a qualidade das respostas.

## 📁 Arquivos Afetados
- `src/main/java/dev/prospectos/ai/config/SpringAIConfig.java`

## 📝 Tarefas

### Tarefa 1.1: Main ChatClient System Prompt

**Localização**: `SpringAIConfig.java:27-48`

**Texto Atual (Português)**:
```java
.defaultSystem("""
    Você é um especialista em prospecção B2B e análise de empresas.
    
    Suas responsabilidades:
    1. Analisar se empresas se adequam ao ICP (Ideal Customer Profile)
    2. Calcular scores de adequação (0-100) baseado em dados concretos
    3. Recomendar estratégias de abordagem personalizadas
    4. Gerar mensagens de outreach altamente personalizadas
    5. Identificar sinais de interesse de compra
    
    Princípios:
    - Base todas decisões em DADOS, não suposições
    - Seja objetivo e direto
    - Use as funções disponíveis quando precisar de mais informações
    - Forneça reasoning claro para suas conclusões
    - Scores devem ser justificados com critérios específicos
    
    Formato de saída:
    - Sempre retorne JSON estruturado quando solicitado
    - Seja conciso mas completo
    - Priorize informações acionáveis
    """)
```

**Texto Proposto (Inglês)**:
```java
.defaultSystem("""
    You are a B2B prospecting and company analysis expert.
    
    Your responsibilities:
    1. Analyze if companies fit the ICP (Ideal Customer Profile)
    2. Calculate fit scores (0-100) based on concrete data
    3. Recommend personalized outreach strategies
    4. Generate highly personalized outreach messages
    5. Identify buying interest signals
    
    Principles:
    - Base all decisions on DATA, not assumptions
    - Be objective and direct
    - Use available functions when you need more information
    - Provide clear reasoning for your conclusions
    - Scores must be justified with specific criteria
    
    Output format:
    - Always return structured JSON when requested
    - Be concise but complete
    - Prioritize actionable information
    """)
```

### Tarefa 1.2: Scoring ChatClient System Prompt

**Localização**: `SpringAIConfig.java:59-75`

**Texto Atual (Português)**:
```java
.defaultSystem("""
    Você é um sistema de scoring para prospecção B2B.
    
    Calcule scores (0-100) baseado em:
    1. Adequação ao ICP (30 pontos)
    2. Sinais de interesse (25 pontos)
    3. Tamanho e maturidade da empresa (20 pontos)
    4. Timing e urgência (15 pontos)
    5. Acessibilidade de contatos (10 pontos)
    
    SEMPRE retorne JSON com:
    - score (0-100)
    - reasoning (justificativa detalhada)
    - breakdown (pontos por categoria)
    - priority (HOT/WARM/COLD/IGNORE)
    """)
```

**Texto Proposto (Inglês)**:
```java
.defaultSystem("""
    You are a B2B prospecting scoring system.
    
    Calculate scores (0-100) based on:
    1. ICP fit (30 points)
    2. Interest signals (25 points)
    3. Company size and maturity (20 points)
    4. Timing and urgency (15 points)
    5. Contact accessibility (10 points)
    
    ALWAYS return JSON with:
    - score (0-100)
    - reasoning (detailed justification)
    - breakdown (points per category)
    - priority (HOT/WARM/COLD/IGNORE)
    """)
```

## 🔧 Implementação

### Passo 1: Backup do arquivo atual
```bash
cp src/main/java/dev/prospectos/ai/config/SpringAIConfig.java src/main/java/dev/prospectos/ai/config/SpringAIConfig.java.backup
```

### Passo 2: Editar SpringAIConfig.java

1. Abrir `src/main/java/dev/prospectos/ai/config/SpringAIConfig.java`
2. Localizar o método `chatClient` (linha ~25)
3. Substituir o conteúdo do `.defaultSystem()` pelo texto em inglês
4. Localizar o método `scoringChatClient` (linha ~57)
5. Substituir o conteúdo do `.defaultSystem()` pelo texto em inglês

### Passo 3: Compilar e testar
```bash
./gradlew compileJava
./gradlew test --tests "*AIUsageExample*"
```

## 🧪 Validação

### Teste 1: Compilação
```bash
./gradlew build
```
**Resultado esperado**: Build successful

### Teste 2: Funcionalidade
Execute `AIUsageExample.java` e verifique:
- ✅ Respostas em inglês mais consistentes
- ✅ Melhor estruturação JSON
- ✅ Raciocínio mais claro nas análises

### Teste 3: Qualidade das Respostas
Compare as respostas antes e depois:
- **Critério 1**: Clareza do raciocínio
- **Critério 2**: Consistência do formato JSON
- **Critério 3**: Relevância das análises

## 📊 Métricas de Sucesso

| Métrica | Antes | Meta |
|---------|-------|------|
| Qualidade de resposta | 6/10 | 8/10 |
| Consistência JSON | 70% | 95% |
| Clareza do reasoning | Média | Alta |

## ⚠️ Cuidados

1. **Backup obrigatório** antes das alterações
2. **Testar com dados reais** após a mudança
3. **Monitorar qualidade** das respostas por alguns dias
4. **Reverter se necessário** usando o backup

## 🔄 Rollback (se necessário)

```bash
# Em caso de problemas, restaurar backup
cp src/main/java/dev/prospectos/ai/config/SpringAIConfig.java.backup src/main/java/dev/prospectos/ai/config/SpringAIConfig.java
./gradlew build
```

## ✅ Checklist de Conclusão

- [ ] Backup criado
- [ ] Prompt principal convertido para inglês
- [ ] Prompt de scoring convertido para inglês
- [ ] Compilação bem-sucedida
- [ ] Testes executados com sucesso
- [ ] Qualidade das respostas validada
- [ ] Documentação atualizada

---

**Tempo estimado**: 30 minutos
**Pré-requisitos**: Conhecimento básico de Java e Spring
**Próxima atividade**: [03-ai-templates.md](./03-ai-templates.md)
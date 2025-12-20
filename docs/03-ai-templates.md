# Atividade 03: Converter AI Templates para Inglês

## 🎯 Objetivo
Converter todos os templates de prompts de IA de português para inglês para melhorar significativamente a qualidade das respostas dos LLMs.

## 📋 Escopo
Atualizar prompts específicos usados nos serviços de IA que contêm instruções detalhadas em português.

## 🔴 Prioridade: ALTA
**Justificativa**: Templates de prompts têm impacto direto na qualidade das respostas de IA. LLMs geram respostas mais precisas e consistentes com prompts em inglês.

## 📁 Arquivos Afetados
- `src/main/java/dev/prospectos/ai/service/StrategyAIService.java`
- `src/main/java/dev/prospectos/ai/service/ScoringAIService.java`  
- `src/main/java/dev/prospectos/ai/service/OutreachAIService.java`
- `src/main/java/dev/prospectos/ai/service/ProspectorAIService.java`

## 📝 Tarefas

### Tarefa 3.1: StrategyAIService - Template de Estratégia

**Arquivo**: `src/main/java/dev/prospectos/ai/service/StrategyAIService.java`
**Localização**: Linha 29-64

**Template Atual (Português)**:
```java
String prompt = String.format("""
        Analise a empresa e recomende a melhor estratégia de abordagem.
        
        EMPRESA:
        Nome: %s
        Setor: %s
        Tamanho: %s
        Localização: %s
        Análise AI: %s
        Score: %s
        
        ICP:
        Tema: %s
        Cargos alvo: %s
        
        TAREFA:
        Com base na análise, recomende a melhor estratégia de abordagem.
        
        Retorne JSON com esta estrutura exata:
        {
          "channel": "email|linkedin|phone|event",
          "targetRole": "CEO|CTO|CMO|etc",
          "timing": "immediate|this_week|this_month|wait",
          "painPoints": ["pain1", "pain2", "pain3"],
          "valueProposition": "Proposta de valor específica",
          "approachRationale": "Explicação da estratégia escolhida"
        }
        """,
        // ... parâmetros
);
```

**Template Proposto (Inglês)**:
```java
String prompt = String.format("""
        Analyze the company and recommend the best outreach strategy.
        
        COMPANY:
        Name: %s
        Industry: %s
        Size: %s
        Location: %s
        AI Analysis: %s
        Score: %s
        
        ICP:
        Theme: %s
        Target Roles: %s
        
        TASK:
        Based on the analysis, recommend the best outreach strategy.
        
        Return JSON with this exact structure:
        {
          "channel": "email|linkedin|phone|event",
          "targetRole": "CEO|CTO|CMO|etc",
          "timing": "immediate|this_week|this_month|wait",
          "painPoints": ["pain1", "pain2", "pain3"],
          "valueProposition": "Specific value proposition",
          "approachRationale": "Explanation of chosen strategy"
        }
        """,
        // ... parâmetros
);
```

### Tarefa 3.2: ScoringAIService - Template de Scoring

**Arquivo**: `src/main/java/dev/prospectos/ai/service/ScoringAIService.java`
**Localização**: Linha 30-64

**Template Atual (Português)**:
```java
String prompt = String.format("""
        EMPRESA:
        Nome: %s
        Setor: %s
        Localização: %s
        Análise AI: %s
        Sinais ativos: %s
        
        ICP ALVO:
        Setores: %s
        Regiões: %s
        Tema: %s
        
        TAREFA:
        Calcule o score (0-100) desta empresa baseado nos critérios:
        1. Adequação ao ICP (30 pontos)
        2. Sinais de interesse (25 pontos)
        3. Tamanho e maturidade da empresa (20 pontos)
        4. Timing e urgência (15 pontos)
        5. Acessibilidade de contatos (10 pontos)
        
        Retorne JSON com exatamente esta estrutura:
        {
          "score": 75,
          "priority": "HOT",
          "reasoning": "Empresa X...",
          "breakdown": {
            "icpFit": 28,
            "signals": 20,
            "companySize": 15,
            "timing": 12,
            "accessibility": 8
          },
          "recommendation": "Priorizar contato imediato porque..."
        }
        """,
        // ... parâmetros
);
```

**Template Proposto (Inglês)**:
```java
String prompt = String.format("""
        COMPANY:
        Name: %s
        Industry: %s
        Location: %s
        AI Analysis: %s
        Active Signals: %s
        
        TARGET ICP:
        Industries: %s
        Regions: %s
        Theme: %s
        
        TASK:
        Calculate the score (0-100) for this company based on the criteria:
        1. ICP fit (30 points)
        2. Interest signals (25 points)
        3. Company size and maturity (20 points)
        4. Timing and urgency (15 points)
        5. Contact accessibility (10 points)
        
        Return JSON with exactly this structure:
        {
          "score": 75,
          "priority": "HOT",
          "reasoning": "Company X...",
          "breakdown": {
            "icpFit": 28,
            "signals": 20,
            "companySize": 15,
            "timing": 12,
            "accessibility": 8
          },
          "recommendation": "Prioritize immediate contact because..."
        }
        """,
        // ... parâmetros
);
```

### Tarefa 3.3: OutreachAIService - Template de Outreach

**Arquivo**: `src/main/java/dev/prospectos/ai/service/OutreachAIService.java`
**Localização**: Linha 29-56

**Template Atual (Português)**:
```java
String prompt = String.format("""
        Crie uma mensagem de outreach B2B altamente personalizada.
        
        EMPRESA ALVO:
        %s - %s
        Análise: %s
        Estratégia recomendada: %s
        
        SEU PRODUTO/SERVIÇO:
        Tema: %s
        Cargos alvo: %s
        
        DIRETRIZES:
        1. Comece com um hook personalizado baseado na análise
        2. Demonstre que pesquisou a empresa
        3. Conecte um pain point identificado com sua solução
        4. Seja conciso (max 150 palavras)
        5. CTA claro e de baixo compromisso
        6. Tom profissional mas não corporativo demais
        
        Retorne JSON:
        {
          "subject": "Assunto do email",
          "body": "Corpo da mensagem",
          "channel": "email|linkedin|phone",
          "tone": "formal|casual|consultivo",
          "callsToAction": ["CTA1", "CTA2"]
        }
        """,
        // ... parâmetros
);
```

**Template Proposto (Inglês)**:
```java
String prompt = String.format("""
        Create a highly personalized B2B outreach message.
        
        TARGET COMPANY:
        %s - %s
        Analysis: %s
        Recommended Strategy: %s
        
        YOUR PRODUCT/SERVICE:
        Theme: %s
        Target Roles: %s
        
        GUIDELINES:
        1. Start with a personalized hook based on the analysis
        2. Demonstrate you've researched the company
        3. Connect an identified pain point with your solution
        4. Be concise (max 150 words)
        5. Clear and low-commitment CTA
        6. Professional but not overly corporate tone
        
        Return JSON:
        {
          "subject": "Email subject",
          "body": "Message body",
          "channel": "email|linkedin|phone",
          "tone": "formal|casual|consultative",
          "callsToAction": ["CTA1", "CTA2"]
        }
        """,
        // ... parâmetros
);
```

### Tarefa 3.4: ProspectorAIService - Templates Múltiplos

**Arquivo**: `src/main/java/dev/prospectos/ai/service/ProspectorAIService.java`

#### 3.4.1: Template shouldInvestigateCompany (Linha 29-50)

**Atual (Português)**:
```java
String prompt = String.format("""
        Empresa: %s
        Website: %s
        Setor: %s
        Localização: %s
        
        ICP (Perfil Ideal):
        - Setores alvo: %s
        - Regiões alvo: %s
        - Tema de interesse: %s
        
        Decisão: Esta empresa vale a pena investigar mais a fundo? 
        Responda apenas: SIM ou NÃO
        """,
        // ... parâmetros
);
```

**Proposto (Inglês)**:
```java
String prompt = String.format("""
        Company: %s
        Website: %s
        Industry: %s
        Location: %s
        
        ICP (Ideal Profile):
        - Target Industries: %s
        - Target Regions: %s
        - Interest Theme: %s
        
        Decision: Is this company worth investigating further? 
        Answer only: YES or NO
        """,
        // ... parâmetros
);
```

#### 3.4.2: Template enrichCompanyWithAI (Linha 69-95)

**Atual (Português)**:
```java
String prompt = String.format("""
        Analise esta empresa e enriqueça com informações relevantes para prospecção B2B.
        
        Empresa: %s
        Website: %s (%s)
        Setor: %s
        Score atual: %s
        
        Contexto ICP:
        - Interesse em: %s
        - Cargos alvo: %s
        
        Forneça uma análise estratégica focada em:
        1. Potencial de fit com nosso ICP
        2. Sinais de crescimento ou mudança
        3. Pain points prováveis
        4. Melhor abordagem recomendada
        
        Seja específico e acionável. Máximo 200 palavras.
        """,
        // ... parâmetros
);
```

**Proposto (Inglês)**:
```java
String prompt = String.format("""
        Analyze this company and enrich with relevant information for B2B prospecting.
        
        Company: %s
        Website: %s (%s)
        Industry: %s
        Current Score: %s
        
        ICP Context:
        - Interest in: %s
        - Target Roles: %s
        
        Provide a strategic analysis focused on:
        1. Potential fit with our ICP
        2. Growth or change signals
        3. Likely pain points
        4. Best recommended approach
        
        Be specific and actionable. Maximum 200 words.
        """,
        // ... parâmetros
);
```

## 🔧 Implementação

### Passo 1: Backup dos Arquivos
```bash
cp src/main/java/dev/prospectos/ai/service/StrategyAIService.java src/main/java/dev/prospectos/ai/service/StrategyAIService.java.backup
cp src/main/java/dev/prospectos/ai/service/ScoringAIService.java src/main/java/dev/prospectos/ai/service/ScoringAIService.java.backup
cp src/main/java/dev/prospectos/ai/service/OutreachAIService.java src/main/java/dev/prospectos/ai/service/OutreachAIService.java.backup
cp src/main/java/dev/prospectos/ai/service/ProspectorAIService.java src/main/java/dev/prospectos/ai/service/ProspectorAIService.java.backup
```

### Passo 2: Implementação por Arquivo

1. **StrategyAIService.java**:
   - Localizar método `recommendStrategy`
   - Substituir o template completo
   - Verificar se os parâmetros `%s` estão corretos

2. **ScoringAIService.java**:
   - Localizar método `scoreCompany`
   - Substituir o template completo
   - Manter a estrutura JSON exata

3. **OutreachAIService.java**:
   - Localizar método `generateOutreach`
   - Substituir o template completo
   - Verificar campos JSON

4. **ProspectorAIService.java**:
   - Substituir template em `shouldInvestigateCompany`
   - Substituir template em `enrichCompanyWithAI`

## 🧪 Validação

### Teste 1: Compilação
```bash
./gradlew compileJava
```

### Teste 2: Teste Funcional
```bash
./gradlew test --tests "*AIUsageExample*"
```

### Teste 3: Qualidade das Respostas
Execute cada serviço e compare:

#### Antes vs Depois - Critérios:
- **Consistência**: Respostas em formato mais padronizado
- **Precisão**: Análises mais detalhadas e precisas  
- **JSON**: Estrutura mais consistente
- **Raciocínio**: Explicações mais claras

### Exemplo de Teste Manual:
```java
// Criar dados de teste
Company testCompany = Company.create("TechCorp", Website.of("https://techcorp.com"), "Software");
ICP testIcp = ICP.create(/* parâmetros de teste */);

// Testar cada serviço
ScoringResult score = scoringService.scoreCompany(testCompany, testIcp);
StrategyRecommendation strategy = strategyService.recommendStrategy(testCompany, testIcp);
OutreachMessage outreach = outreachService.generateOutreach(testCompany, testIcp);
```

## 📊 Métricas de Qualidade

| Serviço | Métrica | Antes | Meta |
|---------|---------|-------|------|
| Scoring | JSON válido | 85% | 98% |
| Strategy | Raciocínio claro | 70% | 90% |
| Outreach | Personalização | 75% | 90% |
| Prospector | Decisão precisa | 80% | 95% |

## ⚠️ Cuidados Especiais

1. **Manter estrutura JSON exata** - Pequenas mudanças podem quebrar parsing
2. **Verificar parâmetros %s** - Ordem deve permanecer a mesma
3. **Testar com dados reais** - Não apenas mock data
4. **Monitorar qualidade** por alguns dias após mudança

## 📋 Checklist de Validação

### ✅ StrategyAIService.java
- [ ] Template convertido para inglês
- [ ] Estrutura JSON mantida
- [ ] Parâmetros %s corretos
- [ ] Compilação ok
- [ ] Teste funcional ok

### ✅ ScoringAIService.java
- [ ] Template convertido para inglês
- [ ] Critérios de scoring claros
- [ ] Estrutura JSON mantida
- [ ] Compilação ok
- [ ] Teste funcional ok

### ✅ OutreachAIService.java
- [ ] Template convertido para inglês
- [ ] Diretrizes claras
- [ ] Estrutura JSON mantida
- [ ] Compilação ok
- [ ] Teste funcional ok

### ✅ ProspectorAIService.java
- [ ] Template shouldInvestigate convertido
- [ ] Template enrichCompany convertido
- [ ] Lógica de decisão mantida
- [ ] Compilação ok
- [ ] Teste funcional ok

## 🔄 Rollback (se necessário)

```bash
# Restaurar backups se houver problemas
cp src/main/java/dev/prospectos/ai/service/StrategyAIService.java.backup src/main/java/dev/prospectos/ai/service/StrategyAIService.java
cp src/main/java/dev/prospectos/ai/service/ScoringAIService.java.backup src/main/java/dev/prospectos/ai/service/ScoringAIService.java
cp src/main/java/dev/prospectos/ai/service/OutreachAIService.java.backup src/main/java/dev/prospectos/ai/service/OutreachAIService.java
cp src/main/java/dev/prospectos/ai/service/ProspectorAIService.java.backup src/main/java/dev/prospectos/ai/service/ProspectorAIService.java

./gradlew build
```

## 🎯 Resultado Esperado

Após esta atividade:
- ✅ **+25% qualidade** nas respostas de IA
- ✅ **+40% consistência** no formato JSON
- ✅ **+30% precisão** nas análises
- ✅ **100% templates** em inglês padronizado

---

**Tempo estimado**: 60 minutos
**Pré-requisitos**: Conhecimento de Java e prompts de IA
**Próxima atividade**: [02-log-messages.md](./02-log-messages.md)
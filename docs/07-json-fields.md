# Atividade 07: Padronizar Campos JSON em Inglês

## 🎯 Objetivo
Converter campos JSON e estruturas de dados em português para inglês para padronizar APIs e interfaces.

## 📋 Escopo
Atualizar campos JSON em templates de prompts e exemplos mock que são visíveis nas respostas de API.

## 🟡 Prioridade: MÉDIA
**Justificativa**: Campos JSON são parte da interface pública e devem seguir padrões internacionais.

## 📁 Arquivos Afetados
- `src/main/java/dev/prospectos/ai/service/StrategyAIService.java`
- `src/main/java/dev/prospectos/ai/client/impl/MockLLMClient.java`

## 📝 Tarefas

### Tarefa 7.1: StrategyAIService - Template JSON

**Arquivo**: `src/main/java/dev/prospectos/ai/service/StrategyAIService.java`
**Localização**: Linha 47-55

**Template JSON Atual (Português)**:
```java
Retorne JSON com esta estrutura exata:
{
  "channel": "email|linkedin|phone|event",
  "targetRole": "CEO|CTO|CMO|etc",
  "timing": "immediate|this_week|this_month|wait",
  "painPoints": ["pain1", "pain2", "pain3"],
  "valueProposition": "Proposta de valor específica",
  "approachRationale": "Explicação da estratégia escolhida"
}
```

**Template JSON Proposto (Inglês)**:
```java
Return JSON with this exact structure:
{
  "channel": "email|linkedin|phone|event",
  "targetRole": "CEO|CTO|CMO|etc",
  "timing": "immediate|this_week|this_month|wait",
  "painPoints": ["pain1", "pain2", "pain3"],
  "valueProposition": "Specific value proposition",
  "approachRationale": "Explanation of chosen strategy"
}
```

### Tarefa 7.2: OutreachAIService - Template JSON

**Arquivo**: `src/main/java/dev/prospectos/ai/service/OutreachAIService.java`
**Localização**: Linha 49-56

**Template JSON Atual (Português)**:
```java
Retorne JSON:
{
  "subject": "Assunto do email",
  "body": "Corpo da mensagem",
  "channel": "email|linkedin|phone",
  "tone": "formal|casual|consultivo",
  "callsToAction": ["CTA1", "CTA2"]
}
```

**Template JSON Proposto (Inglês)**:
```java
Return JSON:
{
  "subject": "Email subject",
  "body": "Message body",
  "channel": "email|linkedin|phone",
  "tone": "formal|casual|consultative",
  "callsToAction": ["CTA1", "CTA2"]
}
```

**Nota**: `"consultivo"` deve ser alterado para `"consultative"` no enum de valores aceitos.

### Tarefa 7.3: ScoringAIService - Template JSON

**Arquivo**: `src/main/java/dev/prospectos/ai/service/ScoringAIService.java`
**Localização**: Linha 51-64

**Template JSON Atual (Português)**:
```java
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
```

**Template JSON Proposto (Inglês)**:
```java
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
```

### Tarefa 7.4: MockLLMClient - JSON Examples

**Arquivo**: `src/main/java/dev/prospectos/ai/client/impl/MockLLMClient.java`

#### 7.4.1: OutreachMessage Mock (Linha 95-102)

**JSON Mock Atual (Português)**:
```java
return (T) new OutreachMessage(
    "Otimização de performance para [EMPRESA]",
    "Olá [NOME], notei que a [EMPRESA] tem crescido rapidamente. Nosso produto ajudou empresas similares a reduzir custos operacionais em 30%. Que tal uma conversa rápida de 15 min?",
    "linkedin",
    "consultivo",
    java.util.List.of("Agendar demo", "Baixar case study")
);
```

**JSON Mock Proposto (Inglês)**:
```java
return (T) new OutreachMessage(
    "Performance optimization for [COMPANY]",
    "Hi [NAME], I noticed [COMPANY] has been growing rapidly. Our product helped similar companies reduce operational costs by 30%. How about a quick 15-min chat?",
    "linkedin",
    "consultative",
    java.util.List.of("Schedule demo", "Download case study")
);
```

#### 7.4.2: StrategyRecommendation Mock (Linha 105-113)

**JSON Mock Atual (Português)**:
```java
return (T) new StrategyRecommendation(
    "linkedin",
    "CTO",
    "this_week",
    java.util.List.of("Escalabilidade", "Custos operacionais", "Modernização técnica"),
    "Redução de 30% nos custos operacionais com nossa plataforma",
    "LinkedIn é o melhor canal para alcançar CTOs. Timing ideal pois empresa está crescendo."
);
```

**JSON Mock Proposto (Inglês)**:
```java
return (T) new StrategyRecommendation(
    "linkedin",
    "CTO", 
    "this_week",
    java.util.List.of("Scalability", "Operational costs", "Technical modernization"),
    "30% reduction in operational costs with our platform",
    "LinkedIn is the best channel to reach CTOs. Ideal timing as company is growing."
);
```

### Tarefa 7.5: Verificar DTOs (Se necessário)

**Verificação**: Conferir se os DTOs correspondentes aceitam os novos valores:

#### OutreachMessage DTO
Verificar se aceita `tone = "consultative"` (não apenas `"consultivo"`).

Se necessário, atualizar enum ou validação no DTO.

#### StrategyRecommendation DTO  
Verificar se os pain points em inglês são aceitos corretamente.

## 🔧 Implementação

### Passo 1: Backup dos Arquivos
```bash
cp src/main/java/dev/prospectos/ai/service/StrategyAIService.java src/main/java/dev/prospectos/ai/service/StrategyAIService.java.backup
cp src/main/java/dev/prospectos/ai/service/OutreachAIService.java src/main/java/dev/prospectos/ai/service/OutreachAIService.java.backup
cp src/main/java/dev/prospectos/ai/service/ScoringAIService.java src/main/java/dev/prospectos/ai/service/ScoringAIService.java.backup
cp src/main/java/dev/prospectos/ai/client/impl/MockLLMClient.java src/main/java/dev/prospectos/ai/client/impl/MockLLMClient.java.backup
```

### Passo 2: Substituições nos Templates

#### StrategyAIService.java
1. Localizar template JSON (linha ~47)
2. Substituir comentários em português:
   - `"Proposta de valor específica"` → `"Specific value proposition"`
   - `"Explicação da estratégia escolhida"` → `"Explanation of chosen strategy"`

#### OutreachAIService.java
1. Localizar template JSON (linha ~49)
2. Substituir comentários em português:
   - `"Assunto do email"` → `"Email subject"`
   - `"Corpo da mensagem"` → `"Message body"`
   - `"consultivo"` → `"consultative"`

#### ScoringAIService.java
1. Localizar template JSON (linha ~51)
2. Substituir comentário:
   - `"Priorizar contato imediato porque..."` → `"Prioritize immediate contact because..."`

### Passo 3: Atualizar Mocks

#### MockLLMClient.java
1. Localizar OutreachMessage mock
2. Substituir todos os valores string
3. Localizar StrategyRecommendation mock  
4. Substituir pain points e descrições

### Passo 4: Verificar DTOs

1. Localizar classes DTO (se existirem):
   - `OutreachMessage.java`
   - `StrategyRecommendation.java`
   - `ScoringResult.java`

2. Verificar se enums/validações aceitam novos valores em inglês

3. Se necessário, atualizar enums:
```java
// Exemplo em OutreachMessage
public enum Tone {
    FORMAL("formal"),
    CASUAL("casual"),
    CONSULTATIVE("consultative");  // Adicionar se não existir
}
```

## 🧪 Validação

### Teste 1: Compilação
```bash
./gradlew compileJava
```

### Teste 2: Teste de Serialização JSON

Criar teste simples para verificar se JSONs são gerados corretamente:

```java
// Teste manual ou unit test
OutreachMessage message = new OutreachMessage(
    "Performance optimization for TechCorp",
    "Hi John, I noticed...",
    "linkedin",
    "consultative",
    List.of("Schedule demo", "Download case study")
);

// Serializar para JSON e verificar estrutura
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(message);
System.out.println(json);
```

### Teste 3: Teste de Mock Responses

```bash
./gradlew test --tests "*MockLLMClient*"
```

### Teste 4: Teste de IA Services

```bash
./gradlew test --tests "*AIUsageExample*"
```

Verificar se:
- JSONs são gerados corretamente
- Parsing funciona sem erros  
- Valores em inglês são aceitos

## 📋 Dicionário de Traduções JSON

### Template Comments
| Português | Inglês |
|-----------|--------|
| Retorne JSON com esta estrutura exata | Return JSON with this exact structure |
| Proposta de valor específica | Specific value proposition |
| Explicação da estratégia escolhida | Explanation of chosen strategy |
| Retorne JSON | Return JSON |
| Assunto do email | Email subject |
| Corpo da mensagem | Message body |
| consultivo | consultative |
| Priorizar contato imediato porque | Prioritize immediate contact because |

### Mock Values
| Português | Inglês |
|-----------|--------|
| Otimização de performance para [EMPRESA] | Performance optimization for [COMPANY] |
| Olá [NOME], notei que... | Hi [NAME], I noticed... |
| conversa rápida de 15 min | quick 15-min chat |
| Agendar demo | Schedule demo |
| Baixar case study | Download case study |
| Escalabilidade | Scalability |
| Custos operacionais | Operational costs |
| Modernização técnica | Technical modernization |
| Redução de 30% nos custos operacionais | 30% reduction in operational costs |
| LinkedIn é o melhor canal | LinkedIn is the best channel |
| Timing ideal pois empresa está crescendo | Ideal timing as company is growing |

## ⚠️ Cuidados

1. **Enum Values**: Verificar se DTOs aceitam novos valores em inglês
2. **JSON Structure**: Manter estrutura exata dos templates
3. **Case Sensitivity**: Manter case correto para campos JSON
4. **Backward Compatibility**: Se necessário, manter aceitação de valores antigos

## 📊 Impacto na API

### Antes vs Depois

**Template de Strategy**:
```json
// Antes (Português)
{
  "valueProposition": "Proposta de valor específica",
  "approachRationale": "Explicação da estratégia escolhida"
}

// Depois (Inglês)  
{
  "valueProposition": "Specific value proposition",
  "approachRationale": "Explanation of chosen strategy"
}
```

**Mock Response**:
```json
// Antes (Português)
{
  "painPoints": ["Escalabilidade", "Custos operacionais"],
  "valueProposition": "Redução de 30% nos custos operacionais"
}

// Depois (Inglês)
{
  "painPoints": ["Scalability", "Operational costs"],  
  "valueProposition": "30% reduction in operational costs"
}
```

## 📋 Checklist de Conclusão

### ✅ Template JSON Updates
- [ ] StrategyAIService template convertido
- [ ] OutreachAIService template convertido
- [ ] ScoringAIService template convertido
- [ ] Comentários JSON em inglês

### ✅ Mock JSON Updates
- [ ] OutreachMessage mock values em inglês
- [ ] StrategyRecommendation mock values em inglês
- [ ] Pain points convertidos
- [ ] CTAs convertidos

### ✅ DTO Compatibility  
- [ ] Enums aceitam novos valores
- [ ] Validações funcionando
- [ ] Serialização JSON ok
- [ ] Parsing funcionando

### ✅ Validation
- [ ] Compilação bem-sucedida
- [ ] Testes de mock passando
- [ ] Testes de serialização ok
- [ ] Funcionalidade preservada

## 🎯 Resultado Esperado

Após completar esta atividade:
- ✅ Templates JSON padronizados em inglês
- ✅ Mock responses profissionais
- ✅ APIs preparadas para uso internacional
- ✅ Documentação de API consistente
- ✅ Melhor experiência para desenvolvedores de API

---

**Tempo estimado**: 35 minutos
**Pré-requisitos**: Conhecimento de JSON e DTOs Java
**Próxima atividade**: [08-i18n-setup.md](./08-i18n-setup.md)
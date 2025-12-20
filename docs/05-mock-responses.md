# Atividade 05: Converter Mock Responses para Inglês

## 🎯 Objetivo
Converter todas as respostas mock em português para inglês para padronizar testes e desenvolvimento.

## 📋 Escopo
Atualizar implementações mock que simulam respostas de IA com conteúdo em português.

## 🟡 Prioridade: MÉDIA
**Justificativa**: Mock responses são usadas em testes e desenvolvimento. Padronizar para inglês melhora consistência e facilita debug internacional.

## 📁 Arquivos Afetados
- `src/main/java/dev/prospectos/ai/client/impl/MockLLMClient.java`

## 📝 Tarefas

### Tarefa 5.1: MockLLMClient - Respostas de Query

**Arquivo**: `src/main/java/dev/prospectos/ai/client/impl/MockLLMClient.java`
**Localização**: Método `query()` - Linha 25-45

#### 5.1.1: Resposta de Decisão Sim/Não

**Atual (Português)**:
```java
if (prompt.toLowerCase().contains("sim ou não")) {
    return "SIM";
}
```

**Proposto (Inglês)**:
```java
if (prompt.toLowerCase().contains("yes or no")) {
    return "YES";
}
```

#### 5.1.2: Análise de Empresa Mock

**Atual (Português)**:
```java
if (prompt.toLowerCase().contains("empresa")) {
    return "Esta é uma empresa de tecnologia com grande potencial para nosso ICP. " +
           "Baseado na análise do website, eles usam tecnologias modernas como Java e Spring, " +
           "têm equipe de 50-200 funcionários e estão em fase de expansão.";
}
```

**Proposto (Inglês)**:
```java
if (prompt.toLowerCase().contains("company")) {
    return "This is a technology company with great potential for our ICP. " +
           "Based on website analysis, they use modern technologies like Java and Spring, " +
           "have a team of 50-200 employees and are in expansion phase.";
}
```

#### 5.1.3: Estratégia Mock

**Atual (Português)**:
```java
if (prompt.toLowerCase().contains("estratégia")) {
    return "Recomendo abordagem via LinkedIn direcionada ao CTO ou VP Engineering. " +
           "Timing ideal: próximas 2 semanas. Pain points: escalabilidade e modernização técnica. " +
           "Proposta de valor: redução de custos operacionais em 30%.";
}
```

**Proposto (Inglês)**:
```java
if (prompt.toLowerCase().contains("strategy")) {
    return "Recommend LinkedIn approach targeted at CTO or VP Engineering. " +
           "Ideal timing: next 2 weeks. Pain points: scalability and technical modernization. " +
           "Value proposition: 30% reduction in operational costs.";
}
```

#### 5.1.4: Resposta Padrão

**Atual (Português)**:
```java
return "Mock response para: " + prompt.substring(0, Math.min(50, prompt.length())) + "...";
```

**Proposto (Inglês)**:
```java
return "Mock response for: " + prompt.substring(0, Math.min(50, prompt.length())) + "...";
```

### Tarefa 5.2: MockLLMClient - Respostas com Functions

**Localização**: Método `queryWithFunctions()` - Linha 48-52

**Atual (Português)**:
```java
return "Mock response com funções: " + prompt.substring(0, Math.min(30, prompt.length())) + 
       ". Análise completa da empresa mostra forte adequação ao ICP.";
```

**Proposto (Inglês)**:
```java
return "Mock response with functions: " + prompt.substring(0, Math.min(30, prompt.length())) + 
       ". Complete company analysis shows strong ICP fit.";
```

### Tarefa 5.3: MockLLMClient - Structured Responses

**Localização**: Método `queryStructured()` - Linha 60-120

#### 5.3.1: ScoringResult Mock

**Atual (Português)**:
```java
return (T) new ScoringResult(
    85,
    Priority.HOT,
    "Empresa com forte adequação ao ICP. Score alto devido à tecnologia moderna, crescimento e equipe qualificada.",
    java.util.Map.of(
        "icpFit", 28,
        "signals", 22,
        "companySize", 18,
        "timing", 12,
        "accessibility", 5
    ),
    "Priorizar contato imediato. Empresa em crescimento com stack tecnológico compatível."
);
```

**Proposto (Inglês)**:
```java
return (T) new ScoringResult(
    85,
    Priority.HOT,
    "Company with strong ICP fit. High score due to modern technology, growth and qualified team.",
    java.util.Map.of(
        "icpFit", 28,
        "signals", 22,
        "companySize", 18,
        "timing", 12,
        "accessibility", 5
    ),
    "Prioritize immediate contact. Growing company with compatible tech stack."
);
```

#### 5.3.2: OutreachMessage Mock

**Atual (Português)**:
```java
return (T) new OutreachMessage(
    "Otimização de performance para [EMPRESA]",
    "Olá [NOME], notei que a [EMPRESA] tem crescido rapidamente. Nosso produto ajudou empresas similares a reduzir custos operacionais em 30%. Que tal uma conversa rápida de 15 min?",
    "linkedin",
    "consultivo",
    java.util.List.of("Agendar demo", "Baixar case study")
);
```

**Proposto (Inglês)**:
```java
return (T) new OutreachMessage(
    "Performance optimization for [COMPANY]",
    "Hi [NAME], I noticed [COMPANY] has been growing rapidly. Our product helped similar companies reduce operational costs by 30%. How about a quick 15-min chat?",
    "linkedin",
    "consultative",
    java.util.List.of("Schedule demo", "Download case study")
);
```

#### 5.3.3: StrategyRecommendation Mock

**Atual (Português)**:
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

**Proposto (Inglês)**:
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

### Tarefa 5.4: MockLLMClient - Log Messages

**Localização**: Linha 26

**Atual (Português)**:
```java
log.debug("🤖 Mock LLM Query: {}", prompt.substring(0, Math.min(100, prompt.length())));
```

**Proposto (Inglês)**:
```java
log.debug("🤖 Mock LLM Query: {}", prompt.substring(0, Math.min(100, prompt.length())));
```
*Nota: Esta já está em inglês, apenas verificar se existe algo em português no debug.*

### Tarefa 5.5: Atualizar Triggers de Detecção

Como mudamos as respostas, precisamos atualizar os triggers que detectam o tipo de prompt:

**Atualizações necessárias**:
```java
// Adicionar triggers em inglês mantendo os originais para compatibilidade
if (prompt.toLowerCase().contains("sim ou não") || 
    prompt.toLowerCase().contains("yes or no")) {
    return "YES";
}

if (prompt.toLowerCase().contains("empresa") || 
    prompt.toLowerCase().contains("company")) {
    // resposta em inglês
}

if (prompt.toLowerCase().contains("estratégia") || 
    prompt.toLowerCase().contains("strategy")) {
    // resposta em inglês
}
```

## 🔧 Implementação

### Passo 1: Backup do Arquivo
```bash
cp src/main/java/dev/prospectos/ai/client/impl/MockLLMClient.java src/main/java/dev/prospectos/ai/client/impl/MockLLMClient.java.backup
```

### Passo 2: Implementação Gradual

#### Estratégia Recomendada: Compatibilidade Dupla
Para não quebrar testes existentes, implemente triggers duplos:

```java
// Exemplo de implementação compatível
if (prompt.toLowerCase().contains("empresa") || 
    prompt.toLowerCase().contains("company")) {
    return "This is a technology company with great potential for our ICP. " +
           "Based on website analysis, they use modern technologies like Java and Spring, " +
           "have a team of 50-200 employees and are in expansion phase.";
}
```

### Passo 3: Substituições Específicas

1. **Abrir MockLLMClient.java**
2. **Atualizar método `query()`**:
   - Modificar detecção "sim ou não" 
   - Atualizar resposta de análise de empresa
   - Atualizar resposta de estratégia
   - Atualizar resposta padrão

3. **Atualizar método `queryWithFunctions()`**:
   - Modificar resposta mock

4. **Atualizar método `queryStructured()`**:
   - Modificar ScoringResult mock
   - Modificar OutreachMessage mock  
   - Modificar StrategyRecommendation mock

## 🧪 Validação

### Teste 1: Compilação
```bash
./gradlew compileJava
```

### Teste 2: Teste Unitário
```bash
./gradlew test --tests "*MockLLMClient*"
```

### Teste 3: Teste de Integration
```bash
./gradlew test --tests "*AIUsageExample*"
```
Verificar se os mocks funcionam corretamente nos exemplos.

### Teste 4: Validação Manual

#### Teste de Query Simples:
```java
MockLLMClient mock = new MockLLMClient();
String response = mock.query("Should we investigate this company? Yes or No");
// Deve retornar "YES"
```

#### Teste de Análise de Empresa:
```java
String response = mock.query("Analyze this company for our ICP");
// Deve retornar resposta em inglês sobre análise da empresa
```

#### Teste de Structured Response:
```java
ScoringResult result = mock.queryStructured("Score this company", ScoringResult.class);
// Deve retornar objeto com reasoning em inglês
```

## 📋 Dicionário de Traduções

| Português | Inglês |
|-----------|--------|
| SIM | YES |
| empresa | company |
| Esta é uma empresa de tecnologia | This is a technology company |
| grande potencial para nosso ICP | great potential for our ICP |
| Baseado na análise do website | Based on website analysis |
| têm equipe de 50-200 funcionários | have a team of 50-200 employees |
| estão em fase de expansão | are in expansion phase |
| estratégia | strategy |
| Recomendo abordagem via LinkedIn | Recommend LinkedIn approach |
| próximas 2 semanas | next 2 weeks |
| escalabilidade e modernização técnica | scalability and technical modernization |
| redução de custos operacionais | reduction in operational costs |
| Mock response para | Mock response for |
| Análise completa da empresa | Complete company analysis |
| forte adequação ao ICP | strong ICP fit |
| Score alto devido à tecnologia moderna | High score due to modern technology |
| Priorizar contato imediato | Prioritize immediate contact |
| Empresa em crescimento | Growing company |
| Otimização de performance | Performance optimization |
| conversa rápida de 15 min | quick 15-min chat |
| Agendar demo | Schedule demo |
| Baixar case study | Download case study |
| Custos operacionais | Operational costs |
| Modernização técnica | Technical modernization |
| melhor canal para alcançar | best channel to reach |
| Timing ideal pois empresa está crescendo | Ideal timing as company is growing |

## ⚠️ Cuidados

1. **Compatibilidade**: Manter triggers duplos (PT/EN) para não quebrar testes
2. **Formato JSON**: Preservar estrutura exata dos objetos mock
3. **Tipo de retorno**: Verificar se tipos genéricos estão corretos
4. **Case sensitivity**: Usar lowercase nos triggers de detecção

## 📊 Impacto dos Mocks

### Cenários de Uso:
1. **Desenvolvimento local** sem API keys configuradas
2. **Testes automatizados** que precisam de respostas previsíveis  
3. **Demonstrações** sem dependência de APIs externas
4. **Debug** de lógica de negócio sem custos de API

### Qualidade Esperada:
- **Antes**: Mocks em português podem confundir desenvolvedores internacionais
- **Depois**: Mocks padronizados facilitam onboarding e debug

## 📋 Checklist de Conclusão

### ✅ Método query()
- [ ] Trigger "yes or no" adicionado
- [ ] Resposta de empresa em inglês
- [ ] Resposta de estratégia em inglês  
- [ ] Resposta padrão em inglês
- [ ] Compatibilidade com triggers antigos

### ✅ Método queryWithFunctions()
- [ ] Resposta mock atualizada para inglês

### ✅ Método queryStructured()
- [ ] ScoringResult reasoning em inglês
- [ ] ScoringResult recommendation em inglês
- [ ] OutreachMessage subject em inglês
- [ ] OutreachMessage body em inglês
- [ ] OutreachMessage CTAs em inglês
- [ ] StrategyRecommendation pain points em inglês
- [ ] StrategyRecommendation value proposition em inglês
- [ ] StrategyRecommendation rationale em inglês

### ✅ Validação
- [ ] Compilação bem-sucedida
- [ ] Testes unitários passando
- [ ] Testes de integração passando
- [ ] Funcionalidade preservada

## 🎯 Resultado Esperado

Após completar esta atividade:
- ✅ Mocks padronizados em inglês
- ✅ Melhor experiência para desenvolvedores internacionais
- ✅ Testes mais profissionais e legíveis
- ✅ Preparação para ambientes de produção internacional
- ✅ Debug facilitado em equipes multilíngues

---

**Tempo estimado**: 40 minutos
**Pré-requisitos**: Conhecimento de Java e tipos genéricos
**Próxima atividade**: [06-documentation.md](./06-documentation.md)
# Atividade 04: Traduzir Enums e Constantes

## 🎯 Objetivo
Converter todas as constantes, enums e valores fixos de português para inglês para padronizar a API e interfaces.

## 📋 Escopo
Atualizar enums, constantes e valores fixos que contêm texto em português usado nas interfaces públicas.

## 🟢 Prioridade: BAIXA
**Justificativa**: Não afeta funcionalidade core, mas melhora padronização e legibilidade internacional.

## 📁 Arquivos Afetados
- `src/main/java/dev/prospectos/ai/client/LLMProvider.java`
- `src/main/java/dev/prospectos/ai/example/AIUsageExample.java`

## 📝 Tarefas

### Tarefa 4.1: LLMProvider Enum

**Arquivo**: `src/main/java/dev/prospectos/ai/client/LLMProvider.java`
**Localização**: Linha 8-11

**Enum Atual (Português)**:
```java
public enum LLMProvider {
    OPENAI("OpenAI GPT-4", "Melhor qualidade geral"),
    ANTHROPIC("Claude 3.5 Sonnet", "Melhor análise complexa"),
    OLLAMA("Ollama Local", "Gratuito, execução local"),
    MOCK("Mock Provider", "Para testes");
    
    private final String displayName;
    private final String description;
    
    LLMProvider(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
```

**Enum Proposto (Inglês)**:
```java
public enum LLMProvider {
    OPENAI("OpenAI GPT-4", "Best overall quality"),
    ANTHROPIC("Claude 3.5 Sonnet", "Best complex analysis"),
    OLLAMA("Ollama Local", "Free, local execution"),
    MOCK("Mock Provider", "For testing");
    
    private final String displayName;
    private final String description;
    
    LLMProvider(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### Tarefa 4.2: AIUsageExample - Constantes de Prompt

**Arquivo**: `src/main/java/dev/prospectos/ai/example/AIUsageExample.java`

#### 4.2.1: Prompt de Análise de Fit (Linha 112-115)

**Atual (Português)**:
```java
String prompt = String.format(
    "Empresa: %s, Setor: %s. ICP: %s. Adequação?",
    company.getName(), company.getIndustry(), icp.getInterestTheme()
);
```

**Proposto (Inglês)**:
```java
String prompt = String.format(
    "Company: %s, Industry: %s. ICP: %s. Fit?",
    company.getName(), company.getIndustry(), icp.getInterestTheme()
);
```

#### 4.2.2: Prompt de Score (Linha 123-126)

**Atual (Português)**:
```java
String prompt = String.format(
    "Score empresa %s do setor %s para ICP %s",
    company.getName(), company.getIndustry(), icp.getInterestTheme()
);
```

**Proposto (Inglês)**:
```java
String prompt = String.format(
    "Score company %s from %s industry for ICP %s",
    company.getName(), company.getIndustry(), icp.getInterestTheme()
);
```

#### 4.2.3: Prompt de Estratégia (Linha 134-137)

**Atual (Português)**:
```java
String prompt = String.format(
    "Estratégia para %s do setor %s",
    company.getName(), company.getIndustry()
);
```

**Proposto (Inglês)**:
```java
String prompt = String.format(
    "Strategy for %s from %s industry",
    company.getName(), company.getIndustry()
);
```

#### 4.2.4: Prompt de Outreach (Linha 145-148)

**Atual (Português)**:
```java
String prompt = String.format(
    "Outreach para %s sobre %s",
    company.getName(), icp.getInterestTheme()
);
```

**Proposto (Inglês)**:
```java
String prompt = String.format(
    "Outreach for %s about %s",
    company.getName(), icp.getInterestTheme()
);
```

### Tarefa 4.3: ProspectorAIService - Constante de String

**Arquivo**: `src/main/java/dev/prospectos/ai/service/ProspectorAIService.java`
**Localização**: Linha 61

**Atual (Português)**:
```java
company.getAiAnalysis() != null ? company.getAiAnalysis() : "Não disponível"
```

**Proposto (Inglês)**:
```java
company.getAiAnalysis() != null ? company.getAiAnalysis() : "Not available"
```

## 🔧 Implementação

### Passo 1: Backup dos Arquivos
```bash
cp src/main/java/dev/prospectos/ai/client/LLMProvider.java src/main/java/dev/prospectos/ai/client/LLMProvider.java.backup
cp src/main/java/dev/prospectos/ai/example/AIUsageExample.java src/main/java/dev/prospectos/ai/example/AIUsageExample.java.backup
cp src/main/java/dev/prospectos/ai/service/ProspectorAIService.java src/main/java/dev/prospectos/ai/service/ProspectorAIService.java.backup
```

### Passo 2: Substituições Específicas

#### LLMProvider.java
1. Abrir arquivo no IDE
2. Localizar as descrições no enum
3. Aplicar substituições:
   - "Melhor qualidade geral" → "Best overall quality"
   - "Melhor análise complexa" → "Best complex analysis"  
   - "Gratuito, execução local" → "Free, local execution"

#### AIUsageExample.java
1. Localizar método `analyzeFit` (linha ~111)
2. Substituir prompt: "Adequação?" → "Fit?"
3. Localizar método `calculateScore` (linha ~122)
4. Substituir prompt conforme especificado
5. Localizar método `generateStrategy` (linha ~133)
6. Substituir prompt conforme especificado
7. Localizar método `generateOutreach` (linha ~144)
8. Substituir prompt conforme especificado

#### ProspectorAIService.java
1. Localizar linha com "Não disponível"
2. Substituir por "Not available"

### Passo 3: Verificação por Find & Replace

```bash
# No IDE, usar Find & Replace (Ctrl+H) com estas substituições:

# LLMProvider.java
Find: "Melhor qualidade geral"
Replace: "Best overall quality"

Find: "Melhor análise complexa"  
Replace: "Best complex analysis"

Find: "Gratuito, execução local"
Replace: "Free, local execution"

# AIUsageExample.java
Find: "Adequação?"
Replace: "Fit?"

Find: "Score empresa %s do setor %s para ICP %s"
Replace: "Score company %s from %s industry for ICP %s"

Find: "Estratégia para %s do setor %s"
Replace: "Strategy for %s from %s industry"

Find: "Outreach para %s sobre %s"
Replace: "Outreach for %s about %s"

# ProspectorAIService.java
Find: "Não disponível"
Replace: "Not available"
```

## 🧪 Validação

### Teste 1: Compilação
```bash
./gradlew compileJava
```
**Resultado esperado**: BUILD SUCCESSFUL

### Teste 2: Verificação de Enums
```java
// Teste manual no IDE ou console
LLMProvider.OPENAI.getDescription(); // deve retornar "Best overall quality"
LLMProvider.ANTHROPIC.getDescription(); // deve retornar "Best complex analysis"
LLMProvider.OLLAMA.getDescription(); // deve retornar "Free, local execution"
```

### Teste 3: Execução dos Exemplos
```bash
./gradlew test --tests "*AIUsageExample*"
```
Verificar nos logs se os prompts aparecem em inglês.

### Teste 4: Busca por Strings Restantes
```bash
# Verificar se ainda há strings em português
grep -r "Melhor\|Gratuito\|Adequação\|Estratégia\|Não disponível" src/main/java/
# Não deve retornar resultados nos arquivos modificados
```

## 📋 Dicionário de Traduções

| Português | Inglês |
|-----------|--------|
| Melhor qualidade geral | Best overall quality |
| Melhor análise complexa | Best complex analysis |
| Gratuito, execução local | Free, local execution |
| Para testes | For testing |
| Adequação? | Fit? |
| Score empresa X do setor Y para ICP Z | Score company X from Y industry for ICP Z |
| Estratégia para X do setor Y | Strategy for X from Y industry |
| Outreach para X sobre Y | Outreach for X about Y |
| Não disponível | Not available |

## ⚠️ Cuidados

1. **Manter formatação**: Preservar estrutura dos String.format()
2. **Case sensitivity**: Manter maiúsculas/minúsculas apropriadas
3. **Pontuação**: Verificar pontos de interrogação e outros sinais
4. **Contexto**: Garantir que a tradução faz sentido no contexto

## 📊 Impacto

### Antes vs Depois

**Enum Descriptions**:
```java
// Antes
OPENAI.getDescription() = "Melhor qualidade geral"
// Depois  
OPENAI.getDescription() = "Best overall quality"
```

**Example Prompts**:
```java
// Antes
"Empresa: TechCorp, Setor: Software. ICP: DevOps. Adequação?"
// Depois
"Company: TechCorp, Industry: Software. ICP: DevOps. Fit?"
```

## 📋 Checklist de Conclusão

### ✅ LLMProvider.java
- [ ] "Melhor qualidade geral" → "Best overall quality"
- [ ] "Melhor análise complexa" → "Best complex analysis"  
- [ ] "Gratuito, execução local" → "Free, local execution"
- [ ] Compilação bem-sucedida
- [ ] Teste de enum funcionando

### ✅ AIUsageExample.java
- [ ] Prompt analyzeFit convertido
- [ ] Prompt calculateScore convertido
- [ ] Prompt generateStrategy convertido
- [ ] Prompt generateOutreach convertido
- [ ] Compilação bem-sucedida
- [ ] Execução de exemplo funcionando

### ✅ ProspectorAIService.java
- [ ] "Não disponível" → "Not available"
- [ ] Compilação bem-sucedida

### ✅ Validação Geral
- [ ] Nenhuma string em português restante
- [ ] Todos os testes passando
- [ ] Funcionalidade preservada

## 🎯 Resultado Esperado

Após completar esta atividade:
- ✅ Todas as constantes públicas em inglês
- ✅ Enums padronizados internacionalmente  
- ✅ Prompts de exemplo mais profissionais
- ✅ Melhor legibilidade para desenvolvedores internacionais
- ✅ Preparação para futuras APIs públicas

---

**Tempo estimado**: 20 minutos
**Pré-requisitos**: Editor com Find & Replace
**Próxima atividade**: [05-mock-responses.md](./05-mock-responses.md)
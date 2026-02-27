# Pull Request - ProspectOS

## Descrição
<!-- Descreva brevemente as mudanças propostas -->

## Tipo de Mudança
- [ ] 🐛 Bug fix (mudança que corrige um problema)
- [ ] ✨ Nova feature (mudança que adiciona funcionalidade)
- [ ] 💥 Breaking change (mudança que quebra compatibilidade)
- [ ] 📝 Documentação (mudança apenas em documentação)
- [ ] ⚙️ Configuração (mudança em profiles, env vars, propriedades)
- [ ] 🧪 Testes (adição ou correção de testes)
- [ ] ♻️ Refatoração (mudança de código sem alterar funcionalidade)

## 🔧 Checklist de Configuração
<!-- Marque se sua mudança afeta qualquer um dos itens abaixo -->

### Profiles e Propriedades
- [ ] Modificou `application.properties` ou `application-*.properties`
- [ ] Adicionou/removeu/alterou variável de ambiente
- [ ] Modificou `DotenvEnvironmentPostProcessor.java`
- [ ] Atualizou `.env.example` correspondentemente

### Documentação de Configuração
- [ ] Atualizou `CLAUDE.md` se mudou comandos ou arquitetura
- [ ] Atualizou `README.md` se mudou configuração ou uso
- [ ] Verificou consistência entre todos os arquivos de propriedades
- [ ] Documentou novas variáveis de ambiente no `.env.example`

### AI Providers
- [ ] Modificou configuração de chaves de AI (OpenAI, Anthropic, Groq)
- [ ] Testou com as convenções preferidas (`PROSPECTOS_AI_GROQ_API_KEY`)
- [ ] Verificou backward compatibility (`GROQ_API_KEY`)
- [ ] Atualizou mensagens de erro para usar convenções corretas

### Lead Sources
- [ ] Modificou `prospectos.leads.allowed-sources`
- [ ] Verificou consistência entre profiles (mock, development, test, production)
- [ ] Atualizou documentação técnica relacionada
- [ ] Testou com fontes habilitadas/desabilitadas

## ✅ Verificações Gerais

### Testes
- [ ] Executei `./gradlew test` e todos passaram
- [ ] Executei `./gradlew test --tests "*ModulithTest"` para verificar boundaries
- [ ] Testei localmente com profile `mock` (sem API keys reais)
- [ ] Se modificou config de teste, rodei testes de integração

### Arquitetura Spring Modulith
- [ ] Não violei boundaries entre módulos (core deve permanecer independente)
- [ ] Adições ao core module não dependem de ai/infrastructure
- [ ] Mudanças seguem os padrões DDD estabelecidos

### Segurança
- [ ] Não commitei secrets, API keys ou tokens
- [ ] Não modifiquei `.env` real (apenas `.env.example`)
- [ ] Chaves de exemplo são claramente dummy/fake

## 🎯 Impacto

### Breaking Changes
<!-- Se marcou "Breaking change" acima, descreva o impacto -->
- [ ] Não há breaking changes
- [ ] Documentei breaking changes e migration path

### Performance
- [ ] Mudanças não afetam performance
- [ ] Testei impacto em performance (se relevante)

## 📋 Testes Executados
<!-- Descreva quais testes específicos executou -->
```bash
# Exemplos:
./gradlew test --tests "dev.prospectos.core.domain.*"
./gradlew test --tests "dev.prospectos.integration.*"
# Adicione os comandos que executou
```

## 📖 Documentação Adicional
<!-- Links para issues, docs, ou contexto adicional -->
- Closes #[número do issue]
- Related: [link para docs/tasks relacionadas]

## ⚠️ Notas para Reviewers
<!-- Anything specific reviewers should focus on -->

---

### 🔍 Para o Reviewer: Checklist de Validação
- [ ] Verificou se mudanças de config são consistentes entre profiles
- [ ] Confirmou que documentação foi atualizada apropriadamente  
- [ ] Testou localmente se possível
- [ ] Validou que boundaries modulith são respeitados
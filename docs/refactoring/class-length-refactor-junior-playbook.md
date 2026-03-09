# Class Length Refactor - Junior Playbook

## Objetivo
Padronizar como reduzir classes longas com seguranca, usando testes de caracterizacao antes da refatoracao.

## Contexto Ja Implementado
- Regra de tamanho documentada no `AGENTS.md`:
  - alvo: 50 linhas por classe
  - limite aceitavel: 100 linhas
- Teste de lint criado: `ClassLengthLintTest`
- Baseline atual criado: `class-length-allowlist.txt`
- Testes de caracterizacao adicionados para `DotenvEnvironmentPostProcessor`

## Pre-requisitos
1. Java 21 instalado.
2. Docker ativo (quando for rodar testes de integracao que dependem disso).
3. Usar o wrapper Gradle do projeto.
4. Em PowerShell, definir o cache local do Gradle para evitar lock externo:

```powershell
$env:GRADLE_USER_HOME='D:\Cursos\prospectos\.gradle-user-home'
```

## Regra de Trabalho (obrigatoria)
1. Sempre comecar por teste de caracterizacao (Red -> Green -> Refactor).
2. Fazer mudancas pequenas e frequentes.
3. Nao misturar refatoracao com mudanca de comportamento.
4. Rodar testes a cada passo pequeno.

## Passo a Passo Operacional

### Passo 1 - Sincronizar e validar baseline
Execute:

```powershell
./gradlew test --tests 'dev.prospectos.quality.ClassLengthLintTest' -x jacocoTestCoverageVerification
```

Esperado: verde. Se falhar, corrigir baseline antes de iniciar qualquer refatoracao.

### Passo 2 - Escolher 1 classe alvo
1. Abrir `src/test/resources/quality/class-length-allowlist.txt`.
2. Escolher apenas uma classe acima de 100 linhas para trabalhar no ciclo.
3. Registrar no PR/issue qual classe foi escolhida.

### Passo 3 - Criar testes de caracterizacao da classe alvo
1. Identificar comportamento publico atual.
2. Escrever testes que descrevem o comportamento atual (inclusive casos de borda).
3. Rodar somente os testes novos e garantir que reproduzem o comportamento existente.

Comando exemplo:

```powershell
./gradlew test --tests 'dev.prospectos.<pacote>.ClasseAlvoTest' -x jacocoTestCoverageVerification
```

### Passo 4 - Refatorar em micro-passos
1. Extrair metodos pequenos com nomes claros.
2. Se necessario, extrair nova classe com responsabilidade unica.
3. Nao alterar contrato publico sem necessidade.
4. A cada micro-passo, rodar os testes da classe alvo.

### Passo 5 - Validar regra de tamanho
Depois da refatoracao, rodar:

```powershell
./gradlew test --tests 'dev.prospectos.quality.ClassLengthLintTest' -x jacocoTestCoverageVerification
```

1. Se a classe caiu para <=100 linhas, remover da allowlist.
2. Se ainda >100, manter allowlist e documentar motivo tecnico + plano de quebra adicional.

### Passo 6 - Validacao final
Rodar suite completa:

```powershell
./gradlew test
```

Esperado: `BUILD SUCCESSFUL`.

## Checklist de Entrega
- [ ] Testes de caracterizacao criados/atualizados para a classe alvo.
- [ ] Refatoracao feita sem regressao comportamental.
- [ ] `ClassLengthLintTest` verde.
- [ ] Allowlist atualizada (removeu entrada obsoleta ou justificou permanencia).
- [ ] Suite completa `./gradlew test` verde.
- [ ] Documentacao do que foi feito no PR.

## Definition of Done
A tarefa so termina quando:
1. Comportamento antigo esta protegido por testes.
2. Refatoracao foi aplicada com seguranca.
3. Limite de 100 linhas foi respeitado ou excecao foi justificada de forma temporaria.
4. Todos os testes passaram localmente.

## Erros Comuns (evitar)
1. Refatorar sem teste de caracterizacao.
2. Mexer em varias classes grandes ao mesmo tempo.
3. Esquecer de limpar entrada obsoleta da allowlist.
4. Rodar apenas teste unitario e nao validar a suite completa.

## Modelo curto para descricao de atividade (junior)
Use este texto ao abrir uma tarefa:

```text
Objetivo: reduzir tamanho da classe <Classe> com seguranca.
Abordagem: criar testes de caracterizacao, refatorar em micro-passos e validar lint de tamanho.
Criterios de aceite:
1) testes da classe alvo verdes,
2) ClassLengthLintTest verde,
3) suite completa ./gradlew test verde,
4) allowlist atualizada.
```

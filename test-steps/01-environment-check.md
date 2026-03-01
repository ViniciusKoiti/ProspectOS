# PASSO 1: Validação do Ambiente

## Objetivo
Verificar se o ambiente está configurado corretamente antes de executar os testes.

## Comandos para Executar

### 1.1 Verificar Java
```bash
java -version
# Deve mostrar Java 21
```

### 1.2 Verificar Gradle
```bash
./gradlew --version
# Deve funcionar sem erros
```

### 1.3 Build do Projeto
```bash
./gradlew build -x test
# Deve compilar sem erros
```

## Critérios de Sucesso
- [ ] Java 21 está instalado
- [ ] Gradle funciona corretamente  
- [ ] Projeto compila sem erros
- [ ] Nenhuma dependência faltando

## Em Caso de Erro
- Verificar JAVA_HOME
- Verificar conexão com internet para dependências
- Revisar logs de erro do Gradle

## Próximo Passo
Se tudo passou → **PASSO 2: Startup da Aplicação**
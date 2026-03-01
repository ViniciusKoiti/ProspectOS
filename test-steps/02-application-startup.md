# PASSO 2: Startup da Aplicação

## Objetivo
Inicializar a aplicação no modo mock e verificar se está funcionando.

## Comandos para Executar

### 2.1 Iniciar Aplicação
```bash
./gradlew bootRun --args="--spring.profiles.active=mock"
```

### 2.2 Aguardar Startup (aguarde ver essas mensagens)
```
✓ Started ProspectosApplication in X.X seconds
✓ Database seeded successfully with X companies and X ICPs  
✓ H2 console available at '/h2-console'
✓ Tomcat started on port 8080
```

### 2.3 Teste de Conectividade (em outro terminal)
```bash
curl http://localhost:8080/api/companies
# Deve retornar JSON com empresas
```

## Critérios de Sucesso
- [ ] Aplicação inicia sem erros
- [ ] Database H2 é criado automaticamente
- [ ] DataSeeder executa e popula dados
- [ ] Servidor responde na porta 8080
- [ ] H2 Console está acessível

## Logs Importantes para Observar
```
✓ HikariPool-1 - Start completed
✓ Database seeded successfully  
✓ Started ProspectosApplication
```

## Em Caso de Erro
- Verificar se porta 8080 está livre
- Verificar logs de erro no console
- Confirmar que perfil "mock" está ativo

## Próximo Passo  
Se aplicação iniciou → **PASSO 3: Testes dos Endpoints Básicos**
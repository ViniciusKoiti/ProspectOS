/**
 * Infrastructure Module
 * 
 * <p>Módulo de infraestrutura responsável por implementações técnicas
 * e integrações com frameworks externos.
 * 
 * <h2>Responsabilidades</h2>
 * <ul>
 *   <li>Implementações JPA dos repositories de domínio</li>
 *   <li>Configurações de banco de dados</li>
 *   <li>Adaptadores para frameworks externos</li>
 *   <li>Implementações de serviços de aplicação</li>
 * </ul>
 * 
 * <h2>Dependências Permitidas</h2>
 * <ul>
 *   <li>core - Acesso ao domínio e interfaces</li>
 *   <li>Spring Framework - Para implementações</li>
 *   <li>JPA/Hibernate - Para persistência</li>
 * </ul>
 * 
 * <h2>Padrões Arquiteturais</h2>
 * <ul>
 *   <li>Adapter Pattern - Para integrar domínio com JPA</li>
 *   <li>Repository Pattern - Implementações concretas</li>
 *   <li>Dependency Inversion - Depende de abstrações do core</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Infrastructure Module",
    allowedDependencies = {"core"}
)
package dev.prospectos.infrastructure;
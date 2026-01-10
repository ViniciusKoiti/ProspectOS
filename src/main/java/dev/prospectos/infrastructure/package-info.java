/**
 * Infrastructure Module
 * 
 * <p>Infrastructure module responsible for technical implementations
 * and integrations with external frameworks.
 * 
 * <h2>Responsabilidades</h2>
 * <ul>
 *   <li>JPA implementations of domain repositories</li>
 *   <li>Database configurations</li>
 *   <li>External framework adapters</li>
 *   <li>Application service implementations</li>
 * </ul>
 * 
 * <h2>Allowed Dependencies</h2>
 * <ul>
 *   <li>core - Access to domain and interfaces</li>
 *   <li>Spring Framework - For implementations</li>
 *   <li>JPA/Hibernate - For persistence</li>
 * </ul>
 * 
 * <h2>Architectural Patterns</h2>
 * <ul>
 *   <li>Adapter Pattern - To integrate domain with JPA</li>
 *   <li>Repository Pattern - Concrete implementations</li>
 *   <li>Dependency Inversion - Depends on core abstractions</li>
 * </ul>
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Infrastructure Module",
    allowedDependencies = {"core"}
)
package dev.prospectos.infrastructure;

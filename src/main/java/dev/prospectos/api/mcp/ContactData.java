package dev.prospectos.api.mcp;

import java.util.Objects;

/**
 * Contact information.
 */
public record ContactData(
    String name,
    String role,
    String email,
    String linkedIn,
    double confidence
) {

    public ContactData {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(linkedIn, "linkedIn must not be null");
    }
}

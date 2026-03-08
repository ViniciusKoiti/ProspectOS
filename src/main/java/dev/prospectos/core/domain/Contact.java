package dev.prospectos.core.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class Contact {

    private final String name;

    @Embedded
    private final Email email;

    private final String position;
    private final String phoneNumber;

    protected Contact() {
        this.name = null;
        this.email = null;
        this.position = null;
        this.phoneNumber = null;
    }

    public Contact(String name, Email email, String position, String phoneNumber) {
        this.name = validateContactName(name);
        this.email = email;
        this.position = position;
        this.phoneNumber = phoneNumber;
    }

    private String validateContactName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name cannot be null or empty");
        }
        return name.trim();
    }

    public boolean hasValidEmail() {
        return email != null;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public String getPosition() {
        return position;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}

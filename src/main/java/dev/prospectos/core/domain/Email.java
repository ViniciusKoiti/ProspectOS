package dev.prospectos.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an email address.
 * Ensures email validation and immutability.
 */
@Embeddable
public final class Email {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    @Column(name = "email_address")
    private final String address;
    
    @Column(name = "email_domain")
    private final String domain;
    
    @Column(name = "email_local_part")
    private final String localPart;
    
    protected Email() {
        // For JPA
        this.address = null;
        this.domain = null;
        this.localPart = null;
    }
    
    private Email(String address) {
        this.address = address.toLowerCase();
        int atIndex = this.address.indexOf('@');
        this.localPart = this.address.substring(0, atIndex);
        this.domain = this.address.substring(atIndex + 1);
    }
    
    public static Email of(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        String trimmedEmail = email.trim();
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        
        return new Email(trimmedEmail);
    }
    
    public String getAddress() {
        return address;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public String getLocalPart() {
        return localPart;
    }
    
    public boolean isPersonalEmail() {
        String[] personalDomains = {
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", 
            "icloud.com", "aol.com", "protonmail.com"
        };
        
        for (String personalDomain : personalDomains) {
            if (domain.equals(personalDomain)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isCorporateEmail() {
        return !isPersonalEmail();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(address, email.address);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
    
    @Override
    public String toString() {
        return address;
    }
}
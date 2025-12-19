package dev.prospectos.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Value object representing a company's website.
 * Ensures URL validation and immutability.
 */
@Embeddable
public final class Website {
    
    @Column(name = "website_url")
    private final String url;
    
    @Column(name = "website_domain")
    private final String domain;
    
    protected Website() {
        // For JPA
        this.url = null;
        this.domain = null;
    }
    
    private Website(String url, String domain) {
        this.url = url;
        this.domain = domain;
    }
    
    public static Website of(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Website URL cannot be null or empty");
        }
        
        String normalizedUrl = normalizeUrl(url.trim());
        String domain = extractDomain(normalizedUrl);
        
        return new Website(normalizedUrl, domain);
    }
    
    private static String normalizeUrl(String url) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            URI uri = new URI(url);
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid website URL: " + url, e);
        }
    }
    
    private static String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) {
                throw new IllegalArgumentException("Cannot extract domain from URL: " + url);
            }
            return host.toLowerCase();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL for domain extraction: " + url, e);
        }
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getDomain() {
        return domain;
    }
    
    public boolean isSecure() {
        return url.startsWith("https://");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Website website = (Website) o;
        return Objects.equals(url, website.url);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
    
    @Override
    public String toString() {
        return url;
    }
}
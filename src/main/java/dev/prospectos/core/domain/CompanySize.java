package dev.prospectos.core.domain;

public enum CompanySize {
    STARTUP("1-10 employees"),
    SMALL("11-50 employees"),
    MEDIUM("51-200 employees"),
    LARGE("201-1000 employees"),
    ENTERPRISE("1000+ employees");

    private final String description;

    CompanySize(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

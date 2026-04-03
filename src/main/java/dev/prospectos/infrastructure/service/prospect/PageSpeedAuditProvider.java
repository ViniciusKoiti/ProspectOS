package dev.prospectos.infrastructure.service.prospect;

public interface PageSpeedAuditProvider {
    PageSpeedAuditResult audit(String website);
}

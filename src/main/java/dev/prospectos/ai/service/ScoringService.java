package dev.prospectos.ai.service;

import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;

/**
 * Interface for company scoring services.
 * Allows different implementations (real AI vs mock) based on configuration.
 */
public interface ScoringService {
    
    /**
     * Calculates a company score (0-100) using AI or mock implementation.
     * Returns a structured object with scoring details.
     */
    ScoringResult scoreCompany(Company company, ICP icp);
}
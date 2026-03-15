package dev.prospectos.core.domain;

import java.util.Map;

import dev.prospectos.core.domain.events.CompanyCreated;
import dev.prospectos.core.domain.events.CompanyScored;
import dev.prospectos.core.domain.events.SignalDetected;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "companies")
public class Company extends CompanyCoreState {

    protected Company() {
    }

    private Company(String name, Website website, String industry) {
        initializeCoreState(name, website, industry);
        registerEvent(new CompanyCreated(id, this.name, website.getUrl()));
    }

    public static Company create(String name, Website website, String industry) {
        return new Company(name, website, industry);
    }

    public void updateProfile(String name, Website website, String industry) {
        if (website == null) {
            throw new IllegalArgumentException("Website cannot be null");
        }
        this.name = validateName(name);
        this.website = website;
        this.industry = industry;
        touch();
    }

    public void normalizeExternalId(Long externalId) {
        assignExternalId(externalId);
    }

    public void updateScore(Score newScore, String reason) {
        if (newScore == null) {
            throw new IllegalArgumentException("Score cannot be null");
        }
        Score previousScore = this.prospectingScore;
        this.prospectingScore = newScore;
        if (prospectingScore.isHighPriority() && status == ProspectingStatus.NEW) {
            status = ProspectingStatus.REVIEWING;
        }
        touch();
        registerEvent(new CompanyScored(id, name, previousScore.getValue(), newScore.getValue(), reason));
    }

    public void detectSignal(SignalDetected.SignalType signalType, String description, Map<String, Object> data) {
        if (signalType == SignalDetected.SignalType.TECHNOLOGY_ADOPTION && data != null) {
            String technology = (String) data.get("technology");
            if (technology != null) {
                addTechnologySignal(technology, description);
            }
        }
        touch();
        registerEvent(new SignalDetected(id, name, signalType, description, data));
    }

    public boolean isQualifiedProspect() {
        return prospectingScore.isHighPriority() && status == ProspectingStatus.QUALIFIED && hasValidContactInfo();
    }

    public void qualify() {
        if (status != ProspectingStatus.NEW && status != ProspectingStatus.REVIEWING) {
            throw new IllegalStateException("Company cannot be qualified in current state");
        }
        status = ProspectingStatus.QUALIFIED;
        touch();
    }

    public void disqualify(String reason) {
        status = ProspectingStatus.DISQUALIFIED;
        touch();
    }
}

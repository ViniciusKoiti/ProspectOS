package dev.prospectos.infrastructure.service.discovery;

public final class CnpjValidationResult {

    private final boolean valid;
    private final String companyName;
    private final String fantasyName;
    private final String email;
    private final String address;
    private final String errorMessage;

    private CnpjValidationResult(
        boolean valid,
        String companyName,
        String fantasyName,
        String email,
        String address,
        String errorMessage
    ) {
        this.valid = valid;
        this.companyName = companyName;
        this.fantasyName = fantasyName;
        this.email = email;
        this.address = address;
        this.errorMessage = errorMessage;
    }

    static CnpjValidationResult valid(String companyName, String fantasyName, String email, String address) {
        return new CnpjValidationResult(true, companyName, fantasyName, email, address, null);
    }

    static CnpjValidationResult invalid(String errorMessage) {
        return new CnpjValidationResult(false, null, null, null, null, errorMessage);
    }

    public boolean isValid() {
        return valid;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getFantasyName() {
        return fantasyName;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

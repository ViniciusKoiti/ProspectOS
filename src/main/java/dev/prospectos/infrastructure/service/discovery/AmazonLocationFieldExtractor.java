package dev.prospectos.infrastructure.service.discovery;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class AmazonLocationFieldExtractor {
    String resolveName(AmazonLocationResultItem item) {
        return item == null ? null : firstNonBlank(item.title(), addressLabel(item.address()));
    }

    String resolveIndustry(List<AmazonLocationCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return "Other";
        }
        return categories.stream()
            .filter(Objects::nonNull)
            .map(AmazonLocationCategory::name)
            .map(this::firstNonBlank)
            .filter(Objects::nonNull)
            .map(value -> value.toLowerCase(Locale.ROOT))
            .findFirst()
            .orElse("Other");
    }

    String resolveDescription(String placeType, AmazonLocationAddress address) {
        String type = firstNonBlank(placeType);
        String label = addressLabel(address);
        if (type != null && label != null) {
            return type + " - " + label;
        }
        return firstNonBlank(label, type, "Discovered via Amazon Location");
    }

    String resolveLocation(AmazonLocationAddress address) {
        if (address == null) {
            return null;
        }
        String locality = firstNonBlank(address.locality(), address.district(), subRegionName(address));
        String country = countryCode(address.country());
        if (locality != null && country != null) {
            return locality + ", " + country;
        }
        return firstNonBlank(locality, country, address.label());
    }

    String resolveWebsite(AmazonLocationContacts contacts) {
        return firstContactValue(contacts == null ? null : contacts.websites());
    }

    List<String> resolveEmails(AmazonLocationContacts contacts) {
        if (contacts == null || contacts.emails() == null || contacts.emails().isEmpty()) {
            return List.of();
        }
        return contacts.emails().stream()
            .filter(Objects::nonNull)
            .map(AmazonLocationContactValue::value)
            .map(this::firstNonBlank)
            .filter(email -> email != null && email.contains("@"))
            .distinct()
            .toList();
    }

    private String firstContactValue(List<AmazonLocationContactValue> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
            .filter(Objects::nonNull)
            .map(AmazonLocationContactValue::value)
            .map(this::firstNonBlank)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private String addressLabel(AmazonLocationAddress address) { return address == null ? null : firstNonBlank(address.label()); }

    private String subRegionName(AmazonLocationAddress address) {
        return address.subRegion() == null ? null : firstNonBlank(address.subRegion().name());
    }

    private String countryCode(AmazonLocationCountry country) {
        return country == null ? null : firstNonBlank(country.code2(), country.code3(), country.name());
    }

    private String firstNonBlank(String... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}

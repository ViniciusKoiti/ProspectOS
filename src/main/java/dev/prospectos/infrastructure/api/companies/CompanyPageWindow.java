package dev.prospectos.infrastructure.api.companies;

import java.util.List;

import dev.prospectos.api.dto.CompanyDTO;

final class CompanyPageWindow {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;

    private final Integer requestedPage;
    private final Integer requestedSize;
    private final int page;
    private final int size;

    private CompanyPageWindow(Integer requestedPage, Integer requestedSize, int page, int size) {
        this.requestedPage = requestedPage;
        this.requestedSize = requestedSize;
        this.page = page;
        this.size = size;
    }

    static CompanyPageWindow resolve(Integer requestedPage, Integer requestedSize) {
        int resolvedPage = requestedPage == null ? DEFAULT_PAGE : requestedPage;
        if (resolvedPage < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        int resolvedSize = requestedSize == null ? DEFAULT_SIZE : requestedSize;
        if ((requestedPage != null || requestedSize != null) && resolvedSize <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        return new CompanyPageWindow(requestedPage, requestedSize, resolvedPage, resolvedSize);
    }

    List<CompanyDTO> apply(List<CompanyDTO> filtered) {
        if (requestedPage == null && requestedSize == null) {
            return filtered;
        }
        int startIndex = page * size;
        if (startIndex >= filtered.size()) {
            return List.of();
        }
        int endIndex = Math.min(startIndex + size, filtered.size());
        return filtered.subList(startIndex, endIndex);
    }
}

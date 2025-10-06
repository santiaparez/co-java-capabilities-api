package com.example.capabilities.domain.model;

import java.util.List;

public record PaginatedCapabilities(
        List<CapabilitySummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public PaginatedCapabilities {
        content = List.copyOf(content);
    }
}

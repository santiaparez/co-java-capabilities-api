package com.example.capabilities.domain.model;

public record CapabilitiesPageRequest(
        int page,
        int size,
        CapabilitiesSortField sortBy,
        SortDirection direction
) {}

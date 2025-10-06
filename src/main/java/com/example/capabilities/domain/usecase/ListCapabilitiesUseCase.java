package com.example.capabilities.domain.usecase;

import com.example.capabilities.domain.error.DomainException;
import com.example.capabilities.domain.error.ErrorCodes;
import com.example.capabilities.domain.model.CapabilitiesPageRequest;
import com.example.capabilities.domain.model.PaginatedCapabilities;
import com.example.capabilities.infrastructure.repository.SpringDataCapabilitiesRepository;
import reactor.core.publisher.Mono;

public class ListCapabilitiesUseCase {

    private final SpringDataCapabilitiesRepository repository;

    public ListCapabilitiesUseCase(SpringDataCapabilitiesRepository repository) {
        this.repository = repository;
    }

    public Mono<PaginatedCapabilities> execute(CapabilitiesPageRequest request) {
        if (request.size() <= 0) {
            return Mono.error(new DomainException(ErrorCodes.VALIDATION_ERROR, "invalid.pagination.size"));
        }
        if (request.page() < 0) {
            return Mono.error(new DomainException(ErrorCodes.VALIDATION_ERROR, "invalid.pagination.page"));
        }
        return repository.findAll(request);
    }
}

package com.example.capabilities.domain.usecase;

import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.infrastructure.repository.SpringDataCapabilitiesRepository;
import reactor.core.publisher.Flux;

public class GetAllTechnologiesUseCase {
    private final SpringDataCapabilitiesRepository repo;

    public GetAllTechnologiesUseCase(SpringDataCapabilitiesRepository repo) {
        this.repo = repo;
    }

    public Flux<Capabilities> execute() {
        return repo.findAll();
    }
}


package com.example.capabilities.domain.usecase;

import com.example.capabilities.domain.error.DomainException;
import com.example.capabilities.domain.error.ErrorCodes;
import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.infrastructure.repository.SpringDataCapabilitiesRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public class CreateCapabilitiesUseCase {
  private final SpringDataCapabilitiesRepository repo;
  public CreateCapabilitiesUseCase(SpringDataCapabilitiesRepository repo) { this.repo = repo; }
  public Mono<Capabilities> execute(String name, String description, List<String> technologies) {
    return repo.findByName(name)
        .flatMap(existing -> Mono.<Capabilities>error(
            new DomainException(ErrorCodes.CONFLICT, "capabilities.name.already.exists")
        ))
        .switchIfEmpty(Mono.defer(() -> {
          try {
            Capabilities capabilities = new Capabilities(UUID.randomUUID().toString(), name, description, technologies);
            return repo.save(capabilities);
          } catch (IllegalArgumentException ex) {
            return Mono.error(new DomainException(ErrorCodes.VALIDATION_ERROR, ex.getMessage()));
          }
        }));
  }
}

package com.example.capabilities.domain.usecase;

import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.infrastructure.repository.SpringDataCapabilitiesRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class GetAllCapabilitiesUseCaseTest {

  private final SpringDataCapabilitiesRepository repo = Mockito.mock(SpringDataCapabilitiesRepository.class);
  private final GetAllTechnologiesUseCase useCase = new GetAllTechnologiesUseCase(repo);

  @Test
  void delegatesToRepository() {
    Capabilities capabilities = new Capabilities("f-1", "Acme", "des", java.util.List.of("t1","t2","t3"));
    Mockito.when(repo.findAll()).thenReturn(Flux.just(capabilities));

    StepVerifier.create(useCase.execute())
        .expectNext(capabilities)
        .verifyComplete();

    Mockito.verify(repo).findAll();
  }
}


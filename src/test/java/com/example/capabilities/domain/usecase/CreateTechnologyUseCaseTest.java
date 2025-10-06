package com.example.capabilities.domain.usecase;

import com.example.capabilities.domain.error.DomainException;
import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.domain.usecase.CreateCapabilitiesUseCase;
import com.example.capabilities.infrastructure.repository.SpringDataCapabilitiesRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

class CreateCapabilitiesUseCaseTest {
  @Test
  void create_ok(){
    SpringDataCapabilitiesRepository repo = Mockito.mock(SpringDataCapabilitiesRepository.class);
    Mockito.when(repo.findByName("My Capabilities")).thenReturn(Mono.empty());
    Mockito.when(repo.save(Mockito.any(Capabilities.class))).thenAnswer(i -> Mono.just((Capabilities) i.getArguments()[0]));
    var uc = new CreateCapabilitiesUseCase(repo);
    StepVerifier.create(uc.execute("My Capabilities", "des"))
        .assertNext(capabilities -> {
          assertNotNull(capabilities.id());
          assertEquals("My Capabilities", capabilities.name());
        })
        .verifyComplete();
  }

  @Test
  void create_conflict_when_name_exists(){
    SpringDataCapabilitiesRepository repo = Mockito.mock(SpringDataCapabilitiesRepository.class);
    Mockito.when(repo.findByName("My Capabilities")).thenReturn(Mono.just(new Capabilities("id", "My Capabilities", "des")));
    var uc = new CreateCapabilitiesUseCase(repo);

    StepVerifier.create(uc.execute("My Capabilities", "des"))
        .expectErrorSatisfies(error -> {
          assertInstanceOf(DomainException.class, error);
          assertEquals("capabilities.name.already.exists", error.getMessage());
        })
        .verify();
  }
}

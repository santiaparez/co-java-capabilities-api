package com.example.capabilities.web;

import com.example.capabilities.application.config.RouterConfig;

import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.domain.usecase.*;
import com.example.capabilities.web.dto.Requests.*;
import com.example.capabilities.web.handler.CapabilitiesHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import reactor.core.publisher.Mono;

class CapabilitiesHandlerTest {

  private final CreateCapabilitiesUseCase create = Mockito.mock(CreateCapabilitiesUseCase.class);
  private final GetAllTechnologiesUseCase getAll = Mockito.mock(GetAllTechnologiesUseCase.class);

  private WebTestClient client;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    CapabilitiesHandler handler = new CapabilitiesHandler(
        validator,
        create,
        getAll
    );
    client = WebTestClient.bindToRouterFunction(new RouterConfig().routes(handler))
        .handlerStrategies(HandlerStrategies.withDefaults())
        .configureClient()
        .baseUrl("/api/v1")
        .build();
  }

  @Test
  void createTech_success() {
    Mockito.when(create.execute("Acme","des", java.util.List.of("t1","t2","t3")))
        .thenReturn(Mono.just(new Capabilities("id-1", "Acme", "des", java.util.List.of("t1","t2","t3"))));

    client.post().uri("/capabilities")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new CreateCapabilitiesRequest("Acme","des", java.util.List.of("t1","t2","t3")))
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.id").isEqualTo("id-1");
  }

  @Test
  void createTech_validationError() {
    client.post().uri("/capabilities")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(new CreateCapabilitiesRequest("","", java.util.List.of("t1","t2","t3")))
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.message").isEqualTo("no debe estar vacío");
  }

}


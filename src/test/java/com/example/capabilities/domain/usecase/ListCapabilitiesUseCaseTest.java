package com.example.capabilities.domain.usecase;

import com.example.capabilities.domain.error.DomainException;
import com.example.capabilities.domain.error.ErrorCodes;
import com.example.capabilities.domain.model.CapabilitiesPageRequest;
import com.example.capabilities.domain.model.CapabilitiesSortField;
import com.example.capabilities.domain.model.PaginatedCapabilities;
import com.example.capabilities.domain.model.SortDirection;
import com.example.capabilities.infrastructure.repository.SpringDataCapabilitiesRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

class ListCapabilitiesUseCaseTest {

    private final SpringDataCapabilitiesRepository repository = Mockito.mock(SpringDataCapabilitiesRepository.class);
    private final ListCapabilitiesUseCase useCase = new ListCapabilitiesUseCase(repository);

    @Test
    void delegatesToRepository() {
        CapabilitiesPageRequest request = new CapabilitiesPageRequest(0, 5, CapabilitiesSortField.NAME, SortDirection.ASC);
        PaginatedCapabilities page = new PaginatedCapabilities(Collections.emptyList(), 0, 5, 0, 0);
        Mockito.when(repository.findAll(request)).thenReturn(Mono.just(page));

        StepVerifier.create(useCase.execute(request))
                .expectNext(page)
                .verifyComplete();

        Mockito.verify(repository).findAll(request);
    }

    @Test
    void failsForInvalidSize() {
        CapabilitiesPageRequest request = new CapabilitiesPageRequest(0, 0, CapabilitiesSortField.NAME, SortDirection.ASC);

        StepVerifier.create(useCase.execute(request))
                .expectErrorSatisfies(error -> {
                    org.junit.jupiter.api.Assertions.assertInstanceOf(DomainException.class, error);
                    DomainException ex = (DomainException) error;
                    org.junit.jupiter.api.Assertions.assertEquals(ErrorCodes.VALIDATION_ERROR, ex.getCode());
                })
                .verify();

        Mockito.verifyNoInteractions(repository);
    }

    @Test
    void failsForNegativePage() {
        CapabilitiesPageRequest request = new CapabilitiesPageRequest(-1, 5, CapabilitiesSortField.NAME, SortDirection.ASC);

        StepVerifier.create(useCase.execute(request))
                .expectError(DomainException.class)
                .verify();

        Mockito.verifyNoInteractions(repository);
    }
}

package com.example.capabilities.infrastructure.repository;

import com.example.capabilities.infrastructure.repository.documents.CapabilitiesEntity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class SpringDataCapabilitiesRepositoryTest {
  private static CapabilitiesEntity sampleEntity() {
    CapabilitiesEntity entity = new CapabilitiesEntity();
    entity.setId("f-1");
    entity.setName("Acme");
    entity.setDescription("des");
    entity.setTechnologies("t1,t2,t3");
    return entity;
  }

  @Test
  void findAllStreamsFromTemplate() {
    R2dbcEntityTemplate template = Mockito.mock(R2dbcEntityTemplate.class);
    SpringDataCapabilitiesRepository repository = new SpringDataCapabilitiesRepository(template);
    CapabilitiesEntity entity = sampleEntity();
    Mockito.when(template.select(Mockito.any(Query.class), Mockito.eq(CapabilitiesEntity.class)))
        .thenReturn(Flux.just(entity));

    StepVerifier.create(repository.findAll())
        .assertNext(capabilities -> {
          assertEquals("Acme", capabilities.name());
          assertEquals(java.util.List.of("t1", "t2", "t3"), capabilities.technologies());
        })
        .verifyComplete();

    Mockito.verify(template).select(Mockito.any(Query.class), Mockito.eq(CapabilitiesEntity.class));
  }

  @Test
  void findByIdUsesTemplate() {
    R2dbcEntityTemplate template = Mockito.mock(R2dbcEntityTemplate.class);
    SpringDataCapabilitiesRepository repository = new SpringDataCapabilitiesRepository(template);
    CapabilitiesEntity entity = sampleEntity();
    Mockito.when(template.selectOne(Mockito.any(Query.class), Mockito.eq(CapabilitiesEntity.class)))
        .thenReturn(Mono.just(entity));

    StepVerifier.create(repository.findById("f-1"))
        .assertNext(capabilities -> {
          assertEquals("Acme", capabilities.name());
          assertEquals(java.util.List.of("t1", "t2", "t3"), capabilities.technologies());
        })
        .verifyComplete();

    Mockito.verify(template).selectOne(Mockito.any(Query.class), Mockito.eq(CapabilitiesEntity.class));
  }

  @Test
  void findByNameUsesTemplate() {
    R2dbcEntityTemplate template = Mockito.mock(R2dbcEntityTemplate.class);
    SpringDataCapabilitiesRepository repository = new SpringDataCapabilitiesRepository(template);
    CapabilitiesEntity entity = sampleEntity();
    Mockito.when(template.selectOne(Mockito.any(Query.class), Mockito.eq(CapabilitiesEntity.class)))
        .thenReturn(Mono.just(entity));

    StepVerifier.create(repository.findByName("Acme"))
        .assertNext(capabilities -> {
          assertEquals("Acme", capabilities.name());
          assertEquals(java.util.List.of("t1", "t2", "t3"), capabilities.technologies());
        })
        .verifyComplete();

    Mockito.verify(template).selectOne(Mockito.any(Query.class), Mockito.eq(CapabilitiesEntity.class));
  }
}


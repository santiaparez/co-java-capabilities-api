package com.example.capabilities.infrastructure.repository;

import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.infrastructure.mapper.TechnologiesMapper;
import com.example.capabilities.infrastructure.repository.documents.CapabilitiesEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class SpringDataCapabilitiesRepository {

  private final R2dbcEntityTemplate template;

  public SpringDataCapabilitiesRepository(R2dbcEntityTemplate template) {
    this.template = template;
  }

  public Mono<Capabilities> findById(String id){
    return template.getDatabaseClient()
            .sql(BASE_SELECT + " WHERE c.id = :id")
            .bind("id", id)
            .map(this::mapRow)
            .all()
            .collectList()
            .flatMap(this::mapSingleResult);
  }

  public Mono<Capabilities> findByName(String name){
    return template.getDatabaseClient()
            .sql(BASE_SELECT + " WHERE c.name = :name")
            .bind("name", name)
            .map(this::mapRow)
            .all()
            .collectList()
            .flatMap(this::mapSingleResult);
  }

  public Mono<Capabilities> save(Capabilities capabilities){
    var entity = TechnologiesMapper.toEntity(capabilities);
    return template.insert(CapabilitiesEntity.class)
            .using(entity)
            .then(insertCapabilityTechnologies(capabilities.id(), capabilities.technologies()))
            .thenReturn(capabilities);
  }

  public Flux<Capabilities> findAll(){
    return template.getDatabaseClient()
            .sql(BASE_SELECT + " ORDER BY c.name, ct.technology_id")
            .map(this::mapRow)
            .all()
            .transform(this::groupRowsByCapability);
  }

  private Mono<Void> insertCapabilityTechnologies(String capabilityId, List<String> technologies) {
    return Flux.fromIterable(technologies)
            .concatMap(technologyId -> template.getDatabaseClient()
                    .sql("INSERT INTO capability_technology (capability_id, technology_id) VALUES (:capabilityId, :technologyId)")
                    .bind("capabilityId", capabilityId)
                    .bind("technologyId", technologyId)
                    .fetch()
                    .rowsUpdated())
            .then();
  }

  private Flux<Capabilities> groupRowsByCapability(Flux<CapabilityTechnologyRow> rows) {
    return rows.groupBy(CapabilityTechnologyRow::capabilityId)
            .concatMap(group -> group.collectList().map(this::mapToDomain));
  }

  private Mono<Capabilities> mapSingleResult(List<CapabilityTechnologyRow> rows) {
    if (rows.isEmpty()) {
      return Mono.empty();
    }
    return Mono.fromCallable(() -> mapToDomain(rows));
  }

  private Capabilities mapToDomain(List<CapabilityTechnologyRow> rows) {
    var first = rows.get(0);
    var technologies = rows.stream()
            .map(CapabilityTechnologyRow::technologyId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    return TechnologiesMapper.toDomain(
            first.capabilityId(),
            first.capabilityName(),
            first.capabilityDescription(),
            List.copyOf(technologies)
    );
  }

  private CapabilityTechnologyRow mapRow(Row row, RowMetadata metadata) {
    return new CapabilityTechnologyRow(
            row.get("capability_id", String.class),
            row.get("capability_name", String.class),
            row.get("capability_description", String.class),
            row.get("technology_id", String.class)
    );
  }

  private record CapabilityTechnologyRow(
          String capabilityId,
          String capabilityName,
          String capabilityDescription,
          String technologyId
  ) {}

  private static final String BASE_SELECT = """
      SELECT c.id AS capability_id,
             c.name AS capability_name,
             c.description AS capability_description,
             ct.technology_id AS technology_id
      FROM capabilities c
      INNER JOIN capability_technology ct ON c.id = ct.capability_id
      """;
}


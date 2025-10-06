package com.example.capabilities.infrastructure.repository;

import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.domain.model.CapabilitiesPageRequest;
import com.example.capabilities.domain.model.CapabilitySummary;
import com.example.capabilities.domain.model.PaginatedCapabilities;
import com.example.capabilities.domain.model.SortDirection;
import com.example.capabilities.domain.model.TechnologySummary;
import com.example.capabilities.infrastructure.mapper.TechnologiesMapper;
import com.example.capabilities.infrastructure.repository.documents.CapabilitiesEntity;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
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

  public Mono<PaginatedCapabilities> findAll(CapabilitiesPageRequest request){
    int offset = request.page() * request.size();
    String orderColumn = switch (request.sortBy()) {
      case NAME -> "name";
      case TECHNOLOGY_COUNT -> "technology_count";
    };
    String orderDirection = request.direction() == SortDirection.DESC ? "DESC" : "ASC";
    String qualifiedOrderColumn = "pc." + orderColumn;

    String sql = String.format(PAGINATED_SELECT_TEMPLATE, orderColumn, orderDirection, qualifiedOrderColumn, orderDirection);

    Flux<CapabilityTechnologyDetailRow> rows = template.getDatabaseClient()
            .sql(sql)
            .bind("limit", request.size())
            .bind("offset", offset)
            .map(this::mapPagedRow)
            .all();

    Mono<List<CapabilitySummary>> capabilities = rows
            .transform(this::groupRowsByCapabilitySummary)
            .collectList();

    Mono<Long> total = template.getDatabaseClient()
            .sql("SELECT COUNT(*) AS total FROM capabilities")
            .map((row, metadata) -> {
              Number count = row.get("total", Number.class);
              return count == null ? 0L : count.longValue();
            })
            .one()
            .defaultIfEmpty(0L);

    return Mono.zip(capabilities, total)
            .map(tuple -> {
              List<CapabilitySummary> content = tuple.getT1();
              long totalElements = tuple.getT2();
              int totalPages = request.size() == 0 ? 0 : (int) Math.ceil(totalElements / (double) request.size());
              return new PaginatedCapabilities(content, request.page(), request.size(), totalElements, totalPages);
            });
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

  private CapabilityTechnologyDetailRow mapPagedRow(Row row, RowMetadata metadata) {
    Number technologyCount = row.get("technology_count", Number.class);
    return new CapabilityTechnologyDetailRow(
            row.get("capability_id", String.class),
            row.get("capability_name", String.class),
            row.get("capability_description", String.class),
            technologyCount == null ? 0 : technologyCount.intValue(),
            row.get("technology_id", String.class),
            row.get("technology_name", String.class)
    );
  }

  private record CapabilityTechnologyRow(
          String capabilityId,
          String capabilityName,
          String capabilityDescription,
          String technologyId
  ) {}

  private record CapabilityTechnologyDetailRow(
          String capabilityId,
          String capabilityName,
          String capabilityDescription,
          int technologyCount,
          String technologyId,
          String technologyName
  ) {}

  private Flux<CapabilitySummary> groupRowsByCapabilitySummary(Flux<CapabilityTechnologyDetailRow> rows) {
    return rows.groupBy(CapabilityTechnologyDetailRow::capabilityId)
            .concatMap(group -> group.collectList().map(this::mapToSummary));
  }

  private CapabilitySummary mapToSummary(List<CapabilityTechnologyDetailRow> rows) {
    if (rows.isEmpty()) {
      throw new IllegalStateException("capability.rows.empty");
    }
    var first = rows.get(0);
    var technologies = rows.stream()
            .filter(row -> row.technologyId() != null && row.technologyName() != null)
            .collect(Collectors.toMap(
                    CapabilityTechnologyDetailRow::technologyId,
                    row -> new TechnologySummary(row.technologyId(), row.technologyName()),
                    (existing, replacement) -> existing,
                    LinkedHashMap::new
            ));
    return new CapabilitySummary(
            first.capabilityId(),
            first.capabilityName(),
            first.capabilityDescription(),
            List.copyOf(technologies.values()),
            first.technologyCount()
    );
  }

  private static final String BASE_SELECT = """
      SELECT c.id AS capability_id,
             c.name AS capability_name,
             c.description AS capability_description,
             ct.technology_id AS technology_id
      FROM capabilities c
      INNER JOIN capability_technology ct ON c.id = ct.capability_id
      """;

  private static final String PAGINATED_SELECT_TEMPLATE = """
      WITH capability_counts AS (
          SELECT c.id,
                 c.name,
                 c.description,
                 COUNT(ct.technology_id) AS technology_count
          FROM capabilities c
          LEFT JOIN capability_technology ct ON ct.capability_id = c.id
          GROUP BY c.id, c.name, c.description
      ),
      paged_capabilities AS (
          SELECT id,
                 name,
                 description,
                 technology_count
          FROM capability_counts
          ORDER BY %s %s, id ASC
          LIMIT :limit OFFSET :offset
      )
      SELECT pc.id AS capability_id,
             pc.name AS capability_name,
             pc.description AS capability_description,
             pc.technology_count AS technology_count,
             t.id AS technology_id,
             t.name AS technology_name
      FROM paged_capabilities pc
      LEFT JOIN capability_technology ct ON ct.capability_id = pc.id
      LEFT JOIN technologies t ON t.id = ct.technology_id
      ORDER BY %s %s, pc.id ASC, t.name ASC, t.id ASC
      """;
}


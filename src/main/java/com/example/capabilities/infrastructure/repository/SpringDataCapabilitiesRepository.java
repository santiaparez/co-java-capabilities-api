package com.example.capabilities.infrastructure.repository;

import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.infrastructure.mapper.TechnologiesMapper;
import com.example.capabilities.infrastructure.repository.documents.CapabilitiesEntity;
import org.springframework.stereotype.Repository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SpringDataCapabilitiesRepository {

  private final R2dbcEntityTemplate template;

  public SpringDataCapabilitiesRepository(R2dbcEntityTemplate template) {
    this.template = template;
  }

  public Mono<Capabilities> findById(String id){
    return template.selectOne(Query.query(Criteria.where("id").is(id)), CapabilitiesEntity.class)
            .map(TechnologiesMapper::toDomain);
  }

  public Mono<Capabilities> findByName(String name){
    return template.selectOne(Query.query(Criteria.where("name").is(name)), CapabilitiesEntity.class)
            .map(TechnologiesMapper::toDomain);
  }

  public Mono<Capabilities> save(Capabilities capabilities){
    var entity = TechnologiesMapper.toEntity(capabilities);
    return template.insert(CapabilitiesEntity.class)
            .using(entity)
            .map(saved -> capabilities);
  }

  public Flux<Capabilities> findAll(){
    return template.select(Query.empty(), CapabilitiesEntity.class)
            .map(TechnologiesMapper::toDomain);
  }
}


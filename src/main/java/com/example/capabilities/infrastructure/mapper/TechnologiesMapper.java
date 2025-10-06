package com.example.capabilities.infrastructure.mapper;

import com.example.capabilities.domain.model.Capabilities;
import com.example.capabilities.infrastructure.repository.documents.CapabilitiesEntity;

import java.util.List;

public class TechnologiesMapper {
  private TechnologiesMapper() {}

  public static Capabilities toDomain(String id, String name, String description, List<String> technologies){
    return new Capabilities(
            id,
            name,
            description,
            technologies
    );
  }

  public static CapabilitiesEntity toEntity(Capabilities domain){
    var entity = new CapabilitiesEntity();
    entity.setId(domain.id());
    entity.setName(domain.name());
    entity.setDescription(domain.description());
    return entity;
  }
}


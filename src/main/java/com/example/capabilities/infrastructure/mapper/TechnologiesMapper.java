package com.example.capabilities.infrastructure.mapper;

import com.example.capabilities.domain.model.*;
import com.example.capabilities.infrastructure.repository.documents.CapabilitiesEntity;

public class TechnologiesMapper {
  public static Capabilities toDomain(CapabilitiesEntity item){
    return new Capabilities(
            item.getId(), item.getName(),
            item.getDescription()
    );
  }
  public static CapabilitiesEntity toEntity(Capabilities domain){
    var entity = new CapabilitiesEntity();
    entity.setId(domain.id()); entity.setName(domain.name());
    entity.setDescription(domain.description());
    return entity;
  }
}


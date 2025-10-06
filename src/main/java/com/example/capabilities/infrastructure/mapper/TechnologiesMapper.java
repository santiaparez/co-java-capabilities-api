package com.example.capabilities.infrastructure.mapper;

import com.example.capabilities.domain.model.*;
import com.example.capabilities.infrastructure.repository.documents.CapabilitiesEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TechnologiesMapper {
  private TechnologiesMapper() {}

  public static Capabilities toDomain(CapabilitiesEntity item){
    return new Capabilities(
            item.getId(),
            item.getName(),
            item.getDescription(),
            parseTechnologies(item.getTechnologies())
    );
  }
  public static CapabilitiesEntity toEntity(Capabilities domain){
    var entity = new CapabilitiesEntity();
    entity.setId(domain.id());
    entity.setName(domain.name());
    entity.setDescription(domain.description());
    entity.setTechnologies(String.join(",", domain.technologies()));
    return entity;
  }

  private static List<String> parseTechnologies(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    return Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(s -> !s.isBlank())
        .collect(Collectors.toList());
  }
}


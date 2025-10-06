package com.example.capabilities.domain.model;

import java.util.HashSet;
import java.util.List;

public record Capabilities(String id, String name, String description, List<String> technologies) {
  public Capabilities {
    if (name == null || name.isBlank() || name.length() > 50) {
      throw new IllegalArgumentException("invalid.tech.name");
    }
    if (description == null || description.isBlank() || description.length() > 90) {
      throw new IllegalArgumentException("invalid.tech.description");
    }
    if (technologies == null) {
      throw new IllegalArgumentException("invalid.capabilities.technologies.required");
    }

    technologies = List.copyOf(technologies);

    if (technologies.size() < 3) {
      throw new IllegalArgumentException("invalid.capabilities.technologies.min");
    }
    if (technologies.size() > 20) {
      throw new IllegalArgumentException("invalid.capabilities.technologies.max");
    }
    if (technologies.stream().anyMatch(t -> t == null || t.isBlank())) {
      throw new IllegalArgumentException("invalid.capabilities.technologies.blank");
    }
    if (new HashSet<>(technologies).size() != technologies.size()) {
      throw new IllegalArgumentException("invalid.capabilities.technologies.duplicated");
    }
  }
}

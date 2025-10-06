package com.example.capabilities.domain.model;


public record Capabilities(String id, String name, String description) {
  public Capabilities {
    if (name == null || name.isBlank() && name.length() > 50) throw new IllegalArgumentException("invalid.tech.name");
    if (description == null || description.isBlank() && description.length() > 90) throw new IllegalArgumentException("invalid.tech.description");
  }
}

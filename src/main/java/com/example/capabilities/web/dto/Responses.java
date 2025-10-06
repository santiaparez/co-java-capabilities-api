package com.example.capabilities.web.dto;

public class Responses {
  public record IdResponse(String id) {}
  public record TechnologyResponse(String id, String name) {}
  public record CapabilitiesResponse(String id, String name, String description, java.util.List<TechnologyResponse> technologies) {}
  public record CapabilitiesPageResponse(java.util.List<CapabilitiesResponse> content, int page, int size, long totalElements, int totalPages) {}
}

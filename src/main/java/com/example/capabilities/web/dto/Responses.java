package com.example.capabilities.web.dto;

import java.util.List;

public class Responses {
  public record IdResponse(String id) {}
  public record CapabilitiesResponse(String id, String name, String description, List<String> technologies) {}
}

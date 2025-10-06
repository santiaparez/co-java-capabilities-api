package com.example.capabilities.web.dto;


public class Responses {
  public record IdResponse(String id) {}
  public record CapabilitiesResponse(String id, String name, String description) {}
}

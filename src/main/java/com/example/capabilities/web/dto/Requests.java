package com.example.capabilities.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class Requests {
  public record CreateCapabilitiesRequest(@NotBlank String name, String description) {}
}

package com.example.capabilities.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CapabilitiesTest {

  @Test
  void createCapabilities_ok() {
    Capabilities capabilities = new Capabilities("f-1", "Acme", "des");

    assertEquals("f-1", capabilities.id());
    assertEquals("Acme", capabilities.name());
    assertEquals("des", capabilities.description());
  }


}


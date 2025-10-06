package com.example.capabilities.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.capabilities")
public class CapabilitiesApplication {
  public static void main(String[] args) { SpringApplication.run(CapabilitiesApplication.class, args); }
}

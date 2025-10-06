package com.example.capabilities.application.config;

import com.example.capabilities.web.handler.CapabilitiesHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {
  @Bean
  public RouterFunction<ServerResponse> routes(CapabilitiesHandler h) {
    return RouterFunctions.nest(RequestPredicates.path("/api/v1"),
      RouterFunctions.route()
        .POST("/capabilities", h::createCapabilities)
        .GET("/capabilities", h::getAllCapabilities)
        .build()
    );
  }
}

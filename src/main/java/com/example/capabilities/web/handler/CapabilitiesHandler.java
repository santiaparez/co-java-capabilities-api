package com.example.capabilities.web.handler;

import com.example.capabilities.domain.error.DomainException;
import com.example.capabilities.domain.model.CapabilitiesPageRequest;
import com.example.capabilities.domain.model.CapabilitiesSortField;
import com.example.capabilities.domain.model.PaginatedCapabilities;
import com.example.capabilities.domain.model.CapabilitySummary;
import com.example.capabilities.domain.model.SortDirection;
import com.example.capabilities.domain.model.TechnologySummary;
import com.example.capabilities.domain.usecase.CreateCapabilitiesUseCase;
import com.example.capabilities.domain.usecase.ListCapabilitiesUseCase;
import com.example.capabilities.web.dto.Requests.*;
import com.example.capabilities.web.dto.Responses.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class CapabilitiesHandler {
  private final Validator validator;
  private final CreateCapabilitiesUseCase createCapabilities;
  private final ListCapabilitiesUseCase listCapabilities;

  public CapabilitiesHandler(Validator validator, CreateCapabilitiesUseCase createCapabilities, ListCapabilitiesUseCase listCapabilities) {
    this.validator = validator; this.createCapabilities = createCapabilities; this.listCapabilities = listCapabilities;
  }

  public Mono<ServerResponse> createCapabilities(ServerRequest req){
    return validatedBody(req, CreateCapabilitiesRequest.class, body ->
      createCapabilities.execute(body.name(), body.description(), body.technologies())
          .flatMap(f -> okJson(new IdResponse(f.id())))
    );
  }

  public Mono<ServerResponse> getAllCapabilities(ServerRequest req) {
    return Mono.justOrEmpty(parseRequest(req))
            .switchIfEmpty(Mono.error(new DomainException(com.example.capabilities.domain.error.ErrorCodes.VALIDATION_ERROR, "invalid.pagination.parameters")))
            .flatMap(listCapabilities::execute)
            .flatMap(page -> okJson(Mapper.page(page)))
            .onErrorResume(DomainException.class, ex -> problem(mapHttp(ex.getCode()), ex.getMessage()));
  }


  // helpers
  private static class Mapper {
    static CapabilitiesPageResponse page(PaginatedCapabilities page) {
      return new CapabilitiesPageResponse(
              page.content().stream().map(Mapper::capabilities).toList(),
              page.page(),
              page.size(),
              page.totalElements(),
              page.totalPages()
      );
    }

    static CapabilitiesResponse capabilities(CapabilitySummary summary){
      return new CapabilitiesResponse(
        summary.id(),
        summary.name(),
        summary.description(),
        summary.technologies().stream().map(Mapper::technology).toList()
      );
    }

    static TechnologyResponse technology(TechnologySummary technology) {
      return new TechnologyResponse(technology.id(), technology.name());
    }
  }

  private java.util.Optional<CapabilitiesPageRequest> parseRequest(ServerRequest req) {
    try {
      int page = req.queryParam("page").map(Integer::parseInt).orElse(0);
      int size = req.queryParam("size").map(Integer::parseInt).orElse(10);
      CapabilitiesSortField sortField = req.queryParam("sortBy")
              .map(String::toUpperCase)
              .map(value -> switch (value) {
                case "NAME" -> CapabilitiesSortField.NAME;
                case "TECHNOLOGY_COUNT", "TECHNOLOGIES" -> CapabilitiesSortField.TECHNOLOGY_COUNT;
                default -> throw new IllegalArgumentException("invalid.sort.by");
              })
              .orElse(CapabilitiesSortField.NAME);
      SortDirection direction = req.queryParam("order")
              .map(String::toUpperCase)
              .map(SortDirection::valueOf)
              .orElse(SortDirection.ASC);
      CapabilitiesPageRequest request = new CapabilitiesPageRequest(page, size, sortField, direction);
      return java.util.Optional.of(request);
    } catch (IllegalArgumentException ex) {
      return java.util.Optional.empty();
    }
  }

  private <T> Mono<ServerResponse> validatedBody(ServerRequest req, Class<T> clazz, java.util.function.Function<T, Mono<ServerResponse>> fn){
    return req.bodyToMono(clazz).flatMap(body -> {
      var errors = new BeanPropertyBindingResult(body, clazz.getSimpleName());
      validator.validate(body, errors);
      if (errors.hasErrors()) {
        String msg = errors.getAllErrors().stream().map(e -> e.getDefaultMessage()==null?"validation.error":e.getDefaultMessage()).reduce((a,b) -> a+","+b).orElse("validation.error");
        return problem(400, msg);
      }
      return fn.apply(body);
    }).onErrorResume(DomainException.class, ex -> problem(mapHttp(ex.getCode()), ex.getMessage()))
        .onErrorResume(IllegalArgumentException.class, ex -> problem(400, ex.getMessage()));
  }

  private Mono<ServerResponse> okJson(Object any){
    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromValue(any));
  }
  private Mono<ServerResponse> problem(int status, String message){
    return ServerResponse.status(status).contentType(MediaType.APPLICATION_JSON).body(fromValue(java.util.Map.of("message", message)));
  }
  private int mapHttp(com.example.capabilities.domain.error.ErrorCodes code){
    return switch (code){
      case TECHNOLOGY_NOT_FOUND, BRANCH_NOT_FOUND, PRODUCT_NOT_FOUND -> 404;
      case VALIDATION_ERROR -> 400;
      case CONFLICT -> 409;
      default -> 500;
    };
  }
}

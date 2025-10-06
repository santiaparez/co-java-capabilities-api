package com.example.capabilities.application.config;

import com.example.capabilities.domain.usecase.*;
import com.example.capabilities.infrastructure.repository.SpringDataCapabilitiesRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCasesConfig {

    @Bean
    public CreateCapabilitiesUseCase createCapabilitiesUseCase(SpringDataCapabilitiesRepository repo) {
        return new CreateCapabilitiesUseCase(repo);
    }

    @Bean
    public GetAllTechnologiesUseCase getAllCapabilitiesUseCase(SpringDataCapabilitiesRepository repo) {
        return new GetAllTechnologiesUseCase(repo);
    }


}


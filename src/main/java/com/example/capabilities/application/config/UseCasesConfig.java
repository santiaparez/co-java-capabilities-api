package com.example.capabilities.application.config;

import com.example.capabilities.domain.usecase.CreateCapabilitiesUseCase;
import com.example.capabilities.domain.usecase.ListCapabilitiesUseCase;
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
    public ListCapabilitiesUseCase listCapabilitiesUseCase(SpringDataCapabilitiesRepository repo) {
        return new ListCapabilitiesUseCase(repo);
    }


}


package com.connecting_microservices.controller;

import com.connecting_microservices.dto.CompanyDto;
import com.connecting_microservices.dto.UserDto;
import com.connecting_microservices.dto.UserDtoWithCompany;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AggregationController {

    private final WebClient.Builder webClientBuilder;

    @GetMapping("/users-with-companies")
    public Mono<List<UserDtoWithCompany>> getUsersWithCompanies() {

        Mono<List<UserDto>> usersMono = webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/users")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UserDto>>() {});

        Mono<List<CompanyDto>> companiesMono = webClientBuilder.build()
                .get()
                .uri("http://localhost:8082/companies")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CompanyDto>>() {});

        return Mono.zip(usersMono, companiesMono)
                .map(tuple -> {
                    List<UserDto> users = tuple.getT1();
                    List<CompanyDto> companies = tuple.getT2();

                    Map<UUID, CompanyDto> companyMap = companies.stream()
                            .collect(Collectors.toMap(CompanyDto::id, Function.identity()));

                    return users.stream()
                            .map(u -> new UserDtoWithCompany(u, companyMap.get(u.companyId())))
                            .collect(Collectors.toList());
                });
    }
}

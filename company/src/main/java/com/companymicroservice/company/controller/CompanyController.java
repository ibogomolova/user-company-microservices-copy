package com.companymicroservice.company.controller;

import com.companymicroservice.company.dto.CompanyDto;
import com.companymicroservice.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST-контроллер для управления компаниями.
 * Предоставляет CRUD-эндпоинты для работы с компаниями.
 */
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public Page<CompanyDto> getAllCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return companyService.getAllCompanies(PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public CompanyDto getCompanyById(@PathVariable UUID id) {
        return companyService.getCompanyById(id);
    }

    @PostMapping
    public CompanyDto createCompany(@RequestBody @Valid CompanyDto companyDto) {
        return companyService.createCompany(companyDto);
    }

    @PutMapping("/{id}")
    public CompanyDto updateCompany(@PathVariable UUID id,
                                    @RequestBody @Valid CompanyDto companyDto) {
        return companyService.updateCompany(id, companyDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}

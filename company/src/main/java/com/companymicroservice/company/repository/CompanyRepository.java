package com.companymicroservice.company.repository;

import com.companymicroservice.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Репозиторий для работы с сущностью {@link com.companymicroservice.company.entity.Company}.
 * Использует Spring Data JPA для доступа к данным.
 */
public interface CompanyRepository extends JpaRepository<Company, UUID> {
}

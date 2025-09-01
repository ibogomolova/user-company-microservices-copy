package com.companymicroservice.company.repository;

import com.companymicroservice.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findByUserIdsContaining(UUID userId);
}

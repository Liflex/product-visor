package ru.dmitartur.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dmitartur.client.entity.Company;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
}





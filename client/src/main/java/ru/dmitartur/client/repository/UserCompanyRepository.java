package ru.dmitartur.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dmitartur.client.entity.UserCompany;

import java.util.Collection;
import java.util.List;

public interface UserCompanyRepository extends JpaRepository<UserCompany, Long> {
    List<UserCompany> findByUserId(java.util.UUID userId);
    boolean existsByUserIdAndCompanyId(java.util.UUID userId, java.util.UUID companyId);
    List<UserCompany> findByCompanyIdIn(Collection<java.util.UUID> companyIds);
}





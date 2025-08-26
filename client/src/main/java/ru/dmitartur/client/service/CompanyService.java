package ru.dmitartur.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.client.entity.Company;
import ru.dmitartur.client.entity.UserCompany;
import ru.dmitartur.client.repository.CompanyRepository;
import ru.dmitartur.client.repository.UserCompanyRepository;
import ru.dmitartur.client.repository.ClientUserRepository;
import ru.dmitartur.client.entity.ClientUser;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.client.mapper.CompanyMapper;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final ClientUserRepository clientUserRepository;
    private final CompanyMapper companyMapper;

    public List<Company> listMyCompanies() {
        UUID userId = JwtUtil.getRequiredOwnerId();
        ClientUser user = clientUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<UserCompany> links = userCompanyRepository.findByUserId(user.getId());
        return links.stream()
                .map(UserCompany::getCompanyId)
                .map(companyRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .toList();
    }

    public Company createCompany(Company company) {
        if (company.getId() == null) company.setId(UUID.randomUUID());
        Company saved = companyRepository.save(company);
        UUID userId = JwtUtil.getRequiredOwnerId();
        ClientUser user = clientUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserCompany uc = new UserCompany();
        uc.setUserId(user.getId());
        uc.setCompanyId(saved.getId());
        uc.setRole("OWNER");
        userCompanyRepository.save(uc);
        return saved;
    }

    public Company updateCompany(UUID companyId, Company updated) {
        ensureAccess(companyId);
        Company existing = companyRepository.findById(companyId).orElseThrow();
        companyMapper.update(existing, updated);
        return companyRepository.save(existing);
    }

    public void deleteCompany(UUID companyId) {
        ensureAccess(companyId);
        companyRepository.deleteById(companyId);
    }

    private void ensureAccess(UUID companyId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        ClientUser user = clientUserRepository.findById(userId)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("User not found"));
        if (!userCompanyRepository.existsByUserIdAndCompanyId(user.getId(), companyId)) {
            throw new org.springframework.security.access.AccessDeniedException("No access to company");
        }
    }

    public void uploadAvatar(UUID companyId, byte[] bytes) {
        ensureAccess(companyId);
        Company c = companyRepository.findById(companyId).orElseThrow();
        c.setAvatar(bytes);
        companyRepository.save(c);
    }

    public Company getCompany(UUID companyId) {
        ensureAccess(companyId);
        return companyRepository.findById(companyId).orElseThrow();
    }

    public byte[] getAvatar(UUID companyId) {
        return getCompany(companyId).getAvatar();
    }
}




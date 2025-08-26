package ru.dmitartur.client.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user_company", schema = "client",
       uniqueConstraints = @UniqueConstraint(name = "uq_user_company", columnNames = {"user_id", "company_id"}))
public class UserCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "role", nullable = false)
    private String role; // OWNER, ADMIN, MEMBER

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}





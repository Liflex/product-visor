package ru.dmitartur.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dmitartur.client.entity.ClientUser;

import java.util.Optional;
import java.util.UUID;

public interface ClientUserRepository extends JpaRepository<ClientUser, UUID> {
}





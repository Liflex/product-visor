package ru.dmitartur.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dmitartur.client.entity.TelegramClient;

@Repository
public interface TelegramClientRepository extends JpaRepository<TelegramClient, Long> {
} 
package ru.dmitartur.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.dmitartur.client.entity.ClientUser;
import ru.dmitartur.client.repository.ClientUserRepository;
import ru.dmitartur.common.utils.JwtUtil;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService {
    private final ClientUserRepository userRepository;

    public ClientUser getMe() {
        UUID id = JwtUtil.getCurrentId().map(UUID::fromString).orElseThrow();
        return userRepository.findById(id).orElseThrow();
    }

    public ClientUser updateMe(ClientUser patch) {
        UUID id = JwtUtil.getCurrentId().map(UUID::fromString).orElseThrow();
        ClientUser me = userRepository.findById(id).orElseThrow();
        if (patch.getFirstName() != null) me.setFirstName(patch.getFirstName());
        if (patch.getLastName() != null) me.setLastName(patch.getLastName());
        if (patch.getMiddleName() != null) me.setMiddleName(patch.getMiddleName());
        if (patch.getPhoto() != null) me.setPhoto(patch.getPhoto());
        if (patch.getPhone() != null) me.setPhone(patch.getPhone());
        if (patch.getLocale() != null) me.setLocale(patch.getLocale());
        if (patch.getTimezone() != null) me.setTimezone(patch.getTimezone());
        return userRepository.save(me);
    }

    public void uploadAvatar(byte[] bytes) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        ClientUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAvatar(bytes);
        userRepository.save(user);
    }

    public byte[] getAvatar() {
        UUID userId = JwtUtil.getRequiredOwnerId();
        ClientUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getAvatar();
    }
}




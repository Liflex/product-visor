package ru.dmitartur.client.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.dmitartur.client.entity.ClientUser;
import ru.dmitartur.client.service.ProfileService;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ClientUser> me() {
        return ResponseEntity.ok(profileService.getMe());
    }

    @PutMapping
    public ResponseEntity<ClientUser> update(@Valid @RequestBody ClientUser patch) {
        return ResponseEntity.ok(profileService.updateMe(patch));
    }

    @PostMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(@RequestPart("file") MultipartFile file) throws Exception {
        profileService.uploadAvatar(file.getBytes());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/avatar", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getAvatar() {
        byte[] avatar = profileService.getAvatar();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(avatar);
    }
}




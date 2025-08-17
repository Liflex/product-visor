package ru.dmitartur.client.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    @GetMapping("/admin/hello")
    public ResponseEntity<Map<String, Object>> adminHello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Admin endpoint!");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        log.info("Admin endpoint accessed by user: {}", authentication.getName());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/hello")
    public ResponseEntity<Map<String, Object>> publicHello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Public endpoint!");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/hello")
    public ResponseEntity<Map<String, Object>> userHello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from User endpoint!");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }
} 
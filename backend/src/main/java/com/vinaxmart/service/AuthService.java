package com.vinaxmart.service;

import com.vinaxmart.entity.Role;
import com.vinaxmart.entity.User;
import com.vinaxmart.repository.UserRepository;
import com.vinaxmart.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder enc;
    private final JwtService jwt;

    public AuthService(UserRepository repo, PasswordEncoder enc, JwtService jwt) {
        this.repo = repo;
        this.enc = enc;
        this.jwt = jwt;
    }

    public Map<String, Object> register(String name, String email, String password) {
        if (repo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(enc.encode(password))
                .role(Role.USER)
                .enabled(true)
                .build();

        repo.save(user);
        return login(email, password);
    }

    public Map<String, Object> login(String email, String password) {
        User user = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isEnabled() || !enc.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return Map.of(
                "token", jwt.generate(user.getEmail(), user.getRole().name()),
                "user", Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole().name()
                )
        );
    }
}

package com.judgemesh.user.service;

import com.judgemesh.api.dto.UserDTO;
import com.judgemesh.user.domain.UserAccount;
import com.judgemesh.user.domain.UserRole;
import com.judgemesh.user.repository.UserStore;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserService {
    private final UserStore store;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserStore store, PasswordEncoder passwordEncoder) {
        this.store = store;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    void seed() {
        if (store.findByEmail("admin@judgemesh.local").isEmpty()) {
            createSeed("admin", "admin@judgemesh.local", "Admin@12345", Set.of(UserRole.ADMIN, UserRole.SETTER, UserRole.STUDENT));
            createSeed("student", "student@judgemesh.local", "Student@12345", Set.of(UserRole.STUDENT));
            createSeed("setter", "setter@judgemesh.local", "Setter@12345", Set.of(UserRole.SETTER, UserRole.STUDENT));
        }
    }

    public UserAccount register(String username, String email, String password, String requestedRole) {
        if (store.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
        }
        if (store.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "username already registered");
        }
        EnumSet<UserRole> roles = EnumSet.of(UserRole.STUDENT);
        if (requestedRole != null && !requestedRole.isBlank()) {
            UserRole role = UserRole.valueOf(requestedRole.trim().toUpperCase(Locale.ROOT));
            if (role == UserRole.SETTER || role == UserRole.ADMIN) {
                roles.add(role);
            }
        }
        UserAccount user = UserAccount.builder()
                .username(username)
                .email(email)
                .nickname(username)
                .passwordHash(passwordEncoder.encode(password))
                .balance(100)
                .totalAc(0)
                .totalSubmit(0)
                .roles(roles)
                .build();
        return store.save(user);
    }

    public UserAccount login(String email, String password) {
        UserAccount user = store.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad credentials"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad credentials");
        }
        return user;
    }

    public UserAccount get(Long id) {
        return store.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user not found"));
    }

    public void deductBalance(Long userId, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be positive");
        }
        UserAccount user = get(userId);
        synchronized (user) {
            if (user.getBalance() < amount) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "balance insufficient");
            }
            user.setBalance(user.getBalance() - amount);
            store.save(user);
        }
    }

    public UserDTO toDto(UserAccount user) {
        List<String> roles = user.getRoles().stream().map(UserRole::name).sorted().toList();
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .balance(user.getBalance())
                .totalAc(user.getTotalAc())
                .totalSubmit(user.getTotalSubmit())
                .role(roles.isEmpty() ? UserRole.STUDENT.name() : roles.get(0))
                .roles(roles)
                .build();
    }

    private void createSeed(String username, String email, String password, Set<UserRole> roles) {
        store.save(UserAccount.builder()
                .username(username)
                .email(email)
                .nickname(username)
                .passwordHash(passwordEncoder.encode(password))
                .balance(100)
                .totalAc(0)
                .totalSubmit(0)
                .roles(EnumSet.copyOf(roles))
                .build());
    }
}

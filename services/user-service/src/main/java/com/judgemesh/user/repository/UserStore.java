package com.judgemesh.user.repository;

import com.judgemesh.user.domain.UserAccount;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserStore {
    private final AtomicLong ids = new AtomicLong(1000);
    private final ConcurrentHashMap<Long, UserAccount> users = new ConcurrentHashMap<>();

    public UserAccount save(UserAccount user) {
        Instant now = Instant.now();
        if (user.getId() == null) {
            user.setId(ids.incrementAndGet());
            user.setCreatedAt(now);
        }
        user.setUpdatedAt(now);
        users.put(user.getId(), user);
        return user;
    }

    public Optional<UserAccount> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<UserAccount> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<UserAccount> findByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<UserAccount> findAll() {
        return new ArrayList<>(users.values());
    }
}

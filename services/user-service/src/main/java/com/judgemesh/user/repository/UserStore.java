package com.judgemesh.user.repository;

import com.judgemesh.user.domain.UserAccount;
import com.judgemesh.user.domain.UserRole;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserStore {
    private final AtomicLong ids = new AtomicLong(1000);
    private final ConcurrentHashMap<Long, UserAccount> users = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbcTemplate;

    public UserStore(ObjectProvider<JdbcTemplate> jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate.getIfAvailable();
    }

    public UserAccount save(UserAccount user) {
        if (jdbcTemplate != null) {
            return saveJdbc(user);
        }
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
        if (jdbcTemplate != null) {
            return queryOne("select * from user where id = ? and deleted_at is null", id);
        }
        return Optional.ofNullable(users.get(id));
    }

    public Optional<UserAccount> findByEmail(String email) {
        if (jdbcTemplate != null) {
            return queryOne("select * from user where lower(email) = lower(?) and deleted_at is null", email);
        }
        return users.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<UserAccount> findByUsername(String username) {
        if (jdbcTemplate != null) {
            return queryOne("select * from user where lower(username) = lower(?) and deleted_at is null", username);
        }
        return users.values().stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public List<UserAccount> findAll() {
        if (jdbcTemplate != null) {
            return jdbcTemplate.query("select * from user where deleted_at is null order by id", mapper());
        }
        return new ArrayList<>(users.values());
    }

    private UserAccount saveJdbc(UserAccount user) {
        Instant now = Instant.now();
        if (user.getId() == null) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement("""
                        insert into user(username, email, password_hash, nickname, avatar_url, balance, total_ac, total_submit, created_at, updated_at)
                        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """, Statement.RETURN_GENERATED_KEYS);
                bindUser(ps, user, now, true);
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new IllegalStateException("user insert did not return generated id");
            }
            user.setId(key.longValue());
            user.setCreatedAt(now);
        } else {
            jdbcTemplate.update("""
                    update user
                    set username = ?, email = ?, password_hash = ?, nickname = ?, avatar_url = ?,
                        balance = ?, total_ac = ?, total_submit = ?, updated_at = ?
                    where id = ?
                    """,
                    user.getUsername(),
                    user.getEmail(),
                    user.getPasswordHash(),
                    user.getNickname(),
                    user.getAvatarUrl(),
                    user.getBalance(),
                    user.getTotalAc(),
                    user.getTotalSubmit(),
                    Timestamp.from(now),
                    user.getId());
        }
        user.setUpdatedAt(now);
        jdbcTemplate.update("delete from user_role where user_id = ?", user.getId());
        for (UserRole role : user.getRoles()) {
            jdbcTemplate.update("insert into user_role(user_id, role) values (?, ?)", user.getId(), role.name());
        }
        return user;
    }

    private void bindUser(PreparedStatement ps, UserAccount user, Instant now, boolean insert) throws java.sql.SQLException {
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPasswordHash());
        ps.setString(4, user.getNickname());
        ps.setString(5, user.getAvatarUrl());
        ps.setInt(6, user.getBalance());
        ps.setInt(7, user.getTotalAc());
        ps.setInt(8, user.getTotalSubmit());
        if (insert) {
            ps.setTimestamp(9, Timestamp.from(now));
            ps.setTimestamp(10, Timestamp.from(now));
        }
    }

    private Optional<UserAccount> queryOne(String sql, Object... args) {
        List<UserAccount> result = jdbcTemplate.query(sql, mapper(), args);
        return result.stream().findFirst();
    }

    private RowMapper<UserAccount> mapper() {
        return (rs, rowNum) -> {
            Long id = rs.getLong("id");
            EnumSet<UserRole> roles = loadRoles(id);
            return UserAccount.builder()
                    .id(id)
                    .username(rs.getString("username"))
                    .email(rs.getString("email"))
                    .passwordHash(rs.getString("password_hash"))
                    .nickname(rs.getString("nickname"))
                    .avatarUrl(rs.getString("avatar_url"))
                    .balance(rs.getInt("balance"))
                    .totalAc(rs.getInt("total_ac"))
                    .totalSubmit(rs.getInt("total_submit"))
                    .roles(roles)
                    .createdAt(toInstant(rs.getTimestamp("created_at")))
                    .updatedAt(toInstant(rs.getTimestamp("updated_at")))
                    .build();
        };
    }

    private EnumSet<UserRole> loadRoles(Long userId) {
        List<UserRole> roles = jdbcTemplate.query(
                "select role from user_role where user_id = ?",
                (rs, rowNum) -> UserRole.valueOf(rs.getString("role")),
                userId);
        return roles.isEmpty() ? EnumSet.of(UserRole.STUDENT) : EnumSet.copyOf(roles);
    }

    private static Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime().toInstant(ZoneOffset.UTC);
    }
}

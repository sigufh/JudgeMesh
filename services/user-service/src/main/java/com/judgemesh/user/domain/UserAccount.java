package com.judgemesh.user.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Data
@Builder
public class UserAccount {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private Integer balance;
    private Integer totalAc;
    private Integer totalSubmit;
    @Builder.Default
    private Set<UserRole> roles = EnumSet.of(UserRole.STUDENT);
    private Instant createdAt;
    private Instant updatedAt;
}

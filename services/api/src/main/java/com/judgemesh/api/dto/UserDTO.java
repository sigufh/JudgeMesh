package com.judgemesh.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Integer balance;
    private Integer totalAc;
    private Integer totalSubmit;
    /** Primary role kept for simple clients. */
    private String role;
    /** STUDENT / SETTER / ADMIN */
    private List<String> roles;
}

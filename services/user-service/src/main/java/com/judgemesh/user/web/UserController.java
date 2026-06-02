package com.judgemesh.user.web;

import com.judgemesh.api.dto.UserDTO;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.user.security.JwtService;
import com.judgemesh.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/api/user/me")
    public ApiResponse<UserDTO> me(@RequestHeader("Authorization") String authorization) {
        JwtService.Claims claims = jwtService.verify(bearerToken(authorization));
        return ApiResponse.ok(userService.toDto(userService.get(claims.userId())));
    }

    @GetMapping("/api/user/{id}")
    public ApiResponse<UserDTO> publicProfile(@PathVariable Long id) {
        return ApiResponse.ok(userService.toDto(userService.get(id)));
    }

    @PutMapping("/api/user/me/profile")
    public ApiResponse<UserDTO> updateProfile(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody UpdateProfileRequest request) {
        JwtService.Claims claims = jwtService.verify(bearerToken(authorization));
        return ApiResponse.ok(userService.toDto(
                userService.updateProfile(claims.userId(), request.nickname(), request.avatarUrl())));
    }

    @PutMapping("/api/user/me/avatar")
    public ApiResponse<UserDTO> updateAvatar(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody UpdateAvatarRequest request) {
        JwtService.Claims claims = jwtService.verify(bearerToken(authorization));
        return ApiResponse.ok(userService.toDto(userService.updateAvatar(claims.userId(), request.avatarUrl())));
    }

    @PostMapping("/api/user/me/password")
    public ApiResponse<Void> changePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {
        JwtService.Claims claims = jwtService.verify(bearerToken(authorization));
        userService.changePassword(claims.userId(), request.oldPassword(), request.newPassword());
        return ApiResponse.ok(null);
    }

    @PutMapping("/api/admin/user/{id}/roles")
    public ApiResponse<UserDTO> updateRoles(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id,
            @Valid @RequestBody UpdateRolesRequest request) {
        requireAdmin(jwtService.verify(bearerToken(authorization)));
        return ApiResponse.ok(userService.toDto(userService.updateRoles(id, request.roles())));
    }

    @PostMapping("/api/user/balance/deduct")
    public ApiResponse<Void> deductBalance(@Valid @RequestBody DeductRequest request) {
        userService.deductBalance(request.userId(), request.amount());
        return ApiResponse.ok(null);
    }

    @GetMapping("/api/user/internal/{id}")
    public UserDTO internalGet(@PathVariable Long id) {
        return userService.toDto(userService.get(id));
    }

    @PostMapping("/api/user/internal/deduct")
    public void internalDeduct(@RequestParam Long userId, @RequestParam Integer amount) {
        userService.deductBalance(userId, amount);
    }

    private static String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("missing bearer token");
        }
        return authorization.substring("Bearer ".length());
    }

    private static void requireAdmin(JwtService.Claims claims) {
        if (!claims.roles().contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "admin role required");
        }
    }

    public record DeductRequest(@NotNull Long userId, @NotNull @Min(1) Integer amount, String reason) {
    }

    public record UpdateProfileRequest(@Size(max = 64) String nickname, @Size(max = 512) String avatarUrl) {
    }

    public record UpdateAvatarRequest(@NotBlank @Size(max = 512) String avatarUrl) {
    }

    public record ChangePasswordRequest(
            @NotBlank String oldPassword,
            @NotBlank @Size(min = 8, max = 128) String newPassword) {
    }

    public record UpdateRolesRequest(@NotNull Set<@NotBlank String> roles) {
    }
}

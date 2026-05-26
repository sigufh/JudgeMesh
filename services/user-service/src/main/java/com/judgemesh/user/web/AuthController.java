package com.judgemesh.user.web;

import com.judgemesh.api.dto.UserDTO;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.user.domain.UserAccount;
import com.judgemesh.user.security.JwtService;
import com.judgemesh.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserAccount user = userService.register(request.username(), request.email(), request.password(), request.role());
        return ApiResponse.ok(authResponse(user));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = userService.login(request.email(), request.password());
        return ApiResponse.ok(authResponse(user));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestHeader("Authorization") String authorization) {
        JwtService.Claims claims = jwtService.verify(bearerToken(authorization));
        UserAccount user = userService.get(claims.userId());
        return ApiResponse.ok(authResponse(user));
    }

    private AuthResponse authResponse(UserAccount user) {
        String token = jwtService.issue(user);
        UserDTO dto = userService.toDto(user);
        return new AuthResponse(token, token, "Bearer", jwtService.ttlSeconds(), dto);
    }

    private static String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("missing bearer token");
        }
        return authorization.substring("Bearer ".length());
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 64) String username,
            @Email @NotBlank String email,
            @NotBlank @Size(min = 8, max = 128) String password,
            String role) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record AuthResponse(String token, String accessToken, String tokenType, long expiresIn, UserDTO user) {
    }
}

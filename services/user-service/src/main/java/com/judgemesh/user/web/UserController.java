package com.judgemesh.user.web;

import com.judgemesh.api.dto.UserDTO;
import com.judgemesh.api.error.ApiResponse;
import com.judgemesh.user.security.JwtService;
import com.judgemesh.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    public record DeductRequest(@NotNull Long userId, @NotNull @Min(1) Integer amount, String reason) {
    }
}

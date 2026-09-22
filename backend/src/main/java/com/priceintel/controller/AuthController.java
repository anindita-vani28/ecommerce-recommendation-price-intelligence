package com.priceintel.controller;

import com.priceintel.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authentication;
    private final JwtService jwt;
    public AuthController(AuthenticationManager authentication, JwtService jwt) {
        this.authentication = authentication;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        authentication.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return new AuthResponse(jwt.generateToken(request.username()), "Bearer", request.username());
    }

    public record LoginRequest(@Email String username, @NotBlank String password) {}
    public record AuthResponse(String accessToken, String tokenType, String username) {}
}

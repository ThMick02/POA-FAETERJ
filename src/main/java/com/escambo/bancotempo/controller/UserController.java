package com.escambo.bancotempo.controller;

import com.escambo.bancotempo.dto.user.UserProfileResponse;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Usuários", description = "Endpoints para consulta de perfil, saldo de horas e pontuação de reputação")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Consultar meu perfil", description = "Retorna os dados do usuário autenticado incluindo saldo disponível de horas, saldo em escrow e reputação.")
    public ResponseEntity<UserProfileResponse> getMyProfile(@Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        UserProfileResponse profile = userService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar perfil público de usuário")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID id) {
        UserProfileResponse profile = userService.getUserProfile(id);
        return ResponseEntity.ok(profile);
    }
}

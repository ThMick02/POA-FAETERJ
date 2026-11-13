package com.escambo.bancotempo.dto.auth;

import java.math.BigDecimal;
import java.util.UUID;

public record AuthResponse(
    String token,
    String type,
    UUID userId,
    String name,
    String email,
    BigDecimal timeBalance,
    Double reputationScore
) {
    public AuthResponse(String token, UUID userId, String name, String email, BigDecimal timeBalance, Double reputationScore) {
        this(token, "Bearer", userId, name, email, timeBalance, reputationScore);
    }
}

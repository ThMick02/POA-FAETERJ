package com.escambo.bancotempo.dto.user;

import com.escambo.bancotempo.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
    UUID id,
    String name,
    String email,
    BigDecimal timeBalance,
    BigDecimal blockedBalance,
    Double reputationScore,
    LocalDateTime createdAt
) {
    public static UserProfileResponse fromEntity(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTimeBalance(),
                user.getBlockedBalance(),
                user.getReputationScore(),
                user.getCreatedAt()
        );
    }
}

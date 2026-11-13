package com.escambo.bancotempo.dto.skill;

import com.escambo.bancotempo.model.SkillCategory;
import com.escambo.bancotempo.model.SkillOffer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SkillResponse(
    UUID id,
    UUID providerId,
    String providerName,
    String title,
    String description,
    SkillCategory category,
    BigDecimal hourlyRateFactor,
    boolean active,
    LocalDateTime createdAt
) {
    public static SkillResponse fromEntity(SkillOffer skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getProvider().getId(),
                skill.getProvider().getName(),
                skill.getTitle(),
                skill.getDescription(),
                skill.getCategory(),
                skill.getHourlyRateFactor(),
                skill.isActive(),
                skill.getCreatedAt()
        );
    }
}

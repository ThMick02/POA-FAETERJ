package com.escambo.bancotempo.dto.skill;

import com.escambo.bancotempo.model.SkillCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateSkillRequest(
    @NotBlank(message = "O título é obrigatório")
    String title,

    @NotBlank(message = "A descrição é obrigatória")
    String description,

    @NotNull(message = "A categoria é obrigatória")
    SkillCategory category,

    @NotNull(message = "O fator de multiplicação por hora é obrigatório")
    @DecimalMin(value = "0.10", message = "O fator deve ser no mínimo 0.10")
    BigDecimal hourlyRateFactor
) {}

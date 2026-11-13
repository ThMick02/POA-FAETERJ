package com.escambo.bancotempo.dto.proposal;

import com.escambo.bancotempo.model.CalculationStrategyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateProposalRequest(
    @NotNull(message = "A habilidade solicitada é obrigatória")
    UUID requestedSkillId,

    UUID offeredSkillId,

    @NotNull(message = "A quantidade base de horas é obrigatória")
    @DecimalMin(value = "0.25", message = "A quantidade base de horas deve ser de no mínimo 15 minutos (0.25)")
    BigDecimal baseHours,

    CalculationStrategyType strategyType,

    @NotNull(message = "A data e hora agendada são obrigatórias")
    @Future(message = "O agendamento deve ser para uma data futura")
    LocalDateTime scheduledAt,

    String notes
) {
    public CalculationStrategyType getEffectiveStrategy() {
        return strategyType != null ? strategyType : CalculationStrategyType.EQUAL_TIME;
    }
}

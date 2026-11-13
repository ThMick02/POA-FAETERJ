package com.escambo.bancotempo.patterns.strategy;

import com.escambo.bancotempo.exception.BusinessRuleException;
import com.escambo.bancotempo.model.CalculationStrategyType;
import com.escambo.bancotempo.model.SkillOffer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("COMPLEXITY_WEIGHTED")
public class ComplexityWeightedStrategy implements ExchangeCalculationStrategy {

    @Override
    public CalculationStrategyType getStrategyType() {
        return CalculationStrategyType.COMPLEXITY_WEIGHTED;
    }

    @Override
    public BigDecimal calculateRequiredHours(SkillOffer requestedSkill, SkillOffer offeredSkill, BigDecimal baseHours) {
        if (baseHours == null || baseHours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("A quantidade base de horas deve ser maior que zero.");
        }
        if (requestedSkill == null) {
            throw new BusinessRuleException("A habilidade solicitada é obrigatória para o cálculo de complexidade.");
        }

        BigDecimal requestedFactor = requestedSkill.getHourlyRateFactor() != null
                ? requestedSkill.getHourlyRateFactor()
                : BigDecimal.ONE;

        if (requestedFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("O fator de complexidade da habilidade solicitada deve ser maior que zero.");
        }

        // Se for permuta direta (troca de serviço por serviço)
        if (offeredSkill != null) {
            BigDecimal offeredFactor = offeredSkill.getHourlyRateFactor() != null
                    ? offeredSkill.getHourlyRateFactor()
                    : BigDecimal.ONE;

            if (offeredFactor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException("O fator de complexidade da habilidade ofertada deve ser maior que zero.");
            }

            // Proporção justa: baseHours * (requestedFactor / offeredFactor)
            return baseHours.multiply(requestedFactor)
                    .divide(offeredFactor, 2, RoundingMode.HALF_UP);
        }

        // Se for troca via saldo de horas (sem serviço ofertado em contrapartida)
        // Horas necessárias = baseHours * requestedFactor
        return baseHours.multiply(requestedFactor).setScale(2, RoundingMode.HALF_UP);
    }
}

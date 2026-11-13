package com.escambo.bancotempo.patterns.strategy;

import com.escambo.bancotempo.exception.BusinessRuleException;
import com.escambo.bancotempo.model.CalculationStrategyType;
import com.escambo.bancotempo.model.SkillOffer;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("EQUAL_TIME")
public class EqualTimeStrategy implements ExchangeCalculationStrategy {

    @Override
    public CalculationStrategyType getStrategyType() {
        return CalculationStrategyType.EQUAL_TIME;
    }

    @Override
    public BigDecimal calculateRequiredHours(SkillOffer requestedSkill, SkillOffer offeredSkill, BigDecimal baseHours) {
        if (baseHours == null || baseHours.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("A quantidade base de horas deve ser maior que zero.");
        }
        // Na equivalência direta 1:1, 1 hora de qualquer serviço equivale a 1 hora de crédito/serviço
        return baseHours.setScale(2, RoundingMode.HALF_UP);
    }
}

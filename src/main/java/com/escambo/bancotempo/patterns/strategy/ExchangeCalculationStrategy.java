package com.escambo.bancotempo.patterns.strategy;

import com.escambo.bancotempo.model.CalculationStrategyType;
import com.escambo.bancotempo.model.SkillOffer;

import java.math.BigDecimal;

public interface ExchangeCalculationStrategy {

    CalculationStrategyType getStrategyType();

    BigDecimal calculateRequiredHours(SkillOffer requestedSkill, SkillOffer offeredSkill, BigDecimal baseHours);
}

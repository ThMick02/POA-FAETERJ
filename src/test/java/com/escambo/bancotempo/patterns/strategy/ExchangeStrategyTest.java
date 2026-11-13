package com.escambo.bancotempo.patterns.strategy;

import com.escambo.bancotempo.exception.BusinessRuleException;
import com.escambo.bancotempo.model.SkillCategory;
import com.escambo.bancotempo.model.SkillOffer;
import com.escambo.bancotempo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do Padrão Strategy - Motor de Cálculo de Equivalência de Horas")
class ExchangeStrategyTest {

    private EqualTimeStrategy equalTimeStrategy;
    private ComplexityWeightedStrategy complexityWeightedStrategy;

    private SkillOffer techSkill;      // hourlyRateFactor = 1.50
    private SkillOffer designSkill;    // hourlyRateFactor = 1.00
    private SkillOffer consultingSkill;// hourlyRateFactor = 2.00

    @BeforeEach
    void setUp() {
        equalTimeStrategy = new EqualTimeStrategy();
        complexityWeightedStrategy = new ComplexityWeightedStrategy();

        User userA = new User(UUID.randomUUID(), "User A", "a@test.com", "pass", BigDecimal.TEN, 5.0);
        User userB = new User(UUID.randomUUID(), "User B", "b@test.com", "pass", BigDecimal.TEN, 5.0);

        techSkill = new SkillOffer(UUID.randomUUID(), userA, "Dev Java", "Java Dev", SkillCategory.TECH, new BigDecimal("1.50"));
        designSkill = new SkillOffer(UUID.randomUUID(), userB, "UI/UX", "Design", SkillCategory.DESIGN, new BigDecimal("1.00"));
        consultingSkill = new SkillOffer(UUID.randomUUID(), userA, "Consultoria", "Gestão", SkillCategory.CONSULTING, new BigDecimal("2.00"));
    }

    // --- Testes da EqualTimeStrategy ---

    @Test
    @DisplayName("EqualTimeStrategy: Deve retornar equivalência 1:1 exata para a quantidade base de horas")
    void testEqualTimeStrategyCalculatesOneToOne() {
        BigDecimal baseHours = new BigDecimal("3.00");
        BigDecimal result = equalTimeStrategy.calculateRequiredHours(techSkill, designSkill, baseHours);

        assertEquals(new BigDecimal("3.00"), result);
    }

    @Test
    @DisplayName("EqualTimeStrategy: Deve funcionar mesmo sem habilidade em contrapartida (troca via saldo)")
    void testEqualTimeStrategyWithoutOfferedSkill() {
        BigDecimal baseHours = new BigDecimal("1.50");
        BigDecimal result = equalTimeStrategy.calculateRequiredHours(techSkill, null, baseHours);

        assertEquals(new BigDecimal("1.50"), result);
    }

    @Test
    @DisplayName("EqualTimeStrategy: Deve lançar BusinessRuleException para quantidade base menor ou igual a zero")
    void testEqualTimeStrategyWithInvalidHours() {
        assertThrows(BusinessRuleException.class, () ->
                equalTimeStrategy.calculateRequiredHours(techSkill, null, BigDecimal.ZERO));
        assertThrows(BusinessRuleException.class, () ->
                equalTimeStrategy.calculateRequiredHours(techSkill, null, new BigDecimal("-1.00")));
        assertThrows(BusinessRuleException.class, () ->
                equalTimeStrategy.calculateRequiredHours(techSkill, null, null));
    }

    // --- Testes da ComplexityWeightedStrategy ---

    @Test
    @DisplayName("ComplexityWeightedStrategy: Troca por Saldo de Horas deve multiplicar horas base pelo fator da habilidade solicitada")
    void testComplexityWeightedHoursBalanceExchange() {
        // 2 horas de serviço técnico (fator 1.50) = 2.00 * 1.50 = 3.00 horas de saldo
        BigDecimal baseHours = new BigDecimal("2.00");
        BigDecimal result = complexityWeightedStrategy.calculateRequiredHours(techSkill, null, baseHours);

        assertEquals(new BigDecimal("3.00"), result);

        // 3 horas de consultoria (fator 2.00) = 3.00 * 2.00 = 6.00 horas de saldo
        BigDecimal resultConsulting = complexityWeightedStrategy.calculateRequiredHours(consultingSkill, null, new BigDecimal("3.00"));
        assertEquals(new BigDecimal("6.00"), resultConsulting);
    }

    @Test
    @DisplayName("ComplexityWeightedStrategy: Permuta direta onde habilidade solicitada é mais complexa que a ofertada")
    void testComplexityWeightedDirectExchangeRequestedMoreComplex() {
        // Solicitado: Consultoria (fator 2.00). Ofertado em troca: Design (fator 1.00).
        // Base: 2 horas solicitadas.
        // Horas necessárias de Design = 2.00 * (2.00 / 1.00) = 4.00 horas
        BigDecimal result = complexityWeightedStrategy.calculateRequiredHours(consultingSkill, designSkill, new BigDecimal("2.00"));

        assertEquals(new BigDecimal("4.00"), result);
    }

    @Test
    @DisplayName("ComplexityWeightedStrategy: Permuta direta onde habilidade solicitada é menos complexa que a ofertada")
    void testComplexityWeightedDirectExchangeRequestedLessComplex() {
        // Solicitado: Design (fator 1.00). Ofertado em troca: Consultoria (fator 2.00).
        // Base: 4 horas de Design solicitadas.
        // Horas necessárias de Consultoria = 4.00 * (1.00 / 2.00) = 2.00 horas
        BigDecimal result = complexityWeightedStrategy.calculateRequiredHours(designSkill, consultingSkill, new BigDecimal("4.00"));

        assertEquals(new BigDecimal("2.00"), result);
    }

    @Test
    @DisplayName("ComplexityWeightedStrategy: Permuta direta com habilidades de fatores iguais deve resultar em equivalência 1:1")
    void testComplexityWeightedDirectExchangeEqualFactors() {
        SkillOffer anotherDesign = new SkillOffer(UUID.randomUUID(), techSkill.getProvider(), "Logo Design", "Desc", SkillCategory.DESIGN, new BigDecimal("1.00"));

        BigDecimal result = complexityWeightedStrategy.calculateRequiredHours(designSkill, anotherDesign, new BigDecimal("3.50"));

        assertEquals(new BigDecimal("3.50"), result);
    }

    @Test
    @DisplayName("ComplexityWeightedStrategy: Deve validar campos obrigatórios e valores inválidos")
    void testComplexityWeightedValidationErrors() {
        assertThrows(BusinessRuleException.class, () ->
                complexityWeightedStrategy.calculateRequiredHours(null, designSkill, new BigDecimal("2.00")));

        assertThrows(BusinessRuleException.class, () ->
                complexityWeightedStrategy.calculateRequiredHours(techSkill, designSkill, BigDecimal.ZERO));
    }
}

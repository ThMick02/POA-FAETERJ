package com.escambo.bancotempo.patterns.observer.events;

import java.math.BigDecimal;
import java.util.UUID;

public record ProposalAcceptedEvent(
    UUID proposalId,
    UUID requesterId,
    UUID providerId,
    BigDecimal hours,
    boolean isDirectExchange
) {}

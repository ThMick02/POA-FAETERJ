package com.escambo.bancotempo.patterns.observer.events;

import java.util.UUID;

public record ProposalInProgressEvent(
    UUID proposalId,
    UUID requesterId,
    UUID providerId
) {}

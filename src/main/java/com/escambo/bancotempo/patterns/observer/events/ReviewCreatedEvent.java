package com.escambo.bancotempo.patterns.observer.events;

import java.util.UUID;

public record ReviewCreatedEvent(
    UUID reviewId,
    UUID proposalId,
    UUID reviewerId,
    UUID reviewedId,
    Integer rating
) {}

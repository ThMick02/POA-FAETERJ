package com.escambo.bancotempo.dto.review;

import com.escambo.bancotempo.model.Review;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
    UUID id,
    UUID proposalId,
    UUID reviewerId,
    String reviewerName,
    UUID reviewedId,
    String reviewedName,
    Integer rating,
    String comment,
    LocalDateTime createdAt
) {
    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProposal().getId(),
                review.getReviewer().getId(),
                review.getReviewer().getName(),
                review.getReviewed().getId(),
                review.getReviewed().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}

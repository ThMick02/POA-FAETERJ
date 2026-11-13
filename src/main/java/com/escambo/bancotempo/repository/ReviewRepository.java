package com.escambo.bancotempo.repository;

import com.escambo.bancotempo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByReviewedIdOrderByCreatedAtDesc(UUID reviewedId);

    List<Review> findByProposalId(UUID proposalId);

    boolean existsByProposalIdAndReviewerId(UUID proposalId, UUID reviewerId);

    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.reviewed.id = :userId")
    Optional<Double> calculateAverageRatingByUserId(@Param("userId") UUID userId);
}

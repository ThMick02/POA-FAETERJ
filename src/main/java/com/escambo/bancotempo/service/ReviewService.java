package com.escambo.bancotempo.service;

import com.escambo.bancotempo.dto.review.CreateReviewRequest;
import com.escambo.bancotempo.dto.review.ReviewResponse;
import com.escambo.bancotempo.exception.BusinessRuleException;
import com.escambo.bancotempo.exception.ResourceNotFoundException;
import com.escambo.bancotempo.exception.UnauthorizedActionException;
import com.escambo.bancotempo.model.ExchangeProposal;
import com.escambo.bancotempo.model.ProposalStatus;
import com.escambo.bancotempo.model.Review;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.patterns.observer.events.ReviewCreatedEvent;
import com.escambo.bancotempo.repository.ExchangeProposalRepository;
import com.escambo.bancotempo.repository.ReviewRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ExchangeProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReviewService(ReviewRepository reviewRepository,
                         ExchangeProposalRepository proposalRepository,
                         UserRepository userRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.proposalRepository = proposalRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReviewResponse createReview(UUID proposalId, CreateReviewRequest request, UUID currentUserId) {
        ExchangeProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta de escambo não encontrada: " + proposalId));

        if (proposal.getStatus() != ProposalStatus.COMPLETED) {
            throw new BusinessRuleException("Avaliações só podem ser realizadas após a conclusão efetiva (COMPLETED) da troca de serviços.");
        }

        User reviewer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário avaliador não encontrado: " + currentUserId));

        boolean isRequester = proposal.getRequester().getId().equals(currentUserId);
        boolean isProvider = proposal.getProvider().getId().equals(currentUserId);

        if (!isRequester && !isProvider) {
            throw new UnauthorizedActionException("Apenas participantes diretos da troca concluída podem avaliá-la.");
        }

        if (reviewRepository.existsByProposalIdAndReviewerId(proposalId, currentUserId)) {
            throw new BusinessRuleException("Você já submeteu uma avaliação para esta proposta de escambo.");
        }

        User reviewed = isRequester ? proposal.getProvider() : proposal.getRequester();

        Review review = new Review(
                UUID.randomUUID(),
                proposal,
                reviewer,
                reviewed,
                request.rating(),
                request.comment() != null ? request.comment().trim() : null
        );

        Review savedReview = reviewRepository.save(review);

        // Dispara o evento de nova avaliação para que o ReputationEventListener recalcule a reputação
        eventPublisher.publishEvent(new ReviewCreatedEvent(
                savedReview.getId(),
                proposal.getId(),
                reviewer.getId(),
                reviewed.getId(),
                request.rating()
        ));

        return ReviewResponse.fromEntity(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado: " + userId);
        }
        return reviewRepository.findByReviewedIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }
}

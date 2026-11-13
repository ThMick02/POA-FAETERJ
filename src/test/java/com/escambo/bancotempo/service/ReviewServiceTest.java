package com.escambo.bancotempo.service;

import com.escambo.bancotempo.dto.review.CreateReviewRequest;
import com.escambo.bancotempo.dto.review.ReviewResponse;
import com.escambo.bancotempo.exception.BusinessRuleException;
import com.escambo.bancotempo.model.*;
import com.escambo.bancotempo.patterns.observer.events.ReviewCreatedEvent;
import com.escambo.bancotempo.repository.ExchangeProposalRepository;
import com.escambo.bancotempo.repository.ReviewRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários do ReviewService (Dupla Validação, Avaliação e Evento de Reputação)")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ExchangeProposalRepository proposalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ReviewService reviewService;

    private User requester;
    private User provider;
    private ExchangeProposal proposal;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, proposalRepository, userRepository, eventPublisher);

        requester = new User(UUID.randomUUID(), "Alice", "alice@test.com", "pass", new BigDecimal("5.00"), 5.0);
        provider = new User(UUID.randomUUID(), "Bob", "bob@test.com", "pass", new BigDecimal("5.00"), 5.0);

        SkillOffer skill = new SkillOffer(UUID.randomUUID(), provider, "Tech Help", "Desc", SkillCategory.TECH, BigDecimal.ONE);

        proposal = new ExchangeProposal(
                UUID.randomUUID(), requester, provider, null, skill,
                new BigDecimal("2.00"), LocalDateTime.now().minusDays(1), "Completed exchange"
        );
        proposal.setStatus(ProposalStatus.COMPLETED);
    }

    @Test
    @DisplayName("Deve registrar avaliação com sucesso em proposta COMPLETED e disparar ReviewCreatedEvent")
    void testCreateReviewSuccess() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Excelente serviço prestado!");

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(reviewRepository.existsByProposalIdAndReviewerId(proposal.getId(), requester.getId())).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewResponse response = reviewService.createReview(proposal.getId(), request, requester.getId());

        assertNotNull(response);
        assertEquals(5, response.rating());
        assertEquals("Excelente serviço prestado!", response.comment());
        assertEquals(provider.getId(), response.reviewedId());

        verify(reviewRepository).save(any(Review.class));
        verify(eventPublisher).publishEvent(any(ReviewCreatedEvent.class));
    }

    @Test
    @DisplayName("Deve proibir avaliação se a proposta NÃO estiver COMPLETED")
    void testCannotReviewProposalNotCompleted() {
        proposal.setStatus(ProposalStatus.IN_PROGRESS);

        CreateReviewRequest request = new CreateReviewRequest(4, "Bom");

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                reviewService.createReview(proposal.getId(), request, requester.getId()));

        assertTrue(exception.getMessage().contains("conclusão efetiva (COMPLETED)"));
        verify(reviewRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Deve proibir avaliação duplicada pelo mesmo usuário na mesma proposta")
    void testCannotReviewTwice() {
        CreateReviewRequest request = new CreateReviewRequest(5, "Segunda tentativa");

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(reviewRepository.existsByProposalIdAndReviewerId(proposal.getId(), requester.getId())).thenReturn(true);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                reviewService.createReview(proposal.getId(), request, requester.getId()));

        assertTrue(exception.getMessage().contains("já submeteu uma avaliação"));
        verify(reviewRepository, never()).save(any());
    }
}

package com.escambo.bancotempo.patterns.observer.listeners;

import com.escambo.bancotempo.patterns.observer.events.ReviewCreatedEvent;
import com.escambo.bancotempo.repository.ReviewRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ReputationEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReputationEventListener.class);

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public ReputationEventListener(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewCreated(ReviewCreatedEvent event) {
        log.info("[REPUTATION] Recalculando reputação do usuário {} após avaliação {}...",
                event.reviewedId(), event.rating());

        userRepository.findById(event.reviewedId()).ifPresent(user -> {
            reviewRepository.calculateAverageRatingByUserId(user.getId()).ifPresent(avgRating -> {
                double roundedScore = BigDecimal.valueOf(avgRating)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                user.setReputationScore(roundedScore);
                userRepository.save(user);
                log.info("[REPUTATION] Usuário {} atualizado com nova reputação: {}", user.getId(), roundedScore);
            });
        });
    }
}

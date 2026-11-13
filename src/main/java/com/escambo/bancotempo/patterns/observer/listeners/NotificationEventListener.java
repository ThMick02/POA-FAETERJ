package com.escambo.bancotempo.patterns.observer.listeners;

import com.escambo.bancotempo.patterns.observer.events.ProposalAcceptedEvent;
import com.escambo.bancotempo.patterns.observer.events.ProposalCancelledEvent;
import com.escambo.bancotempo.patterns.observer.events.ProposalCompletedEvent;
import com.escambo.bancotempo.patterns.observer.events.ReviewCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalAccepted(ProposalAcceptedEvent event) {
        log.info("[NOTIFICATION] Proposta {} aceita! Notificando solicitante {} e prestador {}. Horas: {}",
                event.proposalId(), event.requesterId(), event.providerId(), event.hours());
        // Simulação de envio de notificação (email / push / websocket)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalCompleted(ProposalCompletedEvent event) {
        log.info("[NOTIFICATION] Proposta {} concluída com sucesso! Notificando usuários {} e {} para avaliarem o serviço.",
                event.proposalId(), event.requesterId(), event.providerId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalCancelled(ProposalCancelledEvent event) {
        log.info("[NOTIFICATION] Proposta {} cancelada. Notificando usuários {} e {}.",
                event.proposalId(), event.requesterId(), event.providerId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewCreated(ReviewCreatedEvent event) {
        log.info("[NOTIFICATION] Nova avaliação registrada! Usuário {} recebeu nota {} pela proposta {}.",
                event.reviewedId(), event.rating(), event.proposalId());
    }
}

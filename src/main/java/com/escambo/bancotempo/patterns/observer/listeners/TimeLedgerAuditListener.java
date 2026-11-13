package com.escambo.bancotempo.patterns.observer.listeners;

import com.escambo.bancotempo.model.ExchangeProposal;
import com.escambo.bancotempo.model.TimeLedgerEntry;
import com.escambo.bancotempo.model.TimeLedgerType;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.patterns.observer.events.ProposalCancelledEvent;
import com.escambo.bancotempo.patterns.observer.events.ProposalCompletedEvent;
import com.escambo.bancotempo.patterns.observer.events.ProposalCreatedEvent;
import com.escambo.bancotempo.repository.ExchangeProposalRepository;
import com.escambo.bancotempo.repository.TimeLedgerEntryRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class TimeLedgerAuditListener {

    private static final Logger log = LoggerFactory.getLogger(TimeLedgerAuditListener.class);

    private final TimeLedgerEntryRepository timeLedgerEntryRepository;
    private final UserRepository userRepository;
    private final ExchangeProposalRepository proposalRepository;

    public TimeLedgerAuditListener(TimeLedgerEntryRepository timeLedgerEntryRepository,
                                  UserRepository userRepository,
                                  ExchangeProposalRepository proposalRepository) {
        this.timeLedgerEntryRepository = timeLedgerEntryRepository;
        this.userRepository = userRepository;
        this.proposalRepository = proposalRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalCreated(ProposalCreatedEvent event) {
        if (event.isDirectExchange()) {
            return; // Permuta direta de serviço por serviço não bloqueia saldo financeiro de horas
        }
        log.info("[TIME_LEDGER] Registrando reserva de escrow (lock) de {} horas para o usuário {}",
                event.hours(), event.requesterId());

        User requester = userRepository.findById(event.requesterId()).orElse(null);
        ExchangeProposal proposal = proposalRepository.findById(event.proposalId()).orElse(null);

        if (requester != null) {
            TimeLedgerEntry entry = new TimeLedgerEntry(
                    UUID.randomUUID(),
                    requester,
                    proposal,
                    event.hours(),
                    TimeLedgerType.ESCROW_LOCK,
                    "Reserva de horas em escrow para proposta de escambo " + event.proposalId()
            );
            timeLedgerEntryRepository.save(entry);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalCompleted(ProposalCompletedEvent event) {
        if (event.isDirectExchange()) {
            return;
        }
        log.info("[TIME_LEDGER] Registrando transferência de {} horas do usuário {} para o prestador {}",
                event.hours(), event.requesterId(), event.providerId());

        User requester = userRepository.findById(event.requesterId()).orElse(null);
        User provider = userRepository.findById(event.providerId()).orElse(null);
        ExchangeProposal proposal = proposalRepository.findById(event.proposalId()).orElse(null);

        if (requester != null) {
            TimeLedgerEntry debitEntry = new TimeLedgerEntry(
                    UUID.randomUUID(),
                    requester,
                    proposal,
                    event.hours().negate(),
                    TimeLedgerType.TRANSFER_DEBIT,
                    "Débito definitivo de horas por serviço concluído na proposta " + event.proposalId()
            );
            timeLedgerEntryRepository.save(debitEntry);
        }

        if (provider != null) {
            TimeLedgerEntry creditEntry = new TimeLedgerEntry(
                    UUID.randomUUID(),
                    provider,
                    proposal,
                    event.hours(),
                    TimeLedgerType.TRANSFER_CREDIT,
                    "Crédito de horas recebidas por serviço prestado na proposta " + event.proposalId()
            );
            timeLedgerEntryRepository.save(creditEntry);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProposalCancelled(ProposalCancelledEvent event) {
        if (event.isDirectExchange()) {
            return;
        }
        log.info("[TIME_LEDGER] Registrando estorno de escrow (refund) de {} horas para o usuário {}",
                event.hours(), event.requesterId());

        User requester = userRepository.findById(event.requesterId()).orElse(null);
        ExchangeProposal proposal = proposalRepository.findById(event.proposalId()).orElse(null);

        if (requester != null) {
            TimeLedgerEntry refundEntry = new TimeLedgerEntry(
                    UUID.randomUUID(),
                    requester,
                    proposal,
                    event.hours(),
                    TimeLedgerType.ESCROW_REFUND,
                    "Estorno de horas do escrow devido a cancelamento da proposta " + event.proposalId()
            );
            timeLedgerEntryRepository.save(refundEntry);
        }
    }
}

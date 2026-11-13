package com.escambo.bancotempo.service;

import com.escambo.bancotempo.dto.proposal.CreateProposalRequest;
import com.escambo.bancotempo.dto.proposal.ProposalResponse;
import com.escambo.bancotempo.exception.BusinessRuleException;
import com.escambo.bancotempo.exception.InsufficientBalanceException;
import com.escambo.bancotempo.exception.ResourceNotFoundException;
import com.escambo.bancotempo.exception.UnauthorizedActionException;
import com.escambo.bancotempo.model.ExchangeProposal;
import com.escambo.bancotempo.model.ProposalStatus;
import com.escambo.bancotempo.model.SkillOffer;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.patterns.observer.events.*;
import com.escambo.bancotempo.patterns.state.ProposalContext;
import com.escambo.bancotempo.patterns.strategy.ExchangeCalculationStrategy;
import com.escambo.bancotempo.repository.ExchangeProposalRepository;
import com.escambo.bancotempo.repository.SkillOfferRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class ExchangeProposalService {

    private final ExchangeProposalRepository proposalRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final UserRepository userRepository;
    private final Map<String, ExchangeCalculationStrategy> calculationStrategies;
    private final ApplicationEventPublisher eventPublisher;

    public ExchangeProposalService(ExchangeProposalRepository proposalRepository,
                                  SkillOfferRepository skillOfferRepository,
                                  UserRepository userRepository,
                                  Map<String, ExchangeCalculationStrategy> calculationStrategies,
                                  ApplicationEventPublisher eventPublisher) {
        this.proposalRepository = proposalRepository;
        this.skillOfferRepository = skillOfferRepository;
        this.userRepository = userRepository;
        this.calculationStrategies = calculationStrategies;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ProposalResponse createProposal(CreateProposalRequest request, UUID requesterId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário solicitante não encontrado: " + requesterId));

        SkillOffer requestedSkill = skillOfferRepository.findById(request.requestedSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Habilidade solicitada não encontrada: " + request.requestedSkillId()));

        if (!requestedSkill.isActive()) {
            throw new BusinessRuleException("A habilidade solicitada não está mais ativa para trocas.");
        }

        User provider = requestedSkill.getProvider();

        if (provider.getId().equals(requester.getId())) {
            throw new BusinessRuleException("Não é permitido criar uma proposta de escambo com você mesmo.");
        }

        SkillOffer offeredSkill = null;
        if (request.offeredSkillId() != null) {
            offeredSkill = skillOfferRepository.findById(request.offeredSkillId())
                    .orElseThrow(() -> new ResourceNotFoundException("Habilidade ofertada em contrapartida não encontrada: " + request.offeredSkillId()));

            if (!offeredSkill.isActive()) {
                throw new BusinessRuleException("A sua habilidade ofertada não está mais ativa para trocas.");
            }

            if (!offeredSkill.getProvider().getId().equals(requester.getId())) {
                throw new UnauthorizedActionException("A habilidade ofertada em contrapartida deve pertencer ao solicitante da proposta.");
            }

            if (offeredSkill.getId().equals(requestedSkill.getId())) {
                throw new BusinessRuleException("A habilidade ofertada não pode ser igual à habilidade solicitada.");
            }
        }

        // Aplicação do padrão STRATEGY para cálculo de horas necessárias
        String strategyKey = request.getEffectiveStrategy().name();
        ExchangeCalculationStrategy strategy = calculationStrategies.get(strategyKey);
        if (strategy == null) {
            throw new BusinessRuleException("Estratégia de cálculo não suportada: " + strategyKey);
        }

        BigDecimal hoursAgreed = strategy.calculateRequiredHours(requestedSkill, offeredSkill, request.baseHours());

        // Regra de Reserva de Saldo (Escrow) para trocas baseadas em Saldo de Horas
        boolean isDirectExchange = offeredSkill != null;
        if (!isDirectExchange) {
            if (!requester.hasAvailableBalance(hoursAgreed)) {
                throw new InsufficientBalanceException(String.format(
                        "Saldo insuficiente para reservar as horas da proposta. Saldo disponível: %.2fh, Horas necessárias: %.2fh",
                        requester.getTimeBalance(), hoursAgreed
                ));
            }
            // Bloqueia / reserva o saldo do solicitante em garantia (Escrow)
            requester.lockBalance(hoursAgreed);
            userRepository.save(requester);
        }

        ExchangeProposal proposal = new ExchangeProposal(
                UUID.randomUUID(),
                requester,
                provider,
                offeredSkill,
                requestedSkill,
                hoursAgreed,
                request.scheduledAt(),
                request.notes()
        );

        ExchangeProposal savedProposal = proposalRepository.save(proposal);

        // Disparo do evento de domínio para Observers
        eventPublisher.publishEvent(new ProposalCreatedEvent(
                savedProposal.getId(),
                requester.getId(),
                provider.getId(),
                hoursAgreed,
                isDirectExchange
        ));

        return ProposalResponse.fromEntity(savedProposal);
    }

    @Transactional
    public ProposalResponse transitionProposal(UUID proposalId, ProposalStatus targetStatus, UUID currentUserId) {
        ExchangeProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta de escambo não encontrada: " + proposalId));

        User requester = proposal.getRequester();
        User provider = proposal.getProvider();

        // Validação de pertinência: apenas requester ou provider podem interagir com a proposta
        boolean isRequester = requester.getId().equals(currentUserId);
        boolean isProvider = provider.getId().equals(currentUserId);

        if (!isRequester && !isProvider) {
            throw new UnauthorizedActionException("Você não tem permissão para interagir com esta proposta de escambo.");
        }

        // Validações de ação por papel
        if (targetStatus == ProposalStatus.ACCEPTED && !isProvider) {
            throw new UnauthorizedActionException("Apenas o prestador do serviço pode aceitar a proposta.");
        }

        // Aplicação do padrão STATE para validação e execução da transição
        ProposalContext context = new ProposalContext(proposal);
        context.transitionTo(targetStatus);

        // Efeitos colaterais de negócio baseados no estado alcançado
        boolean isDirectExchange = proposal.isDirectExchange();
        BigDecimal hoursAgreed = proposal.getHoursAgreed();

        switch (targetStatus) {
            case ACCEPTED -> {
                eventPublisher.publishEvent(new ProposalAcceptedEvent(
                        proposal.getId(), requester.getId(), provider.getId(), hoursAgreed, isDirectExchange
                ));
            }
            case IN_PROGRESS -> {
                eventPublisher.publishEvent(new ProposalInProgressEvent(
                        proposal.getId(), requester.getId(), provider.getId()
                ));
            }
            case COMPLETED -> {
                // Se for troca via saldo de horas, efetiva a transferência definitiva
                if (!isDirectExchange) {
                    requester.debitBlockedBalance(hoursAgreed);
                    provider.creditBalance(hoursAgreed);
                    userRepository.save(requester);
                    userRepository.save(provider);
                }
                eventPublisher.publishEvent(new ProposalCompletedEvent(
                        proposal.getId(), requester.getId(), provider.getId(), hoursAgreed, isDirectExchange
                ));
            }
            case CANCELLED -> {
                // Se for troca via saldo de horas, estorna o valor bloqueado para o saldo disponível
                if (!isDirectExchange) {
                    requester.refundBalance(hoursAgreed);
                    userRepository.save(requester);
                }
                eventPublisher.publishEvent(new ProposalCancelledEvent(
                        proposal.getId(), requester.getId(), provider.getId(), hoursAgreed, isDirectExchange
                ));
            }
            default -> {}
        }

        ExchangeProposal updatedProposal = proposalRepository.save(proposal);
        return ProposalResponse.fromEntity(updatedProposal);
    }

    @Transactional(readOnly = true)
    public Page<ProposalResponse> getMyProposals(UUID currentUserId, ProposalStatus status, Pageable pageable) {
        Page<ExchangeProposal> proposals;
        if (status != null) {
            proposals = proposalRepository.findAllByUserAndStatus(currentUserId, status, pageable);
        } else {
            proposals = proposalRepository.findAllByUser(currentUserId, pageable);
        }
        return proposals.map(ProposalResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProposalResponse getProposalById(UUID proposalId, UUID currentUserId) {
        ExchangeProposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposta de escambo não encontrada: " + proposalId));

        if (!proposal.getRequester().getId().equals(currentUserId) && !proposal.getProvider().getId().equals(currentUserId)) {
            throw new UnauthorizedActionException("Você não tem permissão para visualizar os detalhes desta proposta.");
        }

        return ProposalResponse.fromEntity(proposal);
    }
}

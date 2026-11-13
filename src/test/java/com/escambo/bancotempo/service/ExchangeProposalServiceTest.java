package com.escambo.bancotempo.service;

import com.escambo.bancotempo.dto.proposal.CreateProposalRequest;
import com.escambo.bancotempo.dto.proposal.ProposalResponse;
import com.escambo.bancotempo.exception.InsufficientBalanceException;
import com.escambo.bancotempo.exception.InvalidProposalStateException;
import com.escambo.bancotempo.exception.UnauthorizedActionException;
import com.escambo.bancotempo.model.*;
import com.escambo.bancotempo.patterns.observer.events.ProposalAcceptedEvent;
import com.escambo.bancotempo.patterns.observer.events.ProposalCompletedEvent;
import com.escambo.bancotempo.patterns.observer.events.ProposalCreatedEvent;
import com.escambo.bancotempo.patterns.strategy.EqualTimeStrategy;
import com.escambo.bancotempo.patterns.strategy.ExchangeCalculationStrategy;
import com.escambo.bancotempo.repository.ExchangeProposalRepository;
import com.escambo.bancotempo.repository.SkillOfferRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários do ExchangeProposalService (Escrow, Strategy, State e Observer)")
class ExchangeProposalServiceTest {

    @Mock
    private ExchangeProposalRepository proposalRepository;

    @Mock
    private SkillOfferRepository skillOfferRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ExchangeProposalService proposalService;

    private User requester;
    private User provider;
    private SkillOffer requestedSkill;

    @BeforeEach
    void setUp() {
        EqualTimeStrategy equalTimeStrategy = new EqualTimeStrategy();
        Map<String, ExchangeCalculationStrategy> strategies = Map.of(
                "EQUAL_TIME", equalTimeStrategy
        );

        proposalService = new ExchangeProposalService(
                proposalRepository,
                skillOfferRepository,
                userRepository,
                strategies,
                eventPublisher
        );

        requester = new User(UUID.randomUUID(), "Alice", "alice@test.com", "pass", new BigDecimal("5.00"), 5.0);
        provider = new User(UUID.randomUUID(), "Bob", "bob@test.com", "pass", new BigDecimal("5.00"), 5.0);
        requestedSkill = new SkillOffer(UUID.randomUUID(), provider, "Desenvolvimento Web", "Desc", SkillCategory.TECH, new BigDecimal("1.00"));
    }

    @Test
    @DisplayName("Deve criar proposta via saldo de horas com sucesso e bloquear o saldo em Escrow")
    void testCreateProposalWithEscrowLockSuccess() {
        CreateProposalRequest request = new CreateProposalRequest(
                requestedSkill.getId(),
                null, // Sem troca direta, usa saldo de horas
                new BigDecimal("2.00"),
                CalculationStrategyType.EQUAL_TIME,
                LocalDateTime.now().plusDays(1),
                "Proposta de escambo"
        );

        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(skillOfferRepository.findById(requestedSkill.getId())).thenReturn(Optional.of(requestedSkill));
        when(proposalRepository.save(any(ExchangeProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProposalResponse response = proposalService.createProposal(request, requester.getId());

        assertNotNull(response);
        assertEquals(new BigDecimal("2.00"), response.hoursAgreed());
        assertEquals(ProposalStatus.PENDING, response.status());

        // Validação da reserva de saldo (Escrow): saldo disponível foi debitado e bloqueado
        assertEquals(new BigDecimal("3.00"), requester.getTimeBalance());
        assertEquals(new BigDecimal("2.00"), requester.getBlockedBalance());

        verify(userRepository).save(requester);
        verify(proposalRepository).save(any(ExchangeProposal.class));
        verify(eventPublisher).publishEvent(any(ProposalCreatedEvent.class));
    }

    @Test
    @DisplayName("Deve falhar com InsufficientBalanceException quando saldo do solicitante for insuficiente para o Escrow")
    void testCreateProposalFailsWhenInsufficientBalance() {
        requester.setTimeBalance(new BigDecimal("1.00")); // Saldo menor que as 2.00 horas necessárias

        CreateProposalRequest request = new CreateProposalRequest(
                requestedSkill.getId(),
                null,
                new BigDecimal("2.00"),
                CalculationStrategyType.EQUAL_TIME,
                LocalDateTime.now().plusDays(1),
                "Proposta"
        );

        when(userRepository.findById(requester.getId())).thenReturn(Optional.of(requester));
        when(skillOfferRepository.findById(requestedSkill.getId())).thenReturn(Optional.of(requestedSkill));

        assertThrows(InsufficientBalanceException.class, () ->
                proposalService.createProposal(request, requester.getId()));

        verify(proposalRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Prestador deve conseguir aceitar proposta PENDING -> ACCEPTED")
    void testProviderAcceptsProposalSuccess() {
        ExchangeProposal proposal = new ExchangeProposal(
                UUID.randomUUID(), requester, provider, null, requestedSkill,
                new BigDecimal("2.00"), LocalDateTime.now().plusDays(1), "Notes"
        );

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(ExchangeProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProposalResponse response = proposalService.transitionProposal(proposal.getId(), ProposalStatus.ACCEPTED, provider.getId());

        assertEquals(ProposalStatus.ACCEPTED, response.status());
        verify(eventPublisher).publishEvent(any(ProposalAcceptedEvent.class));
    }

    @Test
    @DisplayName("Solicitante NÃO pode aceitar a proposta feita por ele mesmo (apenas o prestador pode)")
    void testRequesterCannotAcceptOwnProposal() {
        ExchangeProposal proposal = new ExchangeProposal(
                UUID.randomUUID(), requester, provider, null, requestedSkill,
                new BigDecimal("2.00"), LocalDateTime.now().plusDays(1), "Notes"
        );

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));

        assertThrows(UnauthorizedActionException.class, () ->
                proposalService.transitionProposal(proposal.getId(), ProposalStatus.ACCEPTED, requester.getId()));
    }

    @Test
    @DisplayName("Concluir proposta (COMPLETED) deve transferir horas do Escrow para o prestador")
    void testCompleteProposalTransfersEscrowHoursToProvider() {
        requester.setTimeBalance(new BigDecimal("3.00"));
        requester.setBlockedBalance(new BigDecimal("2.00"));
        provider.setTimeBalance(new BigDecimal("5.00"));

        ExchangeProposal proposal = new ExchangeProposal(
                UUID.randomUUID(), requester, provider, null, requestedSkill,
                new BigDecimal("2.00"), LocalDateTime.now().plusDays(1), "Notes"
        );
        proposal.setStatus(ProposalStatus.IN_PROGRESS);

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(ExchangeProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProposalResponse response = proposalService.transitionProposal(proposal.getId(), ProposalStatus.COMPLETED, requester.getId());

        assertEquals(ProposalStatus.COMPLETED, response.status());
        // Saldo bloqueado do solicitante zerado
        assertEquals(new BigDecimal("0.00"), requester.getBlockedBalance());
        // Saldo do prestador incrementado de 5.00 para 7.00
        assertEquals(new BigDecimal("7.00"), provider.getTimeBalance());

        verify(userRepository).save(requester);
        verify(userRepository).save(provider);
        verify(eventPublisher).publishEvent(any(ProposalCompletedEvent.class));
    }

    @Test
    @DisplayName("Cancelar proposta (CANCELLED) deve estornar as horas bloqueadas em Escrow de volta ao solicitante")
    void testCancelProposalRefundsEscrowHoursToRequester() {
        requester.setTimeBalance(new BigDecimal("3.00"));
        requester.setBlockedBalance(new BigDecimal("2.00"));

        ExchangeProposal proposal = new ExchangeProposal(
                UUID.randomUUID(), requester, provider, null, requestedSkill,
                new BigDecimal("2.00"), LocalDateTime.now().plusDays(1), "Notes"
        );
        proposal.setStatus(ProposalStatus.PENDING);

        when(proposalRepository.findById(proposal.getId())).thenReturn(Optional.of(proposal));
        when(proposalRepository.save(any(ExchangeProposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProposalResponse response = proposalService.transitionProposal(proposal.getId(), ProposalStatus.CANCELLED, requester.getId());

        assertEquals(ProposalStatus.CANCELLED, response.status());
        // Saldo bloqueado estornado: disponível volta a 5.00 e bloqueado zera
        assertEquals(new BigDecimal("5.00"), requester.getTimeBalance());
        assertEquals(new BigDecimal("0.00"), requester.getBlockedBalance());

        verify(userRepository).save(requester);
    }
}

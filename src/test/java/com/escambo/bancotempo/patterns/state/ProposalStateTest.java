package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.exception.InvalidProposalStateException;
import com.escambo.bancotempo.model.ExchangeProposal;
import com.escambo.bancotempo.model.ProposalStatus;
import com.escambo.bancotempo.model.SkillCategory;
import com.escambo.bancotempo.model.SkillOffer;
import com.escambo.bancotempo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do Padrão State - Ciclo de Vida da Proposta de Escambo")
class ProposalStateTest {

    private ExchangeProposal proposal;
    private ProposalContext context;

    @BeforeEach
    void setUp() {
        User requester = new User(UUID.randomUUID(), "Requester", "req@test.com", "hash", new BigDecimal("5.00"), 5.0);
        User provider = new User(UUID.randomUUID(), "Provider", "prov@test.com", "hash", new BigDecimal("5.00"), 5.0);
        SkillOffer skill = new SkillOffer(UUID.randomUUID(), provider, "Tech Support", "Desc", SkillCategory.TECH, new BigDecimal("1.00"));

        proposal = new ExchangeProposal(
                UUID.randomUUID(),
                requester,
                provider,
                null,
                skill,
                new BigDecimal("2.00"),
                LocalDateTime.now().plusDays(2),
                "Teste de escambo"
        );
        context = new ProposalContext(proposal);
    }

    @Test
    @DisplayName("Deve inicializar a proposta no estado PENDING")
    void testInitialStateIsPending() {
        assertEquals(ProposalStatus.PENDING, proposal.getStatus());
        assertInstanceOf(PendingState.class, context.getCurrentState());
    }

    @Test
    @DisplayName("Deve transicionar com sucesso pelo fluxo feliz: PENDING -> ACCEPTED -> IN_PROGRESS -> COMPLETED")
    void testHappyPathLifecycle() {
        // PENDING -> ACCEPTED
        context.transitionTo(ProposalStatus.ACCEPTED);
        assertEquals(ProposalStatus.ACCEPTED, proposal.getStatus());
        assertInstanceOf(AcceptedState.class, context.getCurrentState());

        // ACCEPTED -> IN_PROGRESS
        context.transitionTo(ProposalStatus.IN_PROGRESS);
        assertEquals(ProposalStatus.IN_PROGRESS, proposal.getStatus());
        assertInstanceOf(InProgressState.class, context.getCurrentState());

        // IN_PROGRESS -> COMPLETED
        context.transitionTo(ProposalStatus.COMPLETED);
        assertEquals(ProposalStatus.COMPLETED, proposal.getStatus());
        assertInstanceOf(CompletedState.class, context.getCurrentState());
    }

    @Test
    @DisplayName("Deve permitir cancelamento a partir do estado PENDING")
    void testCancelFromPending() {
        context.transitionTo(ProposalStatus.CANCELLED);
        assertEquals(ProposalStatus.CANCELLED, proposal.getStatus());
        assertInstanceOf(CancelledState.class, context.getCurrentState());
    }

    @Test
    @DisplayName("Deve permitir cancelamento a partir do estado ACCEPTED")
    void testCancelFromAccepted() {
        context.transitionTo(ProposalStatus.ACCEPTED);
        context.transitionTo(ProposalStatus.CANCELLED);

        assertEquals(ProposalStatus.CANCELLED, proposal.getStatus());
        assertInstanceOf(CancelledState.class, context.getCurrentState());
    }

    @Test
    @DisplayName("Deve permitir cancelamento a partir do estado IN_PROGRESS")
    void testCancelFromInProgress() {
        context.transitionTo(ProposalStatus.ACCEPTED);
        context.transitionTo(ProposalStatus.IN_PROGRESS);
        context.transitionTo(ProposalStatus.CANCELLED);

        assertEquals(ProposalStatus.CANCELLED, proposal.getStatus());
        assertInstanceOf(CancelledState.class, context.getCurrentState());
    }

    @Test
    @DisplayName("Deve proibir iniciar proposta pendente sem aceite prévio (PENDING -> IN_PROGRESS)")
    void testCannotStartPendingProposal() {
        InvalidProposalStateException exception = assertThrows(
                InvalidProposalStateException.class,
                () -> context.transitionTo(ProposalStatus.IN_PROGRESS)
        );
        assertTrue(exception.getMessage().contains("Não é possível iniciar"));
        assertEquals(ProposalStatus.PENDING, proposal.getStatus());
    }

    @Test
    @DisplayName("Deve proibir concluir proposta pendente diretamente (PENDING -> COMPLETED)")
    void testCannotCompletePendingProposal() {
        InvalidProposalStateException exception = assertThrows(
                InvalidProposalStateException.class,
                () -> context.transitionTo(ProposalStatus.COMPLETED)
        );
        assertTrue(exception.getMessage().contains("Não é possível concluir"));
        assertEquals(ProposalStatus.PENDING, proposal.getStatus());
    }

    @Test
    @DisplayName("Deve proibir concluir proposta aceita antes de iniciá-la (ACCEPTED -> COMPLETED)")
    void testCannotCompleteAcceptedProposalWithoutStarting() {
        context.transitionTo(ProposalStatus.ACCEPTED);

        InvalidProposalStateException exception = assertThrows(
                InvalidProposalStateException.class,
                () -> context.transitionTo(ProposalStatus.COMPLETED)
        );
        assertTrue(exception.getMessage().contains("Não é possível concluir uma proposta aceita antes de iniciá-la"));
        assertEquals(ProposalStatus.ACCEPTED, proposal.getStatus());
    }

    @Test
    @DisplayName("Deve proibir regredir estado de IN_PROGRESS para ACCEPTED")
    void testCannotRegressFromInProgressToAccepted() {
        context.transitionTo(ProposalStatus.ACCEPTED);
        context.transitionTo(ProposalStatus.IN_PROGRESS);

        assertThrows(
                InvalidProposalStateException.class,
                () -> context.transitionTo(ProposalStatus.ACCEPTED)
        );
        assertEquals(ProposalStatus.IN_PROGRESS, proposal.getStatus());
    }

    @Test
    @DisplayName("Estado COMPLETED é terminal e proíbe qualquer nova transição")
    void testCompletedIsTerminalState() {
        context.transitionTo(ProposalStatus.ACCEPTED);
        context.transitionTo(ProposalStatus.IN_PROGRESS);
        context.transitionTo(ProposalStatus.COMPLETED);

        assertThrows(InvalidProposalStateException.class, () -> context.transitionTo(ProposalStatus.ACCEPTED));
        assertThrows(InvalidProposalStateException.class, () -> context.transitionTo(ProposalStatus.IN_PROGRESS));
        assertThrows(InvalidProposalStateException.class, () -> context.transitionTo(ProposalStatus.CANCELLED));
        assertThrows(InvalidProposalStateException.class, () -> context.transitionTo(ProposalStatus.COMPLETED));

        assertEquals(ProposalStatus.COMPLETED, proposal.getStatus());
    }

    @Test
    @DisplayName("Estado CANCELLED é terminal e proíbe qualquer nova transição")
    void testCancelledIsTerminalState() {
        context.transitionTo(ProposalStatus.CANCELLED);

        assertThrows(InvalidProposalStateException.class, () -> context.transitionTo(ProposalStatus.ACCEPTED));
        assertThrows(InvalidProposalStateException.class, () -> context.transitionTo(ProposalStatus.IN_PROGRESS));
        assertThrows(InvalidProposalStateException.class, () -> context.transitionTo(ProposalStatus.COMPLETED));
        assertThrows(InvalidProposalStateException.class, () -> context.transitionTo(ProposalStatus.CANCELLED));

        assertEquals(ProposalStatus.CANCELLED, proposal.getStatus());
    }
}

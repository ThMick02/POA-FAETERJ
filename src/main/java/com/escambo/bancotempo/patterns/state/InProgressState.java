package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.exception.InvalidProposalStateException;
import com.escambo.bancotempo.model.ProposalStatus;

public class InProgressState implements ProposalState {

    @Override
    public ProposalStatus getStatus() {
        return ProposalStatus.IN_PROGRESS;
    }

    @Override
    public ProposalState accept(ProposalContext context) {
        throw new InvalidProposalStateException("A proposta já está em andamento, não pode ser aceita novamente.");
    }

    @Override
    public ProposalState start(ProposalContext context) {
        throw new InvalidProposalStateException("A proposta já está em andamento.");
    }

    @Override
    public ProposalState complete(ProposalContext context) {
        return new CompletedState();
    }

    @Override
    public ProposalState cancel(ProposalContext context) {
        return new CancelledState();
    }

    @Override
    public ProposalState transitionTo(ProposalStatus targetStatus, ProposalContext context) {
        return switch (targetStatus) {
            case COMPLETED -> complete(context);
            case CANCELLED -> cancel(context);
            case IN_PROGRESS -> throw new InvalidProposalStateException("A proposta já se encontra no estado IN_PROGRESS.");
            case ACCEPTED -> throw new InvalidProposalStateException("Não é permitido regredir o status de IN_PROGRESS para ACCEPTED.");
            case PENDING -> throw new InvalidProposalStateException("Não é permitido regredir o status para PENDING.");
        };
    }
}

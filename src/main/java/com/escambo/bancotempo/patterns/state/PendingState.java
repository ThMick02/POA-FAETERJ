package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.exception.InvalidProposalStateException;
import com.escambo.bancotempo.model.ProposalStatus;

public class PendingState implements ProposalState {

    @Override
    public ProposalStatus getStatus() {
        return ProposalStatus.PENDING;
    }

    @Override
    public ProposalState accept(ProposalContext context) {
        return new AcceptedState();
    }

    @Override
    public ProposalState start(ProposalContext context) {
        throw new InvalidProposalStateException("Não é possível iniciar uma proposta que ainda está pendente de aceite.");
    }

    @Override
    public ProposalState complete(ProposalContext context) {
        throw new InvalidProposalStateException("Não é possível concluir uma proposta que ainda está pendente.");
    }

    @Override
    public ProposalState cancel(ProposalContext context) {
        return new CancelledState();
    }

    @Override
    public ProposalState transitionTo(ProposalStatus targetStatus, ProposalContext context) {
        return switch (targetStatus) {
            case ACCEPTED -> accept(context);
            case CANCELLED -> cancel(context);
            case IN_PROGRESS -> start(context);
            case COMPLETED -> complete(context);
            case PENDING -> throw new InvalidProposalStateException("A proposta já se encontra no estado PENDING.");
        };
    }
}

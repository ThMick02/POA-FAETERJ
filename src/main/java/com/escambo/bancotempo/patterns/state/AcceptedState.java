package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.exception.InvalidProposalStateException;
import com.escambo.bancotempo.model.ProposalStatus;

public class AcceptedState implements ProposalState {

    @Override
    public ProposalStatus getStatus() {
        return ProposalStatus.ACCEPTED;
    }

    @Override
    public ProposalState accept(ProposalContext context) {
        throw new InvalidProposalStateException("A proposta já foi aceita anteriormente.");
    }

    @Override
    public ProposalState start(ProposalContext context) {
        return new InProgressState();
    }

    @Override
    public ProposalState complete(ProposalContext context) {
        throw new InvalidProposalStateException("Não é possível concluir uma proposta aceita antes de iniciá-la (necessário transicionar para IN_PROGRESS).");
    }

    @Override
    public ProposalState cancel(ProposalContext context) {
        return new CancelledState();
    }

    @Override
    public ProposalState transitionTo(ProposalStatus targetStatus, ProposalContext context) {
        return switch (targetStatus) {
            case IN_PROGRESS -> start(context);
            case CANCELLED -> cancel(context);
            case ACCEPTED -> throw new InvalidProposalStateException("A proposta já se encontra no estado ACCEPTED.");
            case PENDING -> throw new InvalidProposalStateException("Não é permitido retornar uma proposta para PENDING.");
            case COMPLETED -> complete(context);
        };
    }
}

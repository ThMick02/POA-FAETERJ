package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.exception.InvalidProposalStateException;
import com.escambo.bancotempo.model.ProposalStatus;

public class CancelledState implements ProposalState {

    @Override
    public ProposalStatus getStatus() {
        return ProposalStatus.CANCELLED;
    }

    @Override
    public ProposalState accept(ProposalContext context) {
        throw new InvalidProposalStateException("Proposta cancelada não pode ser aceita.");
    }

    @Override
    public ProposalState start(ProposalContext context) {
        throw new InvalidProposalStateException("Proposta cancelada não pode ser iniciada.");
    }

    @Override
    public ProposalState complete(ProposalContext context) {
        throw new InvalidProposalStateException("Proposta cancelada não pode ser concluída.");
    }

    @Override
    public ProposalState cancel(ProposalContext context) {
        throw new InvalidProposalStateException("Proposta já se encontra cancelada.");
    }

    @Override
    public ProposalState transitionTo(ProposalStatus targetStatus, ProposalContext context) {
        throw new InvalidProposalStateException("Proposta cancelada é um estado terminal e não permite novas transições.");
    }
}

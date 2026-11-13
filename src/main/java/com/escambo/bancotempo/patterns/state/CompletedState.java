package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.exception.InvalidProposalStateException;
import com.escambo.bancotempo.model.ProposalStatus;

public class CompletedState implements ProposalState {

    @Override
    public ProposalStatus getStatus() {
        return ProposalStatus.COMPLETED;
    }

    @Override
    public ProposalState accept(ProposalContext context) {
        throw new InvalidProposalStateException("Proposta já concluída não pode ser alterada.");
    }

    @Override
    public ProposalState start(ProposalContext context) {
        throw new InvalidProposalStateException("Proposta já concluída não pode ser alterada.");
    }

    @Override
    public ProposalState complete(ProposalContext context) {
        throw new InvalidProposalStateException("Proposta já se encontra concluída.");
    }

    @Override
    public ProposalState cancel(ProposalContext context) {
        throw new InvalidProposalStateException("Proposta concluída não pode ser cancelada.");
    }

    @Override
    public ProposalState transitionTo(ProposalStatus targetStatus, ProposalContext context) {
        throw new InvalidProposalStateException("Proposta concluída é um estado terminal e não permite novas transições.");
    }
}

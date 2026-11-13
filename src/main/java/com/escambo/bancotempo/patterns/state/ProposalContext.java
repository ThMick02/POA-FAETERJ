package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.model.ExchangeProposal;
import com.escambo.bancotempo.model.ProposalStatus;

public class ProposalContext {

    private final ExchangeProposal proposal;
    private ProposalState currentState;

    public ProposalContext(ExchangeProposal proposal) {
        this.proposal = proposal;
        this.currentState = ProposalStateFactory.getState(proposal.getStatus());
    }

    public ProposalContext(ExchangeProposal proposal, ProposalState initialState) {
        this.proposal = proposal;
        this.currentState = initialState;
        this.proposal.setStatus(initialState.getStatus());
    }

    public ProposalState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ProposalState newState) {
        this.currentState = newState;
        this.proposal.setStatus(newState.getStatus());
    }

    public ExchangeProposal getProposal() {
        return proposal;
    }

    public void accept() {
        ProposalState next = currentState.accept(this);
        setCurrentState(next);
    }

    public void start() {
        ProposalState next = currentState.start(this);
        setCurrentState(next);
    }

    public void complete() {
        ProposalState next = currentState.complete(this);
        setCurrentState(next);
    }

    public void cancel() {
        ProposalState next = currentState.cancel(this);
        setCurrentState(next);
    }

    public void transitionTo(ProposalStatus targetStatus) {
        ProposalState next = currentState.transitionTo(targetStatus, this);
        setCurrentState(next);
    }
}

package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.model.ProposalStatus;

public interface ProposalState {
    ProposalStatus getStatus();
    ProposalState accept(ProposalContext context);
    ProposalState start(ProposalContext context);
    ProposalState complete(ProposalContext context);
    ProposalState cancel(ProposalContext context);
    ProposalState transitionTo(ProposalStatus targetStatus, ProposalContext context);
}

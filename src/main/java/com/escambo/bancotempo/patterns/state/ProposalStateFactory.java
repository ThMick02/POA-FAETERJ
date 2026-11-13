package com.escambo.bancotempo.patterns.state;

import com.escambo.bancotempo.model.ProposalStatus;

public final class ProposalStateFactory {

    private ProposalStateFactory() {
    }

    public static ProposalState getState(ProposalStatus status) {
        if (status == null) {
            return new PendingState();
        }
        return switch (status) {
            case PENDING -> new PendingState();
            case ACCEPTED -> new AcceptedState();
            case IN_PROGRESS -> new InProgressState();
            case COMPLETED -> new CompletedState();
            case CANCELLED -> new CancelledState();
        };
    }
}

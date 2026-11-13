package com.escambo.bancotempo.dto.proposal;

import com.escambo.bancotempo.model.ProposalStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionProposalRequest(
    @NotNull(message = "O estado de destino (targetStatus) é obrigatório")
    ProposalStatus targetStatus
) {}

package com.escambo.bancotempo.dto.proposal;

import com.escambo.bancotempo.model.ExchangeProposal;
import com.escambo.bancotempo.model.ProposalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProposalResponse(
    UUID id,
    UUID requesterId,
    String requesterName,
    UUID providerId,
    String providerName,
    UUID requestedSkillId,
    String requestedSkillTitle,
    UUID offeredSkillId,
    String offeredSkillTitle,
    BigDecimal hoursAgreed,
    ProposalStatus status,
    LocalDateTime scheduledAt,
    String notes,
    boolean directExchange,
    LocalDateTime createdAt
) {
    public static ProposalResponse fromEntity(ExchangeProposal proposal) {
        return new ProposalResponse(
                proposal.getId(),
                proposal.getRequester().getId(),
                proposal.getRequester().getName(),
                proposal.getProvider().getId(),
                proposal.getProvider().getName(),
                proposal.getRequestedSkill().getId(),
                proposal.getRequestedSkill().getTitle(),
                proposal.getOfferedSkill() != null ? proposal.getOfferedSkill().getId() : null,
                proposal.getOfferedSkill() != null ? proposal.getOfferedSkill().getTitle() : null,
                proposal.getHoursAgreed(),
                proposal.getStatus(),
                proposal.getScheduledAt(),
                proposal.getNotes(),
                proposal.isDirectExchange(),
                proposal.getCreatedAt()
        );
    }
}

package com.escambo.bancotempo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "exchange_proposals")
public class ExchangeProposal {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_skill_id")
    private SkillOffer offeredSkill;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_skill_id", nullable = false)
    private SkillOffer requestedSkill;

    @Column(name = "hours_agreed", nullable = false, precision = 10, scale = 2)
    private BigDecimal hoursAgreed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ProposalStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ExchangeProposal() {
    }

    public ExchangeProposal(UUID id, User requester, User provider, SkillOffer offeredSkill,
                            SkillOffer requestedSkill, BigDecimal hoursAgreed, LocalDateTime scheduledAt, String notes) {
        this.id = id != null ? id : UUID.randomUUID();
        this.requester = requester;
        this.provider = provider;
        this.offeredSkill = offeredSkill;
        this.requestedSkill = requestedSkill;
        this.hoursAgreed = hoursAgreed;
        this.status = ProposalStatus.PENDING;
        this.scheduledAt = scheduledAt;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.status == null) {
            this.status = ProposalStatus.PENDING;
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isDirectExchange() {
        return this.offeredSkill != null;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getProvider() {
        return provider;
    }

    public void setProvider(User provider) {
        this.provider = provider;
    }

    public SkillOffer getOfferedSkill() {
        return offeredSkill;
    }

    public void setOfferedSkill(SkillOffer offeredSkill) {
        this.offeredSkill = offeredSkill;
    }

    public SkillOffer getRequestedSkill() {
        return requestedSkill;
    }

    public void setRequestedSkill(SkillOffer requestedSkill) {
        this.requestedSkill = requestedSkill;
    }

    public BigDecimal getHoursAgreed() {
        return hoursAgreed;
    }

    public void setHoursAgreed(BigDecimal hoursAgreed) {
        this.hoursAgreed = hoursAgreed;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeProposal that = (ExchangeProposal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

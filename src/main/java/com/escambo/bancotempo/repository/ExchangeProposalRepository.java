package com.escambo.bancotempo.repository;

import com.escambo.bancotempo.model.ExchangeProposal;
import com.escambo.bancotempo.model.ProposalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExchangeProposalRepository extends JpaRepository<ExchangeProposal, UUID> {

    @Query("SELECT p FROM ExchangeProposal p WHERE p.requester.id = :userId OR p.provider.id = :userId ORDER BY p.createdAt DESC")
    Page<ExchangeProposal> findAllByUser(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM ExchangeProposal p WHERE (p.requester.id = :userId OR p.provider.id = :userId) AND p.status = :status ORDER BY p.createdAt DESC")
    Page<ExchangeProposal> findAllByUserAndStatus(@Param("userId") UUID userId, @Param("status") ProposalStatus status, Pageable pageable);

    List<ExchangeProposal> findByRequesterIdOrProviderId(UUID requesterId, UUID providerId);
}

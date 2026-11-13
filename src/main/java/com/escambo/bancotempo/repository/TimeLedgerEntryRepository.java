package com.escambo.bancotempo.repository;

import com.escambo.bancotempo.model.TimeLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimeLedgerEntryRepository extends JpaRepository<TimeLedgerEntry, UUID> {
    List<TimeLedgerEntry> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<TimeLedgerEntry> findByProposalId(UUID proposalId);
}

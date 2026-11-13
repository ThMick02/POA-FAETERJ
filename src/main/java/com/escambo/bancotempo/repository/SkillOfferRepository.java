package com.escambo.bancotempo.repository;

import com.escambo.bancotempo.model.SkillCategory;
import com.escambo.bancotempo.model.SkillOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SkillOfferRepository extends JpaRepository<SkillOffer, UUID> {
    Page<SkillOffer> findByActiveTrue(Pageable pageable);
    Page<SkillOffer> findByCategoryAndActiveTrue(SkillCategory category, Pageable pageable);
    Page<SkillOffer> findByTitleContainingIgnoreCaseAndActiveTrue(String title, Pageable pageable);
    Page<SkillOffer> findByCategoryAndTitleContainingIgnoreCaseAndActiveTrue(SkillCategory category, String title, Pageable pageable);
    List<SkillOffer> findByProviderIdAndActiveTrue(UUID providerId);
}

package com.escambo.bancotempo.service;

import com.escambo.bancotempo.dto.skill.CreateSkillRequest;
import com.escambo.bancotempo.dto.skill.SkillResponse;
import com.escambo.bancotempo.exception.ResourceNotFoundException;
import com.escambo.bancotempo.model.SkillCategory;
import com.escambo.bancotempo.model.SkillOffer;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.repository.SkillOfferRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SkillOfferService {

    private final SkillOfferRepository skillOfferRepository;
    private final UserRepository userRepository;

    public SkillOfferService(SkillOfferRepository skillOfferRepository, UserRepository userRepository) {
        this.skillOfferRepository = skillOfferRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SkillResponse createSkill(CreateSkillRequest request, UUID currentUserId) {
        User provider = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + currentUserId));

        SkillOffer skillOffer = new SkillOffer(
                UUID.randomUUID(),
                provider,
                request.title().trim(),
                request.description().trim(),
                request.category(),
                request.hourlyRateFactor()
        );

        SkillOffer saved = skillOfferRepository.save(skillOffer);
        return SkillResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<SkillResponse> getSkills(SkillCategory category, String search, Pageable pageable) {
        Page<SkillOffer> skills;

        if (category != null && search != null && !search.isBlank()) {
            skills = skillOfferRepository.findByCategoryAndTitleContainingIgnoreCaseAndActiveTrue(category, search.trim(), pageable);
        } else if (category != null) {
            skills = skillOfferRepository.findByCategoryAndActiveTrue(category, pageable);
        } else if (search != null && !search.isBlank()) {
            skills = skillOfferRepository.findByTitleContainingIgnoreCaseAndActiveTrue(search.trim(), pageable);
        } else {
            skills = skillOfferRepository.findByActiveTrue(pageable);
        }

        return skills.map(SkillResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public SkillResponse getSkillById(UUID skillId) {
        SkillOffer skill = skillOfferRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta de habilidade não encontrada: " + skillId));
        return SkillResponse.fromEntity(skill);
    }
}

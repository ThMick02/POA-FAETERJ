package com.escambo.bancotempo.service;

import com.escambo.bancotempo.dto.user.UserProfileResponse;
import com.escambo.bancotempo.exception.ResourceNotFoundException;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + userId));
        return UserProfileResponse.fromEntity(user);
    }
}

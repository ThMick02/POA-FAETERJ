package com.escambo.bancotempo.service;

import com.escambo.bancotempo.config.JwtTokenProvider;
import com.escambo.bancotempo.dto.auth.AuthResponse;
import com.escambo.bancotempo.dto.auth.LoginRequest;
import com.escambo.bancotempo.dto.auth.RegisterRequest;
import com.escambo.bancotempo.exception.EmailAlreadyExistsException;
import com.escambo.bancotempo.model.TimeLedgerEntry;
import com.escambo.bancotempo.model.TimeLedgerType;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.repository.TimeLedgerEntryRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AuthService {

    private static final BigDecimal INITIAL_TIME_BALANCE = new BigDecimal("2.00");

    private final UserRepository userRepository;
    private final TimeLedgerEntryRepository timeLedgerEntryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       TimeLedgerEntryRepository timeLedgerEntryRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.timeLedgerEntryRepository = timeLedgerEntryRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase().trim())) {
            throw new EmailAlreadyExistsException("O email informado já está em uso na plataforma: " + request.email());
        }

        User user = new User(
                UUID.randomUUID(),
                request.name().trim(),
                request.email().toLowerCase().trim(),
                passwordEncoder.encode(request.password()),
                INITIAL_TIME_BALANCE,
                5.0
        );

        User savedUser = userRepository.save(user);

        // Registro de auditoria do bônus de boas-vindas de Horas-Crédito
        TimeLedgerEntry initialBonus = new TimeLedgerEntry(
                UUID.randomUUID(),
                savedUser,
                null,
                INITIAL_TIME_BALANCE,
                TimeLedgerType.INITIAL_BONUS,
                "Bônus de boas-vindas para fomento das primeiras trocas de serviços"
        );
        timeLedgerEntryRepository.save(initialBonus);

        String token = tokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getTimeBalance(),
                savedUser.getReputationScore()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Email ou senha inválidos."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Email ou senha inválidos.");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTimeBalance(),
                user.getReputationScore()
        );
    }
}

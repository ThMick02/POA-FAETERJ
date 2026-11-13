package com.escambo.bancotempo.service;

import com.escambo.bancotempo.config.JwtTokenProvider;
import com.escambo.bancotempo.dto.auth.AuthResponse;
import com.escambo.bancotempo.dto.auth.LoginRequest;
import com.escambo.bancotempo.dto.auth.RegisterRequest;
import com.escambo.bancotempo.exception.EmailAlreadyExistsException;
import com.escambo.bancotempo.model.TimeLedgerEntry;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.repository.TimeLedgerEntryRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários do AuthService (Cadastro, Bônus Inicial de 2.00 Horas e Login)")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TimeLedgerEntryRepository timeLedgerEntryRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(
                "9a678bc561876543210abcdef9876543210abcdef9876543210abcdef98765432",
                3600000
        );
        authService = new AuthService(userRepository, timeLedgerEntryRepository, passwordEncoder, tokenProvider);
    }

    @Test
    @DisplayName("Deve registrar novo usuário com saldo inicial de 2.00 horas e gerar token JWT válido")
    void testRegisterNewUserSuccess() {
        RegisterRequest request = new RegisterRequest("Carlos Teste", "carlos@test.com", "senha123");

        when(userRepository.existsByEmail("carlos@test.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("carlos@test.com", response.email());
        assertEquals(new BigDecimal("2.00"), response.timeBalance());
        assertNotNull(response.token());
        assertTrue(tokenProvider.validateToken(response.token()));
        assertEquals("carlos@test.com", tokenProvider.getEmailFromToken(response.token()));

        verify(userRepository).save(any(User.class));
        verify(timeLedgerEntryRepository).save(any(TimeLedgerEntry.class));
    }

    @Test
    @DisplayName("Deve proibir cadastro com email já existente")
    void testRegisterWithDuplicateEmailFails() {
        RegisterRequest request = new RegisterRequest("Carlos", "existente@test.com", "senha123");

        when(userRepository.existsByEmail("existente@test.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
        verify(timeLedgerEntryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve autenticar com sucesso quando as credenciais estiverem corretas")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("usuario@test.com", "senhaCorreta");
        User user = new User(UUID.randomUUID(), "Usuario", "usuario@test.com", "encodedHash", new BigDecimal("5.00"), 5.0);

        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaCorreta", "encodedHash")).thenReturn(true);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.token());
        assertTrue(tokenProvider.validateToken(response.token()));
        assertEquals(user.getEmail(), response.email());
    }

    @Test
    @DisplayName("Deve falhar autenticação quando a senha for incorreta")
    void testLoginFailsWithWrongPassword() {
        LoginRequest request = new LoginRequest("usuario@test.com", "senhaIncorreta");
        User user = new User(UUID.randomUUID(), "Usuario", "usuario@test.com", "encodedHash", new BigDecimal("5.00"), 5.0);

        when(userRepository.findByEmail("usuario@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senhaIncorreta", "encodedHash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}

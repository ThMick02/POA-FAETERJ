package com.escambo.bancotempo.exception;

import com.escambo.bancotempo.dto.common.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Testes do GlobalExceptionHandler - Tratamento de Erros e Códigos HTTP")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar status HTTP 422 (UNPROCESSABLE_ENTITY) para InvalidProposalStateException")
    void testInvalidProposalStateReturnsHttp422() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/proposals/test-id/transition");

        InvalidProposalStateException ex = new InvalidProposalStateException("Não é possível concluir uma proposta pendente.");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidProposalState(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(422, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Não é possível concluir uma proposta pendente.", response.getBody().message());
        assertEquals("/api/v1/proposals/test-id/transition", response.getBody().path());
    }

    @Test
    @DisplayName("Deve retornar status HTTP 422 (UNPROCESSABLE_ENTITY) para InsufficientBalanceException")
    void testInsufficientBalanceReturnsHttp422() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/proposals");

        InsufficientBalanceException ex = new InsufficientBalanceException("Saldo insuficiente para garantia.");

        ResponseEntity<ErrorResponse> response = handler.handleInsufficientBalance(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals(422, response.getStatusCode().value());
    }

    @Test
    @DisplayName("Deve retornar status HTTP 404 (NOT_FOUND) para ResourceNotFoundException")
    void testResourceNotFoundReturnsHttp404() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/skills/123");

        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso não encontrado.");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    @DisplayName("Deve retornar status HTTP 403 (FORBIDDEN) para UnauthorizedActionException")
    void testUnauthorizedActionReturnsHttp403() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/proposals/123/transition");

        UnauthorizedActionException ex = new UnauthorizedActionException("Ação não permitida.");

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedAction(ex, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getStatusCode().value());
    }
}

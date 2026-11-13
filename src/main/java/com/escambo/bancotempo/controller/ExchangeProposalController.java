package com.escambo.bancotempo.controller;

import com.escambo.bancotempo.dto.common.ErrorResponse;
import com.escambo.bancotempo.dto.proposal.CreateProposalRequest;
import com.escambo.bancotempo.dto.proposal.ProposalResponse;
import com.escambo.bancotempo.dto.proposal.TransitionProposalRequest;
import com.escambo.bancotempo.model.ProposalStatus;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.service.ExchangeProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/proposals")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Propostas de Escambo", description = "Endpoints para criação e gerenciamento do ciclo de vida de propostas (Padrões State, Strategy e Observer)")
public class ExchangeProposalController {

    private final ExchangeProposalService proposalService;

    public ExchangeProposalController(ExchangeProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @PostMapping
    @Operation(summary = "Criar proposta de escambo", description = "Cria uma nova proposta de escambo aplicando o padrão Strategy para cálculo de horas e bloqueio de saldo em garantia (Escrow).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Proposta criada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProposalResponse.class))),
            @ApiResponse(responseCode = "400", description = "Regra de negócio violada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Habilidade ou usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Saldo de horas insuficiente para o Escrow",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProposalResponse> createProposal(
            @Valid @RequestBody CreateProposalRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        ProposalResponse response = proposalService.createProposal(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/transition")
    @Operation(summary = "Transicionar status da proposta (Padrão State)", description = "Executa transição de estado da proposta (PENDING -> ACCEPTED -> IN_PROGRESS -> COMPLETED ou CANCELLED) e notifica Observers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transição executada com sucesso",
                    content = @Content(schema = @Schema(implementation = ProposalResponse.class))),
            @ApiResponse(responseCode = "403", description = "Ação não permitida para o seu usuário",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Transição inválida para o estado atual da proposta",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProposalResponse> transitionProposal(
            @PathVariable UUID id,
            @Valid @RequestBody TransitionProposalRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        ProposalResponse response = proposalService.transitionProposal(id, request.targetStatus(), currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar minhas propostas", description = "Lista as propostas de escambo onde o usuário autenticado é solicitante ou prestador.")
    public ResponseEntity<Page<ProposalResponse>> getMyProposals(
            @RequestParam(required = false) ProposalStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        Page<ProposalResponse> proposals = proposalService.getMyProposals(currentUser.getId(), status, pageable);
        return ResponseEntity.ok(proposals);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar proposta por ID", description = "Retorna os detalhes completos de uma proposta de escambo caso o usuário participe dela.")
    public ResponseEntity<ProposalResponse> getProposalById(
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        ProposalResponse proposal = proposalService.getProposalById(id, currentUser.getId());
        return ResponseEntity.ok(proposal);
    }
}

package com.escambo.bancotempo.controller;

import com.escambo.bancotempo.dto.common.ErrorResponse;
import com.escambo.bancotempo.dto.skill.CreateSkillRequest;
import com.escambo.bancotempo.dto.skill.SkillResponse;
import com.escambo.bancotempo.model.SkillCategory;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.service.SkillOfferService;
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
@RequestMapping("/api/v1/skills")
@Tag(name = "Ofertas de Habilidades", description = "Endpoints para catálogo e publicação de habilidades para escambo")
public class SkillOfferController {

    private final SkillOfferService skillOfferService;

    public SkillOfferController(SkillOfferService skillOfferService) {
        this.skillOfferService = skillOfferService;
    }

    @GetMapping
    @Operation(summary = "Listar ofertas de habilidades", description = "Retorna lista paginada de ofertas ativas com filtros opcionais por categoria e termo de busca.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<Page<SkillResponse>> listSkills(
            @RequestParam(required = false) SkillCategory category,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SkillResponse> skills = skillOfferService.getSkills(category, search, pageable);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar oferta de habilidade por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Oferta encontrada",
                    content = @Content(schema = @Schema(implementation = SkillResponse.class))),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable UUID id) {
        SkillResponse skill = skillOfferService.getSkillById(id);
        return ResponseEntity.ok(skill);
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Criar nova oferta de serviço/habilidade", description = "Publica uma nova habilidade ofertada pelo usuário autenticado na plataforma.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Habilidade publicada com sucesso",
                    content = @Content(schema = @Schema(implementation = SkillResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<SkillResponse> createSkill(
            @Valid @RequestBody CreateSkillRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        SkillResponse response = skillOfferService.createSkill(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

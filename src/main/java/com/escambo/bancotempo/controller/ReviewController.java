package com.escambo.bancotempo.controller;

import com.escambo.bancotempo.dto.common.ErrorResponse;
import com.escambo.bancotempo.dto.review.CreateReviewRequest;
import com.escambo.bancotempo.dto.review.ReviewResponse;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Avaliações e Reputação", description = "Endpoints para submissão e consulta de avaliações após conclusão de trocas")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/proposals/{id}/reviews")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Avaliar serviço pós-conclusão", description = "Registra nota (1 a 5) e comentário após a conclusão da proposta, disparando o Observer de recálculo da reputação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Avaliação registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
            @ApiResponse(responseCode = "400", description = "Proposta não concluída ou avaliação duplicada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Apenas participantes diretos podem avaliar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Proposta não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable UUID id,
            @Valid @RequestBody CreateReviewRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        ReviewResponse response = reviewService.createReview(id, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/users/{userId}/reviews")
    @Operation(summary = "Listar avaliações recebidas por um usuário", description = "Retorna histórico de notas e comentários recebidos pelo usuário informado.")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(@PathVariable UUID userId) {
        List<ReviewResponse> reviews = reviewService.getReviewsForUser(userId);
        return ResponseEntity.ok(reviews);
    }
}

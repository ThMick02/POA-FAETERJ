package com.escambo.bancotempo;

import com.escambo.bancotempo.dto.auth.LoginRequest;
import com.escambo.bancotempo.dto.auth.RegisterRequest;
import com.escambo.bancotempo.dto.proposal.CreateProposalRequest;
import com.escambo.bancotempo.dto.proposal.TransitionProposalRequest;
import com.escambo.bancotempo.dto.review.CreateReviewRequest;
import com.escambo.bancotempo.dto.skill.CreateSkillRequest;
import com.escambo.bancotempo.model.CalculationStrategyType;
import com.escambo.bancotempo.model.ProposalStatus;
import com.escambo.bancotempo.model.SkillCategory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração End-to-End da API RESTful de Escambo")
class EscamboIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Fluxo completo: Registro -> Oferta -> Proposta -> Transições State -> Conclusão -> Review")
    void testCompleteEscamboLifecycle() throws Exception {
        // 1. Cadastrar Usuário A (Prestador de TI)
        RegisterRequest registerUserA = new RegisterRequest("Alice TI", "alice.e2e@test.com", "senha123");
        MvcResult resUserA = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.timeBalance").value(2.00))
                .andReturn();
        String tokenA = extractToken(resUserA);

        // 2. Cadastrar Usuário B (Solicitante)
        RegisterRequest registerUserB = new RegisterRequest("Bob Solicitante", "bob.e2e@test.com", "senha123");
        MvcResult resUserB = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUserB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.timeBalance").value(2.00))
                .andReturn();
        String tokenB = extractToken(resUserB);

        // 3. Usuário A cria oferta de mentoria em tecnologia (fator 1.00)
        CreateSkillRequest createSkill = new CreateSkillRequest(
                "Mentoria Java 21 & Spring Boot",
                "Desenvolvimento moderno de microsserviços",
                SkillCategory.TECH,
                new BigDecimal("1.00")
        );
        MvcResult resSkill = mockMvc.perform(post("/api/v1/skills")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSkill)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();
        UUID skillId = UUID.fromString(extractJsonField(resSkill, "id"));

        // 4. Listagem pública de habilidades (sem autenticação)
        mockMvc.perform(get("/api/v1/skills")
                        .param("category", "TECH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        // 5. Usuário B cria proposta de escambo de 1.00 hora (usando seu saldo inicial de 2.00 horas)
        CreateProposalRequest createProposal = new CreateProposalRequest(
                skillId,
                null,
                new BigDecimal("1.00"),
                CalculationStrategyType.EQUAL_TIME,
                LocalDateTime.now().plusDays(2),
                "Gostaria de uma aula sobre Spring Boot 3"
        );
        MvcResult resProposal = mockMvc.perform(post("/api/v1/proposals")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createProposal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.hoursAgreed").value(1.00))
                .andReturn();
        UUID proposalId = UUID.fromString(extractJsonField(resProposal, "id"));

        // 6. Verificar que transição ilegal (ex: tentar concluir proposta ainda PENDING) retorna HTTP 422 (State pattern)
        TransitionProposalRequest illegalTransition = new TransitionProposalRequest(ProposalStatus.COMPLETED);
        mockMvc.perform(patch("/api/v1/proposals/" + proposalId + "/transition")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(illegalTransition)))
                .andExpect(status().isUnprocessableEntity()) // HTTP 422
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Não é possível concluir uma proposta que ainda está pendente")));

        // 7. Prestador A aceita a proposta (PENDING -> ACCEPTED)
        TransitionProposalRequest acceptReq = new TransitionProposalRequest(ProposalStatus.ACCEPTED);
        mockMvc.perform(patch("/api/v1/proposals/" + proposalId + "/transition")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // 8. Iniciar o serviço (ACCEPTED -> IN_PROGRESS)
        TransitionProposalRequest startReq = new TransitionProposalRequest(ProposalStatus.IN_PROGRESS);
        mockMvc.perform(patch("/api/v1/proposals/" + proposalId + "/transition")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 9. Concluir o serviço (IN_PROGRESS -> COMPLETED)
        TransitionProposalRequest completeReq = new TransitionProposalRequest(ProposalStatus.COMPLETED);
        mockMvc.perform(patch("/api/v1/proposals/" + proposalId + "/transition")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 10. Submeter avaliação pós-conclusão (Review com nota 5)
        CreateReviewRequest reviewReq = new CreateReviewRequest(5, "Mentoria sensacional, recomendo!");
        mockMvc.perform(post("/api/v1/proposals/" + proposalId + "/reviews")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Mentoria sensacional, recomendo!"));

        // 11. Consultar perfil do Prestador A para verificar reputação e saldo de horas
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeBalance").value(3.00)); // 2.00 inicial + 1.00 recebido da mentoria
    }

    private String extractToken(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String extractJsonField(MvcResult result, String field) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get(field).asText();
    }
}

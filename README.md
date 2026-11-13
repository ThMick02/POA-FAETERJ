# Plataforma de Escambo de Serviços e Habilidades (Banco de Tempo)

API RESTful completa desenvolvida em **Java 21** e **Spring Boot 3.3.5** para gerenciamento de trocas de serviços e habilidades técnicas ou manuais baseadas no modelo de **Banco de Tempo** (Horas-Crédito).

---

## 🛠️ Stack Tecnológica

* **Linguagem:** Java 21 (LTS) — Uso extensivo de `record` para DTOs e eventos.
* **Framework Principal:** Spring Boot 3.3.5.
* **Módulos Spring:** Spring Web, Spring Data JPA, Spring Security, Spring Validation.
* **Segurança:** Autenticação Stateless via **JWT (JSON Web Token)** utilizando a biblioteca `jjwt 0.12.6`.
* **Banco de Dados:** PostgreSQL 16 (com suporte a H2 in-memory para testes unitários/integrados).
* **Controle de Migrações:** Flyway (`V1__init_schema.sql`).
* **Documentação:** SpringDoc OpenAPI 3 / Swagger UI (`/swagger-ui.html`).
* **Precisão Numérica:** `BigDecimal` com escala fixa de 2 casas decimais e arredondamento `HALF_UP` para saldos e cálculos de tempo.
* **Testes:** JUnit 5, Mockito e Spring MockMvc (37 testes automatizados).

---

## 🏛️ Padrões de Projeto (GoF) Implementados

A aplicação aplica explicitamente 3 padrões comportamentais do GoF no pacote `com.escambo.bancotempo.patterns`:

### 1. Padrão State (`patterns.state`) — Ciclo de Vida da Proposta
Gerencia as transições de status da `ExchangeProposal`:
* **Estados Concretos:**
  * `PendingState` (PENDING)
  * `AcceptedState` (ACCEPTED)
  * `InProgressState` (IN_PROGRESS)
  * `CompletedState` (COMPLETED - Terminal)
  * `CancelledState` (CANCELLED - Terminal)
* **Regras & Exceções:** Transições ilegais (como tentar concluir uma proposta pendente ou alterar uma proposta finalizada) disparam `InvalidProposalStateException`, capturada no `GlobalExceptionHandler` (`@RestControllerAdvice`) retornando **HTTP 422 Unprocessable Entity**.

### 2. Padrão Strategy (`patterns.strategy`) — Motor de Equivalência de Horas
Calcula a quantidade de horas acordadas em uma proposta:
* **Interface:** `ExchangeCalculationStrategy`
* **Estratégias Concretas:**
  * `EqualTimeStrategy`: Equivalência direta 1:1 entre horas de qualquer serviço ou saldo.
  * `ComplexityWeightedStrategy`: Pondera a equivalência utilizando o fator de complexidade (`hourlyRateFactor`) da habilidade solicitada e da ofertada.
* Ambas as estratégias são injetadas dinamicamente via `Map<String, ExchangeCalculationStrategy>` no `ExchangeProposalService`.

### 3. Padrão Observer (`patterns.observer`) — Eventos de Domínio Desacoplados
Eventos disparados via `ApplicationEventPublisher` e capturados com `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`:
* **Eventos (`patterns.observer.events`):**
  * `ProposalCreatedEvent`: Notifica a reserva de horas em escrow.
  * `ProposalAcceptedEvent`: Notifica os participantes sobre o aceite.
  * `ProposalCompletedEvent`: Notifica a efetivação e liberação para avaliações.
  * `ProposalCancelledEvent`: Notifica o cancelamento e estorno.
  * `ReviewCreatedEvent`: Notifica o recebimento de nota.
* **Listeners Concretos (`patterns.observer.listeners`):**
  * `NotificationEventListener`: Disparo assíncrono (`@Async`) simulando notificações aos envolvidos.
  * `ReputationEventListener`: Recalcula automaticamente a pontuação média de reputação (`reputationScore`) do usuário avaliado.
  * `TimeLedgerAuditListener`: Registra trilha de auditoria completa na tabela `time_ledger_entries`.

---

## 💼 Regras de Negócio e Domínio

1. **Saldo Inicial (Horas-Crédito):** Ao se cadastrar, todo usuário recebe automaticamente **`2.00` horas** de bônus de boas-vindas.
2. **Modalidades de Troca:**
   * *Troca Direta:* Usuário A oferta um serviço em contrapartida ao serviço do Usuário B (`offeredSkillId != null`).
   * *Troca via Saldo de Horas:* Usuário A paga o serviço do prestador com seu saldo de horas (`offeredSkillId == null`).
3. **Mecanismo de Escrow (Garantia de Saldo):**
   * Ao criar proposta via saldo de horas, as horas necessárias são imediatamente bloqueadas (`lockBalance`), saindo de `timeBalance` para `blockedBalance`.
   * Se a proposta for **CANCELLED**, o saldo é integralmente estornado ao solicitante.
   * Se a proposta for **COMPLETED**, o saldo em escrow é debitado do solicitante e creditado ao prestador.
4. **Dupla Validação e Avaliação:**
   * Avaliações (notas de 1 a 5 e comentários) só podem ser submetidas após a conclusão (`COMPLETED`) do serviço.
   * Cada participante só pode avaliar a contraparte uma única vez por proposta.

---

## 🚀 Como Executar o Projeto

Você pode rodar o projeto de duas formas:
1. **Modo Rápido / Standalone (Sem Docker nem PostgreSQL instalado):** Utiliza banco embarcado persistente H2 pré-configurado.
2. **Modo Produção / Completo (Com Docker e PostgreSQL):** Utiliza o container do PostgreSQL com migrações Flyway.

---

### Opção 1: Rodar sem Docker (Recomendado se não tiver o Docker instalado)

Basta definir o `JAVA_HOME` e executar com o perfil `dev`:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **H2 Console:** `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/escambodb`, Usuário: `sa`, Senha em branco)
* Os dados de demonstração (usuários `alice@bancotempo.com`, `bob@bancotempo.com` e ofertas) já são carregados automaticamente!

---

### Opção 2: Rodar com Docker e PostgreSQL

Caso deseje utilizar o PostgreSQL via Docker:

1. Instale o [Docker Desktop para Windows](https://www.docker.com/products/docker-desktop/).
2. Inicie o container do PostgreSQL:
```powershell
docker compose up -d
```
3. Inicie a aplicação com o perfil padrão:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
.\mvnw.cmd spring-boot:run
```

---

### Executar a Suíte de Testes Automatizados

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
.\mvnw.cmd test
```

### 4. Acessar o Swagger UI (Documentação Interativa)
Abra no navegador:
* Swagger UI: `http://localhost:8080/swagger-ui.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 📡 Endpoints da API (v1)

### 🔐 Autenticação (`/api/v1/auth`)
* `POST /api/v1/auth/register` — Cadastra novo usuário (recebe 2.00 horas iniciais).
* `POST /api/v1/auth/login` — Autentica e retorna token JWT Bearer.

### 🛠️ Ofertas de Habilidades (`/api/v1/skills`)
* `GET /api/v1/skills` — Listagem paginada (filtros: `category`, `search`, `page`, `size`, `sort`).
* `GET /api/v1/skills/{id}` — Detalhes de uma habilidade específica.
* `POST /api/v1/skills` — Cadastra nova habilidade ofertada pelo usuário autenticado.

### 🤝 Propostas de Escambo (`/api/v1/proposals`)
* `POST /api/v1/proposals` — Cria proposta (aplica Strategy e bloqueio em Escrow).
* `PATCH /api/v1/proposals/{id}/transition` — Altera estado da proposta (aplica State e dispara Observers). Retorna 422 em transição inválida.
* `GET /api/v1/proposals` — Lista propostas do usuário autenticado (filtro opcional por `status`).
* `GET /api/v1/proposals/{id}` — Detalhes da proposta.

### ⭐ Avaliações (`/api/v1`)
* `POST /api/v1/proposals/{id}/reviews` — Avaliação pós-conclusão (notas 1 a 5).
* `GET /api/v1/users/{userId}/reviews` — Histórico de avaliações recebidas por um usuário.

### 👤 Perfil do Usuário (`/api/v1/users`)
* `GET /api/v1/users/me` — Consulta saldo disponível, saldo bloqueado e score de reputação do usuário autenticado.
* `GET /api/v1/users/{id}` — Consulta perfil público.

---

## 👥 Usuários Pré-Cadastrados para Teste (Seed Dev)
Ao iniciar com banco vazio, são gerados automaticamente:
* `alice@bancotempo.com` / senha: `senha123` (Mentoria em Java, Reparo de PCs)
* `bob@bancotempo.com` / senha: `senha123` (Design UI/UX Figma)
* `carlos@bancotempo.com` / senha: `senha123` (Inglês para Conversação)

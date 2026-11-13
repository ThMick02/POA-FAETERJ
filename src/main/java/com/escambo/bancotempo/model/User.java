package com.escambo.bancotempo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "time_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal timeBalance;

    @Column(name = "blocked_balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal blockedBalance;

    @Column(name = "reputation_score", nullable = false)
    private Double reputationScore;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(UUID id, String name, String email, String passwordHash, BigDecimal timeBalance, Double reputationScore) {
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.timeBalance = timeBalance != null ? timeBalance : new BigDecimal("2.00");
        this.blockedBalance = BigDecimal.ZERO.setScale(2);
        this.reputationScore = reputationScore != null ? reputationScore : 5.0;
        this.role = "ROLE_USER";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (this.timeBalance == null) {
            this.timeBalance = new BigDecimal("2.00");
        }
        if (this.blockedBalance == null) {
            this.blockedBalance = BigDecimal.ZERO.setScale(2);
        }
        if (this.reputationScore == null) {
            this.reputationScore = 5.0;
        }
        if (this.role == null) {
            this.role = "ROLE_USER";
        }
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Business Methods for Escrow and Balance Management
    public boolean hasAvailableBalance(BigDecimal amount) {
        return this.timeBalance != null && this.timeBalance.compareTo(amount) >= 0;
    }

    public void lockBalance(BigDecimal amount) {
        if (!hasAvailableBalance(amount)) {
            throw new IllegalStateException("Saldo de horas insuficiente para reserva.");
        }
        this.timeBalance = this.timeBalance.subtract(amount);
        this.blockedBalance = this.blockedBalance.add(amount);
    }

    public void refundBalance(BigDecimal amount) {
        if (this.blockedBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo bloqueado insuficiente para estorno.");
        }
        this.blockedBalance = this.blockedBalance.subtract(amount);
        this.timeBalance = this.timeBalance.add(amount);
    }

    public void debitBlockedBalance(BigDecimal amount) {
        if (this.blockedBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo bloqueado insuficiente para débito.");
        }
        this.blockedBalance = this.blockedBalance.subtract(amount);
    }

    public void creditBalance(BigDecimal amount) {
        this.timeBalance = this.timeBalance.add(amount);
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public BigDecimal getTimeBalance() {
        return timeBalance;
    }

    public void setTimeBalance(BigDecimal timeBalance) {
        this.timeBalance = timeBalance;
    }

    public BigDecimal getBlockedBalance() {
        return blockedBalance;
    }

    public void setBlockedBalance(BigDecimal blockedBalance) {
        this.blockedBalance = blockedBalance;
    }

    public Double getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(Double reputationScore) {
        this.reputationScore = reputationScore;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

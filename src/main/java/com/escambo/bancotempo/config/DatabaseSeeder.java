package com.escambo.bancotempo.config;

import com.escambo.bancotempo.model.SkillCategory;
import com.escambo.bancotempo.model.SkillOffer;
import com.escambo.bancotempo.model.User;
import com.escambo.bancotempo.repository.SkillOfferRepository;
import com.escambo.bancotempo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("!test")
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository,
                          SkillOfferRepository skillOfferRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.skillOfferRepository = skillOfferRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Inicializando dados de demonstração no banco de dados...");

        User alice = new User(
                UUID.randomUUID(),
                "Alice Silva",
                "alice@bancotempo.com",
                passwordEncoder.encode("senha123"),
                new BigDecimal("10.00"),
                5.0
        );

        User bob = new User(
                UUID.randomUUID(),
                "Bob Santos",
                "bob@bancotempo.com",
                passwordEncoder.encode("senha123"),
                new BigDecimal("8.00"),
                4.8
        );

        User carlos = new User(
                UUID.randomUUID(),
                "Carlos Oliveira",
                "carlos@bancotempo.com",
                passwordEncoder.encode("senha123"),
                new BigDecimal("5.00"),
                4.9
        );

        userRepository.save(alice);
        userRepository.save(bob);
        userRepository.save(carlos);

        SkillOffer skill1 = new SkillOffer(
                UUID.randomUUID(),
                alice,
                "Mentoria em Java & Spring Boot",
                "Aulas práticas de desenvolvimento de microsserviços e APIs REST com Java e Spring Boot.",
                SkillCategory.TECH,
                new BigDecimal("1.50")
        );

        SkillOffer skill2 = new SkillOffer(
                UUID.randomUUID(),
                bob,
                "Design de Interfaces (UI/UX no Figma)",
                "Criação de protótipos de alta fidelidade e design system no Figma para web e mobile.",
                SkillCategory.DESIGN,
                new BigDecimal("1.00")
        );

        SkillOffer skill3 = new SkillOffer(
                UUID.randomUUID(),
                carlos,
                "Aulas de Inglês para Conversação",
                "Conversação voltada para o mercado de tecnologia e entrevistas de emprego.",
                SkillCategory.EDUCATION,
                new BigDecimal("1.00")
        );

        SkillOffer skill4 = new SkillOffer(
                UUID.randomUUID(),
                alice,
                "Reparo e Manutenção de Computadores",
                "Diagnóstico e manutenção preventiva de hardware e formatação.",
                SkillCategory.HOME_REPAIR,
                new BigDecimal("1.20")
        );

        skillOfferRepository.save(skill1);
        skillOfferRepository.save(skill2);
        skillOfferRepository.save(skill3);
        skillOfferRepository.save(skill4);

        log.info("Dados de demonstração criados com sucesso! Usuários de teste: alice@bancotempo.com, bob@bancotempo.com, carlos@bancotempo.com (senha: senha123)");
    }
}

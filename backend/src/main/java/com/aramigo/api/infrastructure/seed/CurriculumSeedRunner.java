package com.aramigo.api.infrastructure.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CurriculumSeedRunner {

  @Bean
  CommandLineRunner syncCurriculum(CurriculumDataSeeder seeder) {
    return args -> seeder.seed();
  }
}

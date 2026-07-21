package com.example.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing		// com.example/StreakApplication.java에 있던것을 옮김
//Test단계에서 JPA metamodel must not be empty 에러 발생으로 인한 분리
//
public class JpaAuditingConfig {
}

package com.vms.vendor_management_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Persistence-related configuration shared across the application.
 * <p>
 * Enables JPA auditing so entities annotated with {@code @CreatedDate} and {@code @LastModifiedDate}
 * receive automatic timestamp management. An {@link AuditorAware} bean is also provided so the setup
 * is ready for future {@code @CreatedBy}/{@code @LastModifiedBy} usage once security is in place.
 */
@Configuration
@EnableJpaAuditing
public class PersistenceConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // TODO: Replace with authenticated user context once security layer is implemented
        return () -> Optional.of("system");
    }
}



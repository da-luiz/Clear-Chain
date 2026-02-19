package com.vms.vendor_management_system.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Custom OAuth2ClientProperties that skips validation for empty client IDs.
 * This allows OAuth2 properties to be defined but empty without causing startup errors.
 */
@Configuration
public class OAuth2PropertiesConfig {

    /**
     * Custom OAuth2ClientProperties that overrides validation to allow empty client IDs.
     * The actual filtering of empty registrations happens in OAuth2ClientConfig.
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.security.oauth2.client")
    public OAuth2ClientProperties oAuth2ClientProperties() {
        return new OAuth2ClientProperties() {
            @Override
            public void afterPropertiesSet() {
                // Skip validation - allow empty client IDs
                // Empty registrations will be filtered out in OAuth2ClientConfig
            }
        };
    }
}




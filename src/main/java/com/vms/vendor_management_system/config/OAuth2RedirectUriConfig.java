package com.vms.vendor_management_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

/**
 * Configuration to ensure OAuth2 redirect URIs use HTTPS when behind reverse proxy
 */
@Configuration
public class OAuth2RedirectUriConfig {

    /**
     * Enable forwarded header filter to trust X-Forwarded-* headers from Nginx
     * This ensures {baseUrl} resolves to https:// when behind Nginx
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}

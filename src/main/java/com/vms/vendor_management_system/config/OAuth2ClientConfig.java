package com.vms.vendor_management_system.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom OAuth2 Client configuration that filters out empty client registrations.
 * This allows OAuth2 properties to be defined but empty without causing validation errors.
 * OAuth2 will work when credentials are provided via environment variables.
 */
@Configuration
public class OAuth2ClientConfig {

    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
        List<ClientRegistration> registrations = new ArrayList<>();
        
        // Build Google registration if client ID is not empty
        if (properties.getRegistration().containsKey("google")) {
            OAuth2ClientProperties.Registration googleReg = properties.getRegistration().get("google");
            if (googleReg != null && googleReg.getClientId() != null && !googleReg.getClientId().trim().isEmpty()) {
                ClientRegistration googleRegistration = buildGoogleRegistration(googleReg, properties.getProvider().get("google"));
                if (googleRegistration != null) {
                    registrations.add(googleRegistration);
                }
            }
        }
        
        // Build GitHub registration if client ID is not empty
        if (properties.getRegistration().containsKey("github")) {
            OAuth2ClientProperties.Registration githubReg = properties.getRegistration().get("github");
            if (githubReg != null && githubReg.getClientId() != null && !githubReg.getClientId().trim().isEmpty()) {
                ClientRegistration githubRegistration = buildGitHubRegistration(githubReg, properties.getProvider().get("github"));
                if (githubRegistration != null) {
                    registrations.add(githubRegistration);
                }
            }
        }
        
        // Only create repository if we have valid registrations
        // If empty, return a custom implementation that allows empty registrations
        if (registrations.isEmpty()) {
            return new EmptyClientRegistrationRepository();
        }
        
        return new InMemoryClientRegistrationRepository(registrations);
    }
    
    /**
     * Custom ClientRegistrationRepository that allows empty registrations.
     * This allows OAuth2 configuration to exist without credentials.
     * Implements Iterable to support Spring Security's iteration over registrations.
     */
    private static class EmptyClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {
        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return null;
        }
        
        @Override
        public java.util.Iterator<ClientRegistration> iterator() {
            return java.util.Collections.emptyIterator();
        }
    }
    
    private ClientRegistration buildGoogleRegistration(OAuth2ClientProperties.Registration reg, OAuth2ClientProperties.Provider provider) {
        if (reg.getClientId() == null || reg.getClientId().trim().isEmpty() || 
            reg.getClientSecret() == null || reg.getClientSecret().trim().isEmpty()) {
            return null;
        }
        
        OAuth2ClientProperties.Provider googleProvider = provider != null ? provider : new OAuth2ClientProperties.Provider();
        
        return ClientRegistration.withRegistrationId("google")
                .clientId(reg.getClientId())
                .clientSecret(reg.getClientSecret())
                .scope(reg.getScope())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(reg.getRedirectUri() != null ? reg.getRedirectUri() : "{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri(googleProvider.getAuthorizationUri() != null ? googleProvider.getAuthorizationUri() 
                    : "https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri(googleProvider.getTokenUri() != null ? googleProvider.getTokenUri() 
                    : "https://oauth2.googleapis.com/token")
                .userInfoUri(googleProvider.getUserInfoUri() != null ? googleProvider.getUserInfoUri() 
                    : "https://www.googleapis.com/oauth2/v2/userinfo")
                .userNameAttributeName(googleProvider.getUserNameAttribute() != null ? googleProvider.getUserNameAttribute() 
                    : "email")
                .clientName("Google")
                .build();
    }
    
    private ClientRegistration buildGitHubRegistration(OAuth2ClientProperties.Registration reg, OAuth2ClientProperties.Provider provider) {
        if (reg.getClientId() == null || reg.getClientId().trim().isEmpty() || 
            reg.getClientSecret() == null || reg.getClientSecret().trim().isEmpty()) {
            return null;
        }
        
        OAuth2ClientProperties.Provider githubProvider = provider != null ? provider : new OAuth2ClientProperties.Provider();
        
        return ClientRegistration.withRegistrationId("github")
                .clientId(reg.getClientId())
                .clientSecret(reg.getClientSecret())
                .scope(reg.getScope())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(reg.getRedirectUri() != null ? reg.getRedirectUri() : "{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri(githubProvider.getAuthorizationUri() != null ? githubProvider.getAuthorizationUri() 
                    : "https://github.com/login/oauth/authorize")
                .tokenUri(githubProvider.getTokenUri() != null ? githubProvider.getTokenUri() 
                    : "https://github.com/login/oauth/access_token")
                .userInfoUri(githubProvider.getUserInfoUri() != null ? githubProvider.getUserInfoUri() 
                    : "https://api.github.com/user")
                .userNameAttributeName(githubProvider.getUserNameAttribute() != null ? githubProvider.getUserNameAttribute() 
                    : "login")
                .clientName("GitHub")
                .build();
    }
}


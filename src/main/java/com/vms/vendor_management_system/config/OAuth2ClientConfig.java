package com.vms.vendor_management_system.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
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
        
        // Build Azure registration if client ID is not empty
        if (properties.getRegistration().containsKey("azure")) {
            OAuth2ClientProperties.Registration azureReg = properties.getRegistration().get("azure");
            if (azureReg != null && azureReg.getClientId() != null && !azureReg.getClientId().trim().isEmpty()) {
                ClientRegistration azureRegistration = buildAzureRegistration(azureReg, properties.getProvider().get("azure"));
                if (azureRegistration != null) {
                    registrations.add(azureRegistration);
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
        
        // Use explicit HTTPS redirect URI in production, or {baseUrl} for local development
        String redirectUri = reg.getRedirectUri();
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            String baseUrl = System.getenv("OAUTH2_BASE_URL");
            if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                // Replace {registrationId} with actual registration ID for explicit HTTPS URL
                redirectUri = baseUrl + "/login/oauth2/code/google";
            } else {
                redirectUri = "{baseUrl}/login/oauth2/code/{registrationId}";
            }
        }
        
        return ClientRegistration.withRegistrationId("google")
                .clientId(reg.getClientId())
                .clientSecret(reg.getClientSecret())
                .scope(reg.getScope())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
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
        
        // Use explicit HTTPS redirect URI in production, or {baseUrl} for local development
        String redirectUri = reg.getRedirectUri();
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            String baseUrl = System.getenv("OAUTH2_BASE_URL");
            if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                // Replace {registrationId} with actual registration ID for explicit HTTPS URL
                redirectUri = baseUrl + "/login/oauth2/code/github";
            } else {
                redirectUri = "{baseUrl}/login/oauth2/code/{registrationId}";
            }
        }
        
        return ClientRegistration.withRegistrationId("github")
                .clientId(reg.getClientId())
                .clientSecret(reg.getClientSecret())
                .scope(reg.getScope())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri(redirectUri)
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
    
    private ClientRegistration buildAzureRegistration(OAuth2ClientProperties.Registration reg, OAuth2ClientProperties.Provider provider) {
        if (reg.getClientId() == null || reg.getClientId().trim().isEmpty() || 
            reg.getClientSecret() == null || reg.getClientSecret().trim().isEmpty()) {
            return null;
        }
        
        OAuth2ClientProperties.Provider azureProvider = provider != null ? provider : new OAuth2ClientProperties.Provider();
        
        // Azure uses OIDC, so we need the issuer-uri
        String issuerUri = azureProvider.getIssuerUri();
        if (issuerUri == null || issuerUri.trim().isEmpty()) {
            // Try to get tenant ID from environment variable
            String tenantId = System.getenv("AZURE_TENANT_ID");
            if (tenantId != null && !tenantId.trim().isEmpty()) {
                issuerUri = "https://login.microsoftonline.com/" + tenantId + "/v2.0";
            } else {
                return null; // Can't build Azure registration without issuer-uri
            }
        }
        
        // Handle scope - use provided scope or default
        java.util.Set<String> scopes = reg.getScope();
        if (scopes == null || scopes.isEmpty()) {
            scopes = java.util.Set.of("openid", "profile", "email");
        }
        
        // Use explicit HTTPS redirect URI in production, or {baseUrl} for local development
        String redirectUri = reg.getRedirectUri();
        if (redirectUri == null || redirectUri.trim().isEmpty()) {
            String baseUrl = System.getenv("OAUTH2_BASE_URL");
            if (baseUrl != null && !baseUrl.trim().isEmpty()) {
                // Replace {registrationId} with actual registration ID for explicit HTTPS URL
                redirectUri = baseUrl + "/login/oauth2/code/azure";
            } else {
                redirectUri = "{baseUrl}/login/oauth2/code/{registrationId}";
            }
        }
        
        try {
            // Use OIDC issuer location for Azure (auto-discovers endpoints)
            ClientRegistration.Builder builder = ClientRegistrations.fromOidcIssuerLocation(issuerUri)
                    .registrationId("azure")
                    .clientId(reg.getClientId())
                    .clientSecret(reg.getClientSecret())
                    .scope(scopes.toArray(new String[0]))
                    .redirectUri(redirectUri);
            
            return builder.build();
        } catch (Exception e) {
            // If OIDC discovery fails, fall back to manual configuration
            String tenantId = System.getenv("AZURE_TENANT_ID");
            return ClientRegistration.withRegistrationId("azure")
                    .clientId(reg.getClientId())
                    .clientSecret(reg.getClientSecret())
                    .scope(scopes.toArray(new String[0]))
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri(redirectUri)
                    .authorizationUri("https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/authorize")
                    .tokenUri("https://login.microsoftonline.com/" + System.getenv("AZURE_TENANT_ID") + "/oauth2/v2.0/token")
                    .userInfoUri("https://graph.microsoft.com/oidc/userinfo")
                    .userNameAttributeName("email")
                    .clientName("Azure")
                    .build();
        }
    }
}


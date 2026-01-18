package com.vms.vendor_management_system.application.security;

import com.vms.vendor_management_system.domain.entity.User;
import com.vms.vendor_management_system.domain.enums.UserRole;
import com.vms.vendor_management_system.domain.repository.UserRepository;
import com.vms.vendor_management_system.domain.valueobjects.Email;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Handles successful OAuth2 authentication.
 * Creates or updates user account and generates JWT token.
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public OAuth2SuccessHandler(UserRepository userRepository, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Token.getPrincipal();
        String registrationId = oauth2Token.getAuthorizedClientRegistrationId(); // "google" or "github"
        
        // Extract user information from OAuth2 user attributes
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = extractEmail(attributes, registrationId);
        String name = extractName(attributes, registrationId);
        String firstName = extractFirstName(name);
        String lastName = extractLastName(name);
        String oauthId = extractOAuthId(attributes, registrationId);
        String imageUrl = extractImageUrl(attributes, registrationId);
        
        if (email == null || oauthId == null) {
            getRedirectStrategy().sendRedirect(request, response, "/login?error=oauth_email_missing");
            return;
        }
        
        // Find or create user
        User user = findOrCreateUser(registrationId.toUpperCase(), oauthId, email, firstName, lastName, imageUrl);
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUsername());
        
        // Build redirect URL with token
        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/auth/callback")
                .queryParam("token", token)
                .queryParam("username", user.getUsername())
                .queryParam("role", user.getRole().name())
                .queryParam("userId", user.getId())
                .queryParam("firstName", user.getFirstName())
                .queryParam("lastName", user.getLastName())
                .build()
                .toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
    
    private User findOrCreateUser(String provider, String oauthId, String email, String firstName, String lastName, String imageUrl) {
        // First, try to find by OAuth provider and ID
        Optional<User> existingOAuthUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        if (existingOAuthUser.isPresent()) {
            User user = existingOAuthUser.get();
            user.setImageUrl(imageUrl);
            user.updateLastLogin();
            return userRepository.save(user);
        }
        
        // Second, try to find by email (might be existing user linking OAuth)
        Optional<User> existingEmailUser = userRepository.findByEmailValue(email);
        if (existingEmailUser.isPresent()) {
            User user = existingEmailUser.get();
            // Link OAuth to existing account
            user.setOauthProvider(provider);
            user.setOauthId(oauthId);
            user.setImageUrl(imageUrl);
            user.updateLastLogin();
            return userRepository.save(user);
        }
        
        // Create new user from OAuth
        String username = generateUsername(email, firstName, lastName);
        User newUser = new User(
            username,
            firstName != null ? firstName : "User",
            lastName != null ? lastName : "Name",
            new Email(email),
            UserRole.DEPARTMENT_REQUESTER, // Default role for OAuth users
            null // No department by default
        );
        newUser.setOauthProvider(provider);
        newUser.setOauthId(oauthId);
        newUser.setImageUrl(imageUrl);
        newUser.setPassword(null); // No password for OAuth users
        
        return userRepository.save(newUser);
    }
    
    private String generateUsername(String email, String firstName, String lastName) {
        // Use email prefix or generate from name
        String base = email.split("@")[0];
        String username = base;
        int counter = 1;
        
        // Ensure username is unique
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + counter;
            counter++;
        }
        
        return username;
    }
    
    private String extractEmail(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("email");
        } else if ("github".equals(provider)) {
            String email = (String) attributes.get("email");
            if (email == null && attributes.containsKey("login")) {
                // GitHub might not return email, use login with @users.noreply.github.com
                return attributes.get("login") + "@users.noreply.github.com";
            }
            return email;
        }
        return (String) attributes.getOrDefault("email", attributes.get("emailAddress"));
    }
    
    private String extractName(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        } else if ("github".equals(provider)) {
            String name = (String) attributes.get("name");
            if (name == null) {
                return (String) attributes.get("login"); // Fallback to username
            }
            return name;
        }
        return (String) attributes.getOrDefault("name", attributes.get("displayName"));
    }
    
    private String extractOAuthId(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("sub");
        } else if ("github".equals(provider)) {
            return String.valueOf(attributes.get("id"));
        }
        return (String) attributes.getOrDefault("sub", attributes.get("id"));
    }
    
    private String extractImageUrl(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("picture");
        } else if ("github".equals(provider)) {
            return (String) attributes.get("avatar_url");
        }
        return (String) attributes.getOrDefault("picture", attributes.get("avatar_url"));
    }
    
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : null;
    }
    
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return null;
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return String.join(" ", java.util.Arrays.copyOfRange(parts, 1, parts.length));
        }
        return null;
    }
}





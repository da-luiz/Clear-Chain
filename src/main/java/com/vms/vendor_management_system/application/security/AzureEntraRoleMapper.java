package com.vms.vendor_management_system.application.security;

import com.vms.vendor_management_system.domain.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps Azure Entra ID groups/app roles to Spring Security roles.
 * 
 * Supports two mapping strategies:
 * 1. Entra Groups (Group IDs) → Spring Roles
 * 2. Entra App Roles → Spring Roles
 * 
 * Mapping is configured via application properties or environment variables.
 */
@Service
public class AzureEntraRoleMapper {

    private static final Logger logger = LoggerFactory.getLogger(AzureEntraRoleMapper.class);

    @Value("${azure.oauth2.groups-claim-name:groups}")
    private String groupsClaimName;

    @Value("${azure.oauth2.roles-claim-name:roles}")
    private String rolesClaimName;

    /**
     * Mapping of Entra Group IDs to Spring Security roles.
     * Format: ENV variable or property: AZURE_GROUP_ROLE_MAPPING
     * Example: "group-id-1:ROLE_ADMIN,group-id-2:ROLE_FINANCE_APPROVER"
     */
    @Value("${azure.group.role.mapping:}")
    private String groupRoleMappingConfig;

    /**
     * Mapping of Entra App Roles to Spring Security roles.
     * Format: ENV variable or property: AZURE_APP_ROLE_MAPPING
     * Example: "ROLE_ADMIN:ROLE_ADMIN,ROLE_FINANCE:ROLE_FINANCE_APPROVER"
     */
    @Value("${azure.app.role.mapping:}")
    private String appRoleMappingConfig;

    @Value("${azure.role.mapping.deny-unmapped:true}")
    private boolean denyUnmappedUsers;

    // Cache for parsed mappings
    private Map<String, UserRole> groupToRoleMap;
    private Map<String, UserRole> appRoleToRoleMap;

    /**
     * Map Azure Entra ID claims to Spring Security roles.
     * 
     * @param attributes OAuth2 user attributes from Azure token
     * @return Set of Spring Security roles (ROLE_*)
     */
    public Set<UserRole> mapClaimsToRoles(Map<String, Object> attributes) {
        Set<UserRole> roles = new HashSet<>();

        // Parse mappings if not cached
        if (groupToRoleMap == null) {
            groupToRoleMap = parseGroupRoleMapping();
            appRoleToRoleMap = parseAppRoleMapping();
        }

        // Strategy 1: Map from Entra Groups (Group IDs)
        List<String> groups = extractGroups(attributes);
        for (String groupId : groups) {
            UserRole role = groupToRoleMap.get(groupId);
            if (role != null) {
                roles.add(role);
                logger.debug("Mapped Entra group {} to role {}", groupId, role);
            }
        }

        // Strategy 2: Map from Entra App Roles
        List<String> appRoles = extractAppRoles(attributes);
        for (String appRole : appRoles) {
            UserRole role = appRoleToRoleMap.get(appRole);
            if (role != null) {
                roles.add(role);
                logger.debug("Mapped Entra app role {} to role {}", appRole, role);
            }
        }

        // Default behavior if no mapping found
        if (roles.isEmpty()) {
            if (denyUnmappedUsers) {
                logger.warn("No role mapping found for user. Access denied (deny-unmapped=true)");
                return Collections.emptySet(); // Deny access
            } else {
                logger.warn("No role mapping found for user. Assigning default role DEPARTMENT_REQUESTER");
                roles.add(UserRole.DEPARTMENT_REQUESTER); // Default minimal role
            }
        }

        return roles;
    }

    /**
     * Extract group IDs from token claims.
     * Handles both direct group IDs and group overage claim (_claim_sources).
     */
    @SuppressWarnings("unchecked")
    private List<String> extractGroups(Map<String, Object> attributes) {
        List<String> groups = new ArrayList<>();

        // Check for groups claim (direct)
        Object groupsClaim = attributes.get(groupsClaimName);
        if (groupsClaim instanceof List) {
            ((List<?>) groupsClaim).forEach(item -> {
                if (item instanceof String) {
                    groups.add((String) item);
                }
            });
        }

        // Check for group overage claim
        Object claimSources = attributes.get("_claim_sources");
        if (claimSources != null && claimSources instanceof Map) {
            Map<String, Object> sources = (Map<String, Object>) claimSources;
            Object src = sources.get("src1");
            if (src instanceof Map) {
                Map<String, Object> srcMap = (Map<String, Object>) src;
                Object endpoint = srcMap.get("endpoint");
                if (endpoint instanceof String) {
                    logger.info("Group overage detected. Endpoint: {}", endpoint);
                    // TODO: Implement Microsoft Graph API call to fetch groups
                    // See: Microsoft Graph - Read user group membership
                    // This requires additional implementation with Microsoft Graph SDK
                }
            }
        }

        return groups;
    }

    /**
     * Extract app roles from token claims.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractAppRoles(Map<String, Object> attributes) {
        List<String> roles = new ArrayList<>();

        Object rolesClaim = attributes.get(rolesClaimName);
        if (rolesClaim instanceof List) {
            ((List<?>) rolesClaim).forEach(item -> {
                if (item instanceof String) {
                    roles.add((String) item);
                }
            });
        }

        return roles;
    }

    /**
     * Parse group-to-role mapping from configuration.
     * Format: "group-id-1:ROLE_ADMIN,group-id-2:ROLE_FINANCE_APPROVER"
     */
    private Map<String, UserRole> parseGroupRoleMapping() {
        Map<String, UserRole> mapping = new HashMap<>();

        if (groupRoleMappingConfig == null || groupRoleMappingConfig.trim().isEmpty()) {
            logger.warn("No group role mapping configured. Using empty mapping.");
            return mapping;
        }

        String[] entries = groupRoleMappingConfig.split(",");
        for (String entry : entries) {
            String[] parts = entry.trim().split(":");
            if (parts.length == 2) {
                String groupId = parts[0].trim();
                String roleName = parts[1].trim();
                try {
                    UserRole role = UserRole.valueOf(roleName);
                    mapping.put(groupId, role);
                    logger.info("Configured group mapping: {} -> {}", groupId, role);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid role name in group mapping: {}", roleName, e);
                }
            }
        }

        return mapping;
    }

    /**
     * Parse app-role-to-role mapping from configuration.
     * Format: "ROLE_ADMIN:ROLE_ADMIN,ROLE_FINANCE:ROLE_FINANCE_APPROVER"
     */
    private Map<String, UserRole> parseAppRoleMapping() {
        Map<String, UserRole> mapping = new HashMap<>();

        if (appRoleMappingConfig == null || appRoleMappingConfig.trim().isEmpty()) {
            logger.warn("No app role mapping configured. Using empty mapping.");
            return mapping;
        }

        String[] entries = appRoleMappingConfig.split(",");
        for (String entry : entries) {
            String[] parts = entry.trim().split(":");
            if (parts.length == 2) {
                String appRole = parts[0].trim();
                String roleName = parts[1].trim();
                try {
                    UserRole role = UserRole.valueOf(roleName);
                    mapping.put(appRole, role);
                    logger.info("Configured app role mapping: {} -> {}", appRole, role);
                } catch (IllegalArgumentException e) {
                    logger.error("Invalid role name in app role mapping: {}", roleName, e);
                }
            }
        }

        return mapping;
    }

    /**
     * Get default role if no mapping found.
     */
    public UserRole getDefaultRole() {
        return denyUnmappedUsers ? null : UserRole.DEPARTMENT_REQUESTER;
    }
}





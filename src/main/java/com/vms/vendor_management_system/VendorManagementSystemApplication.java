package com.vms.vendor_management_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Excludes default OAuth2 client auto-config so Boot does not register the standard
 * {@code OAuth2ClientProperties} bean (it validates every registration and fails with
 * "Client id must not be empty" when Google/GitHub/Azure env vars are unset).
 * Optional OAuth is wired via {@link com.vms.vendor_management_system.config.OAuth2PropertiesConfig}
 * and {@link com.vms.vendor_management_system.config.OAuth2ClientConfig}.
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration.class
})
public class VendorManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(VendorManagementSystemApplication.class, args);
	}

}

package com.infy.RewardPointCalculator.tokenConfig;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Custom implementation of `BasicAuthenticationEntryPoint` to handle unauthorized access 
 * for requests that require JWT (JSON Web Token) authentication.
 * 
 * This class is responsible for intercepting authentication failures and responding with a 
 * 401 Unauthorized HTTP status, along with a custom error message in JSON format. It also 
 * logs details about unauthorized access attempts for monitoring and debugging purposes.
 * 
 * It overrides the `commence` method from `BasicAuthenticationEntryPoint` to customize the response 
 * when authentication fails, providing more informative error messages and logs.
 * 
 * The `afterPropertiesSet` method is also overridden to set a realm name for the entry point, 
 * providing information about the protected resource.
 */

@Component
public class JwtAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Handles unauthorized access attempts by sending a 401 Unauthorized response along with 
     * a custom error message in JSON format.
     * 
     * This method is triggered when an authentication exception occurs. It logs the details 
     * of the unauthorized access attempt, including the requested URI and the client's IP address. 
     * It then responds with a 401 status and a JSON error message.
     * 
     * @param request The incoming HTTP request that triggered the authentication failure.
     * @param response The HTTP response that will be sent back to the client.
     * @param authException The authentication exception that triggered the failure.
     * @throws IOException If an I/O error occurs while writing the response.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        // Log the authentication failure with details about the request
        log.error("Unauthorized access attempt to URI: {} from IP: {}. Error: {}", 
                  request.getRequestURI(), request.getRemoteAddr(), authException.getMessage());

        // Set the response status to 401 (Unauthorized)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // Send a custom error message to the client
        response.getWriter().write("{ \"message\": \"" + authException.getMessage() + "\" }");

        log.info("Sent 401 Unauthorized response with message: {}", authException.getMessage());
    }

    /**
     * Initializes the realm name for the authentication entry point and logs the initialization.
     * 
     * The realm name provides context for the authentication request, indicating which protected 
     * resource the user is trying to access. This is typically displayed in the HTTP authentication 
     * dialog box in the browser.
     */
    @Override
    public void afterPropertiesSet() {
        // Set the realm name for the authentication entry point
        setRealmName("JWT Authentication");
        super.afterPropertiesSet();
        log.info("JwtAuthenticationEntryPoint initialized with realm name: JWT Authentication");
    }
}

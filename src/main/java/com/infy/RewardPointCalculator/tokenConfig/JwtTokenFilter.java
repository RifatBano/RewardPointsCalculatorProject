package com.infy.RewardPointCalculator.tokenConfig;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.infy.RewardPointCalculator.service.CustomerService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This class is a custom filter that intercepts HTTP requests to validate the presence and validity of 
 * a JWT (JSON Web Token) in the request header. It extends `OncePerRequestFilter`, ensuring that the filter 
 * is applied once per request.
 * 
 * The primary function of this filter is to extract the JWT from the request, validate its authenticity, 
 * and if valid, authenticate the user by setting the authentication context with user details extracted from the token.
 * 
 * The filter ensures that subsequent filters or controllers can access the authenticated user via 
 * Spring Security's `SecurityContext`.
 */
@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);

    private JwtTokenProvider jwtTokenProvider;
    private CustomerService userDetailsService;

    /**
     * Constructor that initializes the JwtTokenFilter with dependencies for JWT handling and user details retrieval.
     * 
     * @param jwtTokenProvider The provider for handling JWT-related operations like token extraction and validation.
     * @param userDetailsService The service for fetching user details based on the username in the token.
     */
    @Autowired
    public JwtTokenFilter(JwtTokenProvider jwtTokenProvider, CustomerService userDetailsService) {
        super();
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        log.info("JwtTokenFilter initialized with JwtTokenProvider and CustomerService.");
    }

    /**
     * This method processes each HTTP request, extracting the JWT token from the request headers, validating it,
     * and setting the authentication in the `SecurityContext` if the token is valid.
     * 
     * The filter chain is continued regardless of the token's validity, allowing the request to proceed to the next filter
     * or controller in the chain.
     * 
     * @param request The HTTP request being processed.
     * @param response The HTTP response that will be returned.
     * @param filterChain The chain of filters that will process the request after this filter.
     * @throws ServletException If an error occurs during request processing.
     * @throws IOException If an I/O error occurs during request or response processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Processing request: {} {}", request.getMethod(), request.getRequestURI());

        String token = jwtTokenProvider.resolveToken(request);

        if (token != null) {
            log.info("Token found in request header.");

            if (jwtTokenProvider.validateToken(token)) {
                log.info("Token is valid. Extracting user details.");

                String username = jwtTokenProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("Authentication set for user: {}", username);
            } else {
                log.warn("Invalid or expired token found.");
            }
        } else {
            log.warn("No token found in request.");
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}

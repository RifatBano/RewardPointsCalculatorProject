package com.infy.RewardPointCalculator.tokenConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.infy.RewardPointCalculator.service.CustomerService;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuration class for setting up security for the application using Spring Security.
 * This class configures various components related to authentication and authorization.
 * It also integrates JWT authentication via the JwtTokenFilter.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtTokenFilter jwtAuthenticationFilter;
    private final CustomerService userDetailsService;

    // Constructor for dependency injection (Spring will inject the required dependencies here)
    @Autowired
    public SecurityConfig(JwtTokenFilter jwtAuthenticationFilter, CustomerService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        log.info("SecurityConfig initialized with JwtTokenFilter and CustomerService.");
    }

    /**
     * Configures the AuthenticationProvider used by Spring Security for handling authentication logic.
     * The AuthenticationProvider is configured to use a UserDetailsService and a PasswordEncoder (BCrypt).
     * 
     * @return The configured AuthenticationProvider.
     */    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        log.info("Configuring AuthenticationProvider.");
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // Ensure UserDetailsService is injected
        provider.setPasswordEncoder(passwordEncoder()); // Ensure PasswordEncoder is set to BCryptPasswordEncoder
        log.info("AuthenticationProvider configured with UserDetailsService and PasswordEncoder.");
        return provider;
    }
    
    /**
     * Initialize the security configuration after the constructor is called.
     * This method logs that the SecurityConfig is initialized.
     */
    @PostConstruct
    public void init() {
        log.info("SecurityConfig initialized, JwtTokenFilter: {}", jwtAuthenticationFilter);
    }

    /**
     * Configures the PasswordEncoder bean. BCryptPasswordEncoder is used here, which is a secure method for password encoding.
     * 
     * @return The configured PasswordEncoder bean.
     */    
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Configuring PasswordEncoder (BCryptPasswordEncoder).");
        return new BCryptPasswordEncoder(); // Prefer BCrypt for password encryption in production
    }


    /**
     * Configures the AuthenticationManager bean. The AuthenticationManager is responsible for handling authentication logic.
     * 
     * @param authenticationConfiguration The AuthenticationConfiguration used to retrieve the AuthenticationManager.
     * @return The configured AuthenticationManager.
     * @throws Exception If an error occurs during the configuration.
     */    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        log.info("Configuring AuthenticationManager.");
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the main SecurityFilterChain, which defines the rules for securing HTTP requests.
     * This configuration includes setting stateless session management, disabling CSRF, and configuring JWT filter.
     * 
     * @param httpSecurity The HttpSecurity object to configure security for HTTP requests.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.info("Configuring SecurityFilterChain.");

        httpSecurity.headers().frameOptions().disable();
        httpSecurity.cors().and().csrf().disable();

        // Configuring the HTTP request authorization rules
        httpSecurity
            .authorizeHttpRequests()
            .requestMatchers("/api/customers/register", "/api/customers/login", "/api/customers/logout", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Allow unauthenticated access to login API
            .anyRequest().authenticated() // All other requests require authentication
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless session management
            .and()
            .exceptionHandling()
            .authenticationEntryPoint((request, response, authException) -> 
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getLocalizedMessage()) // Handle authentication failure
            )
            .and()
            .authenticationProvider(authenticationProvider()) // Register the authentication provider
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter before authentication

        log.info("SecurityFilterChain configured and JWT filter added.");
        return httpSecurity.build(); // Build and return the final SecurityFilterChain
    }
}

package com.infy.RewardPointCalculator.tokenConfig;

import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import com.infy.RewardPointCalculator.exception.*;
import com.infy.RewardPointCalculator.model.BlacklistedToken;
import com.infy.RewardPointCalculator.repository.BlacklistedTokenRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * This class provides functionality for generating, validating, and resolving JWT tokens in the application.
 * It also ensures that tokens are checked for validity and expiration, and supports the management of blacklisted tokens.
 * The class is integrated with Spring Security to work as an authentication mechanism based on JWT tokens.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String SECRET_KEY = "cnliaGRic21zbXN2c2prd3FoZGx4eGtoZ2ZkY2JubXl0cmRkY2dnZHNzc3NkZmZnZ2hoampqa2pqaGJ2Y2N4eHp6enNzYXNzZGRjdnZnaGhubmJibmpqamhoZ2Zkc3NkZmdo";
    private static final SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @PostConstruct
    public void init() {
        log.info("JwtTokenProvider initialized successfully");
    }

    /**
     * This method generates a JWT token for a given authenticated user. 
     * The token includes the username, issued time, and expiration time (1 hour from now).
     * 
     * @param authentication The authentication object containing the user’s details.
     * @return A signed JWT token for the authenticated user.
     */
    public String createToken(Authentication authentication) {
        log.info("Creating JWT token for user: {}", authentication.getName());

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // Token expires in 1 hour

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        log.info("JWT token created for user: {} with expiry date: {}", userDetails.getUsername(), expiryDate);
        return token;
    }

    /**
     * This method extracts the token from the Authorization header of the HTTP request.
     * It checks for the "Bearer" prefix and ensures the token is not blacklisted.
     * 
     * @param request The HTTP request to extract the token from.
     * @return The JWT token if present and valid, or null if not found or blacklisted.
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.info("Resolving token from Authorization header: {}", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7); // Extract the token

            // Check if the token is blacklisted
            if (isTokenBlacklisted(token)) {
                log.error("The token is blacklisted. Customer has logged out.");
                return null; // Return null or handle it appropriately if the token is blacklisted
            }

            log.info("Token resolved: {}", token);
            return token;
        }

        log.warn("No Bearer token found in Authorization header.");
        return null;
    }

    
    /**
     * Validates the JWT token by parsing it using the signing key and checking for common exceptions like
     * malformed, expired, or unsupported tokens.
     * 
     * @param token The JWT token to validate.
     * @return True if the token is valid, false otherwise.
     */    
    public boolean validateToken(String token) {
        log.info("Validating JWT token: {}", token);

        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            log.info("JWT token is valid: {}", token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", token, ex);
        } catch (ExpiredJwtTokenException ex) {
            log.error("Expired JWT token: {}", token, ex);
        } catch (UnsupportedJwtTokenException ex) {
            log.error("Unsupported JWT token: {}", token, ex);
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", token, ex);
        } catch (SignatureException e) {
            log.error("Error with the signature of the token: {}", token, e);
        }

        return false;
    }

    /**
     * Checks if the provided token is still valid (i.e., it hasn't expired).
     * 
     * @param token The JWT token to check.
     * @return True if the token is valid, false if it is expired.
     */
    public boolean isTokenValid(String token) {
        log.info("Checking if token is valid: {}", token);
        return !isTokenExpired(token);
    }

    /**
     * Determines if the provided token has expired by comparing its expiration date to the current date.
     * 
     * @param token The JWT token to check.
     * @return True if the token has expired, false otherwise.
     */
    public boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        boolean expired = expirationDate.before(new Date());
        log.info("Token expiration date: {}. Is token expired? {}", expirationDate, expired);
        return expired;
    }

    /**
     * Extracts the expiration date from the token.
     * 
     * @param token The JWT token.
     * @return The expiration date of the token.
     */
    private Date extractExpiration(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    
    /**
     * Extracts the username (subject) from the token's claims.
     * 
     * @param token The JWT token.
     * @return The username extracted from the token.
     */    
    public String getUsername(String token) {
        log.info("Extracting username from token: {}", token);
        String username = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
        log.info("Extracted username: {}", username);
        return username;
    }

    /**
     * Checks whether the token is blacklisted by querying the BlacklistedToken repository.
     * 
     * @param token The JWT token to check.
     * @return True if the token is blacklisted, false otherwise.
     */
    public boolean isTokenBlacklisted(String token) {
        Optional<BlacklistedToken> blacklistedToken = blacklistedTokenRepository.findByToken(token);
        log.info("Checking if token is blacklisted: {}. Found: {}", token, blacklistedToken.isPresent());

        return blacklistedToken.isPresent();
    }
}

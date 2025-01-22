package com.infy.RewardPointCalculator.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.RewardPointCalculator.model.BlacklistedToken;

/**
 * Repository interface for managing blacklisted tokens in the database.
 * 
 * This interface extends `JpaRepository` to provide CRUD operations for the `BlacklistedToken` entity.
 * It includes an optional custom method to find a blacklisted token by its value (token string).
 * 
 * The repository is used to interact with the database and manage the lifecycle of blacklisted tokens.
 * These tokens may be used to track JWTs or other types of tokens that have been revoked or blacklisted,
 * preventing their further use for authentication.
 * @author rifat.bano
 */
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

	/**
     * Finds a blacklisted token by its value.
     * 
     * This method searches the database for a blacklisted token that matches the provided token string.
     * If no token is found, it returns an empty `Optional`.
     * 
     * @param token The token string to search for in the blacklisted tokens table.
     * @return An `Optional<BlacklistedToken>` containing the blacklisted token if found, otherwise empty.
     */	
    Optional<BlacklistedToken> findByToken(String token);
}

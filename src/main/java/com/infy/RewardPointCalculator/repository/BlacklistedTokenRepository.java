package com.infy.RewardPointCalculator.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.infy.RewardPointCalculator.model.BlacklistedToken;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    // Optional method to find a token by value
    Optional<BlacklistedToken> findByToken(String token);
}

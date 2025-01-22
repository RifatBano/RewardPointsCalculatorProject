package com.infy.RewardPointCalculator.tokenConfig;

import java.security.Key;
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

@Component
@Slf4j
public class JwtTokenProvider {
	
	private static final Logger log=LoggerFactory.getLogger(JwtTokenProvider.class);
//	Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
private static final String SECRET_KEY= "cnliaGRic21zbXN2c2prd3FoZGx4eGtoZ2ZkY2JubXl0cmRkY2dnZHNzc3NkZmZnZ2hoampqa2pqaGJ2Y2N4eHp6enNzYXNzZGRjdnZnaGhubmJibmpqamhoZ2Zkc3NkZmdo";
private static final SecretKey key=Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));

@Autowired
private BlacklistedTokenRepository blacklistedTokenRepository;

    @PostConstruct
    public void init() {
        log.info("JwtTokenProvider initialized successfully");
    }
	
	  public String createToken(Authentication authentication) {
	  
	    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	    Date now = new Date();
	    Date expiryDate = new Date(now.getTime() + 3600000);

	    return Jwts.builder()
	        .setSubject(userDetails.getUsername())
	        .setIssuedAt(new Date())
	        .setExpiration(expiryDate)
	        .signWith(SignatureAlgorithm.HS512, key)
	        .compact();
	  }


//	  public String resolveToken(HttpServletRequest request) {
//
//	    String bearerToken = request.getHeader("Authorization");
//	    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
//	      return bearerToken.substring(7);
//	    }
//	    return null;
//	  }

	  public String resolveToken(HttpServletRequest request) {
	        String bearerToken = request.getHeader("Authorization");
	        
	        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
	            String token = bearerToken.substring(7); // Extract the token

	            // Check if the token is blacklisted
	            if (isTokenBlacklisted(token)) {
	                log.error("The token is blacklisted.Customer has logout");
	                return null; // Return null or handle it appropriately if the token is blacklisted
	            }

	            return token;
	        }
	        return null;
	    }
	  
	  
	  // Check if the token is valid and not expired
	  public boolean validateToken(String token) {
	    
	    try {
	      Jwts.parser().setSigningKey(key).parseClaimsJws(token);
	      return true;
	    } catch (MalformedJwtException ex) {
	      log.error("Invalid JWT token");
	    } catch (ExpiredJwtTokenException ex) {
	      log.error("Expired JWT token");
	    } catch (UnsupportedJwtTokenException ex) {
	      log.error("Unsupported JWT token");
	    } catch (IllegalArgumentException ex) {
	      log.error("JWT claims string is empty");
	    } catch (SignatureException e) {
	      log.error("there is an error with the signature of you token ");
	    }
	    return false;
	  }

	  
	  public boolean isTokenValid(String token) {
	        return !isTokenExpired(token);
	    }

	    public boolean isTokenExpired(String token) {
	        return extractExpiration(token).before(new Date());
	    }

	    private Date extractExpiration(String token) {
	        return Jwts.parser()
	                .setSigningKey(SECRET_KEY)
	                .parseClaimsJws(token)
	                .getBody()
	                .getExpiration();
	    }
	  
	  
		// Extract the username from the JWT token
	  public String getUsername(String token) {
	    
	    return Jwts.parser()
	        .setSigningKey(key)
	        .parseClaimsJws(token)
	        .getBody()
	        .getSubject();
	  }
	  
	  
	  public boolean isTokenBlacklisted(String token) {
	        Optional<BlacklistedToken> blacklistedToken = blacklistedTokenRepository.findByToken(token);
	        log.info("blacklisted token:{}"+blacklistedToken);
	        return blacklistedToken.isPresent();
	    }
	 

}

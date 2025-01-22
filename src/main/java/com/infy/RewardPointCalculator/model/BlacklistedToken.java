package com.infy.RewardPointCalculator.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BlacklistedToken {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	 private String token;
	 private String username;
	 
	 @CreationTimestamp // Automatically sets createdAt when the entity is created 
	 private LocalDateTime createdAt;
	 
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public BlacklistedToken() {
		super();
	}
	
	public BlacklistedToken( String token, String username) {
	
		this.token = token;
		this.username = username;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}

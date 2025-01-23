package com.infy.RewardPointCalculator.exception;

public class UsernameNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	//Handles Username Not Found from JWT Token Exception  
	public UsernameNotFoundException(String message) {
		super(message);
	}
}

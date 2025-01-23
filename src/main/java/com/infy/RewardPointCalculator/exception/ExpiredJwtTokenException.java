package com.infy.RewardPointCalculator.exception;

public class ExpiredJwtTokenException extends RuntimeException {
   
	private static final long serialVersionUID = 1L;

	//Handles JWT Token Expire Exception
	public ExpiredJwtTokenException(String message) {
        super(message);
    }
}

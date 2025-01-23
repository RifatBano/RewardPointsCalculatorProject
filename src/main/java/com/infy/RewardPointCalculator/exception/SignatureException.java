package com.infy.RewardPointCalculator.exception;

public class SignatureException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	//Handles Token Signature Exception
	public SignatureException(String message) {
		super(message);
	}
}

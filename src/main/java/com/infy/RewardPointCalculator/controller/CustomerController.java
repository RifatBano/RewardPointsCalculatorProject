package com.infy.RewardPointCalculator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.dto.CustomerDTO;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.service.CustomerService;

import jakarta.validation.Valid;

/**
 * CustomerController handles the registration
 * 
 * @author rifat.bano
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);
	@Autowired
	private CustomerService customerService;

	/**
	 * Registers a new customer.
	 *
	 * This endpoint handles the registration of a new customer using the provided details. 
	 * It expects a valid customer DTO (data transfer object) containing the necessary 
	 * customer information such as email, name, and other details.
	 * 
	 * @param customerDTO the customer data transfer object containing the registration information
	 * @return ResponseEntity containing the newly created Customer object, with HTTP status CREATED (201)
	 *         if the registration is successful, or throws an exception if there is an error during registration.
	 */
	@PostMapping("/register")
	public ResponseEntity<Customer> register(@RequestBody @Valid CustomerDTO customerDTO) {
		try {
            log.info("Registration request received for email: {}", customerDTO.getEmail());
            
			Customer newCustomer = customerService.register(customerDTO);
			
            log.info("Customer successfully registered with email: {}", newCustomer.getEmail());
            
			return ResponseEntity.status(HttpStatus.CREATED).body(newCustomer);
			
		} catch (DataIntegrityViolationException e) {
            log.error("Email conflict while registering user with email: {}", customerDTO.getEmail(), e);
			// Handling case where email already exists (unique constraint violation)
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use", e);
		} catch (Exception e) {
            log.error("An error occurred while registering customer with email: {}", customerDTO.getEmail(), e);
			// Handling unexpected exceptions
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"An error occurred while registering the customer", e);
		}
	}
}
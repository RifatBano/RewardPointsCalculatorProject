package com.infy.RewardPointCalculator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.dto.CustomerDTO;
import com.infy.RewardPointCalculator.model.BlacklistedToken;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.repository.BlacklistedTokenRepository;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.tokenConfig.JwtTokenProvider;
import com.infy.RewardPointCalculator.exception.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.ArrayList;

/**
 * This is the customer service which includes register, login and logout This
 * also implements the Authentication and JWT token After logged in
 * successfully, this login should generate one jwt token
 * 
 * @author rifat.bano
 */

@Service
public class CustomerService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);  // Logger instance with 'log' as variable name

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Registers and saves the customer details after validating the required fields.
     * This method checks for the presence of essential fields (first name, last name, email, and password)
     * in the provided CustomerDTO. If any of the fields are missing, it throws a BAD_REQUEST error.
     * 
     * If all fields are valid, it hashes the password using BCrypt and saves the customer in the repository.
     * The method also handles cases where the email already exists (DataIntegrityViolationException) 
     * or other unexpected errors that may occur during the registration process.
     *
     * @param customerDTO The customer details to be registered, including first name, last name, email, and password.
     * @return The saved Customer entity after registration.
     * @throws ResponseStatusException If required fields are missing or if an error occurs during registration.
     */
    public Customer register(CustomerDTO customerDTO) {
        try {
            if (customerDTO.getFirstName() == null || customerDTO.getFirstName().isEmpty()) {
                log.warn("First name is required for registration");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name is required");
            }
            if (customerDTO.getLastName() == null || customerDTO.getLastName().isEmpty()) {
                log.warn("Last name is required for registration");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last name is required");
            }
            if (customerDTO.getEmail() == null || customerDTO.getEmail().isEmpty()) {
                log.warn("Email is required for registration");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
            }
            if (customerDTO.getPassword() == null || customerDTO.getPassword().isEmpty()) {
                log.warn("Password is required for registration");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
            }

            // Create a new customer from the DTO and bcrypt the password
            Customer customer = new Customer();
            customer.setFirstName(customerDTO.getFirstName());
            customer.setLastName(customerDTO.getLastName());
            customer.setEmail(customerDTO.getEmail());
            String encodedPassword = passwordEncoder.encode(customerDTO.getPassword());  // Use the injected password encoder

            customer.setPassword(encodedPassword); // Hashing the password using BCryptPasswordEncoder

            Customer savedCustomer = customerRepository.save(customer);
            log.info("Customer registered successfully with email: {}", customer.getEmail());
            return savedCustomer;
        } catch (DataIntegrityViolationException e) {
            log.error("Email already in use for registration: {}", customerDTO.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while registering customer: {}", customerDTO.getEmail(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", e);
        }
    }

    /**
     * loadUserByUsername is used to extract the user details from the database using the provided email.
     * This method is typically called during the authentication process when a user tries to log in with their email.
     * It loads the user's details from the database, and returns a UserDetails object with the user's email and password
     * which is used by Spring Security for authentication and authorization.
     *
     * @param email The email of the user to be authenticated.
     * @return UserDetails The user details, including the email and hashed password.
     * @throws UsernameNotFoundException If the user is not found in the database.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            // Find the user by email from the database
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            log.info("User loaded successfully with email: {}", email);

            // Return the user details with the email and encoded password
            return new org.springframework.security.core.userdetails.User(customer.getEmail(), customer.getPassword(),
                    new ArrayList<>());
        } catch (UsernameNotFoundException e) {
            log.error("User not found for email: {}", email);
            throw e;  // Propagate exception
        } catch (Exception e) {
            log.error("Error occurred while loading user with email: {}", email, e);
            throw new RuntimeException("An error occurred while loading user", e);
        }
    }

    
    /**
     * This method handles the logout functionality by invalidating the provided JWT token.
     * It extracts the username from the token, adds the token to the blacklist (to prevent reuse),
     * and logs the user out.
     *
     * @param token The JWT token of the user attempting to log out.
     */
    public void logout(String token) {
        try {
            // Extract the username associated with the JWT token using the JwtTokenProvider
            String username = jwtTokenProvider.getUsername(token);

            // Create a BlacklistedToken object with the token and username
            // This object will store the token in the blacklist to prevent future use of this token
            BlacklistedToken blacklistedToken = new BlacklistedToken(token, username);

            // Save the blacklisted token to the repository
            // This ensures that the token will no longer be valid in future authentication requests
            blacklistedTokenRepository.save(blacklistedToken);

            log.info("User logged out successfully : {}", username);

        } catch (Exception e) {
            log.error("An error occurred while logging out user with token: {}", token, e);

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while logging out", e);
        }
    }

}

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
import java.util.Optional;

/**
 * This is the customer service which includes register, login and logout 
 * This also implements the Authentication and JWT token
 * After logged in successfully, this login should generate one jwt token 
 * @author rifat.bano
 */

@Service
public class CustomerService implements UserDetailsService{

	public static final Logger log=LoggerFactory.getLogger(CustomerService.class);
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //To register and save the detail
    public Customer register(CustomerDTO customerDTO) {
        try {
            if (customerDTO.getFirstName() == null || customerDTO.getFirstName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name is required");
            }
            if (customerDTO.getLastName() == null || customerDTO.getLastName().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last name is required");
            }
            if (customerDTO.getEmail() == null || customerDTO.getEmail().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
            }
            if (customerDTO.getPassword() == null || customerDTO.getPassword().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
            }

            // Create a new customer from the DTO and bcrypt the password
            Customer customer = new Customer();
            customer.setFirstName(customerDTO.getFirstName());
            customer.setLastName(customerDTO.getLastName());
            customer.setEmail(customerDTO.getEmail());
            String encodedPassword = new BCryptPasswordEncoder().encode(customerDTO.getPassword());

            customer.setPassword(encodedPassword);  // Hashing the password using BCryptPasswordEncoder
            
            return customerRepository.save(customer);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", e);
        }
    }
   
/**
 * loadUserByUsername is defined in JwtTokenFilter to extract the username from token
 */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find the user by email from the database
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // Return the user details with the email and encoded password
        return new org.springframework.security.core.userdetails.User(
                customer.getEmail(), customer.getPassword(), new ArrayList<>()
        );
    }
    
    
    public void logout(String token) {
        try {
            // Extract the username from the token 
            String username = jwtTokenProvider.getUsername(token);
            
            // Blacklist the token by saving it to the repository
            BlacklistedToken blacklistedToken = new BlacklistedToken(token, username);
            blacklistedTokenRepository.save(blacklistedToken);

            log.info("User logged out successfully, token blacklisted for user: {}", username);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while logging out", e);
        }
    }
    
   

}
package com.infy.RewardPointCalculator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.infy.RewardPointCalculator.dto.LoginResponseDTO;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.service.CustomerService;
import com.infy.RewardPointCalculator.service.CustomerTransactionService;
import com.infy.RewardPointCalculator.tokenConfig.JwtTokenProvider;


/**
 * AuthController helps customer to login and logout
 * It includes the apis /login and /logout
 * @author rifat.bano
 */
@CrossOrigin
@RestController
@RequestMapping("/api/customers")
public class AuthController {

	private static final Logger log=LoggerFactory.getLogger(AuthController.class);
	@Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    public CustomerService customerService;
	
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    //login api
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Customer authenticationRequest) {
        try {
            log.info("Authenticating user: " + authenticationRequest.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getEmail(),
                            authenticationRequest.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtTokenProvider.createToken(authentication);
            log.info("JWT Token created: " + jwt);
            
            return ResponseEntity.ok(new LoginResponseDTO(jwt));
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials provided.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (Exception e) {
            log.error("Authentication error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Authentication error");
        }
    }



    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(" you have access now  ");
    }
	
    //logout api
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix from the Authorization header
            String jwtToken = token.substring(7);

            // Call the logout method from CustomerService to blacklist the token
            customerService.logout(jwtToken);

            return ResponseEntity.ok("Successfully logged out");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error logging out");
        }
    }

}

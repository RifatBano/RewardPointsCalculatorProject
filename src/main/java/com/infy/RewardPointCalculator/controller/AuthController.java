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
import com.infy.RewardPointCalculator.tokenConfig.JwtTokenProvider;

/**
 * AuthController helps customer to login and logout. It includes the APIs
 * /login and /logout.
 * 
 * @author rifat.bano
 */
@CrossOrigin
@RestController
@RequestMapping("/api/customers")
public class AuthController {

	private static final Logger log = LoggerFactory.getLogger(AuthController.class);
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	public CustomerService customerService;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	/**
	 * Login API for authenticating the user.
	 * 
	 * This endpoint handles the user authentication by verifying the provided
	 * credentials (email and password). If the authentication is successful, it
	 * generates a JWT token and returns it to the user for subsequent requests. If
	 * authentication fails, it returns an appropriate error message.
	 * 
	 * @param authenticationRequest contains the user's email and password for
	 *                              authentication
	 * @return ResponseEntity with JWT token if authentication is successful, or an
	 *         error message if not
	 */
	@PostMapping("/login")
	public ResponseEntity<?> authenticateUser(@RequestBody Customer authenticationRequest) {
		try {
			log.info("Authenticating user: " + authenticationRequest.getEmail());

			Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					authenticationRequest.getEmail(), authenticationRequest.getPassword()));

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

	/**
	 * Logout API for handling user logout requests.
	 * 
	 * This endpoint handles the user logout process by invalidating the provided
	 * JWT token. It extracts the token from the Authorization header, blacklists it
	 * using the `CustomerService`, and logs the user out. If the logout process is
	 * successful, it returns a success message.
	 * 
	 * @param token the JWT token passed in the Authorization header, which is used
	 *              to identify the user
	 * @return ResponseEntity indicating the outcome of the logout process (success
	 *         or failure)
	 */
	@PostMapping("/logout")
	public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
		try {
			log.info("Logout request received. Token length: {}", token);
			// Remove "Bearer " prefix from the Authorization header
			String jwtToken = token.substring(7);
			log.debug("JWT token extracted: {}", jwtToken);
			// Call the logout method from CustomerService to blacklist the token
			customerService.logout(jwtToken);
			log.info("Successfully logged out.");
			return ResponseEntity.ok("Successfully logged out");
		} catch (Exception e) {
			log.error("Error occurred during logout process", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error logging out");
		}
	}

}

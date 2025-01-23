package com.infy.RewardPointCalculator;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.dto.CustomerDTO;
import com.infy.RewardPointCalculator.model.BlacklistedToken;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.repository.BlacklistedTokenRepository;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.service.CustomerService;
import com.infy.RewardPointCalculator.tokenConfig.JwtTokenProvider;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;

/**
 * Unit tests for the {@link CustomerService} class.
 * <p>
 * This class tests various functionalities of the CustomerService, including
 * customer registration, loading user details by username, and logging out a
 * user.
 * </p>
 */
@ExtendWith(MockitoExtension.class) // Allows the use of Mockito annotations
public class CustomerServiceTest {

	@Mock
	private CustomerRepository customerRepository; // Mock CustomerRepository

	@Mock
	private BlacklistedTokenRepository blacklistedTokenRepository; // Mock BlacklistedTokenRepository

	@Mock
	private JwtTokenProvider jwtTokenProvider; // Mock JwtTokenProvider

	@InjectMocks
	private CustomerService customerService; // Service to be tested

	private CustomerDTO customerDTO; // CustomerDTO object for registration tests

	private Customer customer; // Customer entity for other tests

	/**
	 * Sets up test data before each test is executed. Initializes a
	 * {@link CustomerDTO} and {@link Customer} object with valid test data.
	 */
	@BeforeEach
	public void setUp() {
		// Set up a CustomerDTO object with valid data for testing
		customerDTO = new CustomerDTO("John", "Doe", "john.doe@example.com", "password123");

		// Set up a Customer object (this is the entity returned by repository methods)
		customer = new Customer();
		customer.setFirstName("John");
		customer.setLastName("Doe");
		customer.setEmail("john.doe@example.com");
		customer.setPassword("encodedPassword"); // This will be an encoded password in real use case
	}

	/**
	 * Tests the customer registration functionality. This test simulates a
	 * successful customer registration process where the customer is saved in the
	 * repository and returned.
	 * 
	 * @throws Exception If there is any exception during the test execution
	 */
	@Test
	public void testRegister_Successful() {
		when(customerRepository.save(any(Customer.class))).thenReturn(customer);

		Customer savedCustomer = customerService.register(customerDTO);

		assertNotNull(savedCustomer);
		assertEquals("john.doe@example.com", savedCustomer.getEmail());
		verify(customerRepository, times(1)).save(any(Customer.class)); // Ensure save is called once
	}

	/**
	 * Tests the customer registration functionality when the email is already in
	 * use. This test simulates a scenario where the customer’s email is already
	 * present in the system, causing a {@link DataIntegrityViolationException} to
	 * be thrown.
	 * 
	 * @throws Exception If there is any exception during the test execution
	 */
	@Test
	public void testRegister_EmailAlreadyInUse() {
		when(customerRepository.save(any(Customer.class)))
				.thenThrow(new DataIntegrityViolationException("Email already in use"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> customerService.register(customerDTO));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		assertEquals("Email already in use", exception.getReason());
	}

	/**
	 * Tests the functionality of loading user details by username (email). This
	 * test simulates a successful search for a customer by email in the repository.
	 * 
	 * @throws Exception If there is any exception during the test execution
	 */
	@Test
	public void testLoadUserByUsername_Successful() {
		when(customerRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(customer));

		UserDetails userDetails = customerService.loadUserByUsername("john.doe@example.com");

		assertNotNull(userDetails);
		assertEquals("john.doe@example.com", userDetails.getUsername());
	}

	/**
	 * Tests the logout functionality by successfully invalidating a valid JWT
	 * token. This test simulates extracting the username from a valid token and
	 * adding it to the blacklist.
	 * 
	 * @throws Exception If there is any exception during the test execution
	 */
	@Test
	public void testLogout_Successful() {
		when(jwtTokenProvider.getUsername("validToken")).thenReturn("john.doe@example.com");

		customerService.logout("validToken");

		verify(blacklistedTokenRepository, times(1)).save(any(BlacklistedToken.class)); // Ensure that the token is
																						// saved in the blacklist
	}

	/**
	 * Tests the logout functionality when an invalid token is provided. This test
	 * simulates the case where the token is invalid, causing an exception to be
	 * thrown.
	 * 
	 * @throws Exception If there is any exception during the test execution
	 */
	@Test
	public void testLogout_InvalidToken() {
		when(jwtTokenProvider.getUsername("invalidToken")).thenThrow(new RuntimeException("Token is invalid"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> customerService.logout("invalidToken"));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("An error occurred while logging out", exception.getReason());
	}
}

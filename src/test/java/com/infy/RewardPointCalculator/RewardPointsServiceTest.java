package com.infy.RewardPointCalculator;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.RewardPoints;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.repository.RewardPointsRepository;
import com.infy.RewardPointCalculator.service.RewardPointsService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for the {@link RewardPointsService} class.
 * <p>
 * These tests cover the functionality of the RewardPointsService, ensuring that
 * the methods for retrieving reward points for customers are working as
 * expected, including cases where errors occur.
 * </p>
 */
public class RewardPointsServiceTest {

	@Mock
	private RewardPointsRepository rewardPointsRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private RewardPointsService rewardPointsService;

	private Customer customer;
	private RewardPoints rewardPoints1;
	private RewardPoints rewardPoints2;

	/**
	 * Setup method to initialize test data and mocks before each test.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// Setup mock customer
		customer = new Customer();
		customer.setId(1L);

		// Setup mock reward points
		rewardPoints1 = new RewardPoints();
		rewardPoints1.setPoints(100);
		rewardPoints1.setCustomer(customer);
		rewardPoints1.setMonth(1);
		rewardPoints1.setYear(2025);

		rewardPoints2 = new RewardPoints();
		rewardPoints2.setPoints(200);
		rewardPoints2.setCustomer(customer);
		rewardPoints2.setMonth(1);
		rewardPoints2.setYear(2025);
	}

	/**
	 * Test case for successfully retrieving aggregated reward points for a specific
	 * customer, month, and year.
	 * <p>
	 * This test verifies that the service correctly aggregates the reward points
	 * for a given customer and period, by summing the points for all transactions.
	 * </p>
	 */
	@Test
	public void testGetRewardPoints_Success() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(rewardPointsRepository.findByCustomerAndMonthAndYear(customer, 1, 2025))
				.thenReturn(Arrays.asList(rewardPoints1, rewardPoints2));

		RewardPoints result = rewardPointsService.getRewardPoints(1L, 1, 2025);

		assertNotNull(result);
		assertEquals(300, result.getPoints()); // 100 + 200 points
		assertEquals(1, result.getMonth());
		assertEquals(2025, result.getYear());
	}

	/**
	 * Test case when no reward points are found for a given customer, month, and
	 * year.
	 * <p>
	 * This test checks that when no points are found, the service returns a
	 * RewardPoints object with 0 points.
	 * </p>
	 */
	@Test
	public void testGetRewardPoints_NotFound() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(rewardPointsRepository.findByCustomerAndMonthAndYear(customer, 1, 2025))
				.thenReturn(Collections.emptyList());

		RewardPoints result = rewardPointsService.getRewardPoints(1L, 1, 2025);

		assertNotNull(result);
		assertEquals(0, result.getPoints()); // No points found
		assertEquals(1, result.getMonth());
		assertEquals(2025, result.getYear());
	}

	/**
	 * Test case when the customer is not found in the database.
	 * <p>
	 * This test ensures that when a customer with the given ID does not exist, a
	 * ResponseStatusException is thrown with an appropriate error message and
	 * status code.
	 * </p>
	 */
	@Test
	public void testGetRewardPoints_CustomerNotFound() {
		when(customerRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> rewardPointsService.getRewardPoints(1L, 1, 2025));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("An error occurred while retrieving reward points", exception.getReason());
	}

	/**
	 * Test case for successfully retrieving all reward points for a specific
	 * customer.
	 * <p>
	 * This test ensures that the service correctly retrieves all reward points
	 * associated with a given customer.
	 * </p>
	 */
	@Test
	public void testGetAllRewardPoints_Success() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(rewardPointsRepository.findByCustomer(customer)).thenReturn(Arrays.asList(rewardPoints1, rewardPoints2));

		List<RewardPoints> result = rewardPointsService.getAllRewardPoints(1L);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(100, result.get(0).getPoints());
		assertEquals(200, result.get(1).getPoints());
	}

	/**
	 * Test case when no reward points are found for a given customer.
	 * <p>
	 * This test ensures that the service returns an empty list when no reward
	 * points are found for a customer.
	 * </p>
	 */
	@Test
	public void testGetAllRewardPoints_NotFound() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(rewardPointsRepository.findByCustomer(customer)).thenReturn(Collections.emptyList());

		List<RewardPoints> result = rewardPointsService.getAllRewardPoints(1L);

		assertNotNull(result);
		assertTrue(result.isEmpty()); // No reward points found
	}

	/**
	 * Test case when the customer is not found for retrieving all reward points.
	 * <p>
	 * This test checks that when a customer is not found, a ResponseStatusException
	 * is thrown with an appropriate message.
	 * </p>
	 */
	@Test
	public void testGetAllRewardPoints_CustomerNotFound() {
		when(customerRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> rewardPointsService.getAllRewardPoints(1L));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("An error occurred while retrieving all reward points", exception.getReason());
	}

	/**
	 * Test case when a DataIntegrityViolationException occurs while retrieving
	 * reward points.
	 * <p>
	 * This test checks that when a data integrity violation occurs, the service
	 * throws a ResponseStatusException with a BAD_REQUEST status and an appropriate
	 * error message.
	 * </p>
	 */
	@Test
	public void testGetRewardPoints_DataIntegrityViolation() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(rewardPointsRepository.findByCustomerAndMonthAndYear(customer, 1, 2025))
				.thenThrow(new DataIntegrityViolationException("Data integrity violation"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> rewardPointsService.getRewardPoints(1L, 1, 2025));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("Data integrity violation while retrieving reward points", exception.getReason());
	}

	/**
	 * Test case when a DataIntegrityViolationException occurs while retrieving all
	 * reward points.
	 * <p>
	 * This test checks that when a data integrity violation occurs, the service
	 * throws a ResponseStatusException with a BAD_REQUEST status and an appropriate
	 * error message.
	 * </p>
	 */
	@Test
	public void testGetAllRewardPoints_DataIntegrityViolation() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(rewardPointsRepository.findByCustomer(customer))
				.thenThrow(new DataIntegrityViolationException("Data integrity violation"));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> rewardPointsService.getAllRewardPoints(1L));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
		assertEquals("Data integrity violation while retrieving all reward points", exception.getReason());
	}
}

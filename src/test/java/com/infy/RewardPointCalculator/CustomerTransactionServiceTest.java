package com.infy.RewardPointCalculator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.infy.RewardPointCalculator.dto.CustomerTransactionDTO;
import com.infy.RewardPointCalculator.model.Customer;
import com.infy.RewardPointCalculator.model.CustomerTransaction;
import com.infy.RewardPointCalculator.repository.CustomerRepository;
import com.infy.RewardPointCalculator.repository.CustomerTransactionRepository;
import com.infy.RewardPointCalculator.repository.RewardPointsRepository;
import com.infy.RewardPointCalculator.service.CustomerTransactionService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test class for the {@link CustomerTransactionService}.
 * <p>
 * This class contains tests for various methods in the
 * {@link CustomerTransactionService}, such as adding, editing, deleting, and
 * fetching transactions. It also covers scenarios like when customers or
 * transactions are not found.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
public class CustomerTransactionServiceTest {

	@Mock
	private CustomerTransactionRepository transactionRepository;

	@Mock
	private RewardPointsRepository rewardPointsRepository;

	@Mock
	private CustomerRepository customerRepository;

	@InjectMocks
	private CustomerTransactionService customerTransactionService;

	private Customer customer;
	private CustomerTransaction transaction;
	private CustomerTransactionDTO transactionDTO;

	/**
	 * This method sets up the test data before each test case is run. Initializes a
	 * mock customer, transaction, and DTO object for the tests.
	 */
	@BeforeEach
	public void setUp() {
		customer = new Customer();
		customer.setId(1L);
		customer.setFirstName("John");
		customer.setLastName("Doe");
		customer.setEmail("john.doe@example.com");

		transactionDTO = new CustomerTransactionDTO();
		transactionDTO.setAmount((double) 100);
		transactionDTO.setSpentDetails("Shopping");
		transactionDTO.setTransactionDate(LocalDate.now());

		transaction = new CustomerTransaction();
		transaction.setCustomer(customer);
		transaction.setAmount((double) 100);
		transaction.setSpentDetails("Shopping");
		transaction.setDate(LocalDate.now());
	}

	/**
	 * Tests the {@link CustomerTransactionService#getTransactions(Long)} method.
	 * Verifies that transactions are fetched successfully for a customer.
	 */
	@Test
	public void testGetTransactions_Success() {
		when(transactionRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(transaction));

		List<CustomerTransaction> transactions = customerTransactionService.getTransactions(1L);

		assertNotNull(transactions);
		assertEquals(1, transactions.size());
		assertEquals("Shopping", transactions.get(0).getSpentDetails());
	}

	/**
	 * Tests the
	 * {@link CustomerTransactionService#addTransaction(Long, CustomerTransactionDTO)}
	 * method. Verifies that a transaction is successfully added for an existing
	 * customer.
	 */
	@Test
	public void testAddTransaction_Success() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(transactionRepository.save(any(CustomerTransaction.class))).thenReturn(transaction);

		CustomerTransaction createdTransaction = customerTransactionService.addTransaction(1L, transactionDTO);

		assertNotNull(createdTransaction);
		assertEquals("Shopping", createdTransaction.getSpentDetails());
		verify(transactionRepository, times(1)).save(any(CustomerTransaction.class));
	}

	/**
	 * Tests the
	 * {@link CustomerTransactionService#addTransaction(Long, CustomerTransactionDTO)}
	 * method when the customer is not found. Verifies that an error response is
	 * returned when trying to add a transaction for a non-existent customer.
	 */
	@Test
	public void testAddTransaction_CustomerNotFound() {
		when(customerRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> customerTransactionService.addTransaction(1L, transactionDTO));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("An error occurred while adding the transaction", exception.getReason());
	}

	/**
	 * Tests the
	 * {@link CustomerTransactionService#editTransaction(Long, Long, CustomerTransactionDTO)}
	 * method. Verifies that a transaction is successfully edited when both the
	 * customer and transaction exist.
	 */
	@Test
	public void testEditTransaction_Success() {
		// Mock the customer repository to return a customer when it's requested
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(transactionRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(transaction));
		when(transactionRepository.save(any(CustomerTransaction.class))).thenReturn(transaction);

		CustomerTransaction updatedTransaction = customerTransactionService.editTransaction(1L, 1L, transactionDTO);
		assertNotNull(updatedTransaction);
		assertEquals("Shopping", updatedTransaction.getSpentDetails());
		verify(transactionRepository, times(1)).save(any(CustomerTransaction.class));
	}

	/**
	 * Tests the
	 * {@link CustomerTransactionService#editTransaction(Long, Long, CustomerTransactionDTO)}
	 * method when the transaction is not found. Verifies that an error response is
	 * returned when trying to edit a non-existent transaction.
	 */
	@Test
	public void testEditTransaction_NotFound() {
		when(transactionRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> customerTransactionService.editTransaction(1L, 1L, transactionDTO));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("An error occurred while editing the transaction", exception.getReason());
	}

	/**
	 * Tests the {@link CustomerTransactionService#deleteTransaction(Long, Long)}
	 * method. Verifies that a transaction is successfully deleted for an existing
	 * customer and transaction.
	 */
	@Test
	public void testDeleteTransaction_Success() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(transactionRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(transaction));

		customerTransactionService.deleteTransaction(1L, 1L);

		verify(transactionRepository, times(1)).delete(any(CustomerTransaction.class));
	}

	/**
	 * Tests the {@link CustomerTransactionService#deleteTransaction(Long, Long)}
	 * method when the transaction is not found. Verifies that an error response is
	 * returned when trying to delete a non-existent transaction.
	 */
	@Test
	public void testDeleteTransaction_NotFound() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		when(transactionRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> customerTransactionService.deleteTransaction(1L, 1L));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
		assertEquals("An error occurred while deleting the transaction", exception.getReason());
	}

	/**
	 * Tests the {@link CustomerTransactionService#getCustomerById(Long)} method.
	 * Verifies that customer details are successfully retrieved by ID.
	 */
	@Test
	public void testGetCustomerById_Success() {
		when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

		Customer foundCustomer = customerTransactionService.getCustomerById(1L);

		assertNotNull(foundCustomer);
		assertEquals(1L, foundCustomer.getId());
	}

	/**
	 * Tests the {@link CustomerTransactionService#getCustomerById(Long)} method
	 * when the customer is not found. Verifies that an exception is thrown if the
	 * customer is not found.
	 */
	@Test
	public void testGetCustomerById_CustomerNotFound() {
		when(customerRepository.findById(1L)).thenReturn(Optional.empty());

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> customerTransactionService.getCustomerById(1L));

		assertEquals("Customer not found", exception.getMessage());
	}
}
